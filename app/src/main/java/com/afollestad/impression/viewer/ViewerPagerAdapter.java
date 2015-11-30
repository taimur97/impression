package com.afollestad.impression.viewer;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v4.view.PagerAdapter;

import com.afollestad.impression.api.MediaEntry;
import com.afollestad.impression.utils.FragmentStatePagerAdapter;

import java.util.List;

/**
 * @author Aidan Follestad (afollestad)
 */
public class ViewerPagerAdapter extends FragmentStatePagerAdapter {

    private final List<MediaEntry> mMedia;
    private int mThumbWidth;
    private int mThumbHeight;

    private int mInitialCurrent;

    public ViewerPagerAdapter(FragmentManager fm, List<MediaEntry> media, int width, int height, int initialCurrent) {
        super(fm);
        mMedia = media;
        mThumbWidth = width;
        mThumbHeight = height;
        mInitialCurrent = initialCurrent;
    }

    public void add(MediaEntry p) {
        mMedia.add(0, p);
        notifyDataSetChanged();
    }

    public void remove(int index) {
        mMedia.remove(index);
        notifyDataSetChanged();
    }

    /*private int translateToGridIndex(int local) {
        return mMedia.get(local).realIndex();
    }*/

    @Override
    public int getItemPosition(Object object) {
        return PagerAdapter.POSITION_NONE;
    }

    @Override
    public Fragment getItem(int position) {
        final ViewerPagerFragment viewerPagerFragment = ViewerPagerFragment.create(mMedia.get(position), position, mThumbWidth, mThumbHeight);
        final boolean active = mInitialCurrent == position;
        if (active) {
            viewerPagerFragment.setIsActive(true);
            mInitialCurrent = -1;
        }
        return viewerPagerFragment;
    }

    @Override
    public int getCount() {
        return mMedia.size();
    }
}