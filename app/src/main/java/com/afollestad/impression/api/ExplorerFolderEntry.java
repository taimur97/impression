package com.afollestad.impression.api;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.provider.MediaStore;

import com.afollestad.impression.R;

import java.io.File;

public class ExplorerFolderEntry implements MediaEntry {

    private File mFile;

    public ExplorerFolderEntry() {
    }

    public ExplorerFolderEntry(File file) {
        mFile = file;
    }

    @Override
    public long id() {
        return mFile.hashCode();
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
    public void delete(Activity context) {
        deleteFile(mFile);

        context.getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                MediaStore.Images.Media.DATA + " = ?",
                new String[]{mFile.getAbsolutePath()});
    }

    private void deleteFile(File parent) {
        for (File file : parent.listFiles()) {
            if (!file.isDirectory()) {
                file.delete();
            } else {
                deleteFile(file);
            }
        }
    }
}
