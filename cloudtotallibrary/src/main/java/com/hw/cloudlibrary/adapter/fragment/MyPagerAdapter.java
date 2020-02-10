package com.hw.cloudlibrary.adapter.fragment;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class MyPagerAdapter extends FragmentPagerAdapter {

    private List<Fragment> mFragmentList = new ArrayList<>();
    private List<Integer> mItemIdList = new ArrayList<>();
    private int id = 0;
    private FragmentManager mFm;

    public MyPagerAdapter(FragmentManager fm, @NonNull List<Fragment> fragmentList) {
        super(fm);
        this.mFm = fm;
        for (Fragment fragment : fragmentList) {
            this.mFragmentList.add(fragment);
            mItemIdList.add(id++);
        }

    }

    public MyPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public List<Fragment> getFragmentList() {
        return mFragmentList;
    }

    public void addPage(int index, Fragment fragment) {
        mFragmentList.add(index, fragment);
        mItemIdList.add(index, id++);
        notifyDataSetChanged();
    }

    public void addPage(Fragment fragment) {
        mFragmentList.add(fragment);
        mItemIdList.add(id++);
        notifyDataSetChanged();
    }

    public void delPage(int index) {
        mFragmentList.remove(index);
        mItemIdList.remove(index);
        notifyDataSetChanged();
    }

    public void delPage() {
        mFragmentList.remove(mFragmentList.size()-1);
        mItemIdList.remove(mFragmentList.size()-1);
        notifyDataSetChanged();
    }

    public void updatePage(List<Fragment> fragmentList) {
        mFragmentList.clear();
        mItemIdList.clear();

        for (int i = 0; i < fragmentList.size(); i++) {
            mFragmentList.add(fragmentList.get(i));
            mItemIdList.add(id++);//注意这里是id++，不是i++。
        }
        notifyDataSetChanged();
    }

    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    /**
     * 返回值有三种，
     * POSITION_UNCHANGED  默认值，位置没有改变
     * POSITION_NONE       item已经不存在
     * position            item新的位置
     * 当position发生改变时这个方法应该返回改变后的位置，以便页面刷新。
     */
    @Override
    public int getItemPosition(Object object) {
        if (object instanceof Fragment) {

            if (mFragmentList.contains(object)) {
                return mFragmentList.indexOf(object);
            } else {
                return POSITION_NONE;
            }

        }
        return super.getItemPosition(object);
    }

    @Override
    public long getItemId(int position) {
        return mItemIdList.get(position);
    }

}
