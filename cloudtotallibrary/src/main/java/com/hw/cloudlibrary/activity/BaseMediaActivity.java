package com.hw.cloudlibrary.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.gson.Gson;
import com.huawei.opensdk.callmgr.CallInfo;
import com.huawei.opensdk.callmgr.CallMgr;
import com.huawei.opensdk.commonservice.common.localbroadcast.CustomBroadcastConstants;
import com.huawei.opensdk.commonservice.common.localbroadcast.LocBroadcast;
import com.huawei.opensdk.commonservice.common.localbroadcast.LocBroadcastReceiver;
import com.huawei.opensdk.demoservice.MeetingMgr;
import com.hw.cloudlibrary.ecsdk.common.IntentConstant;
import com.hw.cloudlibrary.ecsdk.common.UIConstants;
import com.hw.cloudlibrary.ecsdk.login.CallFunc;
import com.hw.cloudlibrary.ecsdk.utils.ActivityUtil;
import com.hw.cloudlibrary.utils.StatusBarUtils;
import com.hw.cloudlibrary.utils.ToastHelper;
import com.hw.cloudlibrary.utils.sharedpreferences.SPStaticUtils;

import java.util.Timer;
import java.util.TimerTask;


/**
 * author：pc-20171125
 * data:2019/1/4 14:31
 */
public abstract class BaseMediaActivity extends BaseLibActivity implements LocBroadcastReceiver {
    private static final int CALL_CONNECTED = 100;
    private static final int CALL_UPGRADE = 101;
    private static final int HOLD_CALL_SUCCESS = 102;
    private static final int VIDEO_HOLD_CALL_SUCCESS = 103;
    private static final int MEDIA_CONNECTED = 104;
    private static final int BLD_TRANSFER_SUCCESS = 105;
    private static final int BLD_TRANSFER_FAILED = 106;

    private Gson gson = new Gson();

    private String[] mActions = new String[]{
            CustomBroadcastConstants.ACTION_CALL_CONNECTED,
            CustomBroadcastConstants.CALL_MEDIA_CONNECTED,
            CustomBroadcastConstants.CONF_CALL_CONNECTED,
            CustomBroadcastConstants.ACTION_CALL_END,
            CustomBroadcastConstants.CALL_UPGRADE_ACTION,
            CustomBroadcastConstants.HOLD_CALL_RESULT,
            CustomBroadcastConstants.BLD_TRANSFER_RESULT,
            CustomBroadcastConstants.CALL_TRANSFER_TO_CONFERENCE
    };

    protected String mCallNumber;
    protected String mDisplayName;
    protected boolean mIsVideoCall;
    protected long mCallID = -1;
    protected String mConfID;
    protected boolean mIsConfCall;
    protected boolean mIsCaller;

    private CallFunc mCallFunc;

    protected int mConfToCallHandle;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MEDIA_CONNECTED:
//                    if (msg.obj instanceof CallInfo) {
//                        CallInfo callInfo = (CallInfo) msg.obj;
//                        Log.i(TAG, "69-->" + callInfo.toString());
//                        if (!callInfo.isVideoCall()) {
//                            Intent intent = new Intent(IntentConstant.AUDIO_ACTIVITY_ACTION);
//                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                            intent.addCategory(IntentConstant.DEFAULT_CATEGORY);
//
////                            intent.putExtra(UIConstants.CALL_INFO, callInfo);
//                            PreferencesHelper.saveData(UIConstants.CALL_INFO, callInfo);
//
//                            ActivityUtil.startActivity(BaseMediaActivity.this, intent);
//                            finishActivityLine(73);
//                        }
//                    }
                    break;

                case CALL_CONNECTED:
                    if (msg.obj instanceof CallInfo) {
                        CallInfo callInfo = (CallInfo) msg.obj;
                        Log.i(TAG, "69-->" + callInfo.toString());
                        if (callInfo.isVideoCall()) {
                            boolean isConf = MeetingMgr.getInstance().judgeInviteFormMySelf(callInfo.getConfID());
                            try {
                                isConf = SPStaticUtils.getBoolean(UIConstants.IS_AUTO_ANSWER);
                            } catch (Exception e) {
                                isConf = false;
                            }

                            if (isConf) {
//                                PreferencesHelper.saveData(UIConstants.IS_AUTO_ANSWER, false);
//                                LogUtil.i(UIConstants.DEMO_TAG, "呼叫内容:" + callInfo.toString());
//                                String confID = callInfo.getCallID() + "";
//                                Intent intent = new Intent(IntentConstant.VIDEO_CONF_ACTIVITY_ACTION);
//                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                                intent.putExtra(UIConstants.CONF_ID, confID);
//                                intent.putExtra(UIConstants.CALL_ID, callInfo.getCallID());
//                                intent.putExtra(UIConstants.PEER_NUMBER, callInfo.getPeerNumber());
//
//                                PreferencesHelper.saveData(UIConstants.CALL_INFO, callInfo);
//
//                                ActivityUtil.startActivity(LocContext.getContext(), intent);
//                                finishActivityLine(191);
                            } else {
                                Intent intent = new Intent(IntentConstant.VIDEO_ACTIVITY_ACTION);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.addCategory(IntentConstant.DEFAULT_CATEGORY);

                                SPStaticUtils.put(UIConstants.CALL_INFO, gson.toJson(callInfo));

                                intent.putExtra(UIConstants.IS_MEETING, false);
                                ActivityUtil.startActivity(BaseMediaActivity.this, intent);
                                finishActivityLine(90);


                                //demo写法
//                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                                intent.addCategory(IntentConstant.DEFAULT_CATEGORY);
//                                intent.putExtra(UIConstants.CALL_INFO, callInfo);
//                                ActivityUtil.startActivity(BaseMediaActivity.this, intent);
//                                finish();
                            }
                        }
                    }
                    break;

                //切换到视频通话的请求
                case CALL_UPGRADE:
                    break;

                case HOLD_CALL_SUCCESS: {
                    String textDisplayName = null == mDisplayName ? "" : mDisplayName;
                    String textCallNumber = null == mCallNumber ? "" : mCallNumber;
                }
                break;

                case VIDEO_HOLD_CALL_SUCCESS: {
                    String textDisplayName = null == mDisplayName ? "" : mDisplayName;
                    String textCallNumber = null == mCallNumber ? "" : mCallNumber;
                    textCallNumber = textCallNumber + "Holding";
                }
                break;

                default:
                    break;
            }
        }
    };

    private void finishActivityLine(int line) {
        finish();
    }

    private String TAG = BaseMediaActivity.class.getSimpleName();

    @Override
    protected void findViews() {

    }

    @Override
    protected void initData() {
        StatusBarUtils.setColor(this);

        // 保持屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mCallFunc = CallFunc.getInstance();

        Intent intent = getIntent();

        CallInfo callInfo = gson.fromJson(SPStaticUtils.getString(UIConstants.CALL_INFO), CallInfo.class);

        if (null == callInfo) {
            return;
        }

        Log.i(TAG, "120-->" + callInfo.toString());
        mCallNumber = callInfo.getPeerNumber();
        mDisplayName = callInfo.getPeerDisplayName();
        mIsVideoCall = callInfo.isVideoCall();
        mCallID = callInfo.getCallID();
        mConfID = callInfo.getConfID();
        mIsConfCall = callInfo.isFocus();
        mIsCaller = callInfo.isCaller();

        if ((null != mConfID) && (!"".equals(callInfo.getConfID()))) {
            mConfToCallHandle = Integer.parseInt(callInfo.getConfID());
        }
    }

    @Override
    protected void setListener() {
    }

    @Override
    protected void onResume() {
        super.onResume();

        LocBroadcast.getInstance().registerBroadcast(this, mActions);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        LocBroadcast.getInstance().unRegisterBroadcast(this, mActions);
    }

    @Override
    public void onReceive(String broadcastName, final Object obj) {
        switch (broadcastName) {
            case CustomBroadcastConstants.ACTION_CALL_CONNECTED:
                mHandler.sendMessage(mHandler.obtainMessage(CALL_CONNECTED, obj));
                break;

            case CustomBroadcastConstants.CALL_MEDIA_CONNECTED:
                mHandler.sendMessage(mHandler.obtainMessage(MEDIA_CONNECTED, obj));
                break;

            case CustomBroadcastConstants.CONF_CALL_CONNECTED:
//                PreferencesHelper.saveData(UIConstants.IS_AUTO_ANSWER, false);
//                CallInfo callInfo = (CallInfo) obj;
//                LogUtil.i(UIConstants.DEMO_TAG, "呼叫内容:" + callInfo.toString());
//                String confID = callInfo.getCallID() + "";
//                Intent intent = new Intent(IntentConstant.VIDEO_CONF_ACTIVITY_ACTION);
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                intent.putExtra(UIConstants.CONF_ID, confID);
//                intent.putExtra(UIConstants.CALL_ID, callInfo.getCallID());
//                intent.putExtra(UIConstants.PEER_NUMBER, callInfo.getPeerNumber());
//
//                PreferencesHelper.saveData(UIConstants.CALL_INFO, callInfo);
//
//                ActivityUtil.startActivity(LocContext.getContext(), intent);
                finishActivityLine(191);
                break;

            case CustomBroadcastConstants.ACTION_CALL_END:
                if (obj instanceof CallInfo) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            CallInfo params = (CallInfo) obj;
                            if (403 == params.getReasonCode() || 603 == params.getReasonCode()) {
                                Toast.makeText(BaseMediaActivity.this, "对方已拒接", Toast.LENGTH_SHORT).show();
                                finishActivityLine(202);
                            } else if (404 == params.getReasonCode()) {
                                Toast.makeText(BaseMediaActivity.this, "对方不在线", Toast.LENGTH_SHORT).show();
                                finishActivityLine(205);
                            } else if (486 == params.getReasonCode()) {
                                Toast.makeText(BaseMediaActivity.this, "对方正忙", Toast.LENGTH_SHORT).show();
                                finishActivityLine(208);
                            } else if (0 == params.getReasonCode()) {
                                Toast.makeText(BaseMediaActivity.this, "通话结束", Toast.LENGTH_SHORT).show();
                                finishActivityLine(262);
                            } else {
                                finishActivityLine(210);
                            }
                        }
                    });
                }
                break;

            case CustomBroadcastConstants.CALL_UPGRADE_ACTION:
                mHandler.sendEmptyMessage(CALL_UPGRADE);
                break;

            case CustomBroadcastConstants.HOLD_CALL_RESULT:
                if ("HoldSuccess".equals(obj)) {
                    mHandler.sendEmptyMessage(HOLD_CALL_SUCCESS);
                } else if ("UnHoldSuccess".equals(obj)) {
                    mHandler.sendEmptyMessage(HOLD_CALL_SUCCESS);
                } else if ("VideoHoldSuccess".equals(obj)) {
                    mHandler.sendEmptyMessage(VIDEO_HOLD_CALL_SUCCESS);
                }
                break;

            case CustomBroadcastConstants.CALL_TRANSFER_TO_CONFERENCE:
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (CallMgr.getInstance().isResumeHold()) {
                            CallMgr.getInstance().unHoldCall(CallMgr.getInstance().getOriginal_CallId());
                        }
                    }
                }, 20000);
                break;

            default:
                break;
        }
    }

}
