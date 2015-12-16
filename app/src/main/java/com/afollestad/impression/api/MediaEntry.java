package com.afollestad.impression.api;

import android.app.Activity;
import android.content.Context;

import java.io.Serializable;

public interface MediaEntry extends Serializable {

    long id();

    String data();

    long size();

    /*String title();*/

    String displayName(Context context);

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

    void delete(Activity context);
}
