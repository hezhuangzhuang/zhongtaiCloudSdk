package com.hw.cloudtestdemo;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.huawei.opensdk.callmgr.CallMgr;
import com.huawei.opensdk.callmgr.ctdservice.CtdMgr;
import com.huawei.opensdk.commonservice.common.common.LocContext;
import com.huawei.opensdk.commonservice.common.util.CrashUtil;
import com.huawei.opensdk.commonservice.common.util.LogUtil;
import com.huawei.opensdk.contactservice.eaddr.EnterpriseAddressBookMgr;
import com.huawei.opensdk.demoservice.MeetingMgr;
import com.huawei.opensdk.loginmgr.LoginMgr;
import com.huawei.opensdk.servicemgr.ServiceMgr;
import com.hw.cloudlibrary.ecsdk.common.UIConstants;
import com.hw.cloudlibrary.ecsdk.login.CallFunc;
import com.hw.cloudlibrary.ecsdk.login.ConfFunc;
import com.hw.cloudlibrary.ecsdk.login.EnterpriseAddrBookFunc;
import com.hw.cloudlibrary.ecsdk.login.LoginFunc;
import com.hw.cloudlibrary.ecsdk.utils.FileUtil;
import com.hw.cloudlibrary.inter.HuaweiLoginImp;
import com.hw.cloudlibrary.utils.Constants;
import com.hw.cloudlibrary.utils.ZipUtil;
import com.zxy.recovery.core.Recovery;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

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
