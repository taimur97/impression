package com.afollestad.impression.accounts;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.WorkerThread;
import android.util.Pair;

import com.afollestad.impression.R;
import com.afollestad.impression.accounts.base.Account;
import com.afollestad.impression.api.AlbumEntry;
import com.afollestad.impression.api.AsyncCursor;
import com.afollestad.impression.api.FolderEntry;
import com.afollestad.impression.api.LoaderEntry;
import com.afollestad.impression.api.PhotoEntry;
import com.afollestad.impression.api.VideoEntry;
import com.afollestad.impression.api.base.MediaEntry;
import com.afollestad.impression.media.MediaAdapter;
import com.afollestad.impression.providers.ExcludedFolderProvider;
import com.afollestad.impression.providers.IncludedFolderProvider;
import com.afollestad.impression.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import rx.Single;
import rx.SingleSubscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * @author Aidan Follestad (afollestad)
 */
public class LocalAccount extends Account {

    private final LinkedList<AlbumCallback> mAlbumCallbacks;
    private final LinkedList<AlbumCallback> mIncludedFolderCallbacks;
    private final int mId;
    private AlbumEntry[] mPreEntries;
    private MediaAdapter.FileFilterMode mFilterMode;

    public LocalAccount(Context context, int id) {
        super(context);
        mId = id;
        mAlbumCallbacks = new LinkedList<>();
        mIncludedFolderCallbacks = new LinkedList<>();
    }

    @Override
    public int id() {
        return mId;
    }

    @Override
    public int type() {
        return TYPE_LOCAL;
    }

    @Override
    public String name() {
        return getContext().getString(R.string.local);
    }

    @Override
    public boolean hasIncludedFolders() {
        return true;
    }

    @Override
    public void getAlbums(MediaAdapter.SortMode sort, MediaAdapter.FileFilterMode filter, AlbumCallback callback) {
        mAlbumCallbacks.add(callback);
        Uri[] uris;
        String[][] projections;
        String[] sorts;
        if (filter == MediaAdapter.FileFilterMode.PHOTOS) {
            uris = new Uri[]{
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            };
            projections = new String[][]{
                    new PhotoEntry().projection()
            };
            sorts = new String[]{
                    PhotoEntry.sort(sort)
            };
        } else if (filter == MediaAdapter.FileFilterMode.VIDEOS) {
            uris = new Uri[]{
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            };
            projections = new String[][]{
                    new VideoEntry().projection()
            };
            sorts = new String[]{
                    VideoEntry.sort(sort)
            };
        } else {
            uris = new Uri[]{
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            };
            projections = new String[][]{
                    new PhotoEntry().projection(),
                    new VideoEntry().projection()
            };
            sorts = new String[]{
                    PhotoEntry.sort(sort),
                    VideoEntry.sort(sort)
            };
        }

        new AsyncCursor(getContext())
                .uris(uris)
                .projections(projections)
                .sorts(sorts)
                .selections(new String[]{
                        null,
                        null
                })
                .selectionArgs(new String[][]{
                        null,
                        null
                })
                .query().subscribeOn(Schedulers.io())
                .flatMap(new Func1<Pair<Cursor[], Uri[]>, Single<AlbumEntry[]>>() {
                    @Override
                    public Single<AlbumEntry[]> call(Pair<Cursor[], Uri[]> cursors) {
                        return toEntries(cursors.first, cursors.second);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleSubscriber<MediaEntry[]>() {
                    @Override
                    public void onSuccess(MediaEntry[] value) {
                        mAlbumCallbacks.poll().onAlbums((AlbumEntry[]) value);
                    }

                    @Override
                    public void onError(Throwable error) {

                    }
                });
    }

    @Override
    public void getIncludedFolders(AlbumEntry[] preEntries, AlbumCallback callback) {
        mPreEntries = preEntries;
        mIncludedFolderCallbacks.add(callback);
        new AsyncCursor(getContext())
                .uris(new Uri[]{
                        IncludedFolderProvider.CONTENT_URI
                })
                .projections(new String[][]{
                        new String[]{"path"}
                })
                .sorts(new String[]{null})
                .selections(new String[]{null})
                .selectionArgs(new String[][]{null})
                .query()
                .subscribeOn(Schedulers.io())
                .flatMap(new Func1<Pair<Cursor[], Uri[]>, Single<AlbumEntry[]>>() {
                    @Override
                    public Single<AlbumEntry[]> call(Pair<Cursor[], Uri[]> cursors) {
                        return toEntries(cursors.first, cursors.second);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleSubscriber<MediaEntry[]>() {
                    @Override
                    public void onSuccess(MediaEntry[] value) {
                        mIncludedFolderCallbacks.poll().onAlbums((AlbumEntry[]) value);
                    }

                    @Override
                    public void onError(Throwable error) {

                    }
                });
    }

    private void getOverviewEntries(MediaAdapter.SortMode sort, MediaAdapter.FileFilterMode filter, final EntriesCallback callback) {
        mFilterMode = filter;
        final List<AlbumEntry> mFinalEntries = new ArrayList<>();
        getAlbums(sort, filter, new AlbumCallback() {
            @Override
            public void onAlbums(AlbumEntry[] albums) {
                if (albums != null)
                    Collections.addAll(mFinalEntries, albums);
                getIncludedFolders(albums, new AlbumCallback() {
                    @Override
                    public void onAlbums(AlbumEntry[] albums) {
                        if (albums != null)
                            Collections.addAll(mFinalEntries, albums);
                        callback.onEntries(mFinalEntries.toArray(new AlbumEntry[mFinalEntries.size()]));
                    }

                    @Override
                    public void onError(Exception e) {
                        callback.onError(e);
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }

    @Override
    public void getEntries(String albumPath, int overviewMode, final boolean explorerMode, final MediaAdapter.FileFilterMode filter, final MediaAdapter.SortMode sort, final EntriesCallback callback) {
        if (explorerMode) {
            if (albumPath == null || albumPath.trim().equals(AlbumEntry.ALBUM_OVERVIEW_PATH))
                albumPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            final File dir = new File(albumPath);
            if (!dir.exists()) {
                callback.onError(new Exception("This directory (" + dir.getAbsolutePath() + ") no longer exists."));
                return;
            }
            final Handler mHandler = new Handler();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    final boolean is = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("include_subfolders_included", true);
                    final List<MediaEntry> results = Utils.getEntriesFromFolder(getContext(), dir, true, false, filter);
                    if (callback != null) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onEntries(results.toArray(new MediaEntry[results.size()]));
                            }
                        });
                    }
                }
            }).start();
        } else {
            if ((albumPath == null || albumPath.equals(AlbumEntry.ALBUM_OVERVIEW_PATH) /*||*/
                    /*albumPath.equals(Environment.getExternalStorageDirectory().getAbsolutePath())*/)
                    && overviewMode == 1) {
                getOverviewEntries(sort, filter, callback);
                return;
            }

            final String bucketName = albumPath == null ? null : new File(albumPath).getName();
            final Uri[] uris;
            final String[][] projections;
            String[] selections = null;
            String[][] selectionArgs = null;
            final String[] sorts;
            switch (filter) {
                default:
                    uris = new Uri[]{
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    };
                    projections = new String[][]{
                            new PhotoEntry().projection(),
                            new VideoEntry().projection()
                    };
                    if (albumPath != null && !albumPath.equals(AlbumEntry.ALBUM_OVERVIEW_PATH)) {
                        selections = new String[]{
                                MediaStore.Images.Media.BUCKET_DISPLAY_NAME + " = ?",
                                MediaStore.Video.Media.BUCKET_DISPLAY_NAME + " = ?"
                        };
                        selectionArgs = new String[][]{
                                new String[]{bucketName},
                                new String[]{bucketName}
                        };
                    }
                    sorts = new String[]{
                            PhotoEntry.sort(sort),
                            VideoEntry.sort(sort)
                    };
                    break;
                case PHOTOS:
                    uris = new Uri[]{
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    };
                    projections = new String[][]{
                            new PhotoEntry().projection(),
                    };
                    if (albumPath != null && !albumPath.equals(AlbumEntry.ALBUM_OVERVIEW_PATH)) {
                        selections = new String[]{
                                MediaStore.Images.Media.BUCKET_DISPLAY_NAME + " = ?",
                        };
                        selectionArgs = new String[][]{
                                new String[]{bucketName}
                        };
                    }
                    sorts = new String[]{
                            PhotoEntry.sort(sort)
                    };
                    break;
                case VIDEOS:
                    uris = new Uri[]{
                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    };
                    projections = new String[][]{
                            new VideoEntry().projection()
                    };
                    if (albumPath != null && !albumPath.equals(AlbumEntry.ALBUM_OVERVIEW_PATH)) {
                        selections = new String[]{
                                MediaStore.Video.Media.BUCKET_DISPLAY_NAME + " = ?"
                        };
                        selectionArgs = new String[][]{
                                new String[]{bucketName}
                        };
                    }
                    sorts = new String[]{
                            VideoEntry.sort(sort)
                    };
                    break;
            }
            final Handler mHandler = new Handler();
            final String[] fSelections = selections;
            final String[][] fSelectionArgs = selectionArgs;
            final String fAlbumPath = albumPath;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    final List<MediaEntry> results = new ArrayList<>();
                    ContentResolver r = getContext().getContentResolver();
                    for (int i = 0; i < uris.length; i++) {
                        Cursor data = r.query(uris[i],
                                projections[i],
                                fSelections != null ? fSelections[i] : null,
                                fSelectionArgs != null ? fSelectionArgs[i] : null,
                                sorts[i]);
                        if (data == null) continue;
                        boolean photos = uris[i].toString().equals(MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString());
                        while (data.moveToNext()) {
                            MediaEntry entry = (photos ? new PhotoEntry().load(data) : new VideoEntry().load(data));
                            results.add(entry);
                        }
                        data.close();
                    }
                    if (fAlbumPath != null && !fAlbumPath.equals(AlbumEntry.ALBUM_OVERVIEW_PATH)) {
                        // Load included folders' contents for overview screen while in 'All Media' mode
                        Cursor data = r.query(IncludedFolderProvider.CONTENT_URI, null, null, null, FolderEntry.sort(sort));
                        if (data != null) {
                            while (data.moveToNext()) {
                                final String path = data.getString(1);
                                final boolean is = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("include_subfolders_included", true);
                                final List<MediaEntry> entries = Utils.getEntriesFromFolder(getContext(), new File(path), false, is, filter);
                                for (MediaEntry en : entries) {
                                    boolean found = false;
                                    for (int index = results.size() - 1; index > 0; index--) {
                                        if (results.get(index).data().equals(en.data())) {
                                            found = true;
                                            break;
                                        }
                                    }
                                    if (!found)
                                        results.add(en);
                                }
                            }
                            data.close();
                        }
                    }
                    if (callback != null) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onEntries(results.toArray(new MediaEntry[results.size()]));
                            }
                        });
                    }
                }
            }).start();
        }
    }


    public Single<AlbumEntry[]> toEntries(final Cursor[] cursors, final Uri[] from) {
        return Single.create(new Single.OnSubscribe<AlbumEntry[]>() {
            @Override
            @WorkerThread
            public void call(SingleSubscriber<? super AlbumEntry[]> singleSubscriber) {
                if (from[0].toString().equals(IncludedFolderProvider.CONTENT_URI.toString())) {
                    toEntriesForIncludedFolders(singleSubscriber, cursors);
                } else {
                    toEntriesForAlbums(singleSubscriber, cursors);
                }
                for (int i = 0; i < cursors.length; i++) {
                    cursors[i].close();
                    cursors[i] = null;
                }
            }
        });
    }

    private void toEntriesForAlbums(SingleSubscriber<? super AlbumEntry[]> singleSubscriber, Cursor[] cursors) {
        final List<AlbumEntry> results = new ArrayList<>();
        for (Cursor data : cursors) {
            while (data.moveToNext()) {
                LoaderEntry entry = LoaderEntry.load(data);
                if (entry.data() == null || entry.data().isEmpty())
                    continue;
                String parentPath = entry.parent();
                if (ExcludedFolderProvider.contains(getContext(), parentPath))
                    continue;

                boolean found = false;
                for (AlbumEntry e : results) {
                    if (e.data().equals(parentPath)) {
                        found = true;
                        e.putLoaded(LoaderEntry.load(data));
                        break;
                    }
                }
                if (!found) {
                    AlbumEntry newEntry = new AlbumEntry(parentPath, entry.bucketId());
                    newEntry.putLoaded(entry);
                    results.add(newEntry);
                }
            }
        }
        for (AlbumEntry entry : results)
            entry.processLoaded(getContext());

        singleSubscriber.onSuccess(results.toArray(new AlbumEntry[results.size()]));
    }

    private void toEntriesForIncludedFolders(SingleSubscriber<? super AlbumEntry[]> singleSubscriber, Cursor[] cursors) {
        List<AlbumEntry> results = new ArrayList<>();
        while (cursors[0].moveToNext()) {
            String path = cursors[0].getString(cursors[0].getColumnIndex("path"));
            final boolean is = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("include_subfolders_included", true);
            final List<MediaEntry> contents = Utils.getEntriesFromFolder(getContext(), new File(path), false, is, mFilterMode);

            // Make sure the albums loaded before (which are references here) have the use path ID so they load directory contents when tapped
            if (mPreEntries != null) {
                for (AlbumEntry e : mPreEntries) {
                    if (e.data().equals(path)) {
                        e.setBucketId(AlbumEntry.ALBUM_ID_USEPATH);
                        break;
                    }
                }
            }

            if (contents.size() == 0) {
                AlbumEntry newEntry = new AlbumEntry(path, AlbumEntry.ALBUM_ID_USEPATH);
                results.add(newEntry);
            } else {
                for (MediaEntry data : contents) {
                    LoaderEntry entry = LoaderEntry.load(new File(data.data()));
                    boolean found = false;
                    for (AlbumEntry e : results) {
                        if (e.data().equals(path)) {
                            found = true;
                            e.putLoaded(entry);
                            break;
                        }
                    }
                    if (!found) {
                        AlbumEntry newEntry = new AlbumEntry(path, AlbumEntry.ALBUM_ID_USEPATH);
                        newEntry.putLoaded(entry);
                        results.add(newEntry);
                    }
                }
            }
        }
        for (AlbumEntry entry : results)
            entry.processLoaded(getContext());

        singleSubscriber.onSuccess(results.toArray(new AlbumEntry[results.size()]));
    }
}