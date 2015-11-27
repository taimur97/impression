package com.afollestad.impression.media;

import com.afollestad.impression.api.MediaEntry;

import java.util.Comparator;

public class MediaModifiedSorter implements Comparator<MediaEntry> {

    private final boolean desc;

    public MediaModifiedSorter(boolean desc) {
        this.desc = desc;
    }

    @Override
    public int compare(MediaEntry lhs, MediaEntry rhs) {
        Long right;
        Long left;
        if (rhs != null) {
            right = rhs.dateTaken();
        } else {
            right = 0L;
        }
        if (lhs != null) {
            left = lhs.dateTaken();
        } else {
            left = 0L;
        }

        if (desc) {
            return left.compareTo(right);
        } else {
            return right.compareTo(left);
        }
    }
}
