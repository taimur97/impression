package com.afollestad.impression.accounts;

import android.content.Context;

import com.afollestad.impression.accounts.base.Account;
import com.afollestad.impression.api.MediaEntry;
import com.afollestad.impression.api.MediaFolderEntry;
import com.afollestad.impression.media.MediaAdapter;

import java.util.List;
import java.util.Set;

import rx.Single;

/**
 * @author Aidan Follestad (afollestad)
 */
public class GoogleDriveAccount extends Account {

    private final int mId;
    private final String mName;

    public GoogleDriveAccount(Context context, String name, int id) {
        super(context);
        mId = id;
        mName = name;
    }

    @Override
    public int id() {
        return mId;
    }

    @Override
    public int type() {
        return TYPE_GOOGLE_DRIVE;
    }

    @Override
    public String name() {
        return mName;
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
/*
    @Override
    public Single<List<MediaFolderEntry>> getOverviewFolders(@MediaAdapter.SortMode int sort, @MediaAdapter.FileFilterMode int filter) {
        return null;
    }*/

    @Override
    public Single<List<MediaEntry>> getEntries(String albumPath, boolean explorerMode, @MediaAdapter.FileFilterMode int filter, @MediaAdapter.SortMode int sort) {
        return null;
    }

/*    @Override
public Single<List<MediaFolderEntry>> getMediaFolders(@MediaAdapter.SortMode int getSortQueryForThumb, MediaAdapter.FileFilterMode filter, AlbumCallback callback) {
    callback.onAlbums(null);
}

    @Override
    public void getIncludedFolders(OldAlbumEntry[] preEntries, AlbumCallback callback) {
    }

    @Override
    public void getEntries(String albumPath, int overviewMode, boolean explorerMode, MediaAdapter.FileFilterMode filter, MediaAdapter.SortMode getSortQueryForThumb, EntriesCallback callback) {
    }*/
}
