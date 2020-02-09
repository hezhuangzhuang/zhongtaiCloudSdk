package com.hw.cloudlibrary.utils;

import android.app.PendingIntent;
import android.content.Intent;

import com.huawei.opensdk.commonservice.common.common.LocContext;
import com.huawei.opensdk.commonservice.common.util.LogUtil;
import com.hw.cloudlibrary.activity.VideoConfActivity;
import com.hw.cloudlibrary.ecsdk.common.UIConstants;


public class DeviceUtil {

    /**
     * 将app拉到前台
     */
    public static void bringTaskBackToFront() {
        //后台拉起临时界面
        LogUtil.i(UIConstants.DEMO_TAG, "bringTaskBackToFront.");

        Intent intent = new Intent(LocContext.getContext(), VideoConfActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(LocContext.getContext()
                        , 0, intent, PendingIntent.FLAG_ONE_SHOT);
        try {
            pendingIntent.send();
        } catch (PendingIntent.CanceledException e) {
            LogUtil.e(UIConstants.DEMO_TAG, e.getMessage());
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        LocContext.getContext().startActivity(intent);
    }

    public static void jumpToHomeScreen() {
        LogUtil.i(UIConstants.DEMO_TAG, "jump to home screen.");
        Intent localIntent = new Intent("android.intent.action.MAIN");
        localIntent.addCategory("android.intent.category.HOME");
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        LocContext.getContext().startActivity(localIntent);
    }

    /**
     * 判断当前是否在前台 ture:前台  false：后台
     */
    public static boolean isAppForeground() {
        return false;
//        return ECApplication.getAppCount() > 0;
    }

}
