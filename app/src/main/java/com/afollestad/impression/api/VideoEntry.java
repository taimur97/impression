package com.afollestad.impression.api;

import android.content.Context;
import android.provider.MediaStore;

import com.afollestad.inquiry.annotations.Column;

/**
 * @author Aidan Follestad (afollestad)
 */
//TODO
public class VideoEntry extends PhotoEntry {

    /*public String originalUri;*/

    @Column(name = MediaStore.Video.Media._ID)
    protected long _id;
    @Column(name = MediaStore.Video.Media.DATA)
    protected String _data;
    @Column(name = MediaStore.Video.Media.SIZE)
    protected long _size;
    @Column(name = MediaStore.Video.Media.TITLE)
    protected String title;
    @Column(name = MediaStore.Video.Media.DISPLAY_NAME)
    protected String _displayName;
    @Column(name = MediaStore.Video.Media.MIME_TYPE)
    protected String mimeType;
    @Column(name = MediaStore.Video.Media.DATE_ADDED)
    protected long dateAdded;
    @Column(name = MediaStore.Video.Media.DATE_TAKEN)
    protected long dateTaken;
    @Column(name = MediaStore.Video.Media.DATE_MODIFIED)
    protected long dateModified;
    @Column(name = MediaStore.Video.Media.BUCKET_DISPLAY_NAME)
    protected String bucketDisplayName;
    @Column(name = MediaStore.Video.Media.BUCKET_ID)
    protected String bucketId;
    @Column(name = MediaStore.Video.Media.WIDTH)
    protected int width;

    /* private int mRealIndex;*/
    @Column(name = MediaStore.Video.Media.HEIGHT)
    protected int height;

    public VideoEntry() {
    }

   /* @Override
    public VideoEntry load(Cursor from) {
        VideoEntry a = new VideoEntry();
        a._id = from.getLong(from.getColumnIndex(MediaStore.Video.Media._ID));
        a._data = from.getString(from.getColumnIndex(MediaStore.Video.Media.DATA));
        a.title = from.getString(from.getColumnIndex(MediaStore.Video.Media.TITLE));
        a._size = from.getLong(from.getColumnIndex(MediaStore.Video.Media.SIZE));
        a._displayName = from.getString(from.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME));
        a.mimeType = from.getString(from.getColumnIndex(MediaStore.Video.Media.MIME_TYPE));
        a.dateAdded = from.getLong(from.getColumnIndex(MediaStore.Video.Media.DATE_ADDED));
        a.dateTaken = from.getLong(from.getColumnIndex(MediaStore.Video.Media.DATE_TAKEN));
        a.dateModified = from.getLong(from.getColumnIndex(MediaStore.Video.Media.DATE_MODIFIED));
        a.bucketDisplayName = from.getString(from.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME));
        a.bucketId = from.getLong(from.getColumnIndex(MediaStore.Video.Media.BUCKET_ID));
        a.width = from.getInt(from.getColumnIndex(MediaStore.Video.Media.WIDTH));
        a.height = from.getInt(from.getColumnIndex(MediaStore.Video.Media.HEIGHT));
        return a;
    }

    @Override
    public String[] projection() {
        return new String[]{
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.TITLE,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.MIME_TYPE,
                MediaStore.Video.Media.DATE_ADDED,
                MediaStore.Video.Media.DATE_TAKEN,
                MediaStore.Video.Media.DATE_MODIFIED,
                MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Video.Media.BUCKET_ID,
                MediaStore.Video.Media.WIDTH,
                MediaStore.Video.Media.HEIGHT
        };
    }

    @Override
    public int realIndex() {
        return mRealIndex;
    }

    @Override
    public void setRealIndex(int index) {
        mRealIndex = index;
    }*/

    @Override
    public long id() {
        return _id;
    }
/*
    @Override
    public String title() {
        return title;
    }*/

    @Override
    public String data() {
        return _data;
    }

    @Override
    public long size() {
        return _size;
    }

/*    @Override
    public long dateAdded() {
        return dateAdded;
    }

    @Override
    public long dateModified() {
        return dateModified;
    }*/

    @Override
    public String displayName(Context context) {
        return _displayName;
    }

    @Override
    public String mimeType() {
        return mimeType;
    }

    @Override
    public long dateTaken() {
        return dateTaken;
    }

    @Override
    public String bucketDisplayName() {
        return bucketDisplayName;
    }

    @Override
    public String bucketId() {
        return bucketId;
    }

    @Override
    public int width() {
        return width;
    }

    @Override
    public int height() {
        return height;
    }

    /*@Override
    public boolean isAlbum() {
        return false;
    }

    @Override
    public void delete(final Activity context) {
        try {
            final File currentFile = new File(_data);
            context.getContentResolver().delete(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    MediaStore.Images.Media.DATA + " = ?",
                    new String[]{currentFile.getAbsolutePath()});
            currentFile.delete();
        } catch (final Exception e) {
            e.printStackTrace();
            if (context == null) return;
            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public VideoEntry load(File from) {
        VideoEntry videoEntry = new VideoEntry();
        videoEntry._id = -1;
        videoEntry._data = from.getAbsolutePath();
        videoEntry.title = from.getName();
        videoEntry._size = from.length();
        videoEntry._displayName = from.getName();
        videoEntry.mimeType = Utils.getMimeType(Utils.getExtension(from.getName()));
        videoEntry.dateAdded = from.lastModified();
        videoEntry.dateTaken = from.lastModified();
        videoEntry.dateModified = from.lastModified();
        videoEntry.bucketDisplayName = from.getParentFile().getName();
        videoEntry.bucketId = -1;
        videoEntry.width = -1;
        videoEntry.height = -1;
        return videoEntry;
    }*/

    @Override
    public boolean isVideo() {
        return true;
    }

    @Override
    public boolean isFolder() {
        return false;
    }
}
