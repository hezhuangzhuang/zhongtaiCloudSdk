package com.hw.cloudlibrary.widget.floatView.annotation;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.huawei.opensdk.commonservice.common.util.LogUtil;
import com.huawei.opensdk.demoservice.MeetingMgr;
import com.hw.cloudlibrary.R;
import com.hw.cloudlibrary.ecsdk.common.UIConstants;
import com.hw.cloudlibrary.widget.floatView.util.LayoutUtil;


/**
 * 标注悬浮窗
 *
 */
public class AnnotationDrawingView extends LinearLayout {

    private static final String TAG = AnnotationDrawingView.class.getSimpleName();

    private int penColor;

    private FrameLayout drawingLayout;

    public AnnotationDrawingView(final Context context) {
        super(context);
        LogUtil.i(TAG," enter AnnotationDrawingView ");
        LayoutInflater.from(context).inflate(R.layout.annot_drawing_layout, this);
        drawingLayout = (FrameLayout)findViewById(R.id.annot_drawing_view);
        initAnnotDrawingView(context);
        penColor = MeetingMgr.getInstance().getCurrentPenColor();
        int penWidth = (getResources().getDimensionPixelSize(R.dimen.dp6) * 1440 / LayoutUtil.getScreenDensityDpi());
        MeetingMgr.getInstance().setAnnotationPen(penColor, penWidth);
    }

    public void initAnnotDrawingView(Context context) {
        LogUtil.i(TAG," start initAnnotDrawingView");
        MeetingMgr.getInstance().attachSurfaceView(drawingLayout, context);
    }


    public void clearData() {
        try {

            if (drawingLayout != null) {
                drawingLayout.removeAllViews();
            }
        } catch (Exception e) {
            LogUtil.e(UIConstants.DEMO_TAG," clearData error ");
        }
    }

    public void handleAnnotSwitch(int penColor, int penWidth) {
        // TODO 在这里调用高层SDK的方法
        MeetingMgr.getInstance().setAnnotationPen(penColor, penWidth);
    }
}

