package com.hw.cloudtestdemo;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.gyf.immersionbar.ImmersionBar;
import com.huawei.opensdk.commonservice.common.localbroadcast.CustomBroadcastConstants;
import com.huawei.opensdk.commonservice.common.localbroadcast.LocBroadcast;
import com.huawei.opensdk.commonservice.common.localbroadcast.LocBroadcastReceiver;
import com.huawei.opensdk.commonservice.common.util.LogUtil;
import com.hw.cloudlibrary.activity.BaseLibActivity;
import com.hw.cloudlibrary.ecsdk.common.UIConstants;
import com.hw.cloudlibrary.ecsdk.utils.ActivityStack;
import com.hw.cloudlibrary.inter.HuaweiLoginImp;
import com.hw.cloudlibrary.utils.Constants;
import com.hw.cloudlibrary.utils.ToastHelper;
import com.hw.cloudlibrary.utils.sharedpreferences.SPStaticUtils;

public class LoginActivity extends BaseLibActivity {
    private EditText etUsername;
    private EditText etPassword;
    private Button btLogin;

    public static void startActivity(Context context) {
        context.startActivity(new Intent(context, LoginActivity.class));
    }

    @Override
    public void findViews() {
        etUsername = (EditText) findViewById(R.id.et_username);
        etPassword = (EditText) findViewById(R.id.et_password);
        btLogin = (Button) findViewById(R.id.bt_login);
    }

    @Override
    public void initData() {
        ImmersionBar.with(this)
//                .transparentStatusBar()  //透明状态栏，不写默认透明色
//                .transparentNavigationBar()  //透明导航栏，不写默认黑色(设置此方法，fullScreen()方法自动为true)
//                .transparentBar()             //透明状态栏和导航栏，不写默认状态栏为透明色，导航栏为黑色（设置此方法，fullScreen()方法自动为true）
                .statusBarColor(com.hw.cloudlibrary.R.color.color_548ddf)     //状态栏颜色，不写默认透明色
//                .navigationBarColor(R.color.colorPrimary) //导航栏颜色，不写默认黑色
//                .barColor(R.color.colorPrimary)  //同时自定义状态栏和导航栏颜色，不写默认状态栏为透明色，导航栏为黑色
                .fitsSystemWindows(true)
                .statusBarAlpha(0.3f)  //状态栏透明度，不写默认0.0f
                .navigationBarAlpha(0.4f)  //导航栏透明度，不写默认0.0F
                .barAlpha(0.3f)  //状态栏和导航栏透明度，不写默认0.0f
                .statusBarDarkFont(true)   //状态栏字体是深色，不写默认为亮色
                .navigationBarDarkIcon(true) //导航栏图标是深色，不写默认为亮色
                .autoDarkModeEnable(true) //自动状态栏字体和导航栏图标变色，必须指定状态栏颜色和导航栏颜色才可以自动变色哦
                .autoStatusBarDarkModeEnable(true, 0.2f) //自动状态栏字体变色，必须指定状态栏颜色才可以自动变色哦
                .autoNavigationBarDarkModeEnable(true, 0.2f) //自动导航栏图标变色，必须指定导航栏颜色才可以自动变色哦
                .flymeOSStatusBarFontColor(com.hw.cloudlibrary.R.color.black)  //修改flyme OS状态栏字体颜色
//                .reset()  //重置所以沉浸式参数
                .init();  //必须调用方可应用以上所配置的参数

        //登录广播
        LocBroadcast.getInstance().registerBroadcast(loginReceiver, mActions);

        etUsername.setFocusable(true);
        etUsername.setFocusableInTouchMode(true);
        etUsername.requestFocus();

        etUsername.setText(SPStaticUtils.getString(Constants.USER_NAME));
        etPassword.setText(SPStaticUtils.getString(Constants.PASS_WORD));

        //设置光标位置
        CharSequence text = etUsername.getText();

        if (text instanceof Spannable) {
            Spannable spanText = (Spannable) text;
            Selection.setSelection(spanText, text.length());
        }

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    @Override
    public void setListener() {
        btLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userName = etUsername.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                if (!checkInput(userName, password)) {
                    return;
                }
                ToastHelper.showShort("开始登录");
                HuaweiLoginImp.getInstance().loginRequest(userName, password);
            }
        });
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_login;
    }

    /**
     * 判断是否已经点击过一次回退键
     */
    private boolean isBackPressed = false;

    private void doublePressBackToast() {
        if (!isBackPressed) {
            Log.i("doublePressBackToast", "再次点击返回退出程序");
            isBackPressed = true;
            Toast.makeText(this, "再次点击返回退出程序", Toast.LENGTH_SHORT).show();
        } else {
//            HuaweiLoginImp.getInstance().logOut();
            Log.i("doublePressBackToast", "exit");
            finish();
            ActivityStack.getIns().popup(this);
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                isBackPressed = false;
            }
        }, 2000);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            doublePressBackToast();
            return true;
        } else {
            return super.onKeyUp(keyCode, event);
        }
    }

    /**
     * 判断输入是否合理
     *
     * @param userName
     * @param password
     * @return
     */
    private boolean checkInput(String userName, String password) {
        if (TextUtils.isEmpty(userName)) {
            Toast.makeText(this, "用户名不能为空", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "密码不能为空", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }


    /*华为登录相关start*/
    public static String[] mActions = new String[]{
            CustomBroadcastConstants.LOGIN_SUCCESS,
            CustomBroadcastConstants.LOGIN_FAILED,
            CustomBroadcastConstants.LOGOUT
    };

    private LocBroadcastReceiver loginReceiver = new LocBroadcastReceiver() {
        @Override
        public void onReceive(String broadcastName, Object obj) {
            Log.i(LogUtil.DEMO_LOG, "loginReceiver-->" + broadcastName);
            switch (broadcastName) {
                case CustomBroadcastConstants.LOGIN_SUCCESS:
                    dismissProgressDialog(LoginActivity.this);

                    LogUtil.i(UIConstants.DEMO_TAG, "login success");
                    Toast.makeText(LoginActivity.this, "华为平台登录成功", Toast.LENGTH_SHORT).show();

//                    PreferencesHelper.saveData(Constants.USER_NAME, etUsername.getText().toString().trim());
//                    PreferencesHelper.saveData(Constants.PASS_WORD, etPassword.getText().toString().trim());

                    SPStaticUtils.put(Constants.USER_NAME, etUsername.getText().toString().trim());
                    SPStaticUtils.put(Constants.PASS_WORD, etPassword.getText().toString().trim());

                    ConvokeActivity.startActivity(LoginActivity.this);
                    finish();
                    break;

                case CustomBroadcastConstants.LOGIN_FAILED:
                    dismissProgressDialog(LoginActivity.this);
                    String errorMessage = ((String) obj);
                    LogUtil.i(UIConstants.DEMO_TAG, "login failed," + errorMessage);
                    Toast.makeText(LoginActivity.this, "华为平台登录失败：" + errorMessage, Toast.LENGTH_SHORT).show();
                    break;

                case CustomBroadcastConstants.LOGOUT:
                    LogUtil.i(UIConstants.DEMO_TAG, "logout success");
                    break;

                default:
                    break;
            }
        }
    };

    @Override
    public void onDestroy() {
        LocBroadcast.getInstance().unRegisterBroadcast(loginReceiver, mActions);

        super.onDestroy();
    }

    private ProgressDialog dialog;

    private void showProgressDialog() {
        showProgressDialog(this, "正在登录...");
    }

    private void dismissDialog() {
        dismissProgressDialog(this);
    }

    private Dialog progressDialog;

    public void showProgressDialog(Context context, String content) {
        if (context instanceof Activity) {
            if (((Activity) context).isFinishing()) {
                return;
            }
        }

        if (null != progressDialog && progressDialog.isShowing()) {
            return;
        }

        if (null == progressDialog) {
            progressDialog = new Dialog(context, R.style.CustomDialogStyle);
        }

        View dialogView = View.inflate(context, R.layout.dialog_progress, null);
        TextView tvContent = (TextView) dialogView.findViewById(R.id.tv_content);
        if (TextUtils.isEmpty(content)) {
            tvContent.setVisibility(View.GONE);
        } else {
            tvContent.setText(content);
            tvContent.setVisibility(View.VISIBLE);
        }
        progressDialog.setContentView(dialogView);
        progressDialog.setCancelable(true);
        progressDialog.setCanceledOnTouchOutside(false);
        try {
            progressDialog.show();
        } catch (WindowManager.BadTokenException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * 判断进度对话框是否显示
     *
     * @return
     */
    public void dismissProgressDialog(Context context) {
        if (context instanceof Activity) {
            if (((Activity) context).isFinishing()) {
                progressDialog = null;
                return;
            }
        }

        if (progressDialog != null && progressDialog.isShowing()) {
            Context loadContext = progressDialog.getContext();
            if (loadContext != null && loadContext instanceof Activity) {
                if (((Activity) loadContext).isFinishing()) {
                    progressDialog = null;
                    return;
                }
            }
            progressDialog.dismiss();
            progressDialog = null;
        }
    }
}
