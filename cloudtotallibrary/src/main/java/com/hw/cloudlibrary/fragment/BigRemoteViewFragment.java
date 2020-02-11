package com.hw.cloudlibrary.fragment;


import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.huawei.opensdk.callmgr.VideoMgr;
import com.huawei.opensdk.commonservice.common.localbroadcast.CustomBroadcastConstants;
import com.huawei.opensdk.commonservice.common.localbroadcast.LocBroadcast;
import com.huawei.opensdk.commonservice.common.localbroadcast.LocBroadcastReceiver;
import com.huawei.opensdk.commonservice.common.util.LogUtil;
import com.hw.cloudlibrary.R;
import com.hw.cloudlibrary.utils.DensityUtil;
import com.hw.cloudlibrary.utils.ToastHelper;

/**
 * 显示大画面
 */
public class BigRemoteViewFragment extends BaseLazyFragment {
    private FrameLayout flContent;

    public static final String TAG = "BigRemoteViewFragment";

    public BigRemoteViewFragment() {
    }

    private String[] broadcastNames = new String[]{
            CustomBroadcastConstants.REFRESH_REMOTE_VIEW,
            CustomBroadcastConstants.CONF_STATE_UPDATE
    };

    public static BigRemoteViewFragment newInstance() {
        BigRemoteViewFragment fragment = new BigRemoteViewFragment();
        return fragment;
    }

    @Override
    protected View inflateContentView(LayoutInflater inflater, ViewGroup container) {
        //设置强制更新
        setForceLoad(true);

        LocBroadcast.getInstance().registerBroadcast(receiver, broadcastNames);

        return inflater.inflate(R.layout.fragment_big_remote_view, container, false);
    }

    @Override
    protected void findViews(View view) {
        flContent = (FrameLayout) view.findViewById(R.id.fl_remote);
    }

    @Override
    protected void initData() {
        refreshView(isShowRemote);
    }

    @Override
    protected void addListeners() {

    }

    /**
     * 用户不可见
     */
    @Override
    protected void onInvisible() {
        super.onInvisible();
        if (null != flContent) {
            flContent.removeAllViews();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        flContent.removeAllViews();

        LocBroadcast.getInstance().unRegisterBroadcast(receiver, broadcastNames);
    }

    /**
     * 添加SurfaceView到布局中
     *
     * @param container
     * @param child
     */
    private void addSurfaceView(ViewGroup container, SurfaceView child) {
        container.removeAllViews();

        if (child == null) {
            return;
        }

        if (child.getParent() != null) {
            ViewGroup vGroup = (ViewGroup) child.getParent();
            vGroup.removeAllViews();
        }

        container.addView(child);
    }

    /**
     * 获取本地画面
     *
     * @return
     */
    public SurfaceView getLocalVideoView() {
        SurfaceView localVideoView = VideoMgr.getInstance().getLocalVideoView();
        if (null != localVideoView) {
            ViewGroup.LayoutParams layoutParams = localVideoView.getLayoutParams();
            if(null!=layoutParams){
                layoutParams.width = DensityUtil.getScreenWidth(getContext());
                layoutParams.height = DensityUtil.getScreenHeight(getContext());
                localVideoView.setLayoutParams(layoutParams);
            }
        }
        return localVideoView;
    }

    /**
     * 获取远端画面
     *
     * @return
     */
    public SurfaceView getRemoteBigVideoView() {
        SurfaceView remoteBigVideoView = VideoMgr.getInstance().getRemoteBigVideoView();
        LogUtil.d(TAG, "remoteBigVideoView是否为空:" + (remoteBigVideoView == null));
        return remoteBigVideoView;
    }

    //是否显示远端画面
    boolean isShowRemote = false;

    private LocBroadcastReceiver receiver = new LocBroadcastReceiver() {
        @Override
        public void onReceive(String broadcastName, Object obj) {
            switch (broadcastName) {
                //刷新大画面
                case CustomBroadcastConstants.REFRESH_REMOTE_VIEW:
                    isShowRemote = (boolean) obj;

                    LogUtil.d(TAG, "刷新大布局-->" + isShowRemote);

                    refreshView(isShowRemote);
                    break;

                case CustomBroadcastConstants.CONF_STATE_UPDATE:
                    LogUtil.d(TAG, "会议更新-->" + isShowRemote);
                    break;
            }
        }
    };

    /**
     * 刷新界面
     *
     * @param isRemote
     */
    private void refreshView(final boolean isRemote) {
        if (null != getActivity()) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String content = isRemote ? "远端" : "本端";
                    LogUtil.d(TAG, "获取" + content + "画面");
                    ToastHelper.showShort("获取" + content + "画面");
                    addSurfaceView(flContent, isRemote ? getRemoteBigVideoView() : getLocalVideoView());
                }
            });
        }
    }

}
