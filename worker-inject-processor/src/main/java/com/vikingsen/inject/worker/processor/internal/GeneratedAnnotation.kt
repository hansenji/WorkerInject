package com.vikingsen.inject.worker.processor.internal

import com.squareup.inject.assisted.processor.internal.toClassName
import com.squareup.javapoet.AnnotationSpec
import javax.annotation.processing.Processor
import javax.lang.model.util.Elements

/**
 * Create a `@Generated` annotation using the correct type based on JDK version and availability on
 * the compilation classpath, a `value` with the fully-qualified class name of the calling
 * [Processor], and a comment pointing to this project's GitHub repo. Returns `null` if no
 * annotation type is available on the classpath.
 */
fun Processor.createGeneratedAnnotation(elements: Elements, comments: String = "https://github.com/hansenji/WorkerInject"): AnnotationSpec? {
  val generatedType = elements.getTypeElement("javax.annotation.processing.Generated")
      ?: elements.getTypeElement("javax.annotation.Generated")
      ?: return null
  return AnnotationSpec.builder(generatedType.toClassName())
      .addMember("value", "\$S", javaClass.name)
      .addMember("comments", "\$S", comments)
      .build()
}
