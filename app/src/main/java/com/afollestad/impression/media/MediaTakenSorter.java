package com.afollestad.impression.media;

import com.afollestad.impression.api.MediaEntry;

import java.util.Comparator;

public class MediaTakenSorter implements Comparator<MediaEntry> {

    private final boolean asc;

    public MediaTakenSorter(boolean asc) {
        this.asc = asc;
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

        if (asc) {
            return left.compareTo(right);
        } else {
            return right.compareTo(left);
        }
    }
}
