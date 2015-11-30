package com.afollestad.impression.api;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.os.Parcel;
import android.provider.MediaStore;

import com.afollestad.impression.R;
import com.afollestad.inquiry.annotations.Column;

import java.io.File;

//TODO: Don't repeat everything in PhotoEntry? Superclass problem with Inquiry
public class MediaFolderEntry extends PhotoEntry {

    public static final String OVERVIEW_PATH = "OVERVIEW";
    public static final Creator<MediaFolderEntry> CREATOR = new Creator<MediaFolderEntry>() {
        public MediaFolderEntry createFromParcel(Parcel source) {
            return new MediaFolderEntry(source);
        }

        public MediaFolderEntry[] newArray(int size) {
            return new MediaFolderEntry[size];
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
    @Column(name = MediaStore.Images.Media.HEIGHT)
    protected int height;

    public MediaFolderEntry() {
    }

    protected MediaFolderEntry(Parcel in) {
        this.bucketId = in.readString();
        this.bucketDisplayName = in.readString();
        this.dateTaken = in.readLong();
        this._data = in.readString();
    }

    @Override
    public String toString() {
        return "MediaFolderEntry{" +
                "_id=" + _id +
                ", _data='" + _data + '\'' +
                ", _size=" + _size +
                ", title='" + title + '\'' +
                ", _displayName='" + _displayName + '\'' +
                ", mimeType='" + mimeType + '\'' +
                ", dateAdded=" + dateAdded +
                ", dateTaken=" + dateTaken +
                ", dateModified=" + dateModified +
                ", bucketDisplayName='" + bucketDisplayName + '\'' +
                ", bucketId='" + bucketId + '\'' +
                ", width=" + width +
                ", height=" + height +
                '}';
    }

    public long id() {
        return bucketId.hashCode();
    }

    public String firstPath() {
        return _data;
    }

    @Override
    public String data() {
        return new File(_data).getParent();
    }

    @Override
    public long size() {
        //TODO - like Cabinet, query later
        return -1;
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
    public String displayName(Context context) {
        if (data().equals(Environment.getExternalStorageDirectory().getAbsolutePath())) {
            return context.getString(R.string.internal_storage);
        }
        return bucketDisplayName;
    }

    @Override
    public String mimeType() {
        return "";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.bucketId);
        dest.writeString(this.bucketDisplayName);
        dest.writeLong(this.dateTaken);
        dest.writeString(this._data);
    }

    @Override
    public int hashCode() {
        return 31 * data().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof MediaFolderEntry && data().equals(((MediaFolderEntry) o).data());
    }

    @Override
    public void delete(final Activity context) {
        //TODO
        /*for(File file: new File(data()).listFiles()){
            file.delete();
        }*/
    }
}
