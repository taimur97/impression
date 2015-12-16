package com.afollestad.impression.media;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.WorkerThread;
import android.support.v4.widget.DrawerLayout;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.afollestad.impression.App;
import com.afollestad.impression.R;
import com.afollestad.impression.accounts.base.Account;
import com.afollestad.impression.api.MediaEntry;
import com.afollestad.impression.providers.ExcludedFolderProvider;
import com.afollestad.impression.utils.PrefUtils;
import com.afollestad.impression.utils.TimeUtils;
import com.afollestad.impression.utils.Utils;
import com.afollestad.materialcab.MaterialCab;
import com.afollestad.materialdialogs.MaterialDialog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import rx.Observable;
import rx.Single;
import rx.SingleSubscriber;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * @author Aidan Follestad (afollestad)
 */
public class MediaCab implements Serializable, MaterialCab.Callback {

    public final static int COPY_REQUEST_CODE = 8000;
    public final static int MOVE_REQUEST_CODE = 9000;
    private static final String STATE_MEDIACAB_ENTRIES = "state_media_cab_entries";
    private final transient MainActivity mContext;
    private transient MediaFragment mFragment;
    private List<MediaEntry> mMediaEntries;
    private MaterialCab mCab;

    public MediaCab(Activity context) {
        this((MainActivity) context);
    }

    public MediaCab(MainActivity context) {
        mContext = context;
        mMediaEntries = new ArrayList<>();
    }

    public static MediaCab restoreState(Bundle in, MainActivity context) {
        List<MediaEntry> mediaEntries = (List<MediaEntry>) in.getSerializable(STATE_MEDIACAB_ENTRIES);

        if (mediaEntries != null) {
            MediaCab cab = new MediaCab(context);
            cab.mMediaEntries = mediaEntries;
            cab.mCab = MaterialCab.restoreState(in, context, cab);
            return cab;
        } else {
            return null;
        }
    }

    public void setFragment(MediaFragment frag, boolean invalidateChecked) {
        mFragment = frag;
        if (invalidateChecked) {
            invalidateChecked();
        }
    }

    public void start() {
        mContext.getDrawerLayout().setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        mContext.setMediaCab(this);
        if (mCab == null) {
            mCab = new MaterialCab(mContext, R.id.cab_stub)
                    .setMenu(R.menu.menu_media_cab)
                    .setBackgroundColor(mContext.primaryColor())
                    .setCloseDrawableRes(R.drawable.ic_action_discard)
                    .start(this);
        } else if (!mCab.isActive()) {
            mCab.start(this);
        }
        invalidateChecked();
    }

    private void invalidateChecked() {
        if (mFragment != null && mMediaEntries.size() > 0) {
            for (MediaEntry e : mMediaEntries) {
                (mFragment.getAdapter()).setItemChecked(e, true);
            }
        }
    }

    public void saveState(Bundle out) {
        mCab.saveState(out);
        out.putSerializable(STATE_MEDIACAB_ENTRIES, (Serializable) mMediaEntries);
    }

    public boolean isStarted() {
        return mCab != null && mCab.isActive();
    }

    private void invalidate() {
        if (mMediaEntries.size() == 0) {
            finish();
        } else if (mMediaEntries.size() == 1) {
            mCab.setTitle(mMediaEntries.get(0).displayName(mContext));
        } else {
            mCab.setTitle(mMediaEntries.size() + "");
        }
    }

    public void finish() {
        if (mCab != null) {
            mCab.finish();
            mCab = null;
        }
    }

    public void toggleEntry(MediaEntry p) {
        toggleEntry(p, false);
    }

    private void toggleEntry(MediaEntry p, boolean forceCheckOn) {
        boolean found = false;
        for (int i = 0; i < mMediaEntries.size(); i++) {
            if (mMediaEntries.get(i).data().equals(p.data())) {
                if (!forceCheckOn) {
                    mMediaEntries.remove(i);
                }
                found = true;
                break;
            }
        }
        if (!found) {
            mMediaEntries.add(p);
        }
        (mFragment.getAdapter()).setItemChecked(p, forceCheckOn || !found);
        invalidate();
    }

    private void excludeEntries() {
        new MaterialDialog.Builder(mContext)
                .content(mMediaEntries.size() == 1 ?
                        R.string.exclude_prompt_single : R.string.exclude_prompt, mMediaEntries.size())
                .positiveText(R.string.yes)
                .negativeText(android.R.string.cancel)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        performExclude();
                    }
                })
                .show();
    }

    private void performExclude() {
        final ProgressDialog mDialog = new ProgressDialog(mContext);
        mDialog.setMessage(mContext.getString(R.string.excluding));
        mDialog.setIndeterminate(false);
        mDialog.setMax(mMediaEntries.size());
        mDialog.setCancelable(true);
        mDialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (MediaEntry e : mMediaEntries) {
                    if (!mDialog.isShowing()) {
                        break;
                    }
                    ExcludedFolderProvider.add(mContext, e.data());
                    mDialog.setProgress(mDialog.getProgress() + 1);
                }
                mContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mDialog.dismiss();
                        finish();
                        mFragment.getPresenter().reload();
                        mContext.reloadNavDrawerAlbums();
                    }
                });
            }
        }).start();
    }

    private void shareEntries() {
        Single.create(new Single.OnSubscribe<List<MediaEntry>>() {
            @Override
            public void call(SingleSubscriber<? super List<MediaEntry>> singleSubscriber) {
                final List<MediaEntry> toSend = new ArrayList<>();
                for (final MediaEntry e : mMediaEntries) {
                    if (e.isFolder()) {
                        List<MediaEntry> entries = App.getCurrentAccount(mContext).flatMap(new Func1<Account, Single<List<MediaEntry>>>() {
                            @Override
                            public Single<List<MediaEntry>> call(Account account) {
                                //noinspection ResourceType
                                return account.getEntries(e.data(),
                                        PrefUtils.isExplorerMode(mContext),
                                        PrefUtils.getFilterMode(mContext),
                                        -1);
                            }
                        }).toBlocking().value();
                        toSend.addAll(entries);
                    } else {
                        toSend.add(e);
                    }
                }
                singleSubscriber.onSuccess(toSend);
            }
        }).subscribeOn(Schedulers.io())

                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleSubscriber<List<MediaEntry>>() {
                    @Override
                    public void onSuccess(List<MediaEntry> toSend) {
                        if (toSend.size() > 0) {
                            if (toSend.size() == 1) {
                                String mime = toSend.get(0).isVideo() ? "video*//*" : "image*//*";
                                try {
                                    Intent intent = new Intent(Intent.ACTION_SEND)
                                            .setType(mime)
                                            .putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(toSend.get(0).data())));
                                    mContext.startActivity(Intent.createChooser(intent, mContext.getString(R.string.share_using)));
                                } catch (ActivityNotFoundException e) {
                                    Toast.makeText(mContext, R.string.no_app_complete_action, Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                ArrayList<Uri> uris = new ArrayList<>();
                                boolean foundPhotos = false;
                                boolean foundVideos = false;
                                for (MediaEntry p : toSend) {
                                    foundPhotos = foundPhotos || !p.isVideo();
                                    foundVideos = foundVideos || p.isVideo();
                                    uris.add(Uri.fromFile(new File(p.data())));
                                }
                                String mime = "*/*";
                                if (foundPhotos && !foundVideos) {
                                    mime = "image*//*";
                                } else if (foundVideos && !foundPhotos) {
                                    mime = "video*//*";
                                }
                                try {
                                    Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE)
                                            .setType(mime)
                                            .putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
                                    mContext.startActivity(Intent.createChooser(intent, mContext.getString(R.string.share_using)));
                                } catch (ActivityNotFoundException e) {
                                    Toast.makeText(mContext, R.string.no_app_complete_action, Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                        finish();
                    }

                    @Override
                    public void onError(Throwable error) {

                    }
                });
    }

    public void finishCopyMove(File dest, int requestCode) {
        copyEntries(dest, requestCode == MOVE_REQUEST_CODE);
    }

    private void deleteEntries() {
        final MaterialDialog dialog = new MaterialDialog.Builder(mContext)
                .progress(false, mMediaEntries.size(), true)
                .cancelable(false)
                .content(mContext.getString(R.string.deleting))
                .show();

        Observable.create(new Observable.OnSubscribe<Object>() {
            @Override
            public void call(Subscriber<? super Object> subscriber) {
                for (MediaEntry p : mMediaEntries) {
                    if (!dialog.isShowing()) {
                        break;
                    }
                    p.delete(mContext);
                    dialog.incrementProgress(1);
                }

                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Object>() {
                    @Override
                    public void onCompleted() {
                        dialog.dismiss();

                        CurrentMediaEntriesSingleton.getInstance().removeAll(mMediaEntries);

                        mFragment.getPresenter().updateAdapterEntries();
                        mContext.reloadNavDrawerAlbums();

                        finish();
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Object done) {

                    }
                });
    }

    @WorkerThread
    private void performCopy(Context context, MediaEntry src, File dst, boolean deleteAfter) throws IOException {
        dst = checkDuplicate(dst);
        InputStream in = new FileInputStream(src.data());
        OutputStream out = new FileOutputStream(dst);
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();

        ContentResolver r = context.getContentResolver();
        ContentValues values = new ContentValues();
        if (deleteAfter) {
            if (src.isVideo()) {
                values.put(MediaStore.Video.VideoColumns.DATA, dst.getAbsolutePath());
                r.update(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values,
                        MediaStore.Video.VideoColumns.DATA + " = ?", new String[]{src.data()});
            } else {
                values.put(MediaStore.Images.ImageColumns.DATA, dst.getAbsolutePath());
                r.update(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values,
                        MediaStore.Images.ImageColumns.DATA + " = ?", new String[]{src.data()});
            }
            new File(src.data()).delete();
        } else {
            Log.i("UpdateMediaDatabase", "Scanning " + dst.getPath());
            MediaScannerConnection.scanFile(context,
                    new String[]{dst.getPath()}, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        @Override
                        public void onScanCompleted(String path, Uri uri) {
                            Log.i("Scanner", "Scanned " + path + ":");
                            Log.i("Scanner", "-> uri=" + uri);
                        }
                    });
        }
    }

    private String getNameNoExtension(File file) {
        if (file.isDirectory()) {
            return file.getName();
        }
        String name = file.getName();
        if (name.startsWith(".") || !name.substring(1).contains(".")) {
            return name;
        }
        return name.substring(0, name.lastIndexOf('.'));
    }

    private File checkDuplicate(File newFile) {
        if (!newFile.exists()) {
            return newFile;
        }
        final String parent = newFile.getParent();
        final String name = getNameNoExtension(newFile);
        final String extension = Utils.getExtension(newFile.getName());
        int index = 1;
        while (newFile.exists()) {
            newFile = new File(parent + File.separator + name + " (" + index + ")." + extension);
            index++;
        }
        return newFile;
    }

    private void copyEntries(final File destDir, final boolean deleteAfter) {
        final List<MediaEntry> toMove = new ArrayList<>();
        for (MediaEntry e : mMediaEntries) {
            //TODO
            /*if (e.isAlbum()) {
                OldAlbumEntry album = (OldAlbumEntry) e;
                Collections.addAll(toMove, album.getContents(mContext,
                        album.bucketId() == OldAlbumEntry.ALBUM_ID_USEPATH));
            } else {
                toMove.add(e);
            }*/
        }
        if (toMove.size() == 0) {
            finish();
            return;
        }

        final ProgressDialog mDialog = new ProgressDialog(mContext);
        mDialog.setMessage(mContext.getString(deleteAfter ? R.string.moving : R.string.copying));
        mDialog.setIndeterminate(false);
        mDialog.setMax(toMove.size());
        mDialog.setCancelable(true);
        mDialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (MediaEntry p : toMove) {
                    if (!mDialog.isShowing()) {
                        break;
                    }
                    final File fi = new File(p.data());
                    final File newFi = new File(destDir, fi.getName());
                    try {
                        performCopy(mContext, p, newFi, deleteAfter);
                    } catch (final IOException e) {
                        e.printStackTrace();
                        mContext.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Utils.showErrorDialog(mContext, e);
                            }
                        });
                        break;
                    }
                    mDialog.setProgress(mDialog.getProgress() + 1);
                }
                mContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mContext.reloadNavDrawerAlbums();
                        mDialog.dismiss();
                        finish();
                    }
                });
            }
        }).start();
    }

    private void selectAll() {
        List<MediaEntry> adapterPics = CurrentMediaEntriesSingleton.getInstance()
                .getMediaEntriesCopy(mContext, MediaAdapter.SORT_NOSORT);
        for (MediaEntry p : adapterPics) {
            toggleEntry(p, true);
        }
    }

    @Override
    public boolean onCabCreated(MaterialCab materialCab, Menu menu) {
        materialCab.setTitle(mMediaEntries.size() + "");
        boolean foundDir = false;
        boolean allAlbumsOrFolders = true;
        for (MediaEntry e : mMediaEntries) {
            if (!e.isFolder()) allAlbumsOrFolders = false;
            if (e.isFolder()) {
                foundDir = true;
                break;
            }
        }

        menu.findItem(R.id.share).setVisible(!foundDir);
        menu.findItem(R.id.exclude).setVisible(allAlbumsOrFolders);
        if (mMediaEntries.size() > 0) {
            MediaEntry firstEntry = mMediaEntries.get(0);
            boolean canShow = mMediaEntries.size() == 1 && !firstEntry.isVideo() && !firstEntry.isFolder();
            ;
            menu.findItem(R.id.edit).setVisible(canShow);
            menu.findItem(R.id.details).setVisible(canShow);
        } else {
            menu.findItem(R.id.edit).setVisible(false);
            menu.findItem(R.id.details).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onCabItemClicked(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.share:
                shareEntries();
                return true;
            case R.id.exclude:
                excludeEntries();
                return true;
            case R.id.delete:
                new MaterialDialog.Builder(mContext)
                        .content(R.string.delete_bulk_confirm)
                        .positiveText(R.string.yes)
                        .negativeText(R.string.no)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog materialDialog) {
                                deleteEntries();
                            }

                            @Override
                            public void onNegative(MaterialDialog materialDialog) {
                            }
                        }).build().show();
                return true;
            case R.id.selectAll:
                selectAll();
                return true;
            case R.id.edit:
                try {
                    Uri uri = Uri.fromFile(new File(mMediaEntries.get(0).data()));
                    mContext.startActivity(new Intent(Intent.ACTION_EDIT)
                            .setDataAndType(uri, "image/*"));
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            case R.id.copyTo:
                mContext.startActivityForResult(new Intent(mContext, MainActivity.class)
                        .setAction(MainActivity.ACTION_SELECT_ALBUM)
                        .putExtra("mode", R.id.copyTo), COPY_REQUEST_CODE);
                return true;
            case R.id.moveTo:
                mContext.startActivityForResult(new Intent(mContext, MainActivity.class)
                        .setAction(MainActivity.ACTION_SELECT_ALBUM)
                        .putExtra("mode", R.id.moveTo), MOVE_REQUEST_CODE);
                return true;
            case R.id.details:
                final MediaEntry entry = mMediaEntries.get(0);
                final File file = new File(entry.data());
                Calendar cal = new GregorianCalendar();
                cal.setTimeInMillis(entry.dateTaken());
                new MaterialDialog.Builder(mContext)
                        .title(R.string.details)
                        .content(Html.fromHtml(mContext.getString(R.string.details_contents,
                                TimeUtils.toStringLong(cal),
                               /* entry.width() + " x " + entry.height()*/"",//TODO
                                file.getName(),
                                Utils.readableFileSize(file.length()),
                                file.getAbsolutePath())))
                        .contentLineSpacing(1.6f)
                        .positiveText(R.string.dismiss)
                        .show();
                return true;
        }
        return true;
    }

    @Override
    public boolean onCabFinished(MaterialCab materialCab) {
        if (mContext != null) {
            mContext.setMediaCab(null);
            mContext.getDrawerLayout().setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        }
        if (mFragment != null) {
            (mFragment.getAdapter()).clearChecked();
        }
        mMediaEntries.clear();
        mCab = null;
        return true;
    }
}