package com.afollestad.impression.api;

/**
 * @author Aidan Follestad (afollestad)
 */
public class OldLoaderEntry/* implements Serializable */ {
/*
    private String _displayName;
    private String _data;
    private long _size;
    private long _dateModified;
    private long _bucketId;

    private OldLoaderEntry() {
    }

    public static OldLoaderEntry load(Cursor from) {
        OldLoaderEntry a = new OldLoaderEntry();
        a._displayName = from.getString(from.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME));
        a._data = from.getString(from.getColumnIndex(MediaStore.Images.Media.DATA));
        a._size = from.getLong(from.getColumnIndex(MediaStore.Images.Media.SIZE));
        a._dateModified = from.getLong(from.getColumnIndex(MediaStore.Images.Media.DATE_MODIFIED));
        a._bucketId = from.getLong(from.getColumnIndex(MediaStore.Images.Media.BUCKET_ID));
        return a;
    }

    public static OldLoaderEntry load(File from) {
        OldLoaderEntry a = new OldLoaderEntry();
        a._displayName = from.getName();
        a._data = from.getAbsolutePath();
        a._size = from.length();
        a._dateModified = from.lastModified();
        a._bucketId = -1;
        return a;
    }

    private String displayName() {
        return _displayName;
    }

    public String data() {
        return _data;
    }

    public long size() {
        return _size;
    }

    private long dateModified() {
        return _dateModified;
    }

    public long bucketId() {
        return _bucketId;
    }

    public String parent() {
        return new File(data()).getParent();
    }

    public static class Sorter implements Comparator<OldLoaderEntry> {

        private final MediaAdapter.SortMode mSort;

        public Sorter(@MediaAdapter.SortMode int getSortQueryFromSortMode) {
            mSort = getSortQueryFromSortMode;
        }

        @Override
        public int compare(OldLoaderEntry lhs, OldLoaderEntry rhs) {
            String leftName;
            String rightName;
            Long leftTime;
            Long rightTime;

            if (lhs != null) {
                leftName = lhs.displayName();
                leftTime = lhs.dateModified();
            } else {
                leftName = "";
                leftTime = 0l;
            }
            if (rhs != null) {
                rightName = rhs.displayName();
                rightTime = rhs.dateModified();
            } else {
                rightName = "";
                rightTime = 0l;
            }

            if (leftName == null) leftName = "";
            if (rightName == null) rightName = "";

            switch (mSort) {
                default:
                    return rightName.compareTo(leftName);
                case SORT_NAME_ASC:
                    return leftName.compareTo(rightName);
                case MODIFIED_DATE_DESC:
                    return leftTime.compareTo(rightTime);
                case MODIFIED_DATE_ASC:
                    return rightTime.compareTo(leftTime);
            }
        }
    }*/
}