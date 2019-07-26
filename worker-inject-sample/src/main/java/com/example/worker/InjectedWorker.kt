package com.example.worker

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.Greeter
import com.squareup.inject.assisted.Assisted
import com.vikingsen.inject.worker.WorkerInject

class InjectedWorker
@WorkerInject constructor(
    private val greeter: Greeter,
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters
) : Worker(appContext, workerParams) {
    override fun doWork(): Result {
        Log.d("InjectedWorker", greeter.sayHi("Injected Worker"))
        return Result.success()
    }
}
