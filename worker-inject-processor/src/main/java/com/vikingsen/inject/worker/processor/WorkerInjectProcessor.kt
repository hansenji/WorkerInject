package com.vikingsen.inject.worker.processor

import com.google.auto.service.AutoService
import com.squareup.inject.assisted.processor.AssistedInjection
import com.squareup.inject.assisted.processor.Key
import com.squareup.inject.assisted.processor.NamedKey
import com.squareup.inject.assisted.processor.asDependencyRequest
import com.squareup.inject.assisted.processor.internal.MirrorValue
import com.squareup.inject.assisted.processor.internal.applyEach
import com.squareup.inject.assisted.processor.internal.cast
import com.squareup.inject.assisted.processor.internal.castEach
import com.squareup.inject.assisted.processor.internal.filterNotNullValues
import com.squareup.inject.assisted.processor.internal.findElementsAnnotatedWith
import com.squareup.inject.assisted.processor.internal.getAnnotation
import com.squareup.inject.assisted.processor.internal.getValue
import com.squareup.inject.assisted.processor.internal.hasAnnotation
import com.squareup.inject.assisted.processor.internal.toClassName
import com.squareup.inject.assisted.processor.internal.toTypeName
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.JavaFile
import com.vikingsen.inject.worker.WorkerInject
import com.vikingsen.inject.worker.WorkerModule
import com.vikingsen.inject.worker.processor.internal.createGeneratedAnnotation
import net.ltgt.gradle.incap.IncrementalAnnotationProcessor
import net.ltgt.gradle.incap.IncrementalAnnotationProcessorType
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Filer
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind.CLASS
import javax.lang.model.element.ElementKind.CONSTRUCTOR
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.Modifier.PRIVATE
import javax.lang.model.element.Modifier.STATIC
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.Diagnostic.Kind.ERROR
import javax.tools.Diagnostic.Kind.WARNING

@IncrementalAnnotationProcessor(IncrementalAnnotationProcessorType.ISOLATING)
@AutoService(Processor::class)
class WorkerInjectProcessor : AbstractProcessor() {

    private lateinit var messager: Messager
    private lateinit var filer: Filer
    private lateinit var types: Types
    private lateinit var elements: Elements
    private lateinit var workerType: TypeMirror

    private var userModule: String? = null

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latest()
    override fun getSupportedAnnotationTypes() = setOf(
        WorkerInject::class.java.canonicalName,
        WorkerModule::class.java.canonicalName
    )

    override fun init(env: ProcessingEnvironment) {
        super.init(env)
        messager = env.messager
        filer = env.filer
        types = env.typeUtils
        elements = env.elementUtils
        workerType = elements.getTypeElement("androidx.work.ListenableWorker").asType()
    }

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        val workerInjectElements = roundEnv.findWorkerInjectCandidateTypeElements()
            .mapNotNull { it.toWorkerInjectElementsOrNull() }

        workerInjectElements
            .associateWith { it.toAssistedInjectionOrNull() }
            .filterNotNullValues()
            .forEach { writeWorkerInject(it.key, it.value) }

        val workerModuleElements = roundEnv.findWorkerModuleTypeElement()
            ?.toWorkerModuleElementsOrNull(workerInjectElements)

        if (workerModuleElements != null) {
            val moduleType = workerModuleElements.moduleType

            val userModuleFqcn = userModule
            if (userModuleFqcn != null) {
                val userModuleType = elements.getTypeElement(userModuleFqcn)
                error("Multiple @WorkerModule-annotated modules found.", userModuleType)
                error("Multiple @WorkerModule-annotated modules found.", moduleType)
                userModule = null
            } else {
                userModule = moduleType.qualifiedName.toString()

                val workerInjectionModule = workerModuleElements.toWorkerInjectionModule()
                writeWorkerModule(workerModuleElements, workerInjectionModule)
            }
        }

        // Wait until processing is ending to validate that the @WorkerModule's @Module annotation
        // includes the generated type.
        if (roundEnv.processingOver()) {
            val userModuleFqcn = userModule
            if (userModuleFqcn != null) {
                // In the processing round in which we handle the @WorkerModule the @Module annotation's
                // includes contain an <error> type because we haven't generated the worker module yet.
                // As a result, we need to re-lookup the element so that its referenced types are available.
                val userModule = elements.getTypeElement(userModuleFqcn)

                // Previous validation guarantees this annotation is present.
                val moduleAnnotation = userModule.getAnnotation("dagger.Module")
                // Dagger guarantees this property is present and is an array of types or errors.
                val includes = moduleAnnotation?.getValue("includes", elements)
                    ?.cast<MirrorValue.Array>()
                    ?.filterIsInstance<MirrorValue.Type>() ?: emptyList()

                val generatedModuleName = userModule.toClassName().workerInjectModuleName()
                val referencesGeneratedModule = includes
                    .map { it.toTypeName() }
                    .any { it == generatedModuleName }
                if (!referencesGeneratedModule) {
                    error(
                        "@WorkerModule's @Module must include ${generatedModuleName.simpleName()}",
                        userModule
                    )
                }
            }
        }

        return false
    }

    /**
     * Find [TypeElement]s which are candidates for assisted injection by having a constructor
     * annotated with [WorkerInject].
     */
    private fun RoundEnvironment.findWorkerInjectCandidateTypeElements(): List<TypeElement> {
        return findElementsAnnotatedWith<WorkerInject>()
            .map { it.enclosingElement as TypeElement }
    }

    /**
     * From this [TypeElement] which is a candidate for worker injection, find and validate the
     * syntactical elements required to generate the factory:
     * - Non-private, non-inner target type
     * - Single non-private target constructor
     */
    private fun TypeElement.toWorkerInjectElementsOrNull(): WorkerInjectElements? {
        var valid = true
        if (PRIVATE in modifiers) {
            error("@WorkerInject-using types must not be private", this)
            valid = false
        }
        if (enclosingElement.kind == CLASS && STATIC !in modifiers) {
            error("Nested @WorkerInject-using types must be static", this)
            valid = false
        }
        if (!types.isSubtype(asType(), workerType)) {
            error("@WorkerInject-using tyeps must be a subtype of androidx.work.ListenableWorker or androidx.work.Worker", this)
            valid = false
        }

        val constructors = enclosedElements
            .filter { it.kind == CONSTRUCTOR }
            .filter { it.hasAnnotation<WorkerInject>() }
            .castEach<ExecutableElement>()
        if (constructors.size > 1) {
            error("Multiple @WorkerInject-annotated constructors found.", this)
            valid = false
        }

        if (!valid) return null

        val constructor = constructors.single()
        if (PRIVATE in constructor.modifiers) {
            error("@WorkerInject constructor must not be private", this)
            return null
        }

        return WorkerInjectElements(this, constructor)
    }

    /**
     * From this [WorkerInjectElements], parse and validate the semantic information of the
     * elements which is required to generate the factory:
     * - Optional unqualified assisted parameters of Context and WorkerParameters
     * - At least one provided parameter and no duplicates
     */
    private fun WorkerInjectElements.toAssistedInjectionOrNull(): AssistedInjection? {
        var valid = true

        val requests = targetConstructor.parameters.map { it.asDependencyRequest() }
        val (assistedRequests, providedRequests) = requests.partition { it.isAssisted }
        val assistedKeys = assistedRequests.map { it.namedKey }
        if (assistedKeys.toSet() != FACTORY_KEYS.toSet()) {
            error(
                """
        Worker injection requires Context appContext and WorkerParameters workerParams @Assisted parameters.
          Found:
            $assistedKeys
          Expected:
            $FACTORY_KEYS
        """.trimIndent(), targetConstructor
            )
            valid = false
        }

        if (providedRequests.isEmpty()) {
            warn("Worker injection recommends at least one non-@Assisted paramter.", targetConstructor)
        } else {
            val providedDuplicates = providedRequests.groupBy { it.key }.filterValues { it.size > 1 }
            if (providedDuplicates.isNotEmpty()) {
                error(
                    "Duplicate non-@Assisted parameters declared. Forget a qualifier annotation?"
                            + providedDuplicates.values.flatten().joinToString("\n * ", prefix = "\n * "),
                    targetConstructor
                )
                valid = false
            }
        }

        if (!valid) return null

        val targetType = targetType.asType().toTypeName()
        val generatedAnnotation = createGeneratedAnnotation(elements)
        return AssistedInjection(
            targetType, requests, FACTORY, "create", LISTENABLE_WORKER,
            FACTORY_KEYS, generatedAnnotation
        )
    }

    private fun writeWorkerInject(elements: WorkerInjectElements, injection: AssistedInjection) {
        val generatedTypeSpec = injection.brewJava()
            .toBuilder()
            .addOriginatingElement(elements.targetType)
            .build()
        JavaFile.builder(injection.generatedType.packageName(), generatedTypeSpec)
            .addFileComment("Generated by @WorkerInject. Do not modify!")
            .build()
            .writeTo(filer)
    }

    private fun RoundEnvironment.findWorkerModuleTypeElement(): TypeElement? {
        val workerModules = findElementsAnnotatedWith<WorkerModule>().castEach<TypeElement>()
        if (workerModules.size > 1) {
            workerModules.forEach {
                error("Multiple @WorkerModule-annotated modules found.", it)
            }
            return null
        }
        return workerModules.singleOrNull()
    }

    private fun TypeElement.toWorkerModuleElementsOrNull(
        workerInjectElements: List<WorkerInjectElements>
    ): WorkerModuleElements? {
        if (!hasAnnotation("dagger.Module")) {
            error("@WorkerModule must also be annotated as Dagger @Module", this)
            return null
        }

        val workerTargetTypes = workerInjectElements.map { it.targetType }
        return WorkerModuleElements(this, workerTargetTypes)
    }

    private fun WorkerModuleElements.toWorkerInjectionModule(): WorkerInjectionModule {
        val moduleName = moduleType.toClassName()
        val workerNames = workerTypes.map { it.toClassName() }
        val public = Modifier.PUBLIC in moduleType.modifiers
        val generatedAnnotation = createGeneratedAnnotation(elements)
        return WorkerInjectionModule(moduleName, public, workerNames, generatedAnnotation)
    }

    private fun writeWorkerModule(
        elements: WorkerModuleElements,
        module: WorkerInjectionModule
    ) {
        val generatedTypeSpec = module.brewJava()
            .toBuilder()
            .addOriginatingElement(elements.moduleType)
            .applyEach(elements.workerTypes) {
                addOriginatingElement(it)
            }
            .build()
        JavaFile.builder(module.generatedType.packageName(), generatedTypeSpec)
            .addFileComment("Generated by @WorkerModule. Do not modify!")
            .build()
            .writeTo(filer)
    }

    private fun warn(message: String, element: Element? = null) {
        messager.printMessage(WARNING, message, element)
    }

    private fun error(message: String, element: Element? = null) {
        messager.printMessage(ERROR, message, element)
    }

    private data class WorkerInjectElements(
        val targetType: TypeElement,
        val targetConstructor: ExecutableElement
    )

    private data class WorkerModuleElements(
        val moduleType: TypeElement,
        val workerTypes: List<TypeElement>
    )

}

private val LISTENABLE_WORKER = ClassName.get("androidx.work", "ListenableWorker")
private val FACTORY = ClassName.get("com.vikingsen.inject.worker", "WorkerInjectFactory")
private val FACTORY_KEYS = listOf(
    NamedKey(Key(ClassName.get("android.content", "Context")), "appContext"),
    NamedKey(Key(ClassName.get("androidx.work", "WorkerParameters")), "workerParams")
)
