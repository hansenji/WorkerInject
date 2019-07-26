package com.vikingsen.inject.worker;

import android.content.Context;

import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;

public class WorkerFactory extends androidx.work.WorkerFactory {

    @Override
    public ListenableWorker createWorker(Context appContext, String workerClassName, WorkerParameters workerParameters) {
        throw new RuntimeException("STUB!");
    }
}
