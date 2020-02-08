package com.hw.cloudtestdemo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.huawei.opensdk.commonservice.common.common.LocContext;
import com.hw.cloudlibrary.inter.HuaweiCallImp;
import com.hw.cloudlibrary.inter.HuaweiLoginImp;
import com.hw.cloudlibrary.utils.PermissionConstants;
import com.hw.cloudlibrary.utils.PermissionUtils;
import com.hw.cloudlibrary.utils.ToastHelper;

import java.util.List;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        checkPermission();
    }

    private void checkPermission() {
        PermissionUtils.permission(
                PermissionConstants.STORAGE,
                PermissionConstants.CAMERA,
                PermissionConstants.MICROPHONE
        ).callback(new PermissionUtils.FullCallback() {
            @Override
            public void onGranted(List<String> permissionsGranted) {
//                ToastHelper.showShort("获取权限");
                HuaweiLoginImp.getInstance().initHuawei(App.getInst(), BuildConfig.APPLICATION_ID);
                LoginActivity.startActivity(WelcomeActivity.this);
                finish();
            }

            @Override
            public void onDenied(List<String> permissionsDeniedForever, List<String> permissionsDenied) {
                finish();
            }
        }).request();
    }
}
