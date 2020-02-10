package com.hw.cloudtestdemo;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.gyf.immersionbar.ImmersionBar;
import com.huawei.opensdk.commonservice.common.localbroadcast.CustomBroadcastConstants;
import com.huawei.opensdk.commonservice.common.localbroadcast.LocBroadcast;
import com.huawei.opensdk.commonservice.common.localbroadcast.LocBroadcastReceiver;
import com.huawei.opensdk.commonservice.common.util.LogUtil;
import com.huawei.opensdk.demoservice.ConfDetailInfo;
import com.huawei.opensdk.demoservice.MeetingMgr;
import com.huawei.opensdk.loginmgr.LoginMgr;
import com.hw.cloudlibrary.activity.BaseLibActivity;
import com.hw.cloudlibrary.activity.ConfListActivity;
import com.hw.cloudlibrary.inter.HuaweiCallImp;
import com.hw.cloudlibrary.inter.HuaweiLoginImp;
import com.hw.cloudlibrary.utils.Constants;
import com.hw.cloudlibrary.utils.ToastHelper;
import com.hw.cloudlibrary.utils.sharedpreferences.SPStaticUtils;

import java.util.Arrays;
import java.util.List;


public class ConvokeActivity extends BaseLibActivity implements View.OnClickListener {
    private EditText etNumber;
    private Button btCallNumber;

    private EditText etJoinNumber;
    private EditText etPwd;
    private Button btJoin;

    private EditText etMemberNumber;
    private EditText etConfName;
    private Button btCreateConf;
    private Button btLogout;

    private TextView tvCurrentNumber;
    private Button btMyConf;

    private EditText etStartTime;

    public static void startActivity(Context context) {
        context.startActivity(new Intent(context, ConvokeActivity.class));
    }

    @Override
    public void findViews() {
        etNumber = (EditText) findViewById(R.id.et_number);
        btCallNumber = (Button) findViewById(R.id.bt_call_number);
        etMemberNumber = (EditText) findViewById(R.id.et_member_number);
        etConfName = (EditText) findViewById(R.id.et_conf_name);
        btCreateConf = (Button) findViewById(R.id.bt_create_conf);
        btLogout = (Button) findViewById(R.id.bt_logout);
        tvCurrentNumber = (TextView) findViewById(R.id.tv_current_number);

        etJoinNumber = (EditText) findViewById(R.id.et_join_number);

        etPwd = (EditText) findViewById(R.id.et_pwd);
        etStartTime = (EditText) findViewById(R.id.et_start_time);

        btMyConf = (Button) findViewById(R.id.bt_my_conf);

        btJoin = (Button) findViewById(R.id.bt_join);
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

        LocBroadcast.getInstance().registerBroadcast(receiver, broadcastNames);

        tvCurrentNumber.setText("当前登录SIP呼叫号码:" +
                LoginMgr.getInstance().getTerminal() +
                "\n登录账号:" + SPStaticUtils.getString(Constants.USER_NAME));
    }

    @Override
    public void setListener() {
        btCallNumber.setOnClickListener(this);
        btJoin.setOnClickListener(this);
        btCreateConf.setOnClickListener(this);
        btLogout.setOnClickListener(this);
        btMyConf.setOnClickListener(this);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_convoke;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_call_number:
                String number = etNumber.getText().toString();

                if (number.isEmpty()) {
                    ToastHelper.showShort("呼叫号码不能为空");
                    return;
                }

                HuaweiCallImp.getInstance().callSite(number);
                break;

            case R.id.bt_create_conf:
                String confName = etConfName.getText().toString().trim();
                if (TextUtils.isEmpty(confName)) {
                    ToastHelper.showShort("会议名称不能为空");
                    return;
                }

                String members = etMemberNumber.getText().toString().trim();
                if (TextUtils.isEmpty(members)) {
                    ToastHelper.showShort("参会列表不能为空");
                    return;
                }

                //参会列表会场号码
                List<String> memberNumbers = Arrays.asList(members.split(","));

                HuaweiCallImp.getInstance().createConf(confName, 120, memberNumbers);
                break;

            case R.id.bt_logout:
                HuaweiLoginImp.getInstance().logOut();
                finish();
                break;

            case R.id.bt_my_conf:
                ConfListActivity.startActivity(ConvokeActivity.this);
                break;

            case R.id.bt_join:
                String confId = etJoinNumber.getText().toString();
                String confPwd = etPwd.getText().toString();

                //914460787
                if (confId.isEmpty()) {
                    ToastHelper.showShort("会议id不能为空");
                    return;
                }

                if (confPwd.isEmpty()) {
                    ToastHelper.showShort("会议id进入会议");

                    int i = MeetingMgr.getInstance().queryConfDetail(confId);
                    ToastHelper.showShort(0 == i ? "查询会议成功" : "查询会议失败");
                } else {
                    ToastHelper.showShort("匿名进入会议");
                    //匿名加入会议
                    HuaweiCallImp.getInstance().joinConferenceByAnonymous(
                            confId,
                            confPwd
                    );
                }
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocBroadcast.getInstance().unRegisterBroadcast(receiver, broadcastNames);
    }

    private String[] broadcastNames = new String[]{
            CustomBroadcastConstants.GET_CONF_DETAIL_RESULT
    };

    private LocBroadcastReceiver receiver = new LocBroadcastReceiver() {
        @Override
        public void onReceive(String broadcastName, Object obj) {
            //如果是获取会议详情
            if (CustomBroadcastConstants.GET_CONF_DETAIL_RESULT.equals(broadcastName)) {
                if (obj instanceof ConfDetailInfo) {
                    ConfDetailInfo confDetailInfo = (ConfDetailInfo) obj;
                    LogUtil.d("ConvokeActivity", confDetailInfo.toString());
                    //加入会议
                    HuaweiCallImp.getInstance().joinConf(
                            confDetailInfo.getConfID(),
                            confDetailInfo.getGuestPwd(),
                            confDetailInfo.getAccessNumber()
                    );
                }
            }
        }
    };
}
