package com.hw.cloudlibrary.fragment;

import android.content.Context;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import com.huawei.opensdk.callmgr.VideoMgr;
import com.huawei.opensdk.commonservice.common.localbroadcast.CustomBroadcastConstants;
import com.huawei.opensdk.commonservice.common.localbroadcast.LocBroadcast;
import com.huawei.opensdk.commonservice.common.localbroadcast.LocBroadcastReceiver;
import com.huawei.opensdk.commonservice.common.util.LogUtil;
import com.huawei.opensdk.demoservice.MeetingMgr;
import com.hw.cloudlibrary.R;
import com.hw.cloudlibrary.widget.freesizedraggablelayout.DetailView;
import com.hw.cloudlibrary.widget.freesizedraggablelayout.FreeSizeDraggableLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * 显示会议列表的界面
 */
public class ConfViewFragment extends Fragment {
    public static final String TAG = "ConfViewFragment";
    //下标
    private int index;

    private static final String FILED_INDEX = "FILED_INDEX";

    private OnFragmentInteractionListener mListener;

    private FreeSizeDraggableLayout fsdContent;

    private List<DetailView> detailViews = new ArrayList<>();

    private String[] broadcastNames = new String[]{CustomBroadcastConstants.REFRESH_SMALL_VIEW};


    public ConfViewFragment() {
    }

    public static ConfViewFragment newInstance(int index) {
        ConfViewFragment fragment = new ConfViewFragment();
        Bundle args = new Bundle();
        args.putInt(FILED_INDEX, index);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            index = getArguments().getInt(FILED_INDEX);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_conf_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LocBroadcast.getInstance().registerBroadcast(receiver, broadcastNames);

        fsdContent = view.findViewById(R.id.fsd_content);

        fsdContent.setUnitWidthNum(4);
        fsdContent.setUnitHeightNum(4);

        //设置详情界面
        setDetailViews();
    }

    private void setDetailViews() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    detailViews.clear();
                    fsdContent.removeAllViews();

                    int watchCount = MeetingMgr.getInstance().getCurrentWatchSmallCount() + 1;

                    switch (watchCount) {
                        case 1:
                            detailViews.add(new DetailView(new Point(0, 0), 2, 2, createView(getLocalVideoView())));
                            break;

                        case 2:
                            detailViews.add(new DetailView(new Point(0, 0), 2, 2, createView(getLocalVideoView())));
                            detailViews.add(new DetailView(new Point(2, 0), 2, 2, createView(getRemoteSmallVideoView_01())));
                            break;

                        case 3:
                            detailViews.add(new DetailView(new Point(0, 0), 2, 2, createView(getLocalVideoView())));
                            detailViews.add(new DetailView(new Point(2, 0), 2, 2, createView(getRemoteSmallVideoView_01())));
                            detailViews.add(new DetailView(new Point(0, 2), 2, 2, createView(getRemoteSmallVideoView_02())));
                            break;

                        case 4:
                            detailViews.add(new DetailView(new Point(0, 0), 2, 2, createView(getLocalVideoView())));
                            detailViews.add(new DetailView(new Point(2, 0), 2, 2, createView(getRemoteSmallVideoView_01())));
                            detailViews.add(new DetailView(new Point(0, 2), 2, 2, createView(getRemoteSmallVideoView_02())));
                            detailViews.add(new DetailView(new Point(2, 2), 2, 2, createView(getRemoteSmallVideoView_03())));

                            break;
                    }

                    fsdContent.setList(detailViews);

                    LogUtil.e(TAG, "设置四个画面");
                }
            });
        }
    }

    private void clearDetailViews() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    detailViews.clear();


                    fsdContent.setList(detailViews);
                }
            });
        }
    }

    public SurfaceView getLocalVideoView() {
        return VideoMgr.getInstance().getLocalVideoView();
    }

    public SurfaceView getRemoteSmallVideoView_01() {
        SurfaceView remoteSmallVideoView_01 = VideoMgr.getInstance().getRemoteSmallVideoView_01();
        LogUtil.d(TAG, "remoteSmallVideoView_01是否为空:" + (remoteSmallVideoView_01 == null));
        return VideoMgr.getInstance().getRemoteSmallVideoView_01();
    }

    public SurfaceView getRemoteSmallVideoView_02() {
        SurfaceView remoteSmallVideoView_02 = VideoMgr.getInstance().getRemoteSmallVideoView_02();
        LogUtil.d(TAG, "remoteSmallVideoView_02是否为空:" + (remoteSmallVideoView_02 == null));
        return VideoMgr.getInstance().getRemoteSmallVideoView_02();
    }

    public SurfaceView getRemoteSmallVideoView_03() {
        SurfaceView remoteSmallVideoView_03 = VideoMgr.getInstance().getRemoteSmallVideoView_03();
        LogUtil.d(TAG, "remoteSmallVideoView_03是否为空:" + (remoteSmallVideoView_03 == null));
        return VideoMgr.getInstance().getRemoteSmallVideoView_03();
    }

    private ViewGroup createView(SurfaceView surfaceView) {
        ViewGroup viewGroup = (ViewGroup) View.inflate(getContext(), R.layout.item_conf_view, null);
        addSurfaceView(viewGroup, surfaceView);
        return viewGroup;
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

    private void clearSurfaceView(ViewGroup container) {
        container.removeAllViews();
    }


    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (getUserVisibleHint()) {
            setDetailViews();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        LocBroadcast.getInstance().unRegisterBroadcast(receiver, broadcastNames);
    }

    private LocBroadcastReceiver receiver = new LocBroadcastReceiver() {
        @Override
        public void onReceive(String broadcastName, Object obj) {
            switch (broadcastName) {
                case CustomBroadcastConstants.REFRESH_SMALL_VIEW:
                    setDetailViews();
                    break;
            }
        }
    };


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
