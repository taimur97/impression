package com.afollestad.impression.viewer;

import android.app.Fragment;
import android.app.FragmentManager;

import com.afollestad.impression.api.base.MediaEntry;
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

    private int translateToGridIndex(int local) {
        return mMedia.get(local).realIndex();
    }

    @Override
    public Fragment getItem(int position) {
        int gridPosition = translateToGridIndex(position);
        final ViewerPagerFragment viewerPagerFragment = ViewerPagerFragment.create(mMedia.get(position), gridPosition, mThumbWidth, mThumbHeight)
                .setIsCurrent(mInitialCurrent == position);
        mInitialCurrent = -1;
        return viewerPagerFragment;
    }

    @Override
    public int getCount() {
        return mMedia.size();
    }
}