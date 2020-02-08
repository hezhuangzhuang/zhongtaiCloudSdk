package com.hw.cloudlibrary.adapter;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.huawei.opensdk.demoservice.ConfBaseInfo;
import com.huawei.opensdk.demoservice.ConfConstant;
import com.hw.cloudlibrary.R;
import com.hw.cloudlibrary.widget.rv.BaseItem;
import com.hw.cloudlibrary.widget.rv.ItemViewHolder;

/**
 *
 */
public class ConfItem extends BaseItem<ConfItem> {
    public ConfBaseInfo confBaseInfo;

    public ConfItem(ConfBaseInfo confBaseInfo) {
        super(R.layout.item_conf);
        this.confBaseInfo = confBaseInfo;
    }

    @Override
    public void bind(@NonNull ItemViewHolder holder, int position) {
        //会议正在进行中
        boolean isGoing = confBaseInfo.getConfState() == ConfConstant.ConfConveneStatus.GOING;

        ImageView ivConfStatus = (ImageView) holder.findViewById(R.id.iv_conf_status);
        TextView tvJoin = (TextView) holder.findViewById(R.id.tv_join);
        TextView tvConfName = (TextView) holder.findViewById(R.id.tv_conf_name);
        TextView tvSchedulerName = (TextView) holder.findViewById(R.id.tv_scheduler_name);

        tvConfName.setText(confBaseInfo.getSubject());
        tvSchedulerName.setText("创建人:" + confBaseInfo.getSchedulerName());
        tvJoin.setVisibility(isGoing ? View.VISIBLE : View.GONE);
        ivConfStatus.setImageResource(isGoing ? R.mipmap.ic_conf_status_going : R.mipmap.ic_conf_status_more);
    }
}
