package com.hw.cloudlibrary.adapter;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.huawei.opensdk.commonservice.common.common.LocContext;
import com.huawei.opensdk.demoservice.ConfConstant;
import com.huawei.opensdk.demoservice.Member;
import com.hw.cloudlibrary.R;
import com.hw.cloudlibrary.widget.rv.BaseItem;
import com.hw.cloudlibrary.widget.rv.ItemViewHolder;

/**
 * 会控的适配器
 */
public class ConfControlItem extends BaseItem<ConfControlItem> {
    public Member member;

    public ConfControlItem(Member member) {
        super(R.layout.item_conf_control);
        this.member = member;
    }

    @Override
    public void bind(@NonNull ItemViewHolder itemViewHolder, final int position) {
        //是否在会议中
        final boolean inConf = member.getStatus() == ConfConstant.ParticipantStatus.IN_CONF;

        TextView tvSiteName = (TextView) itemViewHolder.findViewById(R.id.tv_site_name);

        ImageView ivHangupSite = (ImageView) itemViewHolder.findViewById(R.id.iv_hangup_site);
        ImageView ivLouderSite = (ImageView) itemViewHolder.findViewById(R.id.iv_louder_site);
        ImageView ivBroadcastSite = (ImageView) itemViewHolder.findViewById(R.id.iv_broadcast_site);
        ImageView ivWatchSite = (ImageView) itemViewHolder.findViewById(R.id.iv_watch_site);


        tvSiteName.setText(member.getDisplayName());
        tvSiteName.setTextColor(inConf ? LocContext.getColor(R.color.white) : LocContext.getColor(R.color.color_999));
        ivHangupSite.setImageResource(inConf ? R.mipmap.ic_hangup : R.mipmap.ic_call);

        ivBroadcastSite.setImageResource(member.isBroadcastSelf() ? R.mipmap.ic_control_broadcast_true : R.mipmap.ic_control_broadcast_false);

        //挂断
        itemViewHolder.setOnClickListener(R.id.iv_hangup_site, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != onControlItemClickListener) {
                    if (inConf) {
                        onControlItemClickListener.onHangUpSite(member, position);
                    } else {
                        onControlItemClickListener.onCallSite(member, position);
                    }
                }
            }
        });

        //在会议中
        if (inConf) {
            //开关扬声器
            itemViewHolder.setOnClickListener(R.id.iv_louder_site, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != onControlItemClickListener) {
                        onControlItemClickListener.onLoduerSite(member, position);
                    }
                }
            });

            //广播会场
            itemViewHolder.setOnClickListener(R.id.iv_broadcast_site, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != onControlItemClickListener) {
                        onControlItemClickListener.onBroadcastSite(member, position);
                    }
                }
            });

            //观看会场
            itemViewHolder.setOnClickListener(R.id.iv_watch_site, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != onControlItemClickListener) {
                        onControlItemClickListener.onWatchSite(member, position);
                    }
                }
            });
        }
    }

    public interface onControlItemClickListener {
        void onHangUpSite(Member member, int position);

        void onCallSite(Member member, int position);

        void onLoduerSite(Member member, int position);

        void onBroadcastSite(Member member, int position);

        void onWatchSite(Member member, int position);
    }

    public onControlItemClickListener onControlItemClickListener;

    public void setOnControlItemClickListener(ConfControlItem.onControlItemClickListener onControlItemClickListener) {
        this.onControlItemClickListener = onControlItemClickListener;
    }
}
