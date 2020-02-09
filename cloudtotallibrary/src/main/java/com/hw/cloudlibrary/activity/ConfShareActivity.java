package com.hw.cloudlibrary.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.huawei.ecterminalsdk.base.TsdkConfAsStateInfo;
import com.huawei.ecterminalsdk.base.TsdkConfChatMsgInfo;
import com.huawei.ecterminalsdk.base.TsdkConfRole;
import com.huawei.ecterminalsdk.base.TsdkConfShareSubState;
import com.huawei.ecterminalsdk.base.TsdkDocShareDelDocInfo;
import com.huawei.ecterminalsdk.base.TsdkWbDelDocInfo;
import com.huawei.opensdk.callmgr.CallMgr;
import com.huawei.opensdk.callmgr.VideoMgr;
import com.huawei.opensdk.commonservice.common.common.LocContext;
import com.huawei.opensdk.commonservice.common.localbroadcast.CustomBroadcastConstants;
import com.huawei.opensdk.commonservice.common.localbroadcast.LocBroadcast;
import com.huawei.opensdk.commonservice.common.localbroadcast.LocBroadcastReceiver;
import com.huawei.opensdk.demoservice.MeetingMgr;
import com.huawei.opensdk.demoservice.Member;
import com.hw.cloudlibrary.R;
import com.hw.cloudlibrary.ecsdk.common.UIConstants;
import com.hw.cloudlibrary.utils.ToastHelper;
import com.hw.cloudlibrary.widget.BarrageAnimation;
import com.hw.cloudlibrary.widget.floatView.annotation.widget.AnnoToolBar;
import com.hw.cloudlibrary.widget.floatView.annotation.widget.DragFloatActionButton;
import com.hw.cloudlibrary.widget.floatView.screenShare.FloatWindowsManager;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * 会议分享界面
 */
public class ConfShareActivity extends BaseLibActivity implements View.OnClickListener {

    @Override
    protected void findViews() {
        // data layout
        mDataConfLayout = (RelativeLayout) findViewById(R.id.date_conf_rl);

        // data share layout
        mConfShareLayout = (FrameLayout) findViewById(R.id.conf_share_layout);

        // Data sharing has not started
        mConfShareEmptyLayout = (FrameLayout) findViewById(R.id.conf_share_empty);

        //标注笔，先隐藏，有标注能力再显示
        mAnnoFloatButton = (DragFloatActionButton) findViewById(R.id.anno_float_button);
        mAnnoToolbar = (AnnoToolBar) findViewById(R.id.anno_toolbar);
        mAnnoFloatButton.setOnClickAnnon(new DragFloatActionButton.ICallBack() {
            @Override
            public void clickAnnon() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setAnnotbtnVisibility(View.GONE);
                        if (mAnnoToolbar != null) {
                            mAnnoToolbar.reset(true);
//                            resetToolbarPosition();
                            mAnnoToolbar.setVisibility(View.VISIBLE);
                        }

                        startAnnotation();
                        setAnnotationLocalStatus(true);

                    }
                });
            }
        });

        mAnnoToolbar.setOnClickAnnon(new AnnoToolBar.ICallBack() {
            @Override
            public void clickAnnon() {
                if (mAnnoToolbar != null) {
//                    resetToolbarPosition();
                    mAnnoToolbar.setVisibility(View.GONE);
                }
                setAnnotbtnVisibility(View.VISIBLE);
            }
        });

        // 需要隐藏的标题栏
        mTitleBar = (RelativeLayout) findViewById(R.id.title_layout_transparent);
//        mChatBottom = (LinearLayout) findViewById(R.id.chat_data_meeting_layout);

        // 采集视频
        mHideVideoView = (FrameLayout) findViewById(R.id.hide_video_view);
        mLocalVideoView = (FrameLayout) findViewById(R.id.local_video_view);

        // title
        mRightIV = (ImageView) findViewById(R.id.right_iv);
        mTitleTV = (TextView) findViewById(R.id.conf_title);
        mLeaveIV = (ImageView) findViewById(R.id.leave_iv);

        // chat
//        mChatMsg = (EditText) findViewById(R.id.message_input_et);
//        mChatSend = (ImageView) findViewById(R.id.chat_send_iv);

        // barrage display view
        mBarrageLayout = (RelativeLayout) findViewById(R.id.barrage_layout);
    }

    @Override
    protected void initData() {
        Intent intent = getIntent();
        confID = intent.getStringExtra(UIConstants.CONF_ID);
        isVideo = intent.getBooleanExtra(UIConstants.IS_VIDEO_CONF, false);
        isStartShare = intent.getBooleanExtra(UIConstants.IS_START_SHARE_CONF, false);
        isAllowAnnot = intent.getBooleanExtra(UIConstants.IS_ALLOW_ANNOT, false);
        isActiveShare = intent.getBooleanExtra(UIConstants.IS_ACTIVE_SHARE, false);
        if (confID == null) {
            showCustomToast("会议id为空");
            return;
        }

        setConfID(confID);

        mSubject = getSubject();

        mTitleTV.setText(mSubject);
        mRightIV.setVisibility(View.GONE);

        attachSurfaceView(mConfShareLayout, this);

        initHandler();
    }

    @Override
    protected void setListener() {
        mConfShareLayout.setOnClickListener(this);
        mDataConfLayout.setOnClickListener(this);
        mLeaveIV.setOnClickListener(this);
        mChatSend.setOnClickListener(this);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_conf_share;
    }

    /**********************View-start***************************/
    private FrameLayout mConfShareLayout;
    private RelativeLayout mDataConfLayout;
    private FrameLayout mConfShareEmptyLayout;
    private ImageView mLeaveIV;
    private TextView mTitleTV;
    private ImageView mRightIV;
    private String mSubject;
    private String confID;
    private FrameLayout mHideVideoView;
    private FrameLayout mLocalVideoView;
    private RelativeLayout mTitleBar;
    private LinearLayout mChatBottom;
    private EditText mChatMsg;
    private ImageView mChatSend;
    private RelativeLayout mBarrageLayout;
    private DragFloatActionButton mAnnoFloatButton;
    private AnnoToolBar mAnnoToolbar;
    private Handler handler = null;

    private boolean isVideo;
//    private MyTimerTask myTimerTask;
//    private Timer timer;

    /**
     * 是否第一次执行计时器
     */
    private boolean isFirstStart = true;
    /**
     * 是否触发触摸屏幕事件
     */
    private boolean isPressTouch = false;
    /**
     * 控件是否显示
     */
    private boolean isShowBar = false;

    /**
     * 是否正在共享
     */
    private boolean isStartShare = false;

    /**
     * 是否允许标注
     */
    private boolean isAllowAnnot = false;

    /**
     * 是否主动共享，若主动共享则隐藏标注笔
     */
    private boolean isActiveShare = false;

    private static final int STOP_SCREEN_SHARE = 222;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {

                case STOP_SCREEN_SHARE:
                    if (mHandler != null) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                FloatWindowsManager.getInstance().removeAllScreenShareFloatWindow(LocContext.getContext());
                            }
                        });
                    }
                    break;

                default:
                    break;
            }
        }
    };


    //回复标注原始位置
    private void setAnnotbtnVisibility(int visibility) {
        if (null == mAnnoFloatButton) {
            return;
        }
        mAnnoFloatButton.setVisibility(visibility);
        resetAnnotBtnPosition();
    }

    private void resetAnnotBtnPosition() {
        if (mHandler == null) {
            return;
        }
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mAnnoFloatButton.resetAnnotBtnPosition();
            }
        }, 200);
    }

    private void initHandler() {
        try {
            if (handler == null) {
                handler = new Handler();
            }
        } catch (Exception e) {
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (isAllowAnnot) {
            mHandler.sendEmptyMessage(STOP_SCREEN_SHARE);
        }
    }

    public void finishActivity() {
        if (isAllowAnnot) {
            mHandler.sendEmptyMessage(STOP_SCREEN_SHARE);
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (null != mBarrageLayout) {
                    mBarrageLayout.removeAllViews();
                }
                finish();
            }
        });
    }

    public void dataConfActivityShare(final boolean isShare, final boolean isAllowAnnot) {
        this.isAllowAnnot = isAllowAnnot;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isShare) {
                    mConfShareLayout.setVisibility(View.VISIBLE);
                    mConfShareEmptyLayout.setVisibility(View.GONE);
                } else {
                    mConfShareLayout.setVisibility(View.GONE);
                    mConfShareEmptyLayout.setVisibility(View.VISIBLE);
                }
                if (!isActiveShare) {
                    if (isAllowAnnot) {
                        setAnnotbtnVisibility(View.VISIBLE);
                        mAnnoToolbar.setVisibility(View.GONE);
                    } else {
                        setAnnotbtnVisibility(View.GONE);
                        mAnnoToolbar.setVisibility(View.GONE);
                    }
                }
            }
        });
    }

    public void displayConfChatMag(final boolean isSelf, final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView tvMsg = new TextView(LocContext.getContext());
                if (isSelf) {
                    tvMsg.setTextColor(Color.GREEN);
                } else {
                    tvMsg.setTextColor(Color.BLACK);
                }
                tvMsg.setText(msg);
                tvMsg.setMaxEms(12);
                tvMsg.setTextSize(17);
                tvMsg.setBackgroundResource(R.drawable.conf_msg_bg_normal);
                mBarrageLayout.addView(tvMsg);
                tvMsg.measure(0, 0);
                int width = tvMsg.getMeasuredWidth();
                int height = tvMsg.getMeasuredHeight();
                new BarrageAnimation(tvMsg, mBarrageLayout, width, height);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerBroadcast();

        if (isStartShare) {
            mConfShareLayout.setVisibility(View.VISIBLE);
            mConfShareEmptyLayout.setVisibility(View.GONE);
        } else {
            mConfShareLayout.setVisibility(View.GONE);
            mConfShareEmptyLayout.setVisibility(View.VISIBLE);
        }

        if (isVideo) {
            setVideoContainer(this, mLocalVideoView, mHideVideoView);
        }
        if (!isActiveShare) {
            if (isAllowAnnot) {
                setAnnotbtnVisibility(View.VISIBLE);

            }
        }

        // 第一次启动界面让所有按钮显示5s
        if (isFirstStart) {
//            startTimer();
        }
    }

    /**
     * 这个方法的作用是把触摸事件的分发方法，其返回值代表触摸事件是否被当前 View 处理完成(true/false)。
     *
     * @param ev
     * @return
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        mConfShareLayout.onTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterBroadcast();
//        stopTimer();
        if (null != mBarrageLayout) {
            mBarrageLayout.removeAllViews();
        }
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        if(R.id.conf_share_layout==viewId||R.id.date_conf_rl==viewId){
            // 按钮显示，不执行
            if (isFirstStart) {
                return;
            }
            // 点击动作太多，不执行
            if (isPressTouch) {
                return;
            } else {
                isPressTouch = true;
//                    startTimer();
            }
        }else if( R.id.leave_iv==viewId){
            finish();
        }
    }


    /**********************View-end***************************/

    /**********************Presenter-start***************************/

    private String[] broadcastNames = new String[]{
            CustomBroadcastConstants.DATE_CONFERENCE_START_SHARE_STATUS,
            CustomBroadcastConstants.DATE_CONFERENCE_END_SHARE_STATUS,
            CustomBroadcastConstants.DATE_CONFERENCE_CHAT_MSG,
            CustomBroadcastConstants.GET_CONF_END
    };

    private LocBroadcastReceiver receiver = new LocBroadcastReceiver() {
        @Override
        public void onReceive(String broadcastName, Object obj) {
            switch (broadcastName) {
                case CustomBroadcastConstants.DATE_CONFERENCE_START_SHARE_STATUS:
                    if (obj instanceof TsdkConfAsStateInfo) {
                        TsdkConfAsStateInfo asStartInfo = (TsdkConfAsStateInfo) obj;
                        if (null != asStartInfo) {
                            boolean isAllowAnnot = asStartInfo.getSubState() == TsdkConfShareSubState.TSDK_E_CONF_AS_SUB_STATE_ANNOTATION.getIndex() ? true : false;
                            dataConfActivityShare(true, isAllowAnnot);
                        } else {
                            dataConfActivityShare(true, false);
                        }
                        return;
                    }
                    dataConfActivityShare(true, false);
                    break;

                case CustomBroadcastConstants.DATE_CONFERENCE_END_SHARE_STATUS:
                    if (obj instanceof TsdkConfAsStateInfo) {
                        TsdkConfAsStateInfo asStopInfo = (TsdkConfAsStateInfo) obj;
                        if (null != asStopInfo) {
                            boolean isAllowAnnot = asStopInfo.getSubState() == TsdkConfShareSubState.TSDK_E_CONF_AS_SUB_STATE_ANNOTATION.getIndex() ? true : false;
                            dataConfActivityShare(false, isAllowAnnot);
                        } else {
                            dataConfActivityShare(false, false);
                        }
                    }

                    if (obj instanceof TsdkWbDelDocInfo) {
                        dataConfActivityShare(false, false);
                    }
                    if (obj instanceof TsdkDocShareDelDocInfo) {
                        dataConfActivityShare(false, false);
                    }
                    showCustomToast("分享结束");
                    break;

                case CustomBroadcastConstants.GET_CONF_END:
                    finishActivity();
                    break;

                case CustomBroadcastConstants.DATE_CONFERENCE_CHAT_MSG:
                    TsdkConfChatMsgInfo chatMsgInfo = (TsdkConfChatMsgInfo) obj;
                    String msgInfo = "";
                    String userName = chatMsgInfo.getSenderDisplayName();
                    String userNumber = chatMsgInfo.getSenderNumber();
                    boolean isSelfMsg = false;

                    try {
                        msgInfo = URLDecoder.decode(chatMsgInfo.getChatMsg(), "utf-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }

                    Member self = MeetingMgr.getInstance().getCurrentConferenceSelf();
                    if (null != self) {
                        if (self.getDisplayName().equals(userName) || self.getNumber().equals(userNumber)) {
                            isSelfMsg = true;
                        }
                    }

                    if (null == userName || "".equals(userName)) {
                        if (null == userNumber || "".equals(userNumber)) {
                            displayConfChatMag(isSelfMsg, "The sender's name was not obtained.");
                        } else {
                            displayConfChatMag(isSelfMsg, userNumber + ": " + msgInfo);
                        }
                        return;
                    }
                    displayConfChatMag(isSelfMsg, userName + ": " + msgInfo);
                    break;

                default:
                    break;
            }
        }
    };

    public void attachSurfaceView(ViewGroup container, Context context) {
        MeetingMgr.getInstance().attachSurfaceView(container, context);
    }

    public void sendChatMsg(String content) {
        MeetingMgr.getInstance().sendConfMessage(content);
    }

    public int startAnnotation() {
        return MeetingMgr.getInstance().startAnnotation();
    }

    public void setAnnotationLocalStatus(boolean enable) {
        MeetingMgr.getInstance().setAnnotationLocalStatus(enable);
    }

    public void setConfID(String confID) {
        this.confID = confID;
    }

    public String getSubject() {
        return MeetingMgr.getInstance().getCurrentConferenceBaseInfo().getSubject();
    }

    public void closeConf() {
        int result = MeetingMgr.getInstance().leaveConf();
        if (result != 0) {
            showCustomToast("离开会议失败");
            return;
        }
    }

    public void finishConf() {
        int result = MeetingMgr.getInstance().endConf();
        if (result != 0) {
            showCustomToast("结束会议失败");
            return;
        }
    }

    public boolean muteSelf() {
        Member self = MeetingMgr.getInstance().getCurrentConferenceSelf();

        if (self == null) {
            return false;
        }
        int result = MeetingMgr.getInstance().muteAttendee(self, !self.isMute());
        if (result != 0) {
            return false;
        }
        return true;
    }

    public int switchLoudSpeaker() {
        return CallMgr.getInstance().switchAudioRoute();
    }

    public boolean isChairMan() {
        Member self = MeetingMgr.getInstance().getCurrentConferenceSelf();

        return (self.getRole() == TsdkConfRole.TSDK_E_CONF_ROLE_ATTENDEE ? false : true);
    }

    public void registerBroadcast() {
        LocBroadcast.getInstance().registerBroadcast(receiver, broadcastNames);
    }

    public void unregisterBroadcast() {
        LocBroadcast.getInstance().unRegisterBroadcast(receiver, broadcastNames);
    }

    public SurfaceView getHideVideoView() {
        return VideoMgr.getInstance().getLocalHideView();
    }

    public SurfaceView getLocalVideoView() {
        return VideoMgr.getInstance().getLocalVideoView();
    }


    public void setVideoContainer(Context context, ViewGroup smallLayout, ViewGroup hideLayout) {
        if (smallLayout != null) {
            addSurfaceView(smallLayout, getLocalVideoView());
        }

        if (hideLayout != null) {
            addSurfaceView(hideLayout, getHideVideoView());
        }
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

    private void showCustomToast(final String content) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ToastHelper.showShort(content);
            }
        });
    }

    /**********************Presenter-end***************************/
}
