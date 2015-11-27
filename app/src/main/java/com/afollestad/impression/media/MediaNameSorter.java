package com.afollestad.impression.media;

import android.content.Context;

import com.afollestad.impression.api.MediaEntry;

import java.util.Comparator;

public class MediaNameSorter implements Comparator<MediaEntry> {

    private final Context context;
    private final boolean desc;

    public MediaNameSorter(Context context, boolean desc) {
        this.context = context;
        this.desc = desc;
    }

    @Override
    public int compare(MediaEntry lhs, MediaEntry rhs) {
        String right = rhs.displayName(context);
        String left = lhs.displayName(context);
        if (right == null) {
            right = "";
        }
        if (left == null) {
            left = "";
        }
        if (desc) {
            return right.compareTo(left);
        } else {
            return left.compareTo(right);
        }
    }
}
