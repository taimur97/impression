package com.afollestad.impression.api;

import android.content.Context;
import android.os.Environment;
import android.os.Parcel;

import com.afollestad.impression.R;

import java.io.File;

public class ExplorerFolderEntry implements MediaEntry {

    public static final Creator<ExplorerFolderEntry> CREATOR = new Creator<ExplorerFolderEntry>() {
        public ExplorerFolderEntry createFromParcel(Parcel source) {
            return new ExplorerFolderEntry(source);
        }

        public ExplorerFolderEntry[] newArray(int size) {
            return new ExplorerFolderEntry[size];
        }
    };
    private File mFile;

    public ExplorerFolderEntry() {
    }

    public ExplorerFolderEntry(File file) {
        mFile = file;
    }

    protected ExplorerFolderEntry(Parcel in) {
        this.mFile = (File) in.readSerializable();
    }

    @Override
    public long id() {
        return 0;
    }

    @Override
    public String data() {
        return mFile.getAbsolutePath();
    }

    @Override
    public long size() {
        return mFile.listFiles().length;
    }

    @Override
    public String displayName(Context context) {
        if (mFile.equals(Environment.getExternalStorageDirectory().getAbsoluteFile())) {
            return context.getString(R.string.internal_storage);
        }
        return mFile.getName();
    }

    @Override
    public String mimeType() {
        return "";
    }

    @Override
    public long dateTaken() {
        return -1;
    }

    @Override
    public String bucketDisplayName() {
        return mFile.getName();
    }

    @Override
    public String bucketId() {
        return "";
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
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(this.mFile);
    }
}
