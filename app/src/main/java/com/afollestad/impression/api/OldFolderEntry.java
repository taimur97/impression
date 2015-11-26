package com.afollestad.impression.api;

/**
 * @author Aidan Follestad (afollestad)
 */
public class OldFolderEntry /*implements MediaEntry<OldFolderEntry> */ {
/*
    private final File mFile;
    private int mRealIndex;

    public OldFolderEntry(File file) {
        mFile = file;
    }

    public static String getSortQueryFromSortMode(@MediaAdapter.SortMode int from) {
        switch (from) {
            default:
                return "path ASC";
            case SORT_NAME_DESC:
                return "path DESC";
        }
    }

    @Override
    public int realIndex() {
        return mRealIndex;
    }

    @Override
    public void setRealIndex(int index) {
        mRealIndex = index;
    }

    @Override
    public long id() {
        return -1;
    }

    @Override
    public String data() {
        return mFile.getAbsolutePath();
    }

    @Override
    public String title() {
        return mFile.getName();
    }

    @Override
    public long size() {
        return -1;
    }

    @Override
    //TODO: Internal storage, root, etc.
    public String displayName() {
        return mFile.getName();
    }

    @Override
    public String mimeType() {
        return Utils.getMimeType(Utils.getExtension(mFile.getName()));
    }

    @Override
    public long dateAdded() {
        return mFile.lastModified();
    }

    @Override
    public long dateModified() {
        return mFile.lastModified();
    }

    @Override
    public long dateTaken() {
        return -1;
    }

    @Override
    public String bucketDisplayName() {
        return mFile.getParentFile().getName();
    }

    @Override
    public long bucketId() {
        return -1;
    }

    @Override
    public int width() {
        return -1;
    }

    @Override
    public int height() {
        return -1;
    }

    @Override
    public boolean isVideo() {
        return false;
    }

    @Override
    public boolean isFolder() {
        return true;
    }

    @Override
    public boolean isAlbum() {
        return false;
    }

    @Override
    public void delete(Activity context) {
        Utils.deleteFolder(mFile);
    }

    @Override
    public OldFolderEntry load(File from) {
        return null;
    }

    @Override
    public OldFolderEntry load(Cursor from) {
        return null;
    }

    @Override
    public String[] projection() {
        return null;
    }*/
}
