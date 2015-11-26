package com.afollestad.impression.accounts;

import android.content.Context;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.afollestad.impression.R;
import com.afollestad.impression.accounts.base.Account;
import com.afollestad.impression.api.FolderEntry;
import com.afollestad.impression.api.IncludedFolder;
import com.afollestad.impression.api.MediaEntry;
import com.afollestad.impression.api.PhotoEntry;
import com.afollestad.impression.api.VideoEntry;
import com.afollestad.impression.media.MediaAdapter;
import com.afollestad.impression.providers.IncludedFolderProvider;
import com.afollestad.impression.utils.PrefUtils;
import com.afollestad.impression.utils.Utils;
import com.afollestad.inquiry.Inquiry;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

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
    public Single<Set<FolderEntry>> getAlbums(@MediaAdapter.SortMode final int sortMode, @MediaAdapter.FileFilterMode int filter) {
        //mAlbumCallbacks.add(callback);
        final Uri[] uris;
        /*String[][] projections;
        String[] sorts;*/
        if (filter == MediaAdapter.FILTER_PHOTOS) {
            uris = new Uri[]{
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            };
            /*projections = new String[][]{
                    new PhotoEntry().projection()
            };
            sorts = new String[]{
                    PhotoEntry.getSortQueryFromSortMode(sortMode)
            };*/
        } else if (filter == MediaAdapter.FILTER_VIDEOS) {
            uris = new Uri[]{
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            };
           /* projections = new String[][]{
                    new VideoEntry().projection()
            };
            sorts = new String[]{
                    VideoEntry.sortMode(sortMode)
            };*/
        } else {
            uris = new Uri[]{
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            };
            /*projections = new String[][]{
                    new PhotoEntry().projection(),
                    new VideoEntry().projection()
            };
            sorts = new String[]{
                    PhotoEntry.getSortQueryFromSortMode(sortMode),
                    VideoEntry.sortMode(sortMode)
            };*/
        }


        return Single.create(new Single.OnSubscribe<Set<FolderEntry>>() {
            @Override
            public void call(SingleSubscriber<? super Set<FolderEntry>> singleSubscriber) {
                Set<FolderEntry> folders = new TreeSet<>(new Comparator<FolderEntry>() {
                    @Override
                    public int compare(FolderEntry lhs, FolderEntry rhs) {
                        if (lhs.bucketId().equals(rhs.bucketId())) {
                            return 0;
                        } else {
                            return PrefUtils.getSortComparator(sortMode).compare(lhs, rhs);
                        }
                    }
                });
                for (Uri uri : uris) {
                    //WHERE (TRUE) GROUP BY (bucket_id),(bucket_display_name)
                    String bucketGroupBy = "1) GROUP BY (bucket_id),(bucket_display_name";
                    String bucketOrderBy = "MAX(datetaken) DESC";
                    FolderEntry[] albums = Inquiry.get()
                            .selectFrom(uri, FolderEntry.class)
                            .where(bucketGroupBy)
                            .sort(bucketOrderBy)
                            .all();
                    if (albums != null) {
                        Collections.addAll(folders, albums);
                    }
                    Log.e(TAG, Arrays.toString(albums));
                }
                singleSubscriber.onSuccess(folders);
            }
        }).subscribeOn(Schedulers.io());

        /*new AsyncCursor(getContext())
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
                .flatMap(new Func1<Pair<Cursor[], Uri[]>, Single<OldAlbumEntry[]>>() {
                    @Override
                    public Single<OldAlbumEntry[]> call(Pair<Cursor[], Uri[]> cursors) {
                        return toEntries(cursors.first, cursors.second);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleSubscriber<MediaEntry[]>() {
                    @Override
                    public void onSuccess(MediaEntry[] value) {
                        mAlbumCallbacks.poll().onAlbums((OldAlbumEntry[]) value);
                    }

                    @Override
                    public void onError(Throwable error) {

                    }
                });*/
    }

    //TODO
    @Override
    public Single<List<FolderEntry>> getIncludedFolders(final @MediaAdapter.FileFilterMode int filter) {
        // mPreEntries = preEntries;
        //mIncludedFolderCallbacks.add(callback);

        return Single.create(new Single.OnSubscribe<List<FolderEntry>>() {
            @Override
            public void call(SingleSubscriber<? super List<FolderEntry>> singleSubscriber) {
                IncludedFolder[] albums = Inquiry.get()
                        .selectFrom(IncludedFolderProvider.CONTENT_URI, IncludedFolder.class)
                        .all();

                if (albums == null) {
                    singleSubscriber.onError(new Exception("Included folders retrieval error"));
                    return;
                }

                final List<FolderEntry> allMediaFolders = new ArrayList<>();
                for (IncludedFolder folder : albums) {
                    getMediaFoldersInFolder(new File(folder.path), filter).subscribe(new Action1<List<FolderEntry>>() {
                        @Override
                        public void call(List<FolderEntry> mediaFolders) {
                            allMediaFolders.addAll(mediaFolders);
                        }
                    });
                }
                singleSubscriber.onSuccess(allMediaFolders);
            }
        }).subscribeOn(Schedulers.io());

        /*new AsyncCursor(getContext())
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
                .flatMap(new Func1<Pair<Cursor[], Uri[]>, Single<OldAlbumEntry[]>>() {
                    @Override
                    public Single<OldAlbumEntry[]> call(Pair<Cursor[], Uri[]> cursors) {
                        return toEntries(cursors.first, cursors.second);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleSubscriber<MediaEntry[]>() {
                    @Override
                    public void onSuccess(MediaEntry[] value) {
                        mIncludedFolderCallbacks.poll().onAlbums((OldAlbumEntry[]) value);
                    }

                    @Override
                    public void onError(Throwable error) {

                    }
                });*/
    }

    private Single<List<FolderEntry>> getMediaFoldersInFolder(final File root, final @MediaAdapter.FileFilterMode int filter) {
        return Single.create(new Single.OnSubscribe<List<FolderEntry>>() {
            @Override
            public void call(SingleSubscriber<? super List<FolderEntry>> singleSubscriber) {
                final List<FolderEntry> mediaFolders = new ArrayList<>();

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
                        getMediaFoldersInFolder(child, filter).subscribe(new Action1<List<FolderEntry>>() {
                            @Override
                            public void call(List<FolderEntry> childMediaFolders) {
                                mediaFolders.addAll(childMediaFolders);
                            }
                        });
                    }
                }

                Comparator<File> comparator = new Comparator<File>() {
                    @Override
                    public int compare(File lhs, File rhs) {
                        if (lhs.lastModified() < rhs.lastModified()) {
                            return 1;
                        } else if (lhs.lastModified() == rhs.lastModified()) {
                            return 0;
                        } else {
                            return -1;
                        }
                    }
                };

                /*for (File folder : mediaFoldersFiles) {
                    FolderEntry mediaFolder = new FolderEntry();
                    mediaFolder.bucketDisplayName = folder.getName();
                    mediaFolder.bucketId = UUID.randomUUID().toString();

                    File thumb = Collections.max(new ArrayList<>(Arrays.asList(folder.listFiles())), comparator);
                    mediaFolder.dateTaken = thumb.lastModified();
                    mediaFolder._data = thumb.getAbsolutePath();

                    mediaFolders.add(mediaFolder);
                }*/

                singleSubscriber.onSuccess(mediaFolders);
            }
        });

    }

    private Single<List<FolderEntry>> getOverviewFolders(@MediaAdapter.SortMode int sort, final @MediaAdapter.FileFilterMode int filter) {
        final List<FolderEntry> finalEntries = new ArrayList<>();
        return getAlbums(sort, filter)
                .doOnSuccess(new Action1<Set<FolderEntry>>() {
                    @Override
                    public void call(Set<FolderEntry> mediaFolders) {
                        finalEntries.addAll(mediaFolders);
                    }
                })/*.flatMap(new Func1<Set<MediaFolder>, Single<? extends List<MediaFolder>>>() {
                    @Override
                    public Single<? extends List<MediaFolder>> call(Set<MediaFolder> mediaFolders) {
                        return getIncludedFolders(filter);
                    }
                })*/.map(new Func1<Set<FolderEntry>, List<FolderEntry>>() {
                    @Override
                    public List<FolderEntry> call(Set<FolderEntry> mediaFolders) {
                        //
                        // finalEntries.addAll(mediaFolders);
                        return finalEntries;
                    }
                });
    }

    @Override
    public Single<List<? extends MediaEntry>> getEntries(final String albumPath, final boolean explorerMode, final @MediaAdapter.FileFilterMode int filter, final @MediaAdapter.SortMode int sort) {
        /*if (explorerMode) {
            if (albumPath == null || albumPath.trim().equals(OldAlbumEntry.ALBUM_OVERVIEW_PATH))
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
        } else*/ //{
        if ((albumPath == null || albumPath.equals(FolderEntry.OVERVIEW_PATH))/*
                    && overviewMode == 1*/) {
            return getOverviewFolders(sort, filter).map(new Func1<List<FolderEntry>, List<? extends MediaEntry>>() {
                @Override
                public List<? extends MediaEntry> call(List<FolderEntry> folderEntries) {
                    return folderEntries;
                }
            });
            }

        return Single.create(new Single.OnSubscribe<List<? extends MediaEntry>>() {
            @Override
            public void call(SingleSubscriber<? super List<? extends MediaEntry>> singleSubscriber) {
                List<MediaEntry> mediaEntries = new ArrayList<>();

                final String bucketName = albumPath == null ? null : new File(albumPath).getName();

                List<Class<? extends MediaEntry>> entryClasses = new ArrayList<>();
                final List<Uri> uris = new ArrayList<>();
                List<String> selections = new ArrayList<>();
                List<String[]> selectionArgs = new ArrayList<>();
                final List<String> sorts = new ArrayList<>();

                if (filter == MediaAdapter.FILTER_PHOTOS || filter == MediaAdapter.FILTER_ALL) {
                    entryClasses.add(PhotoEntry.class);
                    uris.add(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    selections.add(MediaStore.Images.Media.BUCKET_DISPLAY_NAME + " = ?");
                    selectionArgs.add(new String[]{bucketName});
                    sorts.add(PhotoEntry.getSortQueryFromSortMode(sort));
                    }
                if (filter == MediaAdapter.FILTER_VIDEOS || filter == MediaAdapter.FILTER_ALL) {
                    entryClasses.add(VideoEntry.class);
                    uris.add(MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                    selections.add(MediaStore.Video.Media.BUCKET_DISPLAY_NAME + " = ?");
                    selectionArgs.add(new String[]{bucketName});
                    sorts.add(VideoEntry.getSortQueryFromSortMode(sort));
                    }

                for (int i = 0; i < entryClasses.size(); i++) {
                    Class<? extends MediaEntry> entryClass = entryClasses.get(i);
                    MediaEntry[] entries = Inquiry.get()
                            .selectFrom(uris.get(i), entryClass)
                            .where(selections.get(i), selectionArgs.get(i))
                            .sort(sorts.get(i))
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
        //}
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