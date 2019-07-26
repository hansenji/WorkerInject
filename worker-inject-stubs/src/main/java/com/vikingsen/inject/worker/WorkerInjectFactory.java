package com.vikingsen.inject.worker;

import android.content.Context;

import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;

public interface WorkerInjectFactory {
    ListenableWorker create(Context appContext, WorkerParameters workerParameters);
}
