package com.hw.cloudtestdemo;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.huawei.opensdk.commonservice.common.common.LocContext;
import com.huawei.opensdk.servicemgr.ServiceMgr;
import com.zxy.recovery.core.Recovery;

/**
 * author：Thinkpad
 * data:2019/9/21 08:53
 */
public class App extends Application {
    private static int appCount = 0;

    private static Application inst = null;

    @Override
    public void onCreate() {
        super.onCreate();

        LocContext.init(this);

        //bug本地提示
        Recovery.getInstance()
                .debug(true)
                .recoverInBackground(false)
                .recoverStack(true)
                .mainPage(LoginActivity.class)
                .recoverEnabled(true)
//                .callback(new MyCrashCallback())
                .silent(false, Recovery.SilentMode.RECOVER_ACTIVITY_STACK)
//                .skip(TestActivity.class)
                .init(this);

        setInst(this);

        //Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            }

            @Override
            public void onActivityStarted(Activity activity) {
                appCount++;
            }

            @Override
            public void onActivityResumed(Activity activity) {
            }

            @Override
            public void onActivityPaused(Activity activity) {
            }

            @Override
            public void onActivityStopped(Activity activity) {
                appCount--;
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });
    }

    public static Application getInst() {
        return inst;
    }

    public static void setInst(Application inst) {
        App.inst = inst;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        ServiceMgr.getServiceMgr().stopService();
    }

}
