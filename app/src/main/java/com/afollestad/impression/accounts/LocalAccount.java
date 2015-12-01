package com.afollestad.impression.accounts;

import android.content.Context;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.afollestad.impression.R;
import com.afollestad.impression.accounts.base.Account;
import com.afollestad.impression.api.ExplorerFolderEntry;
import com.afollestad.impression.api.IncludedFolder;
import com.afollestad.impression.api.MediaEntry;
import com.afollestad.impression.api.MediaFolderEntry;
import com.afollestad.impression.api.PhotoEntry;
import com.afollestad.impression.api.VideoEntry;
import com.afollestad.impression.media.MediaAdapter;
import com.afollestad.impression.providers.ExcludedFolderProvider;
import com.afollestad.impression.providers.IncludedFolderProvider;
import com.afollestad.impression.utils.PrefUtils;
import com.afollestad.impression.utils.Utils;
import com.afollestad.inquiry.Inquiry;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import rx.Single;
import rx.SingleSubscriber;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * @author Aidan Follestad (afollestad)
 */
public class LocalAccount extends Account {
    private static final String TAG = "LocalAccount";

    private final int mId;

    public LocalAccount(Context context, int id) {
        super(context);
        mId = id;
    }

    @Override
    public int id() {
        return mId;
    }

    @Type
    @Override
    public int type() {
        return TYPE_LOCAL;
    }

    @Override
    public String name() {
        return getContext().getString(R.string.local);
    }

    @Override
    public boolean supportsIncludedFolders() {
        return true;
    }

    @Override
    public Single<Set<MediaFolderEntry>> getMediaFolders(@MediaAdapter.SortMode final int sortMode, @MediaAdapter.FileFilterMode int filter) {
        final List<Uri> uris = new ArrayList<>();
        if (filter == MediaAdapter.FILTER_PHOTOS || filter == MediaAdapter.FILTER_ALL) {
            uris.add(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        }
        if (filter == MediaAdapter.FILTER_VIDEOS || filter == MediaAdapter.FILTER_ALL) {
            uris.add(MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        }

        return Single.create(new Single.OnSubscribe<Set<MediaFolderEntry>>() {
            @Override
            public void call(SingleSubscriber<? super Set<MediaFolderEntry>> singleSubscriber) {
                Set<MediaFolderEntry> folders = new HashSet<>();
                for (Uri uri : uris) {
                    //WHERE (1) GROUP BY (bucket_id),(bucket_display_name)
                    String bucketGroupBy = "1) GROUP BY (bucket_id),(bucket_display_name";
                    String bucketOrderBy = MediaFolderEntry.getSortQueryForThumb(sortMode);
                    MediaFolderEntry[] albums = Inquiry.get()
                            .selectFrom(uri, MediaFolderEntry.class)
                            .where(bucketGroupBy)
                            .sort(bucketOrderBy)
                            .all();
                    if (albums != null) {
                        Collections.addAll(folders, albums);
                    }
                    Log.e(TAG, Arrays.toString(albums));
                }

                for (Iterator<MediaFolderEntry> iterator = folders.iterator(); iterator.hasNext(); ) {
                    MediaFolderEntry entry = iterator.next();
                    if (ExcludedFolderProvider.contains(getContext(), entry.data())) {
                        iterator.remove();
                    }
                }

                singleSubscriber.onSuccess(folders);
            }
        }).subscribeOn(Schedulers.io());
    }

    //TODO
    @Override
    public Single<List<MediaFolderEntry>> getIncludedFolders(final @MediaAdapter.FileFilterMode int filter) {
        return Single.create(new Single.OnSubscribe<List<MediaFolderEntry>>() {
            @Override
            public void call(SingleSubscriber<? super List<MediaFolderEntry>> singleSubscriber) {
                IncludedFolder[] albums = Inquiry.get()
                        .selectFrom(IncludedFolderProvider.CONTENT_URI, IncludedFolder.class)
                        .all();

                if (albums == null) {
                    singleSubscriber.onError(new Exception("Included folders retrieval error"));
                    return;
                }

                final List<MediaFolderEntry> allMediaFolders = new ArrayList<>();
                for (IncludedFolder folder : albums) {
                    getMediaFoldersInFolder(new File(folder.path), filter).subscribe(new Action1<List<MediaFolderEntry>>() {
                        @Override
                        public void call(List<MediaFolderEntry> mediaFolders) {
                            allMediaFolders.addAll(mediaFolders);
                        }
                    });
                }
                singleSubscriber.onSuccess(allMediaFolders);
            }
        }).subscribeOn(Schedulers.io());
    }

    private Single<List<MediaFolderEntry>> getMediaFoldersInFolder(final File root, final @MediaAdapter.FileFilterMode int filter) {
        return Single.create(new Single.OnSubscribe<List<MediaFolderEntry>>() {
            @Override
            public void call(SingleSubscriber<? super List<MediaFolderEntry>> singleSubscriber) {
                final List<MediaFolderEntry> mediaFolders = new ArrayList<>();

                final List<File> mediaFoldersFiles = new ArrayList<>();

                File[] files = root.listFiles();
                for (File child : files) {
                    if (!child.isDirectory()) {
                        String mime = Utils.getMimeType(Utils.getExtension(child.getName()));
                        if (mime != null) {
                            if (mime.startsWith("image/") && filter != MediaAdapter.FILTER_VIDEOS) {
                                mediaFoldersFiles.add(root);
                            } else if (mime.startsWith("video/") && filter != MediaAdapter.FILTER_PHOTOS) {
                                mediaFoldersFiles.add(root);
                            }
                        }
                    } else if (PrefUtils.isSubfoldersIncluded(getContext())) {
                        getMediaFoldersInFolder(child, filter).subscribe(new Action1<List<MediaFolderEntry>>() {
                            @Override
                            public void call(List<MediaFolderEntry> childMediaFolders) {
                                mediaFolders.addAll(childMediaFolders);
                            }
                        });
                    }
                }

                singleSubscriber.onSuccess(mediaFolders);
            }
        });

    }

    private Single<List<MediaFolderEntry>> getOverviewFolders(@MediaAdapter.SortMode int sort, final @MediaAdapter.FileFilterMode int filter) {
        final List<MediaFolderEntry> finalEntries = new ArrayList<>();
        return getMediaFolders(sort, filter)
                .doOnSuccess(new Action1<Set<MediaFolderEntry>>() {
                    @Override
                    public void call(Set<MediaFolderEntry> mediaFolders) {
                        finalEntries.addAll(mediaFolders);
                    }
                })/*.flatMap(new Func1<Set<MediaFolder>, Single<? extends List<MediaFolder>>>() {
                    @Override
                    public Single<? extends List<MediaFolder>> call(Set<MediaFolder> mediaFolders) {
                        return getIncludedFolders(filter);
                    }
                })*/.map(new Func1<Set<MediaFolderEntry>, List<MediaFolderEntry>>() {
                    @Override
                    public List<MediaFolderEntry> call(Set<MediaFolderEntry> mediaFolders) {
                        //
                        // finalEntries.addAll(mediaFolders);
                        return finalEntries;
                    }
                });
    }

    /**
     * @param sort Used only for thumbnails of overviews.
     */
    @Override
    public Single<List<MediaEntry>> getEntries(final String albumPath, final boolean explorerMode,
                                               final @MediaAdapter.FileFilterMode int filter,
                                               final @MediaAdapter.SortMode int sort) {
        if (explorerMode) {
            return getEntries(albumPath, false, filter, sort)
                    .flatMap(new Func1<List<MediaEntry>, Single<List<MediaEntry>>>() {
                        @Override
                        public Single<List<MediaEntry>> call(final List<MediaEntry> directChildEntries) {
                            return Single.create(new Single.OnSubscribe<List<MediaEntry>>() {
                                @Override
                                public void call(SingleSubscriber<? super List<MediaEntry>> singleSubscriber) {
                                    final File dir = new File(albumPath);

                                    if (!dir.exists()) {
                                        singleSubscriber.onError(new Exception("This directory (" + dir.getAbsolutePath() + ") no longer exists."));
                                        return;
                                    }

                                    for (File fi : dir.listFiles()) {
                                        if (!fi.isDirectory()) {
                                            continue;
                                        }
                                        ExplorerFolderEntry explorerFolderEntry = new ExplorerFolderEntry(fi);
                                        directChildEntries.add(explorerFolderEntry);
                                    }

                                    singleSubscriber.onSuccess(directChildEntries);
                                }
                            });
                        }
                    }).subscribeOn(Schedulers.io());
        } else {
            if ((albumPath == null || albumPath.equals(MediaFolderEntry.OVERVIEW_PATH))/*
                    && overviewMode == 1*/) {
                return getOverviewFolders(sort, filter).map(new Func1<List<MediaFolderEntry>, List<MediaEntry>>() {
                    @Override
                    public List<MediaEntry> call(List<MediaFolderEntry> folderEntries) {
                        return (List<MediaEntry>) (List<? extends MediaEntry>) folderEntries;
                    }
                });
            }

            return Single.create(new Single.OnSubscribe<List<MediaEntry>>() {
                @Override
                public void call(SingleSubscriber<? super List<MediaEntry>> singleSubscriber) {
                    List<MediaEntry> mediaEntries = new ArrayList<>();

                    final String bucketName = new File(albumPath).getName();

                    List<Class<? extends MediaEntry>> entryClasses = new ArrayList<>();
                    final List<Uri> uris = new ArrayList<>();
                    List<String> selections = new ArrayList<>();
                    List<String[]> selectionArgs = new ArrayList<>();

                    if (filter == MediaAdapter.FILTER_PHOTOS || filter == MediaAdapter.FILTER_ALL) {
                        entryClasses.add(PhotoEntry.class);
                        uris.add(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        selections.add(MediaStore.Images.Media.BUCKET_DISPLAY_NAME + " = ?");
                        selectionArgs.add(new String[]{bucketName});
                    }
                    if (filter == MediaAdapter.FILTER_VIDEOS || filter == MediaAdapter.FILTER_ALL) {
                        entryClasses.add(VideoEntry.class);
                        uris.add(MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                        selections.add(MediaStore.Video.Media.BUCKET_DISPLAY_NAME + " = ?");
                        selectionArgs.add(new String[]{bucketName});
                    }

                    for (int i = 0; i < entryClasses.size(); i++) {
                        Class<? extends MediaEntry> entryClass = entryClasses.get(i);
                        MediaEntry[] entries = Inquiry.get()
                                .selectFrom(uris.get(i), entryClass)
                                .where(selections.get(i), selectionArgs.get(i))
                                .all();
                        if (entries != null) {
                            Collections.addAll(mediaEntries, entries);
                        }
                    }
                    singleSubscriber.onSuccess(mediaEntries);
                }
            }).subscribeOn(Schedulers.io());


            /*final Handler mHandler = new Handler();
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
                    if (fAlbumPath != null && !fAlbumPath.equals(OldAlbumEntry.ALBUM_OVERVIEW_PATH)) {
                        // Load included folders' contents for overview screen while in 'All Media' mode
                        Cursor data = r.query(IncludedFolderProvider.CONTENT_URI, null, null, null, OldFolderEntry.getSortCommandFromSortMode(getSortCommandFromSortMode));
                        if (data != null) {
                            while (data.moveToNext()) {
                                final String path = data.getString(1);
                                final boolean is = PrefUtils.isSubfoldersIncluded();
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
            }).start();*/
        }
        //return null;
    }
/*

    public Single<OldAlbumEntry[]> toEntries(final Cursor[] cursors, final Uri[] from) {
        return Single.create(new Single.OnSubscribe<OldAlbumEntry[]>() {
            @Override
            @WorkerThread
            public void call(SingleSubscriber<? super OldAlbumEntry[]> singleSubscriber) {
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

    private void toEntriesForAlbums(SingleSubscriber<? super OldAlbumEntry[]> singleSubscriber, Cursor[] cursors) {
        final List<OldAlbumEntry> results = new ArrayList<>();
        for (Cursor data : cursors) {
            while (data.moveToNext()) {
                OldLoaderEntry entry = OldLoaderEntry.load(data);
                if (entry.data() == null || entry.data().isEmpty())
                    continue;
                String parentPath = entry.parent();
                if (ExcludedFolderProvider.contains(getContext(), parentPath))
                    continue;

                boolean found = false;
                for (OldAlbumEntry e : results) {
                    if (e.data().equals(parentPath)) {
                        found = true;
                        e.putLoaded(OldLoaderEntry.load(data));
                        break;
                    }
                }
                if (!found) {
                    OldAlbumEntry newEntry = new OldAlbumEntry(parentPath, entry.bucketId());
                    newEntry.putLoaded(entry);
                    results.add(newEntry);
                }
            }
        }
        for (OldAlbumEntry entry : results)
            entry.processLoaded(getContext());

        singleSubscriber.onSuccess(results.toArray(new OldAlbumEntry[results.size()]));
    }

    private void toEntriesForIncludedFolders(SingleSubscriber<? super OldAlbumEntry[]> singleSubscriber, Cursor[] cursors) {
        List<OldAlbumEntry> results = new ArrayList<>();
        while (cursors[0].moveToNext()) {
            String path = cursors[0].getString(cursors[0].getColumnIndex("path"));
            final boolean is = PrefUtils.isSubfoldersIncluded();
            final List<MediaEntry> contents = Utils.getEntriesFromFolder(getContext(), new File(path), false, is, mFilterMode);

            // Make sure the albums loaded before (which are references here) have the use path ID so they load directory contents when tapped
            if (mPreEntries != null) {
                for (OldAlbumEntry e : mPreEntries) {
                    if (e.data().equals(path)) {
                        e.setBucketId(OldAlbumEntry.ALBUM_ID_USEPATH);
                        break;
                    }
                }
            }

            if (contents.size() == 0) {
                OldAlbumEntry newEntry = new OldAlbumEntry(path, OldAlbumEntry.ALBUM_ID_USEPATH);
                results.add(newEntry);
            } else {
                for (MediaEntry data : contents) {
                    OldLoaderEntry entry = OldLoaderEntry.load(new File(data.data()));
                    boolean found = false;
                    for (OldAlbumEntry e : results) {
                        if (e.data().equals(path)) {
                            found = true;
                            e.putLoaded(entry);
                            break;
                        }
                    }
                    if (!found) {
                        OldAlbumEntry newEntry = new OldAlbumEntry(path, OldAlbumEntry.ALBUM_ID_USEPATH);
                        newEntry.putLoaded(entry);
                        results.add(newEntry);
                    }
                }
            }
        }
        for (OldAlbumEntry entry : results)
            entry.processLoaded(getContext());

        singleSubscriber.onSuccess(results.toArray(new OldAlbumEntry[results.size()]));
    }*/

}