package androidx.work;

import android.content.Context;

public abstract class WorkerFactory {

    public abstract ListenableWorker createWorker(Context appContext, String workerClassName, WorkerParameters workerParameters);
}
