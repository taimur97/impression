package com.afollestad.impression.media;

import android.content.Context;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;

import com.afollestad.impression.R;
import com.afollestad.impression.adapters.MediaAdapter;
import com.afollestad.impression.adapters.base.HybridCursorAdapter;

public abstract class LoaderPresenter<VH extends RecyclerView.ViewHolder, V extends LoaderView> extends ImpressionListPresenter<V> {
    protected abstract HybridCursorAdapter<VH> createAdapter();

    protected abstract String getAlbumPath();

    protected final MediaAdapter.ViewMode getViewMode() {
        if (!isViewAttached() || getView().getContextCompat() == null)
            return MediaAdapter.ViewMode.GRID;
        int explorerMode = PreferenceManager.getDefaultSharedPreferences(getView().getContextCompat()).getInt("view_mode", 0);
        return MediaAdapter.ViewMode.valueOf(explorerMode);
    }

    protected final int getGridWidth() {
        if (!isViewAttached() || getView().getContextCompat() == null) return 1;

        Context context = getView().getContextCompat();
        final Resources r = context.getResources();
        final int defaultGrid = r.getInteger(R.integer.default_grid_width);
        final int orientation = r.getConfiguration().orientation;
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt("grid_size_" + orientation, defaultGrid);
    }
}
