Assisted Injection for Androidx Workers
=======================================

This library is based on and depends heavily on [Assisted Inject](https://github.com/square/AssistedInject)

WorkerInject supports [Dagger2](https://google.github.io/dagger/) 

This Library will go 1.0 when Assisted Inject goes 1.0.

Usage
-----

#### Worker

Java
```java
class MyWorker extends Worker {
    @WorkerInject
    MyWorker(Long foo, @Assisted Context appContext, @Assisted WorkerParameter workerParams) {
        super(appContext, workerParams)
    }
}
```
Kotlin
```kotlin
class MyWorker
@WorkerInject constructor(
    foo: Long, @Assisted appContext: Context, @Assisted workerParams: WorkerParameter
): Worker(appContext, workerParams) {}
```

#### Module

In order to allow Dagger to use the generated factory, define an assisted dagger module anywhere in 
the same gradle module:

Java
```java
@WorkerModule
@Module(includes = WorkerInject_AssistModule.class)
abstract class AssistModule {}
``` 
Kotlin
```kotlin
@WorkerModule
@Module(includes = [WorkerInject_AssistModule::class])
abstract class AssistModule
``` 

The library will generate the `WorkerInject_AssistModule` for us.

#### Factory

It is recommended to use [On-demand Initialization](https://developer.android.com/topic/libraries/architecture/workmanager/advanced/custom-configuration#on-demand).

To do this inside your Application inject the `com.vikingsen.inject.worker.WorkerFactory`

Java
```java
class App extends Application implements Configuration.Provider {
    @Inject WorkerFactory workerFactory;

    @Override
    public Configuration getWorkManagerConfiguration() {
        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build();
    }
}
```
Kotlin
```kotlin
class App: Application(), Configuration.Provider {
    @Inject 
    lateinit var workerFactory: WorkerFactory

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    }
}
```

And in your `AndroidManifest.xml` add:
```xml
<provider
    android:name="androidx.work.impl.WorkManagerInitializer"
    android:authorities="${applicationId}.workmanager-init"
    tools:node="remove" />
```

Download
--------
```groovy
implementation 'com.vikingsen.inject:worker-inject:0.2.1'
annotationProcessor 'com.vikingsen.inject:worker-inject-processor:0.2.1' // or `kapt` for Kotlin
```

For Snapshots include the following repository:
```groovy
repositories {
    // ...
    maven { url 'https://oss.sonatype.org/content/repositories/snapshots' }
}
```

License
=======

    Copyright 2019 Jordan Hansen

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
