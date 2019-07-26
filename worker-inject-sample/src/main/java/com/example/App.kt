package com.example

import android.app.Application
import androidx.work.Configuration
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.example.inject.DaggerAppComponent
import com.example.worker.InjectedWorker
import com.example.worker.NonInjectedWorker
import com.vikingsen.inject.worker.WorkerFactory
import javax.inject.Inject

class App : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: WorkerFactory

    override fun onCreate() {
        super.onCreate()
        DaggerAppComponent.create().inject(this)

        val workManager = WorkManager.getInstance(this)
        workManager.enqueue(OneTimeWorkRequest.from(InjectedWorker::class.java))
        workManager.enqueue(OneTimeWorkRequest.from(NonInjectedWorker::class.java))
    }

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    }
}