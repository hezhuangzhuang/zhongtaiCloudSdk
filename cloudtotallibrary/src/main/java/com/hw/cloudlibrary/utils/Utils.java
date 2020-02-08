package com.hw.cloudlibrary.utils;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;

public class Utils {

//    private static final ActivityLifecycleImpl ACTIVITY_LIFECYCLE = new ActivityLifecycleImpl();
//    private static final ExecutorService UTIL_POOL          = ThreadUtils.getCachedPool();
    private static final Handler UTIL_HANDLER       = new Handler(Looper.getMainLooper());

    public static void runOnUiThread(final Runnable runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
        } else {
            Utils.UTIL_HANDLER.post(runnable);
        }
    }
}
