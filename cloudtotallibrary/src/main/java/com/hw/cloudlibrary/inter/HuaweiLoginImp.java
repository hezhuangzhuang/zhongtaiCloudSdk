package com.hw.cloudlibrary.inter;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;

import com.google.gson.Gson;
import com.huawei.opensdk.callmgr.CallMgr;
import com.huawei.opensdk.callmgr.ctdservice.CtdMgr;
import com.huawei.opensdk.commonservice.common.common.LocContext;
import com.huawei.opensdk.commonservice.common.util.CrashUtil;
import com.huawei.opensdk.commonservice.common.util.LogUtil;
import com.huawei.opensdk.contactservice.eaddr.EnterpriseAddressBookMgr;
import com.huawei.opensdk.demoservice.MeetingMgr;
import com.huawei.opensdk.loginmgr.LoginConstant;
import com.huawei.opensdk.loginmgr.LoginMgr;
import com.huawei.opensdk.loginmgr.LoginParam;
import com.huawei.opensdk.servicemgr.ServiceMgr;
import com.hw.cloudlibrary.ecsdk.common.UIConstants;
import com.hw.cloudlibrary.ecsdk.login.CallFunc;
import com.hw.cloudlibrary.ecsdk.login.ConfFunc;
import com.hw.cloudlibrary.ecsdk.login.EnterpriseAddrBookFunc;
import com.hw.cloudlibrary.ecsdk.login.LoginFunc;
import com.hw.cloudlibrary.ecsdk.utils.FileUtil;
import com.hw.cloudlibrary.utils.Constants;
import com.hw.cloudlibrary.utils.DateUtil;
import com.hw.cloudlibrary.utils.ZipUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * author：pc-20171125
 * data:2019/1/3 11:11
 * 登录
 */
public class HuaweiLoginImp {
    private static HuaweiLoginImp loginImp = new HuaweiLoginImp();
    private String TAG = HuaweiLoginImp.class.getSimpleName();

    public static HuaweiLoginImp getInstance() {
        return loginImp;
    }

    /**
     * 登出
     */
    public void logOut() {
        LoginMgr.getInstance().logout();
    }

    public void loginRequest(String userName, String password) {
        if (null == Looper.myLooper()) {
            Looper.prepare();
        }

        LoginParam loginParam = new LoginParam();

        loginParam.setServerUrl(Constants.UPORTAL_REGISTER_SERVER);
        loginParam.setServerPort(Integer.parseInt(Constants.UPORTAL_PORT));
        loginParam.setUserName(userName);
        loginParam.setPassword(password);

        loginParam.setVPN(false);

        LoginMgr.getInstance().login(loginParam);

        importFile();
    }

    /**
     * import file.
     */
    private void importFile() {
        LogUtil.i(UIConstants.DEMO_TAG, "import media file!~");
        Executors.newFixedThreadPool(LoginConstant.FIXED_NUMBER).execute(new Runnable() {
            @Override
            public void run() {
                importMediaFile();
                importBmpFile();
                importAnnotFile();
            }
        });
    }

    private void importBmpFile() {
        if (FileUtil.isSdCardExist()) {
            try {
                String bmpPath = Environment.getExternalStorageDirectory() + File.separator + Constants.BMP_FILE;
                InputStream bmpInputStream = LocContext.getContext().getAssets().open(Constants.BMP_FILE);
                FileUtil.copyFile(bmpInputStream, bmpPath);
            } catch (IOException e) {
                LogUtil.e(UIConstants.DEMO_TAG, e.getMessage());
            }
        }
    }

    private void importAnnotFile() {
        if (FileUtil.isSdCardExist()) {
            try {
                String bmpPath = Environment.getExternalStorageDirectory() + File.separator + Constants.ANNOT_FILE;
                File file = new File(bmpPath);
                if (!file.exists()) {
                    file.mkdir();
                }

                String[] bmpNames = new String[]{
                        "check.bmp",
                        "xcheck.bmp",
                        "lpointer.bmp",
                        "rpointer.bmp",
                        "upointer.bmp",
                        "dpointer.bmp",
                        "lp.bmp"
                };
                String[] paths = new String[bmpNames.length];

                for (int list = 0; list < paths.length; ++list) {
                    paths[list] = bmpPath + File.separator + bmpNames[list];
                    InputStream bmpInputStream = LocContext.getContext().getAssets().open(bmpNames[list]);
                    FileUtil.copyFile(bmpInputStream, paths[list]);
                }

            } catch (IOException e) {
                LogUtil.e(UIConstants.DEMO_TAG, e.getMessage());
            }
        }
    }

    private void importMediaFile() {
        if (FileUtil.isSdCardExist()) {
            try {
                String mediaPath = Environment.getExternalStorageDirectory() + File.separator + Constants.RINGING_FILE;
                InputStream mediaInputStream = LocContext.getContext().getAssets().open(Constants.RINGING_FILE);
                FileUtil.copyFile(mediaInputStream, mediaPath);

                String ringBackPath = Environment.getExternalStorageDirectory() + File.separator + Constants.RING_BACK_FILE;
                InputStream ringBackInputStream = LocContext.getContext().getAssets().open(Constants.RING_BACK_FILE);
                FileUtil.copyFile(ringBackInputStream, ringBackPath);
            } catch (IOException e) {
                LogUtil.e(UIConstants.DEMO_TAG, e.getMessage());
            }
        }
    }

    /**
     * IDO协议
     */
    private int mIdoProtocol = 0;

    /**
     * 文件长度
     */
    private static final int EXPECTED_FILE_LENGTH = 7;

    /********************************************资源初始化部分 Begin ******************************************************/
    public void initHuawei(Application application, String pageName) {
        //判断时间
        if (DateUtil.test(UIConstants.LEGITIMATE_TIME)) {
            return;
        }

        LocContext.init(application);

        if (!isFrontProcess(application, pageName)) {
            LocContext.init(application);
            CrashUtil.getInstance().init(application);
            Log.i("SDKDemo", "onCreate: PUSH Process.");
            return;
        }

        String appPath = application.getApplicationInfo().dataDir + "/lib";
        boolean b = ServiceMgr.getServiceMgr().startService(application, appPath, mIdoProtocol);
        if (!b) {
            return;
        }

        Log.i("SDKDemo", "onCreate: MAIN Process.");

        LoginMgr.getInstance().regLoginEventNotification(LoginFunc.getInstance());
        CallMgr.getInstance().regCallServiceNotification(CallFunc.getInstance());
        CtdMgr.getInstance().regCtdNotification(CallFunc.getInstance());
        MeetingMgr.getInstance().regConfServiceNotification(ConfFunc.getInstance());
        EnterpriseAddressBookMgr.getInstance().registerNotification(EnterpriseAddrBookFunc.getInstance());

        ServiceMgr.getServiceMgr().securityParam(
                Constants.SRTP_MODE,
                Constants.SIP_TRANSPORT_MODE,
                Constants.APP_CONFIG,
                Constants.TUNNEL_MODE
        );

        ServiceMgr.getServiceMgr().networkParam(
                Constants.UDP_DEFAULT,
                Constants.TLS_DEFAULT,
                Constants.PORT_CONFIG_PRIORITY
        );

        initResourceFile();
    }

    private static boolean isFrontProcess(Context context, String frontPkg) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        List<ActivityManager.RunningAppProcessInfo> infos = manager.getRunningAppProcesses();
        if (infos == null || infos.isEmpty()) {
            return false;
        }

        final int pid = android.os.Process.myPid();
        for (ActivityManager.RunningAppProcessInfo info : infos) {
            if (info.pid == pid) {
                Log.i(UIConstants.DEMO_TAG, "processName-->" + info.processName);
                return frontPkg.equals(info.processName);
            }
        }

        return false;
    }

    private void initResourceFile() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                initDataConfRes();
            }
        }).start();
    }

    private void initDataConfRes() {
        String path = LocContext.getContext().getFilesDir() + "/AnnoRes";
        File file = new File(path);
        if (file.exists()) {
            LogUtil.i(UIConstants.DEMO_TAG, file.getAbsolutePath());
            File[] files = file.listFiles();
            if (null != files && EXPECTED_FILE_LENGTH == files.length) {
                return;
            } else {
                FileUtil.deleteFile(file);
            }
        }

        try {
            InputStream inputStream = LocContext.getContext().getAssets().open("AnnoRes.zip");
            ZipUtil.unZipFile(inputStream, path);
        } catch (IOException e) {
            LogUtil.i(UIConstants.DEMO_TAG, "close...Exception->e" + e.toString());
        }
    }
    /********************************************资源初始化部分 End ******************************************************/

}
