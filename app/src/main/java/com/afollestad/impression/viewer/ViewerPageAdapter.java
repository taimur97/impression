package com.afollestad.impression.viewer;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.view.ViewGroup;

import com.afollestad.impression.api.base.MediaEntry;

import java.util.List;

/**
 * @author Aidan Follestad (afollestad)
 */
public class ViewerPageAdapter extends FragmentStatePagerAdapter {

    private final List<MediaEntry> mMedia;
    public int mCurrentPage;
    private int mCurrentPageWidth;
    private int mCurrentPageHeight;
    private ViewerPageFragment mCurrentFragment;

    public ViewerPageAdapter(FragmentManager fm, List<MediaEntry> media, int width, int height, int initialOffset) {
        super(fm);
        mMedia = media;
        mCurrentPageWidth = width;
        mCurrentPageHeight = height;
        mCurrentPage = initialOffset;
    }

    public void add(MediaEntry p) {
        mMedia.add(0, p);
        notifyDataSetChanged();
    }

    public void remove(int index) {
        mMedia.remove(index);
        notifyDataSetChanged();
    }

    private int translateToGridIndex(int local) {
        return mMedia.get(local).realIndex();
    }

    @Override
    public Fragment getItem(int position) {
        int width = ViewerPageFragment.INIT_DIMEN_NONE;
        int height = ViewerPageFragment.INIT_DIMEN_NONE;
        if (mCurrentPage == position) {
            width = mCurrentPageWidth;
            height = mCurrentPageHeight;
            mCurrentPageWidth = ViewerPageFragment.INIT_DIMEN_NONE;
            mCurrentPageHeight = ViewerPageFragment.INIT_DIMEN_NONE;
        }
        int gridPosition = translateToGridIndex(position);
        return ViewerPageFragment.create(mMedia.get(position), gridPosition, width, height)
                .setIsActive(mCurrentPage == position);
    }

    @Override
    public int getCount() {
        return mMedia.size();
    }

    @Override
    public int getItemPosition(Object object) {
        return PagerAdapter.POSITION_NONE;
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        super.setPrimaryItem(container, position, object);
        mCurrentFragment = (ViewerPageFragment) object;
    }

    public ViewerPageFragment getCurrentDetailsFragment() {
        return mCurrentFragment;
    }
}