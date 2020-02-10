package com.hw.cloudlibrary.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
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
import com.huawei.ecterminalsdk.base.TsdkConfMediaType;
import com.huawei.ecterminalsdk.base.TsdkConfRole;
import com.huawei.ecterminalsdk.base.TsdkConfSvcWatchAttendee;
import com.huawei.ecterminalsdk.base.TsdkConfSvcWatchInfo;
import com.huawei.opensdk.callmgr.CallConstant;
import com.huawei.opensdk.callmgr.CallInfo;
import com.huawei.opensdk.callmgr.CallMgr;
import com.huawei.opensdk.callmgr.VideoMgr;
import com.huawei.opensdk.commonservice.common.localbroadcast.CustomBroadcastConstants;
import com.huawei.opensdk.commonservice.common.localbroadcast.LocBroadcast;
import com.huawei.opensdk.commonservice.common.localbroadcast.LocBroadcastReceiver;
import com.huawei.opensdk.commonservice.common.util.LogUtil;
import com.huawei.opensdk.demoservice.ConfBaseInfo;
import com.huawei.opensdk.demoservice.ConfConstant;
import com.huawei.opensdk.demoservice.MeetingMgr;
import com.huawei.opensdk.demoservice.Member;
import com.hw.cloudlibrary.R;
import com.hw.cloudlibrary.adapter.ConfControlItem;
import com.hw.cloudlibrary.adapter.fragment.MyPagerAdapter;
import com.hw.cloudlibrary.ecsdk.common.UIConstants;
import com.hw.cloudlibrary.ecsdk.login.CallFunc;
import com.hw.cloudlibrary.fragment.BigRemoteViewFragment;
import com.hw.cloudlibrary.fragment.ConfViewFragment;
import com.hw.cloudlibrary.utils.DensityUtil;
import com.hw.cloudlibrary.utils.DeviceUtil;
import com.hw.cloudlibrary.utils.StatusBarUtils;
import com.hw.cloudlibrary.utils.ToastHelper;
import com.hw.cloudlibrary.utils.sharedpreferences.SPStaticUtils;
import com.hw.cloudlibrary.widget.CustomDialog;
import com.hw.cloudlibrary.widget.DragFrameLayout;
import com.hw.cloudlibrary.widget.dialog.OnDialogClickListener;
import com.hw.cloudlibrary.widget.rv.BaseItemAdapter;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import static com.huawei.ecterminalsdk.base.TsdkConfRole.TSDK_E_CONF_ROLE_CHAIRMAN;

/**
 * 点对点视频界面
 */
public class VideoConfGridActivity extends BaseLibActivity implements View.OnClickListener, LocBroadcastReceiver {
    private static final int ADD_LOCAL_VIEW = 101;
    public static final String TAG = "VideoConfGridActivity";

    private String[] mActions = new String[]{
            CustomBroadcastConstants.CONF_STATE_UPDATE,
            CustomBroadcastConstants.GET_DATA_CONF_PARAM_RESULT,
            CustomBroadcastConstants.DATA_CONFERENCE_JOIN_RESULT,
            CustomBroadcastConstants.ADD_LOCAL_VIEW,
            CustomBroadcastConstants.DEL_LOCAL_VIEW,
            CustomBroadcastConstants.DATE_CONFERENCE_START_SHARE_STATUS,
            CustomBroadcastConstants.DATE_CONFERENCE_END_SHARE_STATUS,
            CustomBroadcastConstants.UPGRADE_CONF_RESULT,
            CustomBroadcastConstants.UN_MUTE_CONF_RESULT,
            CustomBroadcastConstants.MUTE_CONF_RESULT,
            CustomBroadcastConstants.LOCK_CONF_RESULT,
            CustomBroadcastConstants.UN_LOCK_CONF_RESULT,
            CustomBroadcastConstants.ADD_ATTENDEE_RESULT,
            CustomBroadcastConstants.DEL_ATTENDEE_RESULT,
            CustomBroadcastConstants.MUTE_ATTENDEE_RESULT,
            CustomBroadcastConstants.UN_MUTE_ATTENDEE_RESULT,
            CustomBroadcastConstants.HAND_UP_RESULT,
            CustomBroadcastConstants.CANCEL_HAND_UP_RESULT,
            CustomBroadcastConstants.SET_CONF_MODE_RESULT,
            CustomBroadcastConstants.WATCH_ATTENDEE_CONF_RESULT,
            CustomBroadcastConstants.BROADCAST_ATTENDEE_CONF_RESULT,
            CustomBroadcastConstants.CANCEL_BROADCAST_CONF_RESULT,
            CustomBroadcastConstants.REQUEST_CHAIRMAN_RESULT,
            CustomBroadcastConstants.RELEASE_CHAIRMAN_RESULT,
            //发言人通知--CustomBroadcastConstants.SPEAKER_LIST_IND
            // CustomBroadcastConstants.SPEAKER_LIST_IND,
            CustomBroadcastConstants.GET_CONF_END,
            CustomBroadcastConstants.SCREEN_SHARE_STATE,
            //网络情况--CustomBroadcastConstants.STATISTIC_LOCAL_QOS
            //CustomBroadcastConstants.STATISTIC_LOCAL_QOS,
            CustomBroadcastConstants.GET_SVC_WATCH_INFO,
            CustomBroadcastConstants.RESUME_JOIN_CONF_RESULT,
            CustomBroadcastConstants.RESUME_JOIN_CONF_IND,
            CustomBroadcastConstants.LOGIN_STATUS_RESUME_IND,
            CustomBroadcastConstants.LOGIN_STATUS_RESUME_RESULT,
            CustomBroadcastConstants.LOGIN_FAILED,
            CustomBroadcastConstants.JOIN_CONF_FAILED
    };

    /*会控顶部*/
    private ImageView ivBg;
    private RelativeLayout llTopControl;
    private LinearLayout llBottomControl;
    /*会控顶部-end*/

    //远端大画面
    private FrameLayout mRemoteView;
    //本地小画面
    private DragFrameLayout mLocalView;
    private FrameLayout mHideView;

    private ImageView ivRequestChair;
    private ImageView ivAddMember;

    private TextView tvHangUp;
    private TextView tvMic;
    private TextView tvMute;
    private ImageView ivSwitchCamera;
    private ImageView ivCloseCamera;
    private ImageView ivMuteConf;
    private TextView tvConfName;

    private CallInfo mCallInfo;
    private long mCallID;
    private Object thisVideoActivity = this;

    private CallMgr mCallMgr;
    private CallFunc mCallFunc;
    private MeetingMgr instance;

    private String confID;

    //是否显示控制栏
    private boolean showControl = true;

    private Gson gson = new Gson();

    //是否只有本地画面
    private boolean isOnlyLocal = true;

    //设置本地画面为最大
    private boolean isSetOnlyLocalWind = false;

    //多流会议
    private boolean isSvcConf;

    //视频会议
    private boolean isVideo;

    // svc会议的小窗口是否隐藏
    private boolean isHideVideoWindow = false;

    // 是否有人正在共享
    private boolean isSharing = false;

    // 自己是否正在共享
    private boolean isShareOwner = false;

    private int currentShowSmallWndCount = 0;

    private List<Long> svcLabel = MeetingMgr.getInstance().getSvcConfInfo().getSvcLabel();
    private Map<String, Integer> watchMap = new IdentityHashMap<>();

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ADD_LOCAL_VIEW:
//                    setAvcVideoContainer(mLocalView, mRemoteView, mHideView);
                    updateLocalVideo();

                    setAutoRotation(thisVideoActivity, true);
                    break;

                default:
                    break;
            }
        }
    };
    private FrameLayout mConfRemoteSmallVideoLayout_01;
    private FrameLayout mConfRemoteSmallVideoLayout_02;
    private FrameLayout mConfRemoteSmallVideoLayout_03;

    private TextView mConfRemoteSmallVideoText_01;
    private TextView mConfRemoteSmallVideoText_02;
    private TextView mConfRemoteSmallVideoText_03;

    private ImageView mConfVideoBackIV;
    private ImageView mConfVideoForwardIV;

    private FrameLayout mConfLocalVideoLayout;
    private TextView mConfLocalVideoText;

    //多画面的容器
    private LinearLayout mConfSmallVideoWndLL;

    private ViewPager vpContent;
    private MyPagerAdapter pagerAdapter;

    @Override
    protected void findViews() {
        mRemoteView = (FrameLayout) findViewById(R.id.big_remote_view);
        mLocalView = (DragFrameLayout) findViewById(R.id.conf_video_small_logo);
        mHideView = (FrameLayout) findViewById(R.id.hide_video_view);
        tvHangUp = (TextView) findViewById(R.id.tv_hang_up);

        llTopControl = (RelativeLayout) findViewById(R.id.ll_top_control);
        llBottomControl = (LinearLayout) findViewById(R.id.ll_bottom_control);
        ivBg = (ImageView) findViewById(R.id.iv_bg);

        tvConfName = (TextView) findViewById(R.id.tv_conf_name);
        tvMic = (TextView) findViewById(R.id.tv_mic);
        tvMute = (TextView) findViewById(R.id.tv_mute);
        ivSwitchCamera = (ImageView) findViewById(R.id.iv_switch_camera);
        ivCloseCamera = (ImageView) findViewById(R.id.iv_close_camera);

        ivMuteConf = (ImageView) findViewById(R.id.iv_mute_conf);


        ivRequestChair = (ImageView) findViewById(R.id.iv_request_chair);
        ivAddMember = (ImageView) findViewById(R.id.iv_add_member);

        mConfLocalVideoLayout = (FrameLayout) findViewById(R.id.conf_local_video_layout);
        mConfLocalVideoText = (TextView) findViewById(R.id.conf_local_video_text);

        mConfRemoteSmallVideoLayout_01 = (FrameLayout) findViewById(R.id.conf_remote_small_video_layout_01);
        mConfRemoteSmallVideoLayout_02 = (FrameLayout) findViewById(R.id.conf_remote_small_video_layout_02);
        mConfRemoteSmallVideoLayout_03 = (FrameLayout) findViewById(R.id.conf_remote_small_video_layout_03);

        mConfRemoteSmallVideoText_01 = (TextView) findViewById(R.id.conf_remote_small_video_text_01);
        mConfRemoteSmallVideoText_02 = (TextView) findViewById(R.id.conf_remote_small_video_text_02);
        mConfRemoteSmallVideoText_03 = (TextView) findViewById(R.id.conf_remote_small_video_text_03);

        mConfVideoBackIV = (ImageView) findViewById(R.id.watch_previous_page);
        mConfVideoForwardIV = (ImageView) findViewById(R.id.watch_next_page);

        mConfSmallVideoWndLL = (LinearLayout) findViewById(R.id.conf_video_ll);

        //viewpager
        vpContent = (ViewPager) findViewById(R.id.vp_content);
    }

    @Override
    protected void initData() {
        if (Build.VERSION.SDK_INT < 28) {
            StatusBarUtils.setTransparent(this);
        } else {
            StatusBarUtils.setTranslucentForImageView(this);
        }

        //设置会议画面的高度
        setConfVideoSize(false);

        // 保持屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Intent intent = getIntent();

        confID = intent.getStringExtra(UIConstants.CONF_ID);

        isSvcConf = intent.getBooleanExtra(UIConstants.IS_SVC_VIDEO_CONF, false);

        isVideo = intent.getBooleanExtra(UIConstants.IS_VIDEO_CONF, false);

        mCallInfo = gson.fromJson(SPStaticUtils.getString(UIConstants.CALL_INFO), CallInfo.class);

        mCallID = mCallInfo.getCallID();

        mCallMgr = CallMgr.getInstance();
        mCallFunc = CallFunc.getInstance();
        instance = MeetingMgr.getInstance();

        //设置为扬声器模式
        setLoudSpeaker();

        ivRequestChair.setVisibility(View.VISIBLE);

        initPagerAdapter();
    }

    private List<Fragment> fragments = new ArrayList<>();

    /**
     * 初始化适配器
     */
    private void initPagerAdapter() {
        fragments.add(BigRemoteViewFragment.newInstance());
        fragments.add(ConfViewFragment.newInstance(1));

        pagerAdapter = new MyPagerAdapter(getSupportFragmentManager(), fragments);

        vpContent.setAdapter(pagerAdapter);
        vpContent.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int index) {
                ToastHelper.showShort("index->" + index);
                if (index > 0) {
                    watchAttendee(index);
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
    }

    /**
     * 判断是否是
     *
     * @param isBack
     */
    private void turnPage(boolean isBack) {
        int page = MeetingMgr.getInstance().getCurrentWatchPage();
        int sumPage = MeetingMgr.getInstance().getTotalWatchablePage();
        if (isBack) {
            // 向后翻页
            if (1 == page) {
//                showToast(R.string.first_page);
                return;
            }
            page = page - 1;
        } else {
            // 向前翻页
            if (page == sumPage) {
//                showToast(R.string.last_page);
                return;
            }
            page = page + 1;
        }

        MeetingMgr.getInstance().watchAttendee(page);
    }

    /**
     * 切换显示
     *
     * @param page
     */
    private void watchAttendee(int page) {
        MeetingMgr.getInstance().watchAttendee(page);
    }

    @Override
    protected void setListener() {
        ivBg.setOnClickListener(this);
        tvHangUp.setOnClickListener(this);

        tvMic.setOnClickListener(this);
        tvMute.setOnClickListener(this);
        ivSwitchCamera.setOnClickListener(this);
        ivCloseCamera.setOnClickListener(this);

        ivRequestChair.setOnClickListener(this);
        ivAddMember.setOnClickListener(this);
        ivMuteConf.setOnClickListener(this);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_video_conf_grid;
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

    public SurfaceView getRemoteBigVideoView() {
        return VideoMgr.getInstance().getRemoteBigVideoView();
    }

    public SurfaceView getRemoteSmallVideoView_01() {
        return VideoMgr.getInstance().getRemoteSmallVideoView_01();
    }

    public SurfaceView getRemoteSmallVideoView_02() {
        return VideoMgr.getInstance().getRemoteSmallVideoView_02();
    }

    public SurfaceView getRemoteSmallVideoView_03() {
        return VideoMgr.getInstance().getRemoteSmallVideoView_03();
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
            if (isChairMan()) {
                //结束会议
                showEndConfDialog();
            } else {
                //离开会议
                showLeaveConfDialog();
            }
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
        }//申请主席
        else if (R.id.iv_request_chair == v.getId()) {
            if (isChairMan()) {
                showReleaseChairDialog();
            } else {
                showRequestChairDialog();
            }
        }//添加成员
        else if (R.id.iv_add_member == v.getId()) {
            //显示会控列表
            showConfControlDialog(getMemberList());
        }//静音会议
        else if (R.id.iv_mute_conf == v.getId()) {
            //静音会议
            MeetingMgr.getInstance().muteConf(!getCurrentConfBaseInfo().isMuteAll());
        }
    }

    /**
     * 申请主席的对话框
     */
    private CustomDialog requestChairDialog;

    /**
     * 申请主席
     */
    private void showRequestChairDialog() {
        if (null == requestChairDialog) {
            requestChairDialog = new CustomDialog(this);
            requestChairDialog.setDialogClickListener(new OnDialogClickListener() {
                @Override
                public void onConfirmClickListener(String content) {
                    requestChairman(content);
                }

                @Override
                public void onConfirmClickListener() {

                }

                @Override
                public void onCancleClickListener() {

                }
            });
        }
        requestChairDialog.show();
    }


    /**
     * 结束会议的对话框
     */
    private CustomDialog endConfDialog;

    /**
     * 结束会议
     */
    private void showEndConfDialog() {
        if (null == endConfDialog) {
            endConfDialog = new CustomDialog(this, 2);
            endConfDialog.setDialogClickListener(new OnDialogClickListener() {
                @Override
                public void onConfirmClickListener(String content) {
                }

                @Override
                public void onConfirmClickListener() {
                    endConf();
                }

                @Override
                public void onCancleClickListener() {
                    leaveConf();
                }
            });
        }
        endConfDialog.show();
    }

    /**
     * 离开会议的对话框
     */
    private CustomDialog leaveConfDialog;

    /**
     * 离开会议
     */
    private void showLeaveConfDialog() {
        if (null == leaveConfDialog) {
            leaveConfDialog = new CustomDialog(this, 3);
            leaveConfDialog.setDialogClickListener(new OnDialogClickListener() {
                @Override
                public void onConfirmClickListener(String content) {
                }

                @Override
                public void onConfirmClickListener() {
                    leaveConf();
                }

                @Override
                public void onCancleClickListener() {

                }
            });
        }
        leaveConfDialog.show();
    }

    /**
     * 释放主席的对话框
     */
    private CustomDialog releaseChairDialog;

    /**
     * 释放主席
     */
    private void showReleaseChairDialog() {
        if (null == releaseChairDialog) {
            releaseChairDialog = new CustomDialog(this, 1);
            releaseChairDialog.setDialogClickListener(new OnDialogClickListener() {
                @Override
                public void onConfirmClickListener(String content) {

                }

                @Override
                public void onConfirmClickListener() {
                    releaseChairman();
                }

                @Override
                public void onCancleClickListener() {

                }
            });
        }
        releaseChairDialog.show();
    }

    /**
     * 离开会议
     */
    private void leaveConf() {
        int result = MeetingMgr.getInstance().leaveConf();
        if (result != 0) {
            return;
        }

        LocBroadcast.getInstance().unRegisterBroadcast(this, mActions);
        finish();
    }

    /**
     * 结束会议
     */
    public void endConf() {
        int result = MeetingMgr.getInstance().endConf();
        if (result != 0) {
            return;
        }
        LocBroadcast.getInstance().unRegisterBroadcast(this, mActions);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();

        LocBroadcast.getInstance().registerBroadcast(this, mActions);

        if (isSvcConf) {
            if (isOnlyLocal) {
                setOnlyLocalVideoContainer(this, mRemoteView, mHideView);
                mConfSmallVideoWndLL.setVisibility(View.GONE);
                isSetOnlyLocalWind = true;
            } else {
                setSvcAllVideoContainer(this,
                        mConfLocalVideoLayout,
                        mRemoteView,
                        mHideView,
                        mConfRemoteSmallVideoLayout_01,
                        mConfRemoteSmallVideoLayout_02,
                        mConfRemoteSmallVideoLayout_03);

                if (!isHideVideoWindow) {
                    mConfSmallVideoWndLL.setVisibility(View.VISIBLE);
                }
                isSetOnlyLocalWind = false;
            }
        } else {
            // AVC会议保持原逻辑不变
            setAvcVideoContainer(
                    mConfLocalVideoLayout,
                    mRemoteView,
                    mHideView);
        }

        setAutoRotation(this, true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!isVideo) {
            return;
        }
        closeOrOpenLocalVideo(false);
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (isVideo) {
            if (!DeviceUtil.isAppForeground()) {
                closeOrOpenLocalVideo(true);
            }
        }

//        isFirstStart = true;
//        isPressTouch = false;
//        isShowBar = false;
    }

    /**
     * 关闭或打开摄像头
     *
     * @param close true：关闭，false：打开
     * @return
     */
    public boolean closeOrOpenLocalVideo(boolean close) {
        long callID = MeetingMgr.getInstance().getCurrentConferenceCallID();
        if (callID == 0) {
            return false;
        }

        if (close) {
            CallMgr.getInstance().closeCamera(callID);
        } else {
            CallMgr.getInstance().openCamera(callID);
            VideoMgr.getInstance().setVideoOrient(callID, mCameraIndex);
        }

        return true;
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

        //是否自动接听
        SPStaticUtils.put(UIConstants.IS_AUTO_ANSWER, false);
    }


    @Override
    public void onReceive(String broadcastName, Object obj) {
        final int result;
        LogUtil.d(UIConstants.DEMO_TAG, "收到广播" + broadcastName + "-->" + obj);
        switch (broadcastName) {
            case CustomBroadcastConstants.CONF_STATE_UPDATE:
                LogUtil.d(UIConstants.DEMO_TAG, "CustomBroadcastConstants.CONF_STATE_UPDATE-->" + obj);
                String conferenceID = (String) obj;
                if (!conferenceID.equals(confID)) {
                    return;
                }

                //判断会议状态，如果会议结束，则关闭会议界面
                ConfBaseInfo confBaseInfo = getCurrentConfBaseInfo();

                if (null == confBaseInfo) {
                    return;
                }

                //获取与会者列表
                List<Member> memberList = getMemberList();

                if (memberList == null) {
                    return;
                }

                //刷新会议状态
                refreshConfStatus(confBaseInfo);

                //刷新列表
                refreshMemberList(memberList);

                //SVC:多流会议时的处理
                refreshWatchMemberPage();

                //刷新选看窗口的显示名称
                refreshSvcWatchDisplayName(memberList);

                //远端小窗口+本地窗口数
                int num = MeetingMgr.getInstance().getCurrentWatchSmallCount() + 1;
                if (currentShowSmallWndCount != num) {
                    currentShowSmallWndCount = num;
                    //设置其他画面是否显示
                    setSmallVideoVisible(currentShowSmallWndCount);
                }

                for (final Member member : memberList) {
                    if (member.isSelf()) {
                        postRunOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //设置麦克风状态
                                setMicStatus(member.isMute());

                                updateAttendeeButton(member);
                            }
                        });
                    }
                }
                break;

            case CustomBroadcastConstants.GET_CONF_END:
                LogUtil.d(UIConstants.DEMO_TAG, "CustomBroadcastConstants.GET_CONF_END-->" + obj);
                postRunOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        videoDestroy();
                        finish();
                    }
                });
                break;

            case CustomBroadcastConstants.ADD_LOCAL_VIEW:
                LogUtil.d(UIConstants.DEMO_TAG, "CustomBroadcastConstants.ADD_LOCAL_VIEW-->" + obj);
                mHandler.sendEmptyMessage(ADD_LOCAL_VIEW);
                break;

            case CustomBroadcastConstants.DEL_LOCAL_VIEW:
                break;

            case CustomBroadcastConstants.CONF_CALL_CONNECTED:
                postRunOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                });
                break;

            case CustomBroadcastConstants.ACTION_CALL_END_FAILED:
                postRunOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                });
                break;

            // 静音会议结果
            case CustomBroadcastConstants.MUTE_CONF_RESULT:
                result = (int) obj;
                if (result != 0) {
                    showToast("静音会议失败");
                } else {
                    showToast("静音会议成功");
                }

                break;

            // 取消静音会议结果
            case CustomBroadcastConstants.UN_MUTE_CONF_RESULT:
                result = (int) obj;
                if (result != 0) {
                    showToast("取消静音会议失败");
                } else {
                    showToast("取消静音会议成功");
                }

                break;

            // 邀请与会者结果
            case CustomBroadcastConstants.ADD_ATTENDEE_RESULT:
                result = (int) obj;
                LogUtil.d(UIConstants.DEMO_TAG, "add attendee result: " + result);
                if (result != 0) {
                    showToast("邀请与会者失败");
                    return;
                }
                break;

            // 删除与会者结果
            case CustomBroadcastConstants.DEL_ATTENDEE_RESULT:
                result = (int) obj;
                LogUtil.d(UIConstants.DEMO_TAG, "add attendee result: " + result);
                if (result != 0) {
                    showToast("删除与会者失败");
                    return;
                }
                break;

            // 静音与会者结果
            case CustomBroadcastConstants.MUTE_ATTENDEE_RESULT:
                result = (int) obj;
                if (result != 0) {
                    showToast("静音与会者失败");
                    return;
                }
                break;

            // 取消静音与会者结果
            case CustomBroadcastConstants.UN_MUTE_ATTENDEE_RESULT:
                result = (int) obj;
                if (result != 0) {
                    showToast("取消静音与会者失败");
                    return;
                }
                break;

            // 广播与会者结果
            case CustomBroadcastConstants.BROADCAST_ATTENDEE_CONF_RESULT:
                result = (int) obj;
                if (result != 0) {
                    showToast("广播与会者失败");
                    return;
                }
                break;

            // 取消广播与会者结果
            case CustomBroadcastConstants.CANCEL_BROADCAST_CONF_RESULT:
                result = (int) obj;
                if (result != 0) {
                    showToast("取消广播与会者失败");
                    return;
                }
                break;

            // 请求主席结果
            case CustomBroadcastConstants.REQUEST_CHAIRMAN_RESULT:
                result = (int) obj;
                if (result != 0) {
                    showToast("请求主席失败");
                    return;
                }
                //TODO:设置主席样式
                setSelfPresenter();
                break;

            // 释放主席结果
            case CustomBroadcastConstants.RELEASE_CHAIRMAN_RESULT:
                result = (int) obj;
                if (result != 0) {
                    showToast("释放主席失败");
                    return;
                }
                break;

            //正在观看画面信息通知
            case CustomBroadcastConstants.GET_SVC_WATCH_INFO:
                LogUtil.d(UIConstants.DEMO_TAG, "CustomBroadcastConstants.GET_SVC_WATCH_INFO-->" + obj);

                TsdkConfSvcWatchInfo svcWatchInfo = (TsdkConfSvcWatchInfo) obj;
                if (svcWatchInfo.getWatchAttendeeNum() <= 0 || svcLabel.size() <= 0) {
                    return;
                }
                showSvcWatchInfo(svcWatchInfo.getWatchAttendees());
                break;

            default:
                break;
        }
    }

    /**
     * 显示toast
     *
     * @param content
     */
    private void showToast(final String content) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ToastHelper.showShort(content);
            }
        });

    }

    /**
     * 刷新列表
     *
     * @param memberList
     */
    private void refreshMemberList(final List<Member> memberList) {
        postRunOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (null != confControlAdapter) {
                    boolean b = confControlAdapter.replaceItems(memberListToMemberItemList(memberList), true);
                }
            }
        });
    }

    /**
     * 判断是否是主席
     */
    private void refreshConfStatus(final ConfBaseInfo currentConfBaseInfo) {
        if (null == currentConfBaseInfo) {
            return;
        }
        //是否有主席
        postRunOnUiThread(new Runnable() {
            @Override
            public void run() {
                //添加成员按钮
                ivAddMember.setVisibility(isChairMan() ? View.VISIBLE : View.GONE);

                //静音会议按钮
                ivMuteConf.setVisibility(isChairMan() ? View.VISIBLE : View.GONE);

                //静音会议
                ivMuteConf.setImageResource(currentConfBaseInfo.isMuteAll() ? R.mipmap.ic_close_all_mic : R.mipmap.ic_open_all_mic);

                tvConfName.setText("会议名称:" + currentConfBaseInfo.getSubject() + "\n会议ID:" + currentConfBaseInfo.getConfID());
            }
        });
    }

    /**
     * 更新布局
     *
     * @param runnable
     */
    private void postRunOnUiThread(Runnable runnable) {
        runOnUiThread(runnable);
    }

    private void setSelfPresenter() {
        ConfBaseInfo confBaseInfo = getCurrentConfBaseInfo();
        if (null == confBaseInfo) {
            return;
        }

        if (confBaseInfo.getMediaType() == TsdkConfMediaType.TSDK_E_CONF_MEDIA_VIDEO
                || confBaseInfo.getMediaType() == TsdkConfMediaType.TSDK_E_CONF_MEDIA_VOICE) {
            return;
        }

        int result = 0;
        Member self = MeetingMgr.getInstance().getCurrentConferenceSelf();
        if (null == self) {
            return;
        }

        if (self.getRole() == TsdkConfRole.TSDK_E_CONF_ROLE_CHAIRMAN && !self.isPresent()) {
            result = MeetingMgr.getInstance().setPresenter(self);
        }

        if (0 != result) {
//            getView().showCustomToast(R.string.set_presenter_failed);
        }
    }


    public void videoDestroy() {
        if (null != CallMgr.getInstance().getVideoDevice()) {
            LogUtil.d(UIConstants.DEMO_TAG, "onCallClosed destroy.");
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
        boolean currentMuteStatus = getMicStatus();
        if (CallMgr.getInstance().muteMic(mCallID, !currentMuteStatus)) {
            mCallFunc.setMuteStatus(!currentMuteStatus);
            setMicStatus(!currentMuteStatus);
        }
    }

    /**
     * 获取麦克风状态
     *
     * @return
     */
    public boolean getMicStatus() {
        return mCallFunc.isMuteStatus();
    }

    /**
     * 设置麦克风图片
     *
     * @param currentMuteStatus true:代表静音，false:非静音
     */
    private void setMicStatus(boolean currentMuteStatus) {
        //更新状态静音按钮状态
        tvMic.setCompoundDrawablesWithIntrinsicBounds(0, currentMuteStatus ? R.mipmap.icon_mic_close : R.mipmap.icon_mic, 0, 0);
    }

    /**
     * 更新自己的状态
     *
     * @param member
     */
    public void updateAttendeeButton(final Member member) {
        this.isShareOwner = member.isShareOwner();
        mCallInfo.setVideoCall(member.isVideo());
        if (!isVideo || !isSvcConf) {
            return;
        }
        if (isOnlyLocal || isHideVideoWindow) {
            return;
        }
        mConfLocalVideoText.setText("(我)" + member.getDisplayName());
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
     * 开关本地摄像头
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

    /**
     * 添加与会者
     *
     * @param name
     * @param number
     * @param account
     */
    public void addMember(String name, String number, String account) {
        Member member = new Member();
        member.setNumber(number);
        member.setDisplayName(name);
        member.setAccountId(account);
        member.setRole(TsdkConfRole.TSDK_E_CONF_ROLE_ATTENDEE);

        int result = MeetingMgr.getInstance().addAttendee(member);
        if (result != 0) {
            showToast("添加与会者失败");
        }
    }

    /**
     * 移除
     *
     * @param member
     */
    public void delMember(Member member) {
        int result = MeetingMgr.getInstance().removeAttendee(member);
        if (result != 0) {
            showToast("移除与会者失败");
        }
    }

    /**
     * 静音与会者
     *
     * @param member
     * @param isMute
     */
    public void muteMember(Member member, boolean isMute) {
        int result = MeetingMgr.getInstance().muteAttendee(member, isMute);
        if (result != 0) {
            if (isMute) {
                showToast("静音与会者失败");
            } else {
                showToast("取消静音与会者失败");
            }
        }
    }

    /**
     * 静音会议
     *
     * @param isMute
     */
    public void muteConf(boolean isMute) {
        int result = MeetingMgr.getInstance().muteConf(isMute);
        if (result != 0) {
            if (isMute) {
                showToast("静音会议失败");
            } else {
                showToast("取消会议失败");
            }
        }
    }

    /**
     * 广播与会者
     *
     * @param member
     * @param isBroad
     */
    public void broadcastAttendee(Member member, boolean isBroad) {
        int result = MeetingMgr.getInstance().broadcastAttendee(member, isBroad);
        if (0 != result) {
            if (isBroad) {
                showToast("广播与会者失败失败");
            } else {
                showToast("取消广播与会者失败");
            }
        }
    }

    /**
     * 挂断与会者
     *
     * @param member
     */
    public void hangupAttendee(Member member) {
        int result = MeetingMgr.getInstance().hangupAttendee(member);
        if (0 != result) {
            showToast("挂断与会者失败");
        }
    }

    /**
     * 呼叫与会者
     *
     * @param member
     */
    public void redialAttendee(Member member) {
        int result = MeetingMgr.getInstance().redialAttendee(member);
        if (0 != result) {
            showToast("呼叫与会者失败");
        }
    }

    /**
     * 观看与会者
     *
     * @param member
     */
    public void watchAttendee(Member member) {
        int result = MeetingMgr.getInstance().watchAttendee(member);
        if (0 != result) {
            showToast("观看与会者失败");
        }
    }

    /**
     * 判断是否是主席
     *
     * @return
     */
    public boolean isChairMan() {
        Member self = getSelf();
        if (self == null) {
            return false;
        }
        return self.getRole() == TSDK_E_CONF_ROLE_CHAIRMAN;
    }

    /**
     * 获取当前的会议详情
     *
     * @return
     */
    private ConfBaseInfo getCurrentConfBaseInfo() {
        return MeetingMgr.getInstance().getCurrentConferenceBaseInfo();
    }

    /**
     * 获取当前的member
     *
     * @return
     */
    private Member getSelf() {
        return MeetingMgr.getInstance().getCurrentConferenceSelf();
    }

    /**
     * 获取与会者列表
     *
     * @return
     */
    private List<Member> getMemberList() {
        List<Member> memberList = MeetingMgr.getInstance().getCurrentConferenceMemberList();
        if (null == memberList) {
            return new ArrayList<>();
        } else {
            return memberList;
        }
    }

    /**
     * 申请主席
     *
     * @param chairmanPassword
     */
    public void requestChairman(String chairmanPassword) {
        int result = MeetingMgr.getInstance().requestChairman(chairmanPassword);
        if (result != 0) {
            showToast("申请主席失败");
            return;
        }
    }

    /**
     * 释放主席
     */
    public void releaseChairman() {
        int result = MeetingMgr.getInstance().releaseChairman();
        if (result != 0) {
            showToast("释放主席失败");
            return;
        }
    }

    /***************************会控的对话框-start*************************/
    private BottomSheetDialog confControlDialog;
    private TextView tvControlCancel;
    private TextView tvControlConfirm;

    private RecyclerView rvControl;
    private BaseItemAdapter<ConfControlItem> confControlAdapter;

    /**
     * 显示会控的对话框
     */
    private void showConfControlDialog(List<Member> siteList) {
        if (null != confControlDialog) {
            //刷新数据
            confControlAdapter.replaceItems(memberListToMemberItemList(siteList), true);
        } else {
            initConfControlDialog(siteList);
        }

        //设置添添加人员按钮是否显示
        tvControlConfirm.setVisibility(isChairMan() ? View.VISIBLE : View.INVISIBLE);

        confControlDialog.show();
    }

    private void initConfControlDialog(List<Member> siteList) {
        //初始化适配器
        initConfControlAdapter(siteList);

        //构造函数的第二个参数可以设置BottomSheetDialog的主题样式
        confControlDialog = new BottomSheetDialog(this);
        //导入底部reycler布局
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_site, null, false);

        rvControl = view.findViewById(R.id.rv_add_attendees);
        rvControl.setLayoutManager(new LinearLayoutManager(this));
        rvControl.setAdapter(confControlAdapter);

        tvControlCancel = view.findViewById(R.id.tv_add_cancle);
        tvControlConfirm = view.findViewById(R.id.tv_add_confirm);
        TextView tvLable = view.findViewById(R.id.tv_lable);
        tvLable.setText("与会列表");

        View speaceHolder = view.findViewById(R.id.view_speaceHolder);

        //配置点击外部区域消失
        speaceHolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confControlDialog.dismiss();
            }
        });

        tvControlCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confControlDialog.dismiss();
            }
        });

        tvControlConfirm.setText("添加用户");

        tvControlConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //获取所有会场
//                getAllPeople();
            }
        });

        confControlDialog.setContentView(view);
        try {
            ViewGroup parent = (ViewGroup) view.getParent();
            parent.setBackgroundColor(ContextCompat.getColor(this, R.color.tran));
        } catch (Exception e) {
            e.printStackTrace();
        }

        BottomSheetBehavior mBehavior = BottomSheetBehavior.from((View) view.getParent());
        //设置默认弹出高度为屏幕的0.4倍
        //mBehavior.setPeekHeight((int) (0.4 * height));
        mBehavior.setPeekHeight((int) (DensityUtil.getScreenHeight(this)));

        //设置点击dialog外部不消失
        confControlDialog.setCanceledOnTouchOutside(false);
        confControlDialog.setCancelable(false);
    }

    /**
     * 初始化会控适配器
     *
     * @param siteList
     */
    private void initConfControlAdapter(List<Member> siteList) {
        confControlAdapter = new BaseItemAdapter<ConfControlItem>(false);
        confControlAdapter.setItems(memberListToMemberItemList(siteList));
    }

    /**
     * 返回成员
     *
     * @param memberList
     * @return
     */
    private List<ConfControlItem> memberListToMemberItemList(List<Member> memberList) {
        List<ConfControlItem> confControlItems = new ArrayList<>();

        ConfControlItem controlItem = null;

        for (Member member : memberList) {
            controlItem = new ConfControlItem(member);
            controlItem.setOnControlItemClickListener(confControlItemClickListener);
            confControlItems.add(controlItem);
        }
        return confControlItems;
    }

    /**
     * 会控按钮
     */
    private ConfControlItem.onControlItemClickListener confControlItemClickListener = new ConfControlItem.onControlItemClickListener() {
        @Override
        public void onHangUpSite(Member member, int position) {
            if (member.getStatus() != ConfConstant.ParticipantStatus.IN_CONF) {
                redialAttendee(member);
            } else {
                hangupAttendee(member);
            }
        }

        @Override
        public void onCallSite(Member member, int position) {
            if (member.getStatus() != ConfConstant.ParticipantStatus.IN_CONF) {
                redialAttendee(member);
            } else {
                hangupAttendee(member);
            }
        }

        @Override
        public void onLoduerSite(Member member, int position) {
        }

        @Override
        public void onBroadcastSite(Member member, int position) {
            broadcastAttendee(member, !member.isBroadcastSelf());
        }

        @Override
        public void onWatchSite(Member member, int position) {
//            ToastHelper.showShort("onWatchSite" + member.getDisplayName());
//            watchAttendee(member);
        }
    };
    /***************************会控的对话框-end*************************/


    /************************显示远端画面-start***********************/
    public void refreshWatchMemberPage() {
        LogUtil.d(UIConstants.DEMO_TAG, "refreshWatchMemberPage");
        //当前页数
        final int currentPage = MeetingMgr.getInstance().getCurrentWatchPage();
        //总页数
        final int totalPage = MeetingMgr.getInstance().getTotalWatchablePage();
        //观看的总数量
        final int watchSum = MeetingMgr.getInstance().getWatchSum();

        LogUtil.d(TAG, "currentPage->" + currentPage + ", totalPage->" + totalPage + ", watchSum->" + watchSum);

        if (0 == watchSum) {
            isOnlyLocal = true;
        } else {
            isOnlyLocal = false;
        }

        if (watchSum > 0) {
            //判断观看的数量是否可以被3整除
            int mo = watchSum % 3;

            //当前viewpager的总页数
            int totalPageNumber = 0;

            //如果不能整除
            if (mo != 0) {
                totalPageNumber = watchSum / 3 + 1;
            }

            //需要添加的页数，应总页数-当前pager总页数
            int addNumber = (totalPageNumber + 1) - pagerAdapter.getCount();

            if (addNumber < 0) {
                for (int i = pagerAdapter.getCount(); i > Math.abs(addNumber); i--) {
                    deletePage();
                }
            }else if(addNumber>0) {
                for (int i = 0; i < addNumber; i++) {
                    addPage();
                }
            }

        }

        //更新本地画面
        updateLocalVideo();

        if (isHideVideoWindow) {
            return;
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isSvcConf && isVideo) {
                    if (totalPage <= 1) {
                        mConfVideoBackIV.setVisibility(View.INVISIBLE);
                        mConfVideoForwardIV.setVisibility(View.INVISIBLE);
                    } else {
                        if (currentPage == 1) {
                            mConfVideoBackIV.setVisibility(View.INVISIBLE);
                            mConfVideoForwardIV.setVisibility(View.VISIBLE);
                        } else if (currentPage == totalPage) {
                            mConfVideoBackIV.setVisibility(View.VISIBLE);
                            mConfVideoForwardIV.setVisibility(View.INVISIBLE);
                        } else {
                            mConfVideoBackIV.setVisibility(View.VISIBLE);
                            mConfVideoForwardIV.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }
        });
    }

    /**
     * 显示远端画面的显示
     *
     * @param sum
     */
    public void setSmallVideoVisible(final int sum) {
        if (!isVideo || !isSvcConf) {
            return;
        }

        if (isHideVideoWindow) {
            return;
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                switch (sum) {
//                    case 0:
//                        getLocalVideoView().setVisibility(View.GONE);
//                        getRemoteSmallVideoView_01().setVisibility(View.GONE);
//                        getRemoteSmallVideoView_02().setVisibility(View.GONE);
//                        getRemoteSmallVideoView_03().setVisibility(View.GONE);
//
//                        mConfSmallVideoWndLL.setVisibility(View.GONE);
//                        mConfLocalVideoLayout.setVisibility(View.GONE);
//                        mConfRemoteSmallVideoLayout_01.setVisibility(View.GONE);
//                        mConfRemoteSmallVideoLayout_02.setVisibility(View.GONE);
//                        mConfRemoteSmallVideoLayout_03.setVisibility(View.GONE);
//
//                        mConfLocalVideoText.setVisibility(View.GONE);
//                        mConfRemoteSmallVideoText_01.setVisibility(View.GONE);
//                        mConfRemoteSmallVideoText_02.setVisibility(View.GONE);
//                        mConfRemoteSmallVideoText_03.setVisibility(View.GONE);
//                        break;
//
//                    case 1:
//                        getLocalVideoView().setVisibility(View.VISIBLE);
//                        getRemoteSmallVideoView_01().setVisibility(View.GONE);
//                        getRemoteSmallVideoView_02().setVisibility(View.GONE);
//                        getRemoteSmallVideoView_03().setVisibility(View.GONE);
//
//                        mConfSmallVideoWndLL.setVisibility(View.GONE);
//                        mConfLocalVideoLayout.setVisibility(View.GONE);
//                        mConfRemoteSmallVideoLayout_01.setVisibility(View.GONE);
//                        mConfRemoteSmallVideoLayout_02.setVisibility(View.GONE);
//                        mConfRemoteSmallVideoLayout_03.setVisibility(View.GONE);
//
//                        mConfLocalVideoText.setVisibility(View.GONE);
//                        mConfRemoteSmallVideoText_01.setVisibility(View.GONE);
//                        mConfRemoteSmallVideoText_02.setVisibility(View.GONE);
//                        mConfRemoteSmallVideoText_03.setVisibility(View.GONE);
//                        break;
//
//                    case 2:
//                        getLocalVideoView().setVisibility(View.VISIBLE);
//                        getRemoteSmallVideoView_01().setVisibility(View.VISIBLE);
//                        getRemoteSmallVideoView_02().setVisibility(View.GONE);
//                        getRemoteSmallVideoView_03().setVisibility(View.GONE);
//
//                        mConfSmallVideoWndLL.setVisibility(View.VISIBLE);
//                        mConfLocalVideoLayout.setVisibility(View.VISIBLE);
//                        mConfRemoteSmallVideoLayout_01.setVisibility(View.VISIBLE);
//                        mConfRemoteSmallVideoLayout_02.setVisibility(View.GONE);
//                        mConfRemoteSmallVideoLayout_03.setVisibility(View.GONE);
//
//                        mConfLocalVideoText.setVisibility(View.VISIBLE);
//                        mConfRemoteSmallVideoText_01.setVisibility(View.VISIBLE);
//                        mConfRemoteSmallVideoText_02.setVisibility(View.GONE);
//                        mConfRemoteSmallVideoText_03.setVisibility(View.GONE);
//                        break;
//
//                    case 3:
//                        getLocalVideoView().setVisibility(View.VISIBLE);
//                        getRemoteSmallVideoView_01().setVisibility(View.VISIBLE);
//                        getRemoteSmallVideoView_02().setVisibility(View.VISIBLE);
//                        getRemoteSmallVideoView_03().setVisibility(View.GONE);
//
//                        mConfSmallVideoWndLL.setVisibility(View.VISIBLE);
//                        mConfLocalVideoLayout.setVisibility(View.VISIBLE);
//                        mConfRemoteSmallVideoLayout_01.setVisibility(View.VISIBLE);
//                        mConfRemoteSmallVideoLayout_02.setVisibility(View.VISIBLE);
//                        mConfRemoteSmallVideoLayout_03.setVisibility(View.GONE);
//
//                        mConfLocalVideoText.setVisibility(View.VISIBLE);
//                        mConfRemoteSmallVideoText_01.setVisibility(View.VISIBLE);
//                        mConfRemoteSmallVideoText_02.setVisibility(View.VISIBLE);
//                        mConfRemoteSmallVideoText_03.setVisibility(View.GONE);
//                        break;
//
//                    case 4:
//                        getLocalVideoView().setVisibility(View.VISIBLE);
//                        getRemoteSmallVideoView_01().setVisibility(View.VISIBLE);
//                        getRemoteSmallVideoView_02().setVisibility(View.VISIBLE);
//                        getRemoteSmallVideoView_03().setVisibility(View.VISIBLE);
//
//                        mConfSmallVideoWndLL.setVisibility(View.VISIBLE);
//                        mConfLocalVideoLayout.setVisibility(View.VISIBLE);
//                        mConfRemoteSmallVideoLayout_01.setVisibility(View.VISIBLE);
//                        mConfRemoteSmallVideoLayout_02.setVisibility(View.VISIBLE);
//                        mConfRemoteSmallVideoLayout_03.setVisibility(View.VISIBLE);
//
//                        mConfLocalVideoText.setVisibility(View.VISIBLE);
//                        mConfRemoteSmallVideoText_01.setVisibility(View.VISIBLE);
//                        mConfRemoteSmallVideoText_02.setVisibility(View.VISIBLE);
//                        mConfRemoteSmallVideoText_03.setVisibility(View.VISIBLE);
//                        break;
//
//                    default:
//                        break;
//                }
            }
        });
    }

    /**
     * 刷新svc会场的显示信息
     *
     * @param list
     */
    private void refreshSvcWatchDisplayName(List<Member> list) {
        if (null == watchMap || watchMap.isEmpty()) {
            return;
        }

        for (Member member : list) {
            for (String key : watchMap.keySet()) {
                if (member.getNumber().equals(key)) {
                    switch (watchMap.get(key)) {
                        case REMOTE_DISPLAY:
                            remoteDisplay = member.getDisplayName();
                            break;

                        case SMALL_DISPLAY_01:
                            smallDisplay_01 = member.getDisplayName();
                            break;

                        case SMALL_DISPLAY_02:
                            smallDisplay_02 = member.getDisplayName();
                            break;

                        case SMALL_DISPLAY_03:
                            smallDisplay_03 = member.getDisplayName();
                            break;

                        default:
                            break;
                    }
                }
            }
        }

        //刷新名称
        refreshSvcWatchDisplayName(
                remoteDisplay,
                smallDisplay_01,
                smallDisplay_02,
                smallDisplay_03);
    }

    /**
     * 显示远端的名称
     *
     * @param remote
     * @param small_01
     * @param small_02
     * @param small_03
     */
    public void refreshSvcWatchDisplayName(final String remote,
                                           final String small_01,
                                           final String small_02,
                                           final String small_03) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!TextUtils.isEmpty(remote)) {
                    //显示远端画面的文字
//                    mConfRemoteBigVideoText.setVisibility(View.VISIBLE);
                }
//                mConfRemoteBigVideoText.setText(remote);
                mConfRemoteSmallVideoText_01.setText(small_01);
                mConfRemoteSmallVideoText_02.setText(small_02);
                mConfRemoteSmallVideoText_03.setText(small_03);
            }
        });
    }

    /**
     * 更新远端画面
     */
    public void updateLocalVideo() {
        if (!isVideo) {
            return;
        }
//        if (mOrientation == ORIENTATION_LANDSCAPE) {
//            mConfSmallVideoWndLL.setOrientation(LinearLayout.VERTICAL);
//            setConfVideoSize(false);
//        } else {
//            mConfSmallVideoWndLL.setOrientation(LinearLayout.HORIZONTAL);
//            setConfVideoSize(true);
//        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isSvcConf) {
                    if (isOnlyLocal) {
                        if (isSetOnlyLocalWind) {
                            return;
                        }

                        //只有本地画面时
                        setOnlyLocalVideoContainer(
                                VideoConfGridActivity.this,
                                mRemoteView,
                                mHideView);

                        mConfSmallVideoWndLL.setVisibility(View.GONE);

                        isSetOnlyLocalWind = true;
                    } else {
                        if (!isSetOnlyLocalWind) {
                            return;
                        }

                        //多流会议
                        setSvcAllVideoContainer(
                                VideoConfGridActivity.this,
                                mConfLocalVideoLayout,
                                mRemoteView,
                                mHideView,
                                mConfRemoteSmallVideoLayout_01,
                                mConfRemoteSmallVideoLayout_02,
                                mConfRemoteSmallVideoLayout_03);

                        mConfSmallVideoWndLL.setVisibility(View.VISIBLE);

                        isSetOnlyLocalWind = false;
                    }
                } else {
                    // AVC会议保持原逻辑不变
                    //单流会议
                    setAvcVideoContainer(
                            mLocalView,
                            mRemoteView,
                            mHideView);
                }
            }
        });
    }


    private static final int REMOTE_DISPLAY = 0;
    private static final int SMALL_DISPLAY_01 = 1;
    private static final int SMALL_DISPLAY_02 = 2;
    private static final int SMALL_DISPLAY_03 = 3;

    private String remoteDisplay = "";
    private String smallDisplay_01 = "";
    private String smallDisplay_02 = "";
    private String smallDisplay_03 = "";

    /**
     * 存储远端画面的名称
     *
     * @param watchAttendees
     */
    private void showSvcWatchInfo(List<TsdkConfSvcWatchAttendee> watchAttendees) {
        watchMap.clear();
        for (TsdkConfSvcWatchAttendee watchAttendee : watchAttendees) {
            if (svcLabel.get(0) == watchAttendee.getLabel()) {
                remoteDisplay = watchAttendee.getBaseInfo().getDisplayName();
                watchMap.put(watchAttendee.getBaseInfo().getNumber(), REMOTE_DISPLAY);
            }//
            else if (svcLabel.get(1) == watchAttendee.getLabel()) {
                smallDisplay_01 = watchAttendee.getBaseInfo().getDisplayName();
                watchMap.put(watchAttendee.getBaseInfo().getNumber(), SMALL_DISPLAY_01);
            }//
            else if (svcLabel.get(2) == watchAttendee.getLabel()) {
                smallDisplay_02 = watchAttendee.getBaseInfo().getDisplayName();
                watchMap.put(watchAttendee.getBaseInfo().getNumber(), SMALL_DISPLAY_02);
            } //
            else if (svcLabel.get(3) == watchAttendee.getLabel()) {
                smallDisplay_03 = watchAttendee.getBaseInfo().getDisplayName();
                watchMap.put(watchAttendee.getBaseInfo().getNumber(), SMALL_DISPLAY_03);
            }
        }
        //显示远端画面的名称
        refreshSvcWatchDisplayName(
                remoteDisplay,
                smallDisplay_01,
                smallDisplay_02,
                smallDisplay_03);
    }

    /**
     * 设置单流画面时的画面显示
     */
    private void setAvcVideoContainer(
            ViewGroup smallLayout,
            ViewGroup bigLayout,
            ViewGroup hideLayout) {

        LogUtil.d(UIConstants.DEMO_TAG, "setAvcVideoContainer");

        addSurfaceView(mConfLocalVideoLayout, getLocalVideoView());

        addSurfaceView(smallLayout, getLocalVideoView());
        addSurfaceView(bigLayout, getRemoteBigVideoView());
        addSurfaceView(hideLayout, getHideVideoView());
    }


    /**
     * 只有本地画面时，大画面显示本地画面
     *
     * @param context
     * @param bigLayout
     * @param hideLayout
     */
    public void setOnlyLocalVideoContainer(Context context,
                                           ViewGroup bigLayout,
                                           ViewGroup hideLayout) {
        LogUtil.d(UIConstants.DEMO_TAG, "只有本地画面时-->setOnlyLocalVideoContainer");

        //隐藏本地小画面
        mLocalView.setVisibility(View.GONE);

        if (bigLayout != null) {
//            addSurfaceView(bigLayout, getLocalVideoView());
        }

        ToastHelper.showShort("只有本地画面时-->setOnlyLocalVideo");

        //移除fragment
        for (int i = 1; i < pagerAdapter.getCount(); i++) {
            deletePage(i);
        }

        LocBroadcast.getInstance().sendBroadcast(CustomBroadcastConstants.REFRESH_REMOTE_VIEW, false);

        if (hideLayout != null) {
            addSurfaceView(hideLayout, getHideVideoView());
        }
    }


    /**
     * 设置多流会议画面
     *
     * @param videoConfActivity
     * @param smallLayout
     * @param mRemoteView
     * @param mHideView
     * @param mHideView
     * @param mConfRemoteSmallVideoLayout_01
     * @param mConfRemoteSmallVideoLayout_02
     * @param mConfRemoteSmallVideoLayout_03
     */
    private void setSvcAllVideoContainer(VideoConfGridActivity videoConfActivity,
                                         FrameLayout smallLayout,
                                         FrameLayout mRemoteView,
                                         FrameLayout mHideView,
                                         FrameLayout mConfRemoteSmallVideoLayout_01,
                                         FrameLayout mConfRemoteSmallVideoLayout_02,
                                         FrameLayout mConfRemoteSmallVideoLayout_03) {
        LogUtil.d(UIConstants.DEMO_TAG, "设置多流会议画面->setSvcAllVideoContainer");

        mLocalView.setVisibility(View.GONE);

        ToastHelper.showShort("设置多流会议画面-->setSvcAllVideoContainer");

        LocBroadcast.getInstance().sendBroadcast(CustomBroadcastConstants.REFRESH_REMOTE_VIEW, true);

        LocBroadcast.getInstance().sendBroadcast(CustomBroadcastConstants.REFRESH_SMALL_VIEW, vpContent.getCurrentItem());

        if (smallLayout != null) {
            addSurfaceView(smallLayout, getLocalVideoView());
        }

        if (mRemoteView != null) {
//            addSurfaceView(mRemoteView, getRemoteBigVideoView());
        }

        if (mConfRemoteSmallVideoLayout_01 != null) {
            addSurfaceView(mConfRemoteSmallVideoLayout_01, getRemoteSmallVideoView_01());
        }

        if (mConfRemoteSmallVideoLayout_02 != null) {
            addSurfaceView(mConfRemoteSmallVideoLayout_02, getRemoteSmallVideoView_02());
        }

        if (mConfRemoteSmallVideoLayout_03 != null) {
            addSurfaceView(mConfRemoteSmallVideoLayout_03, getRemoteSmallVideoView_03());
        }

        if (mHideView != null) {
            addSurfaceView(mHideView, getHideVideoView());
        }
    }

    /**
     * 设置会议画面的高度
     *
     * @param isVertical
     */
    private void setConfVideoSize(boolean isVertical) {
        // 获取屏幕的宽和高
        int px = DensityUtil.dip2px(40.0f);
        int mScreenWidth = DensityUtil.getScreenHeight(this) - px;
        int mScreenHeight = DensityUtil.getScreenHeight(this) - px;
        if (isVertical) {
            mConfVideoBackIV.setRotation(0);
            mConfVideoForwardIV.setRotation(0);
            mConfLocalVideoLayout.getLayoutParams().width = mScreenWidth / 4;
            mConfLocalVideoLayout.getLayoutParams().height = (int) (mScreenWidth / 4 * (16.0 / 9.0));
            mConfRemoteSmallVideoLayout_01.getLayoutParams().width = mScreenWidth / 4;
            mConfRemoteSmallVideoLayout_01.getLayoutParams().height = (int) (mScreenWidth / 4 * (16.0 / 9.0));
            mConfRemoteSmallVideoLayout_02.getLayoutParams().width = mScreenWidth / 4;
            mConfRemoteSmallVideoLayout_02.getLayoutParams().height = (int) (mScreenWidth / 4 * (16.0 / 9.0));
            mConfRemoteSmallVideoLayout_03.getLayoutParams().width = mScreenWidth / 4;
            mConfRemoteSmallVideoLayout_03.getLayoutParams().height = (int) (mScreenWidth / 4 * (16.0 / 9.0));
        } else {
            mConfVideoBackIV.setRotation(90);
            mConfVideoForwardIV.setRotation(90);
            mConfLocalVideoLayout.getLayoutParams().width = (int) (mScreenWidth / 2 * (16.0 / 9.0));
            mConfLocalVideoLayout.getLayoutParams().height = mScreenWidth / 2;

            mConfRemoteSmallVideoLayout_01.getLayoutParams().width = (int) (mScreenWidth / 2 * (16.0 / 9.0));
            mConfRemoteSmallVideoLayout_01.getLayoutParams().height = mScreenWidth / 2;
            mConfRemoteSmallVideoLayout_02.getLayoutParams().width = (int) (mScreenWidth / 2 * (16.0 / 9.0));
            mConfRemoteSmallVideoLayout_02.getLayoutParams().height = mScreenWidth / 2;
            mConfRemoteSmallVideoLayout_03.getLayoutParams().width = (int) (mScreenWidth / 2 * (16.0 / 9.0));
            mConfRemoteSmallVideoLayout_03.getLayoutParams().height = mScreenWidth / 2;
        }
    }

    /**
     * 删除fragment
     *
     */
    private void deletePage() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                pagerAdapter.delPage();
            }
        });
    }

    /**
     * 删除fragment
     *
     * @param index
     */
    private void deletePage(final int index) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                pagerAdapter.delPage(index);
            }
        });

    }

    /**
     * 添加view
     */
    private void addPage() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                pagerAdapter.addPage(ConfViewFragment.newInstance(1));
            }
        });
    }

    /************************显示远端画面-end***********************/

}
