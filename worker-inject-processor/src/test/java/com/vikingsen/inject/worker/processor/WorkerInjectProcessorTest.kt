package com.vikingsen.inject.worker.processor

import com.google.common.truth.Truth.assertAbout
import com.google.testing.compile.JavaFileObjects
import com.google.testing.compile.JavaSourceSubjectFactory.javaSource
import com.google.testing.compile.JavaSourcesSubjectFactory.javaSources
import org.junit.Test

private const val GENERATED_TYPE = "javax.annotation.Generated" // TODO vary once JDK 9 works.
private const val GENERATED_ANNOTATION = """
@Generated(
  value = "com.vikingsen.inject.worker.processor.WorkerInjectProcessor",
  comments = "https://github.com/hansenji/WorkerInject"
)
"""

class WorkerInjectProcessorTest {

    @Test
    fun simpleTest() {
        val inputWorker = JavaFileObjects.forSourceString(
            "test.TestWorker", """
            package test;

            import android.content.Context;
            import androidx.work.Worker;
            import androidx.work.WorkerParameters;
            import com.squareup.inject.assisted.Assisted;
            import com.vikingsen.inject.worker.WorkerInject;

            class TestWorker extends Worker {
                @WorkerInject
                TestWorker(Long foo, @Assisted Context appContext, @Assisted WorkerParameters workerParams) {
                    super(appContext, workerParams);
                }
            }
        """
        )
        val inputModule = JavaFileObjects.forSourceString(
            "test.TestModule", """
            package test;

            import com.vikingsen.inject.worker.WorkerModule;
            import dagger.Module;

            @WorkerModule
            @Module(includes = WorkerInject_TestModule.class)
            abstract class TestModule {}
        """
        )

        val expectedFactory = JavaFileObjects.forSourceString(
            "test.TestWorker_AssistedFactory", """
            package test;

            import android.content.Context
            import androidx.work.ListenableWorker;
            import androidx.work.WorkerParameters;
            import com.vikingsen.inject.worker.WorkerInjectFactory;
            import java.lang.Long;
            import java.lang.Override;
            import $GENERATED_TYPE;
            import javax.inject.Inject;
            import javax.inject.Provider;

            $GENERATED_ANNOTATION
            public final class TestWorker_AssistedFactory implements WorkerInjectFactory {
                private final Provider<Long> foo;

                @Inject public TestWorker_AssistedFactory(Provider<Long> foo) {
                    this.foo = foo;
                }

                @Override public ListenableWorker create(Context appContext, WorkerParameters workerParams) {
                    return new TestWorker(foo.get(), appContext, workerParams);
                }
            }
        """
        )

        val expectedModule = JavaFileObjects.forSourceString(
            "test.WorkerInject_TestModule", """
            package test;

            import com.vikingsen.inject.worker.WorkerInjectFactory;
            import dagger.Binds;
            import dagger.Module;
            import dagger.multibindings.IntoMap;
            import dagger.multibindings.StringKey;
            import $GENERATED_TYPE;

            @Module
            $GENERATED_ANNOTATION
            abstract class WorkerInject_TestModule {
                private WorkerInject_TestModule() {}

                @Binds
                @IntoMap
                @StringKey("test.TestWorker")
                abstract WorkerInjectFactory bind_test_TestWorker(TestWorker_AssistedFactory factory);
            }
        """
        )

        assertAbout(javaSources())
            .that(listOf(inputWorker, inputModule))
            .processedWith(WorkerInjectProcessor())
            .compilesWithoutError()
            .and()
            .generatesSources(expectedFactory, expectedModule)
    }

    @Test
    fun typeExtendsListenableWorkerTest() {
        val inputWorker = JavaFileObjects.forSourceString(
            "test.TestWorker", """
            package test;

            import android.content.Context;
            import androidx.work.ListenableWorker;
            import androidx.work.WorkerParameters;
            import com.squareup.inject.assisted.Assisted;
            import com.vikingsen.inject.worker.WorkerInject;

            class TestWorker extends ListenableWorker {
                @WorkerInject
                TestWorker(Long foo, @Assisted Context appContext, @Assisted WorkerParameters workerParams) {
                    super(appContext, workerParams);
                }
            }
        """
        )
        val inputModule = JavaFileObjects.forSourceString(
            "test.TestModule", """
            package test;

            import com.vikingsen.inject.worker.WorkerModule;
            import dagger.Module;

            @WorkerModule
            @Module(includes = WorkerInject_TestModule.class)
            abstract class TestModule {}
        """
        )

        val expectedFactory = JavaFileObjects.forSourceString(
            "test.TestWorker_AssistedFactory", """
            package test;

            import android.content.Context
            import androidx.work.ListenableWorker;
            import androidx.work.WorkerParameters;
            import com.vikingsen.inject.worker.WorkerInjectFactory;
            import java.lang.Long;
            import java.lang.Override;
            import $GENERATED_TYPE;
            import javax.inject.Inject;
            import javax.inject.Provider;

            $GENERATED_ANNOTATION
            public final class TestWorker_AssistedFactory implements WorkerInjectFactory {
                private final Provider<Long> foo;

                @Inject public TestWorker_AssistedFactory(Provider<Long> foo) {
                    this.foo = foo;
                }

                @Override public ListenableWorker create(Context appContext, WorkerParameters workerParams) {
                    return new TestWorker(foo.get(), appContext, workerParams);
                }
            }
        """
        )

        val expectedModule = JavaFileObjects.forSourceString(
            "test.WorkerInject_TestModule", """
            package test;

            import com.vikingsen.inject.worker.WorkerInjectFactory;
            import dagger.Binds;
            import dagger.Module;
            import dagger.multibindings.IntoMap;
            import dagger.multibindings.StringKey;
            import $GENERATED_TYPE;

            @Module
            $GENERATED_ANNOTATION
            abstract class WorkerInject_TestModule {
                private WorkerInject_TestModule() {}

                @Binds
                @IntoMap
                @StringKey("test.TestWorker")
                abstract WorkerInjectFactory bind_test_TestWorker(TestWorker_AssistedFactory factory);
            }
        """
        )

        assertAbout(javaSources())
            .that(listOf(inputWorker, inputModule))
            .processedWith(WorkerInjectProcessor())
            .compilesWithoutError()
            .and()
            .generatesSources(expectedFactory, expectedModule)
    }

    @Test
    fun publicTest() {
        val inputWorker = JavaFileObjects.forSourceString(
            "test.TestWorker", """
            package test;

            import android.content.Context;
            import androidx.work.Worker;
            import androidx.work.WorkerParameters;
            import com.squareup.inject.assisted.Assisted;
            import com.vikingsen.inject.worker.WorkerInject;

            class TestWorker extends Worker {
                @WorkerInject
                TestWorker(Long foo, @Assisted Context appContext, @Assisted WorkerParameters workerParams) {
                    super(appContext, workerParams);
                }
            }
        """
        )
        val inputModule = JavaFileObjects.forSourceString(
            "test.TestModule", """
            package test;

            import com.vikingsen.inject.worker.WorkerModule;
            import dagger.Module;

            @WorkerModule
            @Module(includes = WorkerInject_TestModule.class)
            public abstract class TestModule {}
        """
        )

        val expectedModule = JavaFileObjects.forSourceString(
            "test.WorkerInject_TestModule", """
            package test;

            import com.vikingsen.inject.worker.WorkerInjectFactory;
            import dagger.Binds;
            import dagger.Module;
            import dagger.multibindings.IntoMap;
            import dagger.multibindings.StringKey;
            import $GENERATED_TYPE;

            @Module
            $GENERATED_ANNOTATION
            public abstract class WorkerInject_TestModule {
                private WorkerInject_TestModule() {}

                @Binds
                @IntoMap
                @StringKey("test.TestWorker")
                abstract WorkerInjectFactory bind_test_TestWorker(TestWorker_AssistedFactory factory);
            }
        """
        )

        assertAbout(javaSources())
            .that(listOf(inputWorker, inputModule))
            .processedWith(WorkerInjectProcessor())
            .compilesWithoutError()
            .and()
            .generatesSources(expectedModule)
    }

    @Test
    fun nestedTest() {
        val inputWorker = JavaFileObjects.forSourceString(
            "test.TestWorker", """
            package test;

            import android.content.Context;
            import androidx.work.Worker;
            import androidx.work.WorkerParameters;
            import com.squareup.inject.assisted.Assisted;
            import com.vikingsen.inject.worker.WorkerInject;

            class Outer {
                static class TestWorker extends Worker {
                    @WorkerInject
                    TestWorker(Long foo, @Assisted Context appContext, @Assisted WorkerParameters workerParams) {
                        super(appContext, workerParams);
                    }
                }
            }
        """
        )
        val inputModule = JavaFileObjects.forSourceString(
            "test.TestModule", """
            package test;

            import com.vikingsen.inject.worker.WorkerModule;
            import dagger.Module;

            @WorkerModule
            @Module(includes = WorkerInject_TestModule.class)
            abstract class TestModule {}
        """
        )

        val expectedFactory = JavaFileObjects.forSourceString(
            "test.TestWorker_AssistedFactory", """
            package test;

            import android.content.Context
            import androidx.work.ListenableWorker;
            import androidx.work.WorkerParameters;
            import com.vikingsen.inject.worker.WorkerInjectFactory;
            import java.lang.Long;
            import java.lang.Override;
            import $GENERATED_TYPE;
            import javax.inject.Inject;
            import javax.inject.Provider;

            $GENERATED_ANNOTATION
            public final class Outer${'$'}TestWorker_AssistedFactory implements WorkerInjectFactory {
                private final Provider<Long> foo;

                @Inject public Outer${'$'}TestWorker_AssistedFactory(Provider<Long> foo) {
                    this.foo = foo;
                }

                @Override public ListenableWorker create(Context appContext, WorkerParameters workerParams) {
                    return new Outer.TestWorker(foo.get(), appContext, workerParams);
                }
            }
        """
        )

        val expectedModule = JavaFileObjects.forSourceString(
            "test.WorkerInject_TestModule", """
            package test;

            import com.vikingsen.inject.worker.WorkerInjectFactory;
            import dagger.Binds;
            import dagger.Module;
            import dagger.multibindings.IntoMap;
            import dagger.multibindings.StringKey;
            import $GENERATED_TYPE;

            @Module
            $GENERATED_ANNOTATION
            abstract class WorkerInject_TestModule {
                private WorkerInject_TestModule() {}

                @Binds
                @IntoMap
                @StringKey("test.Outer.TestWorker")
                abstract WorkerInjectFactory bind_test_Outer${'$'}TestWorker(Outer${'$'}TestWorker_AssistedFactory factory);
            }
        """
        )

        assertAbout(javaSources())
            .that(listOf(inputWorker, inputModule))
            .processedWith(WorkerInjectProcessor())
            .compilesWithoutError()
            .and()
            .generatesSources(expectedFactory, expectedModule)
    }

    @Test
    fun assistedParametersFirstTest() {
        val inputWorker = JavaFileObjects.forSourceString(
            "test.TestWorker", """
            package test;

            import android.content.Context;
            import androidx.work.Worker;
            import androidx.work.WorkerParameters;
            import com.squareup.inject.assisted.Assisted;
            import com.vikingsen.inject.worker.WorkerInject;

            class TestWorker extends Worker {
                @WorkerInject
                TestWorker(@Assisted Context appContext, @Assisted WorkerParameters workerParams, Long foo) {
                    super(appContext, workerParams);
                }
            }
        """
        )
        val inputModule = JavaFileObjects.forSourceString(
            "test.TestModule", """
            package test;

            import com.vikingsen.inject.worker.WorkerModule;
            import dagger.Module;

            @WorkerModule
            @Module(includes = WorkerInject_TestModule.class)
            abstract class TestModule {}
        """
        )

        val expectedFactory = JavaFileObjects.forSourceString(
            "test.TestWorker_AssistedFactory", """
            package test;

            import android.content.Context
            import androidx.work.ListenableWorker;
            import androidx.work.WorkerParameters;
            import com.vikingsen.inject.worker.WorkerInjectFactory;
            import java.lang.Long;
            import java.lang.Override;
            import $GENERATED_TYPE;
            import javax.inject.Inject;
            import javax.inject.Provider;

            $GENERATED_ANNOTATION
            public final class TestWorker_AssistedFactory implements WorkerInjectFactory {
                private final Provider<Long> foo;

                @Inject public TestWorker_AssistedFactory(Provider<Long> foo) {
                    this.foo = foo;
                }

                @Override public ListenableWorker create(Context appContext, WorkerParameters workerParams) {
                    return new TestWorker(appContext, workerParams, foo.get());
                }
            }
        """
        )

        val expectedModule = JavaFileObjects.forSourceString(
            "test.WorkerInject_TestModule", """
            package test;

            import com.vikingsen.inject.worker.WorkerInjectFactory;
            import dagger.Binds;
            import dagger.Module;
            import dagger.multibindings.IntoMap;
            import dagger.multibindings.StringKey;
            import $GENERATED_TYPE;

            @Module
            $GENERATED_ANNOTATION
            abstract class WorkerInject_TestModule {
                private WorkerInject_TestModule() {}

                @Binds
                @IntoMap
                @StringKey("test.TestWorker")
                abstract WorkerInjectFactory bind_test_TestWorker(TestWorker_AssistedFactory factory);
            }
        """
        )

        assertAbout(javaSources())
            .that(listOf(inputWorker, inputModule))
            .processedWith(WorkerInjectProcessor())
            .compilesWithoutError()
            .and()
            .generatesSources(expectedFactory, expectedModule)
    }

    @Test
    fun differentNameContextTest() {
        val inputWorker = JavaFileObjects.forSourceString(
            "test.TestWorker", """
            package test;

            import android.content.Context;
            import androidx.work.Worker;
            import androidx.work.WorkerParameters;
            import com.squareup.inject.assisted.Assisted;
            import com.vikingsen.inject.worker.WorkerInject;

            class TestWorker extends Worker {
                @WorkerInject
                TestWorker(Long foo, @Assisted Context c, @Assisted WorkerParameters workerParams) {
                    super(c, workerParams);
                }
            }
        """
        )

        assertAbout(javaSource())
            .that(inputWorker)
            .processedWith(WorkerInjectProcessor())
            .failsToCompile()
            .withErrorContaining("""
            Worker injection requires Context appContext and WorkerParameters workerParams @Assisted parameters.
                Found:
                  [android.content.Context c, androidx.work.WorkerParameters workerParams]
                Expected:
                  [android.content.Context appContext, androidx.work.WorkerParameters workerParams]
            """.trimIndent())
            .`in`(inputWorker).onLine(12)
    }

    @Test
    fun differentNameWorkerParamsTest() {
        val inputWorker = JavaFileObjects.forSourceString(
            "test.TestWorker", """
            package test;

            import android.content.Context;
            import androidx.work.Worker;
            import androidx.work.WorkerParameters;
            import com.squareup.inject.assisted.Assisted;
            import com.vikingsen.inject.worker.WorkerInject;

            class TestWorker extends Worker {
                @WorkerInject
                TestWorker(Long foo, @Assisted Context appContext, @Assisted WorkerParameters wp) {
                    super(appContext, wp);
                }
            }
        """
        )

        assertAbout(javaSource())
            .that(inputWorker)
            .processedWith(WorkerInjectProcessor())
            .failsToCompile()
            .withErrorContaining("""
            Worker injection requires Context appContext and WorkerParameters workerParams @Assisted parameters.
                Found:
                  [android.content.Context appContext, androidx.work.WorkerParameters wp]
                Expected:
                  [android.content.Context appContext, androidx.work.WorkerParameters workerParams]
            """.trimIndent())
            .`in`(inputWorker).onLine(12)
    }

    @Test
    fun contextAndWorkerParametersSwappedTest() {
        val inputWorker = JavaFileObjects.forSourceString(
            "test.TestWorker", """
            package test;

            import android.content.Context;
            import androidx.work.Worker;
            import androidx.work.WorkerParameters;
            import com.squareup.inject.assisted.Assisted;
            import com.vikingsen.inject.worker.WorkerInject;

            class TestWorker extends Worker {
                @WorkerInject
                TestWorker(@Assisted WorkerParameters workerParams, @Assisted Context appContext, Long foo) {
                    super(appContext, workerParams);
                }
            }
        """
        )
        val inputModule = JavaFileObjects.forSourceString(
            "test.TestModule", """
            package test;

            import com.vikingsen.inject.worker.WorkerModule;
            import dagger.Module;

            @WorkerModule
            @Module(includes = WorkerInject_TestModule.class)
            abstract class TestModule {}
        """
        )

        val expectedFactory = JavaFileObjects.forSourceString(
            "test.TestWorker_AssistedFactory", """
            package test;

            import android.content.Context
            import androidx.work.ListenableWorker;
            import androidx.work.WorkerParameters;
            import com.vikingsen.inject.worker.WorkerInjectFactory;
            import java.lang.Long;
            import java.lang.Override;
            import $GENERATED_TYPE;
            import javax.inject.Inject;
            import javax.inject.Provider;

            $GENERATED_ANNOTATION
            public final class TestWorker_AssistedFactory implements WorkerInjectFactory {
                private final Provider<Long> foo;

                @Inject public TestWorker_AssistedFactory(Provider<Long> foo) {
                    this.foo = foo;
                }

                @Override public ListenableWorker create(Context appContext, WorkerParameters workerParams) {
                    return new TestWorker(workerParams, appContext, foo.get());
                }
            }
        """
        )

        val expectedModule = JavaFileObjects.forSourceString(
            "test.WorkerInject_TestModule", """
            package test;

            import com.vikingsen.inject.worker.WorkerInjectFactory;
            import dagger.Binds;
            import dagger.Module;
            import dagger.multibindings.IntoMap;
            import dagger.multibindings.StringKey;
            import $GENERATED_TYPE;

            @Module
            $GENERATED_ANNOTATION
            abstract class WorkerInject_TestModule {
                private WorkerInject_TestModule() {}

                @Binds
                @IntoMap
                @StringKey("test.TestWorker")
                abstract WorkerInjectFactory bind_test_TestWorker(TestWorker_AssistedFactory factory);
            }
        """
        )

        assertAbout(javaSources())
            .that(listOf(inputWorker, inputModule))
            .processedWith(WorkerInjectProcessor())
            .compilesWithoutError()
            .and()
            .generatesSources(expectedFactory, expectedModule)
    }

    @Test
    fun typeDoesNotExtendWorkerTest() {
        val inputWorker = JavaFileObjects.forSourceString(
            "test.TestWorker", """
            package test;

            import android.content.Context;
            import androidx.work.WorkerParameters;
            import com.squareup.inject.assisted.Assisted;
            import com.vikingsen.inject.worker.WorkerInject;

            class TestWorker {
                @WorkerInject
                TestWorker(Long foo, @Assisted Context appContext, @Assisted WorkerParameters workerParams) {
                    super(appContext, workerParams);
                }
            }
        """
        )

        assertAbout(javaSource())
            .that(inputWorker)
            .processedWith(WorkerInjectProcessor())
            .failsToCompile()
            .withErrorContaining("""
                @WorkerInject-using tyeps must be a subtype of androidx.work.ListenableWorker or androidx.work.Worker
            """.trimIndent())
            .`in`(inputWorker).onLine(9)
    }

    @Test
    fun baseAndSubtypeInjectionTest() {
        val inputWorker1 = JavaFileObjects.forSourceString(
            "test.TestWorker1", """
            package test;

            import android.content.Context;
            import androidx.work.Worker;
            import androidx.work.WorkerParameters;
            import com.squareup.inject.assisted.Assisted;
            import com.vikingsen.inject.worker.WorkerInject;

            class TestWorker1 extends Worker {
                @WorkerInject
                TestWorker1(Long foo, @Assisted Context appContext, @Assisted WorkerParameters workerParams) {
                    super(appContext, workerParams);
                }
            }
        """
        )

        val inputWorker2 = JavaFileObjects.forSourceString(
            "test.TestWorker2", """
            package test;

            import android.content.Context;
            import androidx.work.WorkerParameters;
            import com.squareup.inject.assisted.Assisted;
            import com.vikingsen.inject.worker.WorkerInject;

            class TestWorker2 extends TestWorker1 {
                @WorkerInject
                TestWorker2(Long foo, @Assisted Context appContext, @Assisted WorkerParameters workerParams) {
                    super(foo, appContext, workerParams);
                }
            }
        """
        )

        val expectedFactory1 = JavaFileObjects.forSourceString(
            "test.TestWorker1_AssistedFactory", """
            package test;

            import android.content.Context
            import androidx.work.ListenableWorker;
            import androidx.work.WorkerParameters;
            import com.vikingsen.inject.worker.WorkerInjectFactory;
            import java.lang.Long;
            import java.lang.Override;
            import $GENERATED_TYPE;
            import javax.inject.Inject;
            import javax.inject.Provider;

            $GENERATED_ANNOTATION
            public final class TestWorker1_AssistedFactory implements WorkerInjectFactory {
                private final Provider<Long> foo;

                @Inject public TestWorker1_AssistedFactory(Provider<Long> foo) {
                    this.foo = foo;
                }

                @Override public ListenableWorker create(Context appContext, WorkerParameters workerParams) {
                    return new TestWorker1(foo.get(), appContext, workerParams);
                }
            }
        """
        )

        val expectedFactory2 = JavaFileObjects.forSourceString(
            "test.TestWorker2_AssistedFactory", """
            package test;

            import android.content.Context
            import androidx.work.ListenableWorker;
            import androidx.work.WorkerParameters;
            import com.vikingsen.inject.worker.WorkerInjectFactory;
            import java.lang.Long;
            import java.lang.Override;
            import $GENERATED_TYPE;
            import javax.inject.Inject;
            import javax.inject.Provider;

            $GENERATED_ANNOTATION
            public final class TestWorker2_AssistedFactory implements WorkerInjectFactory {
                private final Provider<Long> foo;

                @Inject public TestWorker2_AssistedFactory(Provider<Long> foo) {
                    this.foo = foo;
                }

                @Override public ListenableWorker create(Context appContext, WorkerParameters workerParams) {
                    return new TestWorker2(foo.get(), appContext, workerParams);
                }
            }
        """
        )

        assertAbout(javaSources())
            .that(listOf(inputWorker1, inputWorker2))
            .processedWith(WorkerInjectProcessor())
            .compilesWithoutError()
            .and()
            .generatesSources(expectedFactory1, expectedFactory2)
    }

    @Test
    fun constructorMissingAssistedParametersFailsTest() {
        val inputWorker = JavaFileObjects.forSourceString(
            "test.TestWorker", """
            package test;

            import androidx.work.Worker;
            import com.vikingsen.inject.worker.WorkerInject;

            class TestWorker extends Worker {
                @WorkerInject
                TestWorker(Long foo) {
                    super(null, null);
                }
            }
        """
        )

        assertAbout(javaSource())
            .that(inputWorker)
            .processedWith(WorkerInjectProcessor())
            .failsToCompile()
            .withErrorContaining("""
            Worker injection requires Context appContext and WorkerParameters workerParams @Assisted parameters.
                Found:
                  []
                Expected:
                  [android.content.Context appContext, androidx.work.WorkerParameters workerParams]
            """.trimIndent())
            .`in`(inputWorker).onLine(9)
    }

    @Test
    fun constructorExtraAssistedParameterFailsTest() {
        val inputWorker = JavaFileObjects.forSourceString(
            "test.TestWorker", """
            package test;

            import android.content.Context;
            import androidx.work.Worker;
            import androidx.work.WorkerParameters;
            import com.squareup.inject.assisted.Assisted;
            import com.vikingsen.inject.worker.WorkerInject;

            class TestWorker extends Worker {
                @WorkerInject
                TestWorker(Long foo, @Assisted Context appContext, @Assisted WorkerParameters workerParams, @Assisted String hey) {
                    super(appContext, workerParams);
                }
            }
        """
        )

        assertAbout(javaSource())
            .that(inputWorker)
            .processedWith(WorkerInjectProcessor())
            .failsToCompile()
            .withErrorContaining("""
            Worker injection requires Context appContext and WorkerParameters workerParams @Assisted parameters.
                Found:
                  [android.content.Context appContext, androidx.work.WorkerParameters workerParams, java.lang.String hey]
                Expected:
                  [android.content.Context appContext, androidx.work.WorkerParameters workerParams]
            """.trimIndent())
            .`in`(inputWorker).onLine(12)
    }

    @Test
    fun constructorMissingContextFailsTest() {
        val inputWorker = JavaFileObjects.forSourceString(
            "test.TestWorker", """
            package test;

            import android.content.Context;
            import androidx.work.Worker;
            import androidx.work.WorkerParameters;
            import com.squareup.inject.assisted.Assisted;
            import com.vikingsen.inject.worker.WorkerInject;

            class TestWorker extends Worker {
                @WorkerInject
                TestWorker(Long foo, @Assisted WorkerParameters workerParams) {
                    super(null, workerParams);
                }
            }
        """
        )

        assertAbout(javaSource())
            .that(inputWorker)
            .processedWith(WorkerInjectProcessor())
            .failsToCompile()
            .withErrorContaining("""
            Worker injection requires Context appContext and WorkerParameters workerParams @Assisted parameters.
                Found:
                  [androidx.work.WorkerParameters workerParams]
                Expected:
                  [android.content.Context appContext, androidx.work.WorkerParameters workerParams]
            """.trimIndent())
            .`in`(inputWorker).onLine(12)
    }

    @Test
    fun constructorMissingWorkerParamsTest() {
        val inputWorker = JavaFileObjects.forSourceString(
            "test.TestWorker", """
            package test;

            import android.content.Context;
            import androidx.work.Worker;
            import androidx.work.WorkerParameters;
            import com.squareup.inject.assisted.Assisted;
            import com.vikingsen.inject.worker.WorkerInject;

            class TestWorker extends Worker {
                @WorkerInject
                TestWorker(Long foo, @Assisted Context appContext) {
                    super(appContext, null);
                }
            }
        """
        )

        assertAbout(javaSource())
            .that(inputWorker)
            .processedWith(WorkerInjectProcessor())
            .failsToCompile()
            .withErrorContaining("""
            Worker injection requires Context appContext and WorkerParameters workerParams @Assisted parameters.
                Found:
                  [android.content.Context appContext]
                Expected:
                  [android.content.Context appContext, androidx.work.WorkerParameters workerParams]
            """.trimIndent())
            .`in`(inputWorker).onLine(12)
    }

    @Test
    fun constructorMissingProvidedParametersWarnsTest() {
        val inputWorker = JavaFileObjects.forSourceString(
            "test.TestWorker", """
            package test;

            import android.content.Context;
            import androidx.work.Worker;
            import androidx.work.WorkerParameters;
            import com.squareup.inject.assisted.Assisted;
            import com.vikingsen.inject.worker.WorkerInject;

            class TestWorker extends Worker {
                @WorkerInject
                TestWorker(@Assisted Context appContext, @Assisted WorkerParameters workerParams) {
                    super(appContext, workerParams);
                }
            }
        """
        )

        assertAbout(javaSource())
            .that(inputWorker)
            .processedWith(WorkerInjectProcessor())
            .compilesWithoutError()
            .withWarningContaining("""
                Worker injection recommends at least one non-@Assisted paramter.
            """.trimIndent())
            .`in`(inputWorker).onLine(12)
    }

    @Test
    fun privateConstructorFailsTest() {
        val inputWorker = JavaFileObjects.forSourceString(
            "test.TestWorker", """
            package test;

            import android.content.Context;
            import androidx.work.Worker;
            import androidx.work.WorkerParameters;
            import com.squareup.inject.assisted.Assisted;
            import com.vikingsen.inject.worker.WorkerInject;

            class TestWorker extends Worker {
                @WorkerInject
                private TestWorker(Long foo, @Assisted Context appContext, @Assisted WorkerParameters workerParams) {
                    super(appContext, workerParams);
                }
            }
        """
        )

        assertAbout(javaSource())
            .that(inputWorker)
            .processedWith(WorkerInjectProcessor())
            .failsToCompile()
            .withErrorContaining("""
                @WorkerInject constructor must not be private
            """.trimIndent())
            .`in`(inputWorker).onLine(10)
    }

    @Test
    fun nestedPrivateTypeFailsTest() {
        val inputWorker = JavaFileObjects.forSourceString(
            "test.TestWorker", """
            package test;

            import android.content.Context;
            import androidx.work.Worker;
            import androidx.work.WorkerParameters;
            import com.squareup.inject.assisted.Assisted;
            import com.vikingsen.inject.worker.WorkerInject;

            class Outer {
                private static class TestWorker extends Worker {
                    @WorkerInject
                    private TestWorker(Long foo, @Assisted Context appContext, @Assisted WorkerParameters workerParams) {
                        super(appContext, workerParams);
                    }
                }
            }
        """
        )

        assertAbout(javaSource())
            .that(inputWorker)
            .processedWith(WorkerInjectProcessor())
            .failsToCompile()
            .withErrorContaining("""
                @WorkerInject-using types must not be private
            """.trimIndent())
            .`in`(inputWorker).onLine(11)
    }

    @Test
    fun nestedNonStaticFailsTest() {
        val inputWorker = JavaFileObjects.forSourceString(
            "test.TestWorker", """
            package test;

            import android.content.Context;
            import androidx.work.Worker;
            import androidx.work.WorkerParameters;
            import com.squareup.inject.assisted.Assisted;
            import com.vikingsen.inject.worker.WorkerInject;

            class Outer {
                class TestWorker extends Worker {
                    @WorkerInject
                    private TestWorker(Long foo, @Assisted Context appContext, @Assisted WorkerParameters workerParams) {
                        super(appContext, workerParams);
                    }
                }
            }
        """
        )

        assertAbout(javaSource())
            .that(inputWorker)
            .processedWith(WorkerInjectProcessor())
            .failsToCompile()
            .withErrorContaining("""
                @WorkerInject-using types must be static
            """.trimIndent())
            .`in`(inputWorker).onLine(11)
    }

    @Test
    fun multipleInflationInjectConstructorsFailsTest() {
        val inputWorker = JavaFileObjects.forSourceString(
            "test.TestWorker", """
            package test;

            import android.content.Context;
            import androidx.work.Worker;
            import androidx.work.WorkerParameters;
            import com.squareup.inject.assisted.Assisted;
            import com.vikingsen.inject.worker.WorkerInject;

            class TestWorker extends Worker {
                @WorkerInject
                TestWorker(Long foo, @Assisted Context appContext, @Assisted WorkerParameters workerParams) {
                    super(appContext, workerParams);
                }
                
                @WorkerInject
                TestWorker(String foo, @Assisted Context appContext, @Assisted WorkerParameters workerParams) {
                    super(appContext, workerParams);
                }
            }
        """
        )

        assertAbout(javaSource())
            .that(inputWorker)
            .processedWith(WorkerInjectProcessor())
            .failsToCompile()
            .withErrorContaining("""
                Multiple @WorkerInject-annotated constructors found.
            """.trimIndent())
            .`in`(inputWorker).onLine(10)
    }

    @Test
    fun moduleWithoutModuleAnnotationFailsTest() {
        val inputModule = JavaFileObjects.forSourceString(
            "test.TestModule", """
            package test;

            import com.vikingsen.inject.worker.WorkerModule;

            @WorkerModule
            abstract class TestModule {}
        """
        )

        assertAbout(javaSource())
            .that(inputModule)
            .processedWith(WorkerInjectProcessor())
            .failsToCompile()
            .withErrorContaining("""
                @WorkerModule must also be annotated as Dagger @Module
            """.trimIndent())
            .`in`(inputModule).onLine(7)
    }

    @Test
    fun moduleWithNoIncludesFailsTest() {
        val inputModule = JavaFileObjects.forSourceString(
            "test.TestModule", """
            package test;

            import com.vikingsen.inject.worker.WorkerModule;
            import dagger.Module;

            @WorkerModule
            @Module
            abstract class TestModule {}
        """
        )

        assertAbout(javaSource())
            .that(inputModule)
            .processedWith(WorkerInjectProcessor())
            .failsToCompile()
            .withErrorContaining("""
                @WorkerModule's @Module must include WorkerInject_TestModule
            """.trimIndent())
            .`in`(inputModule).onLine(9)
    }

    @Test
    fun moduleWithoutIncludesFailsTest() {
        val inputModule = JavaFileObjects.forSourceString(
            "test.TestModule", """
            package test;

            import com.vikingsen.inject.worker.WorkerModule;
            import dagger.Module;

            @WorkerModule
            @Module(includes = TestModule2.cass)
            abstract class TestModule {}
            
            @Module
            abstract class TestModule2 {}
        """
        )

        assertAbout(javaSource())
            .that(inputModule)
            .processedWith(WorkerInjectProcessor())
            .failsToCompile()
            .withErrorContaining("""
                @WorkerModule's @Module must include WorkerInject_TestModule
            """.trimIndent())
            .`in`(inputModule).onLine(9)
    }

    @Test
    fun multipleModulesFailsTest() {
        val inputModule1 = JavaFileObjects.forSourceString(
            "test.TestModule1", """
            package test;

            import com.vikingsen.inject.worker.WorkerModule;
            import dagger.Module;

            @WorkerModule
            @Module(includes = WorkerInject_TestModule1.cass)
            abstract class TestModule1 {}
        """
        )

        val inputModule2 = JavaFileObjects.forSourceString(
            "test.TestModule2", """
            package test;

            import com.vikingsen.inject.worker.WorkerModule;
            import dagger.Module;

            @WorkerModule
            @Module(includes = WorkerInject_TestModule2.cass)
            abstract class TestModule2 {}
        """
        )

        assertAbout(javaSources())
            .that(listOf(inputModule1, inputModule2))
            .processedWith(WorkerInjectProcessor())
            .failsToCompile()
            .withErrorContaining("""
                Multiple @WorkerModule-annotated modules found.
            """.trimIndent())
            .`in`(inputModule1).onLine(9)
            .and()
            .withErrorContaining("""
                Multiple @WorkerModule-annotated modules found.
            """.trimIndent())
            .`in`(inputModule1).onLine(9)
    }

}