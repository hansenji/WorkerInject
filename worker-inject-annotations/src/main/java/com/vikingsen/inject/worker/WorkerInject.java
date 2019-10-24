package com.vikingsen.inject.worker;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.RetentionPolicy.CLASS;
import static java.lang.annotation.RetentionPolicy.SOURCE;

@Retention(CLASS)
@Target(CONSTRUCTOR)
public @interface WorkerInject {
}
