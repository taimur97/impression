package com.afollestad.impression.accounts.base;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.IntDef;

import com.afollestad.impression.accounts.LocalAccount;
import com.afollestad.impression.api.MediaEntry;
import com.afollestad.impression.api.MediaFolderEntry;
import com.afollestad.impression.media.MediaAdapter;
import com.afollestad.impression.providers.AccountProvider;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import rx.Single;
import rx.SingleSubscriber;
import rx.schedulers.Schedulers;

/**
 * @author Aidan Follestad (afollestad)
 */
public abstract class Account {

    public static final int TYPE_LOCAL = 1;
    public static final int TYPE_GOOGLE_DRIVE = 2;
    public static final int TYPE_DROPBOX = 3;
    private final static Object LOCK = new Object();
    private static Account[] mAccountsSingleton;
    private final Context mContext;

    protected Account(Context context) {
        mContext = context;
    }

    public static Single<Account[]> getAll(final Context context) {
        return Single.create(new Single.OnSubscribe<Account[]>() {
            @Override
            public void call(SingleSubscriber<? super Account[]> singleSubscriber) {
                synchronized (LOCK) {
                    if (mAccountsSingleton != null) {
                        singleSubscriber.onSuccess(mAccountsSingleton);
                        return;
                    }


                    final List<Account> results = new ArrayList<>();
                    Cursor cursor = context.getContentResolver().query(AccountProvider.CONTENT_URI, null, null, null, null);
                    if (cursor == null) {
                        singleSubscriber.onError(new Exception());
                        return;
                    }
                    while (cursor.moveToNext()) {
                        int id = cursor.getInt(0);
                        int type = cursor.getInt(cursor.getColumnIndex("type"));
                        switch (type) {
                            case TYPE_LOCAL:
                                results.add(new LocalAccount(context, id));
                                break;
                            case TYPE_GOOGLE_DRIVE:
                                // TODO
                                break;
                            case TYPE_DROPBOX:
                                // TODO
                                break;
                        }
                    }
                    cursor.close();

                    mAccountsSingleton = results.toArray(new Account[results.size()]);

                    singleSubscriber.onSuccess(mAccountsSingleton);
                }
            }
        }).subscribeOn(Schedulers.io());
    }

    protected Context getContext() {
        return mContext;
    }

    public abstract int id();

    @Type
    public abstract int type();

    public abstract String name();

    public abstract boolean supportsIncludedFolders();

    public abstract Single<Set<MediaFolderEntry>> getMediaFolders(@MediaAdapter.SortMode int sort, @MediaAdapter.FileFilterMode int filter);

    public abstract Single<List<MediaFolderEntry>> getIncludedFolders(@MediaAdapter.FileFilterMode int filter);

    public abstract Single<List<MediaEntry>> getEntries(String albumPath, boolean explorerMode, @MediaAdapter.FileFilterMode int filter, @MediaAdapter.SortMode int sort);

    @IntDef({TYPE_LOCAL, TYPE_GOOGLE_DRIVE, TYPE_DROPBOX})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Type {
    }

    /*public interface AccountCallback {
        void onAccount(Account account);
    }

    public interface AccountsCallback {
        void onAccounts(Account[] accounts);
    }*//*
    public interface EntriesCallback {
        void onEntries(MediaEntry[] entries);

        void onError(Exception e);
    }

    public static abstract class AlbumCallback {

        public abstract void onAlbums(OldAlbumEntry[] albums);

        public abstract void onError(Exception e);
    }*/
}