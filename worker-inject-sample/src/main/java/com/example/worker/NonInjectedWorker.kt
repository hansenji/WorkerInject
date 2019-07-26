package com.example.worker

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters

class NonInjectedWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : Worker(appContext, workerParams) {
    override fun doWork(): Result {
        Log.d("NonInjectedWorker", "Waz Up NonInjected Worker")
        return Result.success()
    }
}