package com.afollestad.impression.media;

import com.afollestad.impression.api.MediaEntry;

import java.util.Comparator;

public class MediaNameSorter implements Comparator<MediaEntry> {

    private final boolean desc;

    public MediaNameSorter(boolean desc) {
        this.desc = desc;
    }

    @Override
    public int compare(MediaEntry lhs, MediaEntry rhs) {
        String right = rhs.displayName();
        String left = lhs.displayName();
        if (right == null) right = "";
        if (left == null) left = "";
        if (desc) {
            return right.compareTo(left);
        } else {
            return left.compareTo(right);
        }
    }
}
