package com.afollestad.impression.api;

import android.os.Parcel;
import android.provider.MediaStore;

import com.afollestad.impression.media.MediaAdapter;
import com.afollestad.inquiry.annotations.Column;

/**
 * @author Aidan Follestad (afollestad)
 */
public class PhotoEntry implements MediaEntry {

    /*public String originalUri;*/

    public static final Creator<PhotoEntry> CREATOR = new Creator<PhotoEntry>() {
        public PhotoEntry createFromParcel(Parcel source) {
            return new PhotoEntry(source);
        }

        public PhotoEntry[] newArray(int size) {
            return new PhotoEntry[size];
        }
    };
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

    protected PhotoEntry(Parcel in) {
        this._id = in.readLong();
        this._data = in.readString();
        this._size = in.readLong();
        this.title = in.readString();
        this._displayName = in.readString();
        this.mimeType = in.readString();
        this.dateAdded = in.readLong();
        this.dateTaken = in.readLong();
        this.dateModified = in.readLong();
        this.bucketDisplayName = in.readString();
        this.bucketId = in.readString();
        this.width = in.readInt();
        this.height = in.readInt();
    }

    public static String getSortQueryFromSortMode(@MediaAdapter.SortMode int from) {
        switch (from) {
            default:
                return MediaStore.Images.Media.DISPLAY_NAME + " DESC";
            case MediaAdapter.SORT_NAME_ASC:
                return MediaStore.Images.Media.DISPLAY_NAME + " ASC";
            case MediaAdapter.SORT_MODIFIED_DATE_DESC:
                return MediaStore.Images.Media.DATE_MODIFIED + " DESC";
            case MediaAdapter.SORT_MODIFIED_DATE_ASC:
                return MediaStore.Images.Media.DATE_MODIFIED + " ASC";
        }
    }

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
    public String displayName() {
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

    /*@Override
    public void delete(final Activity context) {
        try {
            final File currentFile = new File(_data);
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
    }*/

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this._id);
        dest.writeString(this._data);
        dest.writeLong(this._size);
        dest.writeString(this.title);
        dest.writeString(this._displayName);
        dest.writeString(this.mimeType);
        dest.writeLong(this.dateAdded);
        dest.writeLong(this.dateTaken);
        dest.writeLong(this.dateModified);
        dest.writeString(this.bucketDisplayName);
        dest.writeString(this.bucketId);
        dest.writeInt(this.width);
        dest.writeInt(this.height);
    }
}
