package com.vikingsen.inject.worker;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;
import static java.lang.annotation.RetentionPolicy.SOURCE;

@Target(TYPE)
@Retention(CLASS)
public @interface WorkerModule {
}
