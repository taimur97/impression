package com.afollestad.impression.media;

import android.content.Context;

import com.afollestad.impression.api.MediaEntry;
import com.afollestad.impression.utils.PrefUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CurrentMediaEntriesSingleton {
    private static CurrentMediaEntriesSingleton currentMediaEntries;

    private List<MediaEntry> mMediaEntries;

    private CurrentMediaEntriesSingleton() {
        mMediaEntries = new ArrayList<>();
    }

    public static CurrentMediaEntriesSingleton getInstance() {
        if (currentMediaEntries == null) {
            currentMediaEntries = new CurrentMediaEntriesSingleton();
        }
        return currentMediaEntries;
    }

    public static boolean instanceExists() {
        return currentMediaEntries != null;
    }

    public void set(List<MediaEntry> newItems) {
        mMediaEntries.clear();
        mMediaEntries.addAll(newItems);
    }

    public void remove(MediaEntry entry) {
        mMediaEntries.remove(entry);
    }

    public void removeAll(List<MediaEntry> ids) {
        mMediaEntries.removeAll(ids);
    }

    public List<MediaEntry> getMediaEntriesCopy(Context context, @MediaAdapter.SortMode int sortMode) {
        if (sortMode != MediaAdapter.SORT_NOSORT && mMediaEntries.size() > 0) {
            Collections.sort(mMediaEntries, PrefUtils.getSortComparator(context, sortMode));
        }
        return new ArrayList<>(mMediaEntries);
    }

}
