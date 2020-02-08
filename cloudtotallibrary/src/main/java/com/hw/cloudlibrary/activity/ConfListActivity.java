package com.hw.cloudlibrary.activity;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.huawei.opensdk.commonservice.common.localbroadcast.CustomBroadcastConstants;
import com.huawei.opensdk.commonservice.common.localbroadcast.LocBroadcast;
import com.huawei.opensdk.commonservice.common.localbroadcast.LocBroadcastReceiver;
import com.huawei.opensdk.commonservice.common.util.LogUtil;
import com.huawei.opensdk.demoservice.ConfBaseInfo;
import com.huawei.opensdk.demoservice.ConfConstant;
import com.huawei.opensdk.demoservice.MeetingMgr;
import com.hw.cloudlibrary.R;
import com.hw.cloudlibrary.adapter.ConfItem;
import com.hw.cloudlibrary.inter.HuaweiCallImp;
import com.hw.cloudlibrary.inter.HuaweiLoginImp;
import com.hw.cloudlibrary.utils.DateUtil;
import com.hw.cloudlibrary.utils.ToastHelper;
import com.hw.cloudlibrary.widget.rv.BaseItem;
import com.hw.cloudlibrary.widget.rv.BaseItemAdapter;
import com.hw.cloudlibrary.widget.rv.ItemViewHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 会议列表
 */
public class ConfListActivity extends BaseLibActivity {
    private ImageView ivBack;
    private RecyclerView rvList;

    private BaseItemAdapter<ConfItem> baseItemBaseItemAdapter;

    //会议列表
    private List<ConfBaseInfo> confList = new ArrayList<>();

    /**
     *
     */
    public static void startActivity(Context context) {
        Intent intent = new Intent(context, ConfListActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void findViews() {
        ivBack = (ImageView) findViewById(R.id.iv_back);
        rvList = (RecyclerView) findViewById(R.id.rv_list);
    }

    @Override
    protected void initData() {
        LocBroadcast.getInstance().registerBroadcast(receiver, broadcastNames);

        baseItemBaseItemAdapter = new BaseItemAdapter<ConfItem>(false);
        baseItemBaseItemAdapter.setItems(getConfList());
        rvList.setAdapter(baseItemBaseItemAdapter);
        rvList.setLayoutManager(new LinearLayoutManager(this));
    }

    public List<ConfItem> getConfList() {
        return new ArrayList<>();
    }

    @Override
    protected void setListener() {
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_conf_list;
    }

    @Override
    protected void onResume() {
        super.onResume();

        MeetingMgr.getInstance().queryMyConfList(ConfConstant.ConfRight.MY_CREATE_AND_JOIN);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        LocBroadcast.getInstance().unRegisterBroadcast(receiver, broadcastNames);
    }

    private String[] broadcastNames = new String[]{
            CustomBroadcastConstants.GET_CONF_LIST_RESULT
    };

    private LocBroadcastReceiver receiver = new LocBroadcastReceiver() {
        @Override
        public void onReceive(String broadcastName, Object obj) {
            LogUtil.d("ConfListActivity", broadcastName + ",obj->" + obj);
            switch (broadcastName) {
                case CustomBroadcastConstants.GET_CONF_LIST_RESULT:
                    if (obj != null) {
                        confList = (List<ConfBaseInfo>) obj;
                        Collections.sort(confList, getComparator());
                        final List<ConfItem> newConfBeans = new ArrayList<>();
                        ConfItem confItem = null;
                        for (ConfBaseInfo info : confList) {
                            LogUtil.d(info.toString());
                            confItem = new ConfItem(info);
                            confItem.setOnItemClickListener(itemClickListener);
                            newConfBeans.add(confItem);
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //刷新界面
                                baseItemBaseItemAdapter.replaceItems(newConfBeans, true);
                            }
                        });
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private BaseItem.OnItemClickListener<ConfItem> itemClickListener = new BaseItem.OnItemClickListener<ConfItem>() {
        @Override
        public void onItemClick(ItemViewHolder holder, ConfItem item, int position) {
            HuaweiCallImp.getInstance().joinConf(
                    item.confBaseInfo.getConfID(),
                    item.confBaseInfo.getGuestPwd(),
                    item.confBaseInfo.getAccessNumber());
        }
    };

    @NonNull
    private Comparator<ConfBaseInfo> getComparator() {
        return new Comparator<ConfBaseInfo>() {
            @Override
            public int compare(ConfBaseInfo lhs, ConfBaseInfo rhs) {
                long time1 = DateUtil.parseDateStr(lhs.getStartTime(), DateUtil.UTC, DateUtil.FORMAT_DATE_TIME).getTime();
                long time2 = DateUtil.parseDateStr(rhs.getStartTime(), DateUtil.UTC, DateUtil.FORMAT_DATE_TIME).getTime();
                if (time1 < time2) {
                    return 1;
                }
                return -1;
            }
        };
    }
}
