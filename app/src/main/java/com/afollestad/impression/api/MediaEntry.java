package com.afollestad.impression.api;

import android.os.Parcelable;

public interface MediaEntry extends Parcelable /*extends CursorItem<T>, Serializable*/ {

    /*int realIndex();

    void setRealIndex(int index);*/

    //TODO: What uses this?
    long id();

    String data();

    long size();

    /*String title();*/

    String displayName();

    String mimeType();

    /*long dateAdded();*/

    /*long dateModified();*/

    long dateTaken();

    String bucketDisplayName();

    String bucketId();

    int width();

    int height();

    boolean isVideo();

    boolean isFolder();

    //boolean isExplorerFolder();

    /*boolean isFolder();*/
    /*void delete(Activity context);*/
}
