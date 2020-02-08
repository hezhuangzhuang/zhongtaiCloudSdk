package com.hw.cloudlibrary.activity;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.huawei.opensdk.callmgr.CallMgr;
import com.huawei.opensdk.demoservice.MeetingMgr;
import com.hw.cloudlibrary.R;
import com.hw.cloudlibrary.ecsdk.common.UIConstants;
import com.hw.cloudlibrary.ecsdk.utils.ActivityStack;
import com.hw.cloudlibrary.utils.sharedpreferences.PreferencesHelper;

/**
 * 来电显示界面
 */
public class CallerIDActivity extends BaseMediaActivity implements View.OnClickListener {
    private ImageView ivAvatar;
    private TextView tvNumber;
    private TextView tvHangUp;
    private TextView tvAnswer;

    @Override
    protected void findViews() {
        ivAvatar = (ImageView) findViewById(R.id.iv_avatar);
        tvNumber = (TextView) findViewById(R.id.tv_number);
        tvHangUp = (TextView) findViewById(R.id.tv_hang_up);
        tvAnswer = (TextView) findViewById(R.id.tv_answer);

        //是否移动端创建会议填false
        PreferencesHelper.saveData(UIConstants.IS_CREATE, false);
    }

    @Override
    protected void setListener() {
        tvHangUp.setOnClickListener(this);
        tvAnswer.setOnClickListener(this);

        tvNumber.setText(TextUtils.isEmpty(String.valueOf(mCallNumber)) ? "" : String.valueOf(mCallNumber));
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_caller_id;
    }

    @Override
    public void onClick(View v) {
        if (R.id.tv_hang_up == v.getId()) {
            hangUp();
        } else if (R.id.tv_answer == v.getId()) {
            answer();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * 接听
     */
    private void answer() {
//        CallMgr.getInstance().answerCall(mCallID, mIsVideoCall);

        if (0 == mConfToCallHandle) {
            CallMgr.getInstance().answerCall(mCallID, mIsVideoCall);
        } else {
            CallMgr.getInstance().stopPlayRingingTone();
            CallMgr.getInstance().stopPlayRingBackTone();

            MeetingMgr.getInstance().acceptConf(mIsVideoCall);
            finish();
        }
    }

    /**
     * 挂断
     */
    private void hangUp() {
        //结束掉等待的对话框
//        ActivityStack.getIns().finishActivity(LoadingActivity.class);
        ActivityStack.getIns().popup(LoadingActivity.class);

        if (0 == mConfToCallHandle) {
            CallMgr.getInstance().endCall(mCallID);
        } else {
            CallMgr.getInstance().stopPlayRingingTone();
            CallMgr.getInstance().stopPlayRingBackTone();

            //TsdkConference tsdkConference = TsdkManager.getInstance().getConferenceManager().getConferenceByConfHandle(mConfToCallHandle);
            MeetingMgr.getInstance().rejectConf();
        }

//        CallMgr.getInstance().endCall(mCallID);
        CallMgr.getInstance().stopPlayRingBackTone();
        CallMgr.getInstance().stopPlayRingingTone();
        finish();
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        hangUp();
    }
}
