package com.afollestad.impression.media;


import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.GridLayoutManager;
import android.view.View;
import android.widget.ImageView;

import com.afollestad.impression.R;
import com.afollestad.impression.adapters.MediaAdapter;
import com.afollestad.impression.adapters.base.HybridCursorAdapter;
import com.afollestad.impression.api.AlbumEntry;
import com.afollestad.impression.api.base.MediaEntry;
import com.afollestad.impression.cab.MediaCab;
import com.afollestad.impression.providers.SortMemoryProvider;
import com.afollestad.impression.ui.viewer.ViewerActivity;
import com.afollestad.impression.utils.PrefUtils;
import com.afollestad.impression.utils.Utils;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.bitmap.BitmapInfo;

import java.io.File;

import static com.afollestad.impression.media.MainActivity.EXTRA_CURRENT_ITEM_POSITION;
import static com.afollestad.impression.media.MainActivity.EXTRA_OLD_ITEM_POSITION;

public class MediaPresenter extends LoaderPresenter<MediaAdapter.ViewHolder, MediaView> {

    public static final String INIT_ALBUM_PATH = "albumPath";

    private String mAlbumPath;
    private boolean mLastDarkTheme;

    public static MediaFragment newInstance(String albumPath) {
        MediaFragment frag = new MediaFragment();
        Bundle args = new Bundle();
        args.putString(INIT_ALBUM_PATH, albumPath);
        frag.setArguments(args);
        return frag;
    }

    public final void jumpToTop(boolean animateChange) {
        if (isViewAttached() && getView().getContextCompat() != null) {
            if (animateChange) {
                //getView().getAdapter().stopAnimation();
                getView().getRecyclerView().smoothScrollToPosition(0);
            } else {
                getView().getRecyclerView().scrollToPosition(0);
            }
        }
    }

    private void invalidateTitle(MainActivity act) {
        if (isViewAttached()) {
            act.setTitle(getTitle());
        }
    }

    protected void create() {
        if (isViewAttached()) {
            //noinspection ConstantConditions
            mAlbumPath = getView().getArguments().getString(INIT_ALBUM_PATH);
            mLastDarkTheme = PrefUtils.isDarkTheme(getView().getContextCompat());
        }
    }

    protected void resume() {
        if (isViewAttached() && getView().getContextCompat() != null) {
            MainActivity act = (MainActivity) getView().getContextCompat();
            if (act.getMediaCab() != null) {
                act.getMediaCab().setFragment((MediaFragment) getView(), true);
            }

            boolean darkTheme = PreferenceManager.getDefaultSharedPreferences(act).getBoolean("dark_theme", false);
            if (darkTheme != mLastDarkTheme) {
                getView().invalidateLayoutManagerAndAdapter();
            }

            setAlbumPath(mAlbumPath);
        }
    }

    protected void createOptionsMenu() {

    }

    @Override
    String getTitle() {
        if (isViewAttached()) {
            if (PrefUtils.isExplorerMode(getView().getContextCompat())) {
                // In explorer mode, the path is displayed in the bread crumbs so the name is shown instead
                return getView().getContextCompat().getString(R.string.app_name);
            } else if (mAlbumPath == null || mAlbumPath.equals(AlbumEntry.ALBUM_OVERVIEW) ||
                    mAlbumPath.equals(Environment.getExternalStorageDirectory().getAbsolutePath())) {
                return getView().getContextCompat().getString(R.string.overview);
            }
            return new File(mAlbumPath).getName();
        }
        return null;
    }

    @Override
    int getEmptyText() {
        return R.string.no_photosorvideos;
    }

    @Override
    GridLayoutManager createLayoutManager() {
        if (isViewAttached()) {
            int columnCount = getViewMode() == MediaAdapter.ViewMode.GRID ? getGridWidth() : 1;
            return new GridLayoutManager(getView().getContextCompat(), columnCount);
        } else {
            return null;
        }
    }

    public String getAlbumPath() {
        return mAlbumPath;
    }

    /**
     * Set the directory (different from the current one).
     */
    public void setAlbumPath(String directory) {
        if (isViewAttached()) {
            getView().saveScrollPosition();
            mAlbumPath = directory;

            final MainActivity mainActivity = (MainActivity) getView().getContextCompat();

            invalidateTitle(mainActivity);
            mainActivity.invalidateArrow(mAlbumPath);
            mainActivity.supportInvalidateOptionsMenu();
            getView().setCrumb(mainActivity.getCrumbs().findCrumb(mAlbumPath));
            getView().reload();
        }
    }

    protected HybridCursorAdapter<MediaAdapter.ViewHolder> createAdapter() {
        if (isViewAttached()) {
            MainActivity act = (MainActivity) getView().getContextCompat();
            MediaAdapter.Callback callback = new MediaCallbackImpl();
            return new MediaAdapter(act, SortMemoryProvider.remember(act, mAlbumPath),
                    getViewMode(), callback, act.isSelectAlbumMode());
        } else {
            return null;
        }
    }

    private class MediaCallbackImpl implements MediaAdapter.Callback {

        @Override
        public void onItemClick(int index, View view, MediaEntry pic, boolean longClick) {
            if (!isViewAttached()) {
                return;
            }

            final MainActivity act = (MainActivity) getView().getContextCompat();

            if (act == null) {
                return;
            }

            act.setIsReentering(false);
            act.setTmpState(new Bundle());
            act.getTmpState().putInt(EXTRA_CURRENT_ITEM_POSITION, index);
            act.getTmpState().putInt(EXTRA_OLD_ITEM_POSITION, index);

            if (act.isPickMode() || act.isSelectAlbumMode()) {
                if (pic.isFolder() || pic.isAlbum()) {
                    act.switchAlbum(pic.data());
                } else {
                    // This will never be called for album selection mode, only pick mode
                    final File file = new File(pic.data());
                    final Uri uri = Utils.getImageContentUri(act, file);
                    act.setResult(Activity.RESULT_OK, new Intent().setData(uri));
                    act.finish();
                }
            } else if (longClick) {
                if (act.getMediaCab() == null)
                    act.setMediaCab(new MediaCab(act));
                if (!act.getMediaCab().isStarted())
                    act.getMediaCab().start();
                act.getMediaCab().setFragment((MediaFragment) getView(), false);
                act.getMediaCab().toggleEntry(pic);
            } else {
                if (act.getMediaCab() != null && act.getMediaCab().isStarted()) {
                    act.getMediaCab().setFragment((MediaFragment) getView(), false);
                    act.getMediaCab().toggleEntry(pic);
                } else {
                    if (pic.isFolder() || pic.isAlbum()) {
                        act.switchAlbum(pic.data());
                    } else {
                        ImageView iv = (ImageView) view.findViewById(R.id.image);
                        BitmapInfo bi = Ion.with(iv).getBitmapInfo();
                        ViewerActivity.MediaWrapper wrapper = ((MediaAdapter) getView().getAdapter()).getMedia();
                        final Intent intent = new Intent(act, ViewerActivity.class)
                                .putExtra("media_entries", wrapper)
                                .putExtra(EXTRA_CURRENT_ITEM_POSITION, index)
                                .putExtra("bitmapInfo", bi != null ? bi.key : null);
                        final String transName = "view_" + index;
                        final ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                                act, iv, transName);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            //Somehow this works (setting status bar color in both MainActivity and here)
                            //to avoid image glitching through on when ViewActivity is first created.
                            //TODO: Look into why this works and whether some code is unnecessary
                            act.getWindow().setStatusBarColor(act.primaryColorDark());
                            View statusBar = act.getWindow().getDecorView().findViewById(android.R.id.statusBarBackground);
                            if (statusBar != null) {
                                statusBar.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        ActivityCompat.startActivityForResult(act, intent, 2000, options.toBundle());
                                    }
                                });
                                return;
                            }
                        }
                        ActivityCompat.startActivityForResult(act, intent, 2000, options.toBundle());
                    }
                }
            }
        }
    }
}
