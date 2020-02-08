package com.hw.cloudlibrary.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.huawei.opensdk.callmgr.CallConstant;
import com.huawei.opensdk.callmgr.CallInfo;
import com.huawei.opensdk.callmgr.CallMgr;
import com.huawei.opensdk.callmgr.VideoMgr;
import com.huawei.opensdk.commonservice.common.common.LocContext;
import com.huawei.opensdk.commonservice.common.localbroadcast.CustomBroadcastConstants;
import com.huawei.opensdk.commonservice.common.localbroadcast.LocBroadcast;
import com.huawei.opensdk.commonservice.common.localbroadcast.LocBroadcastReceiver;
import com.huawei.opensdk.commonservice.common.util.LogUtil;
import com.huawei.opensdk.demoservice.MeetingMgr;
import com.hw.cloudlibrary.R;
import com.hw.cloudlibrary.ecsdk.common.UIConstants;
import com.hw.cloudlibrary.ecsdk.login.CallFunc;
import com.hw.cloudlibrary.utils.DensityUtil;
import com.hw.cloudlibrary.utils.StatusBarUtils;
import com.hw.cloudlibrary.utils.sharedpreferences.SPStaticUtils;
import com.hw.cloudlibrary.widget.DragFrameLayout;

/**
 * 点对点视频界面
 */
public class VideoActivity extends BaseLibActivity implements View.OnClickListener, LocBroadcastReceiver {
    private static final int ADD_LOCAL_VIEW = 101;

    private String[] mActions = new String[]{
            CustomBroadcastConstants.ACTION_CALL_END,
            CustomBroadcastConstants.ADD_LOCAL_VIEW,
            CustomBroadcastConstants.DEL_LOCAL_VIEW,
            CustomBroadcastConstants.CONF_CALL_CONNECTED,
            CustomBroadcastConstants.ACTION_CALL_END_FAILED,
            CustomBroadcastConstants.STATISTIC_LOCAL_QOS};

    /*会控顶部*/
    private ImageView ivBg;
    private RelativeLayout llTopControl;
    private LinearLayout llBottomControl;
    /*会控顶部-end*/

    private FrameLayout mRemoteView;
    private DragFrameLayout mLocalView;
    private FrameLayout mHideView;

    private FrameLayout flControl;
    private TextView tvHangUp;
    private TextView tvMic;
    private TextView tvMute;
    private ImageView ivSwitchCamera;
    private ImageView ivCloseCamera;

    private CallInfo mCallInfo;
    private long mCallID;
    private Object thisVideoActivity = this;

    private CallMgr mCallMgr;
    private CallFunc mCallFunc;
    private MeetingMgr instance;

    private boolean showControl = true;//是否显示控制栏

    private Gson gson = new Gson();

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ADD_LOCAL_VIEW:
                    addSurfaceView(true);
                    setAutoRotation(thisVideoActivity, true);
                    break;

                default:
                    break;
            }
        }
    };

    @Override
    protected void findViews() {
        mRemoteView = (FrameLayout) findViewById(R.id.conf_share_layout);
        mLocalView = (DragFrameLayout) findViewById(R.id.conf_video_small_logo);
        mHideView = (FrameLayout) findViewById(R.id.hide_video_view);
        llTopControl = (RelativeLayout) findViewById(R.id.ll_top_control);
        llBottomControl = (LinearLayout) findViewById(R.id.ll_bottom_control);
        ivBg = (ImageView) findViewById(R.id.iv_bg);

        tvHangUp = (TextView) findViewById(R.id.tv_hang_up);
        tvMic = (TextView) findViewById(R.id.tv_mic);
        tvMute = (TextView) findViewById(R.id.tv_mute);
        ivSwitchCamera = (ImageView) findViewById(R.id.iv_switch_camera);
        ivCloseCamera = (ImageView) findViewById(R.id.iv_close_camera);

        flControl = (FrameLayout) findViewById(R.id.fl_control);

    }

    @Override
    protected void initData() {
        if (Build.VERSION.SDK_INT < 28) {
            StatusBarUtils.setTransparent(this);
        } else {
            StatusBarUtils.setTranslucentForImageView(this);
        }

        // 保持屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Intent intent = getIntent();
//        mCallInfo = PreferencesHelper.getData(UIConstants.CALL_INFO, CallInfo.class);

        mCallInfo = gson.fromJson(SPStaticUtils.getString(UIConstants.CALL_INFO), CallInfo.class);

        mCallID = mCallInfo.getCallID();

        mCallMgr = CallMgr.getInstance();
        mCallFunc = CallFunc.getInstance();
        instance = MeetingMgr.getInstance();

        //设置为扬声器模式
        setLoudSpeaker();
    }

    private OrientationEventListener orientationEventListener;

    @Override
    protected void setListener() {
        tvHangUp.setOnClickListener(this);
        ivBg.setOnClickListener(this);
        tvMic.setOnClickListener(this);
        tvMute.setOnClickListener(this);
        ivSwitchCamera.setOnClickListener(this);
        ivCloseCamera.setOnClickListener(this);

        // 启一个新的监听，监听设备旋转角度
        orientationEventListener = new OrientationEventListener(LocContext.getContext()) {
            @Override
            public void onOrientationChanged(int orientation) {

                Log.d(UIConstants.DEMO_TAG, "onOrientationChanged: " + orientation);
                if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) {
                    return; // 手机平放时，检测不到有效的角度
                }
                // 只检测是否有四个角度的改变
                if (orientation > 350 || orientation < 10) {
                    // 0度：手机默认竖屏状态（home键在正下方）
                    ViewGroup.LayoutParams layoutParams = flControl.getLayoutParams();
                    int width = layoutParams.width;
                    int height = layoutParams.height;

                    flControl.setRotation(0);

                    Log.d(UIConstants.DEMO_TAG, "下" + "宽是:" + width + "，高是：" + height);

                    RelativeLayout.LayoutParams fl_lp = new RelativeLayout.LayoutParams(
                            DensityUtil.getScreenWidth(VideoActivity.this),
                            DensityUtil.getScreenHeight(VideoActivity.this)
                    );

                    flControl.setLayoutParams(fl_lp);
                } else if (orientation > 80 && orientation < 100) {
                    // 90度：手机顺时针旋转90度横屏（home建在左侧）

                    ViewGroup.LayoutParams layoutParams = flControl.getLayoutParams();
                    int width = layoutParams.width;
                    int height = layoutParams.height;
                    Log.d(UIConstants.DEMO_TAG, "左" + "宽是:" + width + "，高是：" + height);

                    flControl.setRotation(270);

                    RelativeLayout.LayoutParams fl_lp = new RelativeLayout.LayoutParams(
                            DensityUtil.getScreenHeight(VideoActivity.this),
                            DensityUtil.getScreenWidth(VideoActivity.this)
                    );

                    flControl.setLayoutParams(fl_lp);
                } else if (orientation > 170 && orientation < 190) {
                    // 180度：手机顺时针旋转180度竖屏（home键在上方）

                    ViewGroup.LayoutParams layoutParams = flControl.getLayoutParams();
                    int width = layoutParams.width;
                    int height = layoutParams.height;
                    Log.d(UIConstants.DEMO_TAG, "上" + "宽是:" + width + "，高是：" + height);

                    flControl.setRotation(180);

                    RelativeLayout.LayoutParams fl_lp = new RelativeLayout.LayoutParams(
                            DensityUtil.getScreenWidth(VideoActivity.this),
                            DensityUtil.getScreenHeight(VideoActivity.this)
                    );

                    flControl.setLayoutParams(fl_lp);
                } else if (orientation > 260 && orientation < 280) {
                    // 270度：手机顺时针旋转270度横屏，（home键在右侧）
                    ViewGroup.LayoutParams layoutParams = flControl.getLayoutParams();
                    int width = layoutParams.width;
                    int height = layoutParams.height;

                    int width1 = ivBg.getWidth();
                    int height1 = ivBg.getHeight();

                    Log.d(UIConstants.DEMO_TAG, "右" + "宽是:" + width + "，高是：" + height + "背景宽:" + width1 + "，高：" + height1);

                    flControl.setRotation(90);

                    RelativeLayout.LayoutParams fl_lp = new RelativeLayout.LayoutParams(
                            DensityUtil.getScreenHeight(VideoActivity.this),
                            DensityUtil.getScreenWidth(VideoActivity.this)
                    );

                    flControl.setLayoutParams(fl_lp);
                }
            }
        };

        // 启动监听
//        orientationEventListener.enable();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_video;
    }

    private void addSurfaceView(boolean onlyLocal) {
        if (!onlyLocal) {
            addSurfaceView(mRemoteView, getRemoteVideoView());
        }
        addSurfaceView(mLocalView, getLocalVideoView());
        addSurfaceView(mHideView, getHideVideoView());
    }

    private void addSurfaceView(ViewGroup container, SurfaceView child) {
        if (child == null) {
            return;
        }
        if (child.getParent() != null) {
            ViewGroup vGroup = (ViewGroup) child.getParent();
            vGroup.removeAllViews();
        }
        container.addView(child);
    }

    public SurfaceView getHideVideoView() {
        return VideoMgr.getInstance().getLocalHideView();
    }

    public SurfaceView getLocalVideoView() {
        return VideoMgr.getInstance().getLocalVideoView();
    }

    public SurfaceView getRemoteVideoView() {
        return VideoMgr.getInstance().getRemoteBigVideoView();
    }

    public void setAutoRotation(Object object, boolean isOpen) {
        VideoMgr.getInstance().setAutoRotation(object, isOpen, 1);
    }

    /**
     * 设置为扬声器
     */
    public void setLoudSpeaker() {
        //获取扬声器状态
        //如果不是扬声器则切换成扬声器
        if ((CallConstant.TYPE_LOUD_SPEAKER != CallMgr.getInstance().getCurrentAudioRoute())) {
            CallMgr.getInstance().switchAudioRoute();
        }
    }

    @Override
    public void onClick(View v) {
        if (R.id.tv_hang_up == v.getId()) {
            //结束会议
            mCallMgr.endCall(mCallID);
        } else if (R.id.iv_bg == v.getId()) {
            if (showControl) {
                hideControl();
            } else {
                showControl();
            }
        }//麦克风
        else if (R.id.tv_mic == v.getId()) {
            muteMicCall();
        }//扬声器
        else if (R.id.tv_mute == v.getId()) {
            final int audioRoute = CallMgr.getInstance().switchAudioRoute();
            tvMute.setCompoundDrawablesWithIntrinsicBounds(0, audioRoute == CallConstant.TYPE_LOUD_SPEAKER ? R.mipmap.icon_unmute : R.mipmap.icon_mute, 0, 0);

        }//切换摄像头
        else if (R.id.iv_switch_camera == v.getId()) {
            if (isLocalCameraClose) {
                return;
            }
            switchCamera();
        }//关闭本地摄像头
        else if (R.id.iv_close_camera == v.getId()) {
            switchCameraStatus();
        }//扬声器
        else if (R.id.tv_mute == v.getId()) {

        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        LocBroadcast.getInstance().registerBroadcast(this, mActions);

        addSurfaceView(false);

        setAutoRotation(this, true);
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        LocBroadcast.getInstance().unRegisterBroadcast(this, mActions);

        mHandler.removeCallbacksAndMessages(null);

        setAutoRotation(this, false);

        //是否需要自动接听
        SPStaticUtils.put(UIConstants.IS_AUTO_ANSWER, false);
    }

    @Override
    public void onReceive(String broadcastName, Object obj) {
        switch (broadcastName) {
            case CustomBroadcastConstants.ACTION_CALL_END:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        videoDestroy();
                        finish();
                    }
                });
                break;

            case CustomBroadcastConstants.ADD_LOCAL_VIEW:
                mHandler.sendEmptyMessage(ADD_LOCAL_VIEW);
                break;

            case CustomBroadcastConstants.DEL_LOCAL_VIEW:
                break;

            case CustomBroadcastConstants.CONF_CALL_CONNECTED:
                finish();
                break;


            case CustomBroadcastConstants.ACTION_CALL_END_FAILED:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                });
                break;

            case CustomBroadcastConstants.STATISTIC_LOCAL_QOS:
                final long signalStrength = (long) obj;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        if (signalStrength == 1) {
//                            mSignalView.setBackground(getDrawable(R.drawable.signal_1));
//                        }
//                        if (signalStrength == 2) {
//                            mSignalView.setBackground(getDrawable(R.drawable.signal_2));
//                        }
//                        if (signalStrength == 3) {
//                            mSignalView.setBackground(getDrawable(R.drawable.signal_3));
//                        }
//                        if (signalStrength == 4 || signalStrength == 5) {
//                            mSignalView.setBackground(getDrawable(R.drawable.signal_4));
//                        }
                    }
                });
                break;

            default:
                break;
        }
    }


    public void videoDestroy() {
        if (null != CallMgr.getInstance().getVideoDevice()) {
            LogUtil.i(UIConstants.DEMO_TAG, "onCallClosed destroy.");
            CallMgr.getInstance().videoDestroy();
        }
    }

    private void showControl() {
        llTopControl.setVisibility(View.VISIBLE);
        getViewAlphaAnimator(llTopControl, 1).start();
        llBottomControl.setVisibility(View.VISIBLE);
        getViewAlphaAnimator(llBottomControl, 1).start();
    }

    private void hideControl() {
        getViewAlphaAnimator(llBottomControl, 0).start();
        getViewAlphaAnimator(llTopControl, 0).start();
    }

    private ViewPropertyAnimator getViewAlphaAnimator(final View view, final float alpha) {
        ViewPropertyAnimator viewPropertyAnimator = view.animate().alpha(alpha).setDuration(300);
        viewPropertyAnimator.setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                view.setVisibility(alpha > 0 ? View.VISIBLE : View.GONE);
                showControl = alpha > 0 ? true : false;
            }
        });
        return viewPropertyAnimator;
    }

    /**
     * 更换麦克风状态
     */
    public void muteMicCall() {
        boolean currentMuteStatus = getIsMuteMic();
        if (CallMgr.getInstance().muteMic(mCallID, !currentMuteStatus)) {
            mCallFunc.setMuteStatus(!currentMuteStatus);
            setMicStatus(!currentMuteStatus);
        }
    }

    public boolean getIsMuteMic() {
        boolean currentMuteStatus = mCallFunc.isMuteStatus();
        return currentMuteStatus;
    }

    /**
     * 设置麦克风图片
     *
     * @param currentMuteStatus
     */
    private void setMicStatus(boolean currentMuteStatus) {
        //更新状态静音按钮状态
        tvMic.setCompoundDrawablesWithIntrinsicBounds(0, currentMuteStatus ? R.mipmap.icon_mic_close : R.mipmap.icon_mic, 0, 0);
    }

    //摄像头方向
    private int mCameraIndex = CallConstant.FRONT_CAMERA;

    /**
     * 切换本地摄像头
     */
    public void switchCamera() {
        mCameraIndex = CallConstant.FRONT_CAMERA == mCameraIndex ?
                CallConstant.BACK_CAMERA : CallConstant.FRONT_CAMERA;
        CallMgr.getInstance().switchCamera(mCallID, mCameraIndex);
    }

    //本地摄像头是否关闭
    private boolean isLocalCameraClose = false;

    private static final int NOT_ALPHA = 255;
    private static final int HALF_ALPHA = 127;

    /**
     * 关闭本地摄像头
     */
    public void switchCameraStatus() {
        isLocalCameraClose = !isLocalCameraClose;
        if (isLocalCameraClose) {
            CallMgr.getInstance().closeCamera(mCallID);
            ivCloseCamera.setImageResource(R.mipmap.icon_state_open_camera);
            ivSwitchCamera.getDrawable().setAlpha(HALF_ALPHA);
        } else {
            CallMgr.getInstance().openCamera(mCallID);
            ivCloseCamera.setImageResource(R.mipmap.icon_state_close_camera);
            ivSwitchCamera.getDrawable().setAlpha(NOT_ALPHA);
        }
    }

}
