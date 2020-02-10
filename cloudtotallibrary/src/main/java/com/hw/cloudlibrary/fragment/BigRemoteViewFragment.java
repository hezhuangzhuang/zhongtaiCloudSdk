package com.hw.cloudlibrary.fragment;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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

/**
 * 显示大画面
 */
public class BigRemoteViewFragment extends Fragment {
    private FrameLayout flContent;

    public static final String TAG = "BigRemoteViewFragment";

    public BigRemoteViewFragment() {
    }

    private String[] broadcastNames = new String[]{CustomBroadcastConstants.REFRESH_REMOTE_VIEW};

    public static BigRemoteViewFragment newInstance() {
        BigRemoteViewFragment fragment = new BigRemoteViewFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_big_remote_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        flContent = (FrameLayout) view.findViewById(R.id.fl_remote);

        LocBroadcast.getInstance().registerBroadcast(receiver, broadcastNames);

        addSurfaceView(flContent, getLocalVideoView());
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (getUserVisibleHint()) {
            refreshView(isRemote);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        LocBroadcast.getInstance().unRegisterBroadcast(receiver, broadcastNames);
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

    public SurfaceView getLocalVideoView() {
        return VideoMgr.getInstance().getLocalVideoView();
    }

    public SurfaceView getRemoteBigVideoView() {
        SurfaceView remoteBigVideoView = VideoMgr.getInstance().getRemoteBigVideoView();
        LogUtil.d(TAG, "remoteBigVideoView是否为空:" + (remoteBigVideoView == null));
        return remoteBigVideoView;
    }

    //是否是远端
    boolean isRemote = false;

    private LocBroadcastReceiver receiver = new LocBroadcastReceiver() {
        @Override
        public void onReceive(String broadcastName, Object obj) {
            if (CustomBroadcastConstants.REFRESH_REMOTE_VIEW.equals(broadcastName)) {
                isRemote = (boolean) obj;

                LogUtil.d(TAG, "刷新大布局-->" + isRemote);

                refreshView(isRemote);
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
                    addSurfaceView(flContent, isRemote ? getRemoteBigVideoView() : getLocalVideoView());
                }
            });
        }
    }

}
