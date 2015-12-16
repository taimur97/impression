package com.afollestad.impression.viewer;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.support.v4.view.PagerAdapter;

import com.afollestad.impression.api.MediaEntry;
import com.afollestad.impression.media.CurrentMediaEntriesSingleton;
import com.afollestad.impression.media.MediaAdapter;
import com.afollestad.impression.utils.FragmentStatePagerAdapter;

import java.util.List;

public class ViewerPagerAdapter extends FragmentStatePagerAdapter {

    private final Context mContext;
    private final
    @MediaAdapter.SortMode
    int mSortMode;
    private List<MediaEntry> mMedia;
    private int mThumbWidth;
    private int mThumbHeight;

    private int mInitialCurrent;

    public ViewerPagerAdapter(Context context, FragmentManager fm, int width, int height, int initialCurrent, @MediaAdapter.SortMode int sortMode) {
        super(fm);
        mContext = context;
        mThumbWidth = width;
        mThumbHeight = height;
        mInitialCurrent = initialCurrent;
        mSortMode = sortMode;

        mMedia = CurrentMediaEntriesSingleton.getInstance().getMediaEntriesCopy(mContext, mSortMode);
    }

    protected List<MediaEntry> getEntries() {
        return mMedia;
    }

    public void updateEntries() {
        mMedia = CurrentMediaEntriesSingleton.getInstance().getMediaEntriesCopy(mContext, mSortMode);
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