package com.hw.cloudlibrary.adapter;

import android.support.annotation.NonNull;
import android.view.SurfaceView;
import android.view.ViewGroup;

import com.hw.cloudlibrary.R;
import com.hw.cloudlibrary.utils.ToastHelper;
import com.hw.cloudlibrary.widget.rv.BaseItem;
import com.hw.cloudlibrary.widget.rv.ItemViewHolder;

public class ConfViewItem extends BaseItem<ConfViewItem> {
    private SurfaceView surfaceView;

//    public ConfViewItem(@NonNull View view) {
//        super(view);
//    }

    public ConfViewItem(SurfaceView surfaceView) {
        super(R.layout.item_conf_view);
        this.surfaceView = surfaceView;
    }

    @Override
    public void bind(@NonNull ItemViewHolder holder, int position) {
        ViewGroup container = (ViewGroup) holder.itemView;
        addSurfaceView(container, surfaceView);
        ToastHelper.showShort("position-->"+position);
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

}
