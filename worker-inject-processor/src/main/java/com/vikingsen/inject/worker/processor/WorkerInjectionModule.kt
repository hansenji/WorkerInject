package com.vikingsen.inject.worker.processor

import com.squareup.inject.assisted.processor.assistedInjectFactoryName
import com.squareup.inject.assisted.processor.internal.applyEach
import com.squareup.inject.assisted.processor.internal.peerClassWithReflectionNesting
import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import javax.lang.model.element.Modifier.ABSTRACT
import javax.lang.model.element.Modifier.PRIVATE
import javax.lang.model.element.Modifier.PUBLIC

private val MODULE = ClassName.get("dagger", "Module")
private val BINDS = ClassName.get("dagger", "Binds")
private val INTO_MAP = ClassName.get("dagger.multibindings", "IntoMap")
private val STRING_KEY = ClassName.get("dagger.multibindings", "StringKey")
private val WORKER_INJECT_FACTORY = ClassName.get("com.vikingsen.inject.worker", "WorkerInjectFactory")

class WorkerInjectionModule(
    val moduleName: ClassName,
    val public: Boolean,
    val injectedNames: List<ClassName>,
    /** An optional `@Generated` annotation marker. */
    val generatedAnnotation: AnnotationSpec? = null
) {
    val generatedType = moduleName.workerInjectModuleName()

    fun brewJava(): TypeSpec {
        return TypeSpec.classBuilder(generatedType)
            .addAnnotation(MODULE)
            .apply {
                if (generatedAnnotation != null) {
                    addAnnotation(generatedAnnotation)
                }
            }
            .addModifiers(ABSTRACT)
            .apply {
                if (public) {
                    addModifiers(PUBLIC)
                }
            }
            .addMethod(
                MethodSpec.constructorBuilder()
                    .addModifiers(PRIVATE)
                    .build()
            )
            .applyEach(injectedNames) { injectedName ->
                addMethod(
                    MethodSpec.methodBuilder(injectedName.bindMethodName())
                        .addAnnotation(BINDS)
                        .addAnnotation(INTO_MAP)
                        .addAnnotation(
                            AnnotationSpec.builder(STRING_KEY)
                                .addMember("value", "\$S", injectedName)
                                .build()
                        )
                        .addModifiers(ABSTRACT)
                        .returns(WORKER_INJECT_FACTORY)
                        .addParameter(injectedName.assistedInjectFactoryName(), "factory")
                        .build()
                )
            }
            .build()
    }
}

private fun ClassName.bindMethodName() = "bind_" + reflectionName().replace('.', '_')

internal fun ClassName.workerInjectModuleName() = peerClassWithReflectionNesting("""WorkerInject_${simpleName()}""")
