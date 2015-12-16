package com.afollestad.impression.api;

import android.app.Activity;
import android.content.Context;
import android.provider.MediaStore;
import android.widget.Toast;

import com.afollestad.inquiry.annotations.Column;

import java.io.File;

public class PhotoEntry implements MediaEntry {

    /*public String originalUri;*/

    @Column(name = MediaStore.Images.Media._ID)
    protected long _id;
    @Column(name = MediaStore.Images.Media.DATA)
    protected String _data;
    @Column(name = MediaStore.Images.Media.SIZE)
    protected long _size;
    @Column(name = MediaStore.Images.Media.TITLE)
    protected String title;
    @Column(name = MediaStore.Images.Media.DISPLAY_NAME)
    protected String _displayName;
    @Column(name = MediaStore.Images.Media.MIME_TYPE)
    protected String mimeType;
    @Column(name = MediaStore.Images.Media.DATE_ADDED)
    protected long dateAdded;
    @Column(name = MediaStore.Images.Media.DATE_TAKEN)
    protected long dateTaken;
    @Column(name = MediaStore.Images.Media.DATE_MODIFIED)
    protected long dateModified;
    @Column(name = MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
    protected String bucketDisplayName;
    @Column(name = MediaStore.Images.Media.BUCKET_ID)
    protected String bucketId;
    @Column(name = MediaStore.Images.Media.WIDTH)
    protected int width;

    /*private int mRealIndex;*/
    @Column(name = MediaStore.Images.Media.HEIGHT)
    protected int height;

    public PhotoEntry() {
    }

    /*@Override
    public PhotoEntry load(Cursor from) {
        PhotoEntry a = new PhotoEntry();
        a._id = from.getLong(from.getColumnIndex(MediaStore.Images.Media._ID));
        a.title = from.getString(from.getColumnIndex(MediaStore.Images.Media.TITLE));
        a._data = from.getString(from.getColumnIndex(MediaStore.Images.Media.DATA));
        a._size = from.getLong(from.getColumnIndex(MediaStore.Images.Media.SIZE));
        a._displayName = from.getString(from.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME));
        a.mimeType = from.getString(from.getColumnIndex(MediaStore.Images.Media.MIME_TYPE));
        a.dateAdded = from.getLong(from.getColumnIndex(MediaStore.Images.Media.DATE_ADDED));
        a.dateTaken = from.getLong(from.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN));
        a.dateModified = from.getLong(from.getColumnIndex(MediaStore.Images.Media.DATE_MODIFIED));
        a.bucketDisplayName = from.getString(from.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
        a.bucketId = from.getLong(from.getColumnIndex(MediaStore.Images.Media.BUCKET_ID));
        a.width = from.getInt(from.getColumnIndex(MediaStore.Images.Media.WIDTH));
        a.height = from.getInt(from.getColumnIndex(MediaStore.Images.Media.HEIGHT));
        return a;
    }*/

   /* @Override
    public String[] projection() {
        return new String[]{
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.TITLE,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.MIME_TYPE,
                MediaStore.Images.Media.DATE_ADDED,
                MediaStore.Images.Media.DATE_TAKEN,
                MediaStore.Images.Media.DATE_MODIFIED,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Images.Media.BUCKET_ID,
                MediaStore.Images.Media.WIDTH,
                MediaStore.Images.Media.HEIGHT
        };
    }*/

    /*@Override
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

    /*@Override
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

    @Override
    public String displayName(Context context) {
        return _displayName;
    }

    @Override
    public String mimeType() {
        return mimeType;
    }

    /*@Override
    public long dateAdded() {
        return dateAdded;
    }

    @Override
    public long dateModified() {
        return dateModified;
    }
*/
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

    @Override
    public void delete(final Activity context) {
        try {
            final File currentFile = new File(data());
            context.getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
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

   /* @Override
    public PhotoEntry load(File from) {
        PhotoEntry photoEntry = new PhotoEntry();
        photoEntry._id = -1;
        photoEntry.title = from.getName();
        photoEntry._data = from.getAbsolutePath();
        photoEntry._size = from.length();
        photoEntry._displayName = from.getName();
        photoEntry.mimeType = Utils.getMimeType(Utils.getExtension(from.getName()));
        photoEntry.dateAdded = from.lastModified();
        photoEntry.dateTaken = from.lastModified();
        photoEntry.dateModified = from.lastModified();
        photoEntry.bucketDisplayName = from.getParentFile().getName();
        photoEntry.bucketId = -1;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(from.getAbsolutePath(), options);
        photoEntry.width = options.outWidth;
        photoEntry.height = options.outHeight;
        return photoEntry;
    }*/

    @Override
    public boolean isVideo() {
        return false;
    }

    @Override
    public boolean isFolder() {
        return false;
    }
}
