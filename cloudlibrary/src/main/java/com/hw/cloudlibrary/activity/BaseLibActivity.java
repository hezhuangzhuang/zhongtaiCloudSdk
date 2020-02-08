package com.hw.cloudlibrary.activity;

import android.app.KeyguardManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import com.hw.cloudlibrary.ecsdk.utils.ActivityStack;

/**
 * author：Thinkpad
 * data:2017/11/15 14:56
 */

public abstract class BaseLibActivity extends AppCompatActivity {
    /**
     * 初始化view
     */
    protected abstract void findViews();

    /**
     * 初始化view的数据
     */
    protected abstract void initData();

    /**
     * 设置view的监听事件
     */
    protected abstract void setListener();

    /**
     * 获得布局layout id
     *
     * @return
     */
    protected abstract int getLayoutId();


    protected void showWhenLocked() {
        //TODO：在锁屏时弹出界面
        KeyguardManager km =
                (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        boolean showingLocked = km.inKeyguardRestrictedInputMode();
        //是否有锁屏
        if (showingLocked) {
            getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                            | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        }

    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showWhenLocked();
        // 设置点亮屏幕
        setContentView(getLayoutId());

        //添加Activity到管理栈中
        ActivityStack.getIns().push(this);
        findViews();
        initData();
        setListener();
    }

    @Override
    protected void onDestroy() {
        // 结束Activity&从堆栈中移除
        ActivityStack.getIns().popup(this);
        super.onDestroy();
    }

    protected void showProgressDialog(String message) {
//        DialogUtils.showProgressDialog(this, message);
    }

    protected void dismissProgressDialog() {
//        DialogUtils.dismissProgressDialog(this);
    }
}
