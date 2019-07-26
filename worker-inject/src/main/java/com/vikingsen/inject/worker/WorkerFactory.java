package com.vikingsen.inject.worker;

import android.content.Context;
import android.util.Log;

import java.lang.reflect.Constructor;
import java.util.Map;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.work.ListenableWorker;
import androidx.work.Logger;
import androidx.work.WorkerParameters;

public class WorkerFactory extends androidx.work.WorkerFactory {

    private static final String TAG = "WI-WorkerFactory";

    private final Map<String, WorkerInjectFactory> factories;

    @Inject
    public WorkerFactory(@NonNull Map<String, WorkerInjectFactory> factories) {
        if (factories == null) throw new NullPointerException("factories == null");
        this.factories = factories;
    }

    @Nullable
    @Override
    public ListenableWorker createWorker(@NonNull Context appContext, @NonNull String workerClassName, @NonNull WorkerParameters workerParameters) {
        WorkerInjectFactory factory = factories.get(workerClassName);
        if (factory != null) {
            try {
                return factory.create(appContext, workerParameters);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }
}
