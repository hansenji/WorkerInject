package com.vikingsen.inject.worker.processor

import com.google.common.truth.Truth.assertAbout
import com.google.testing.compile.JavaFileObjects
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
}