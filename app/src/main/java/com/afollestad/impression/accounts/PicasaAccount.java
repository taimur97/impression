package com.afollestad.impression.accounts;

import android.app.Activity;
import android.content.Context;

import com.afollestad.impression.accounts.base.Account;
import com.afollestad.impression.api.MediaEntry;
import com.afollestad.impression.api.MediaFolderEntry;
import com.afollestad.impression.media.MediaAdapter;

import java.util.List;
import java.util.Set;

import rx.Single;

public class PicasaAccount extends Account {

    protected PicasaAccount(Context context) {
        super(context);
    }

    public void add(Activity activity) {

    }

    private void pickUserAccount(Activity activity) {

    }

    @Override
    public int id() {
        return 0;
    }

    @Override
    public int type() {
        return 0;
    }

    @Override
    public String name() {
        return null;
    }

    @Override
    public boolean supportsIncludedFolders() {
        return false;
    }

    @Override
    public Single<Set<MediaFolderEntry>> getMediaFolders(@MediaAdapter.SortMode int sort, @MediaAdapter.FileFilterMode int filter) {
        return null;
    }

    @Override
    public Single<List<MediaFolderEntry>> getIncludedFolders(@MediaAdapter.FileFilterMode int filter) {
        return null;
    }

    @Override
    public Single<List<MediaEntry>> getEntries(String albumPath, boolean explorerMode, @MediaAdapter.FileFilterMode int filter, @MediaAdapter.SortMode int sort) {
        return null;
    }
}
