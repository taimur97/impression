package com.afollestad.impression.utils;

import android.content.Context;
import android.content.res.Resources;
import android.preference.PreferenceManager;

import com.afollestad.impression.R;
import com.afollestad.impression.adapters.MediaAdapter;

public abstract class PrefUtils {
    public static final String DARK_THEME = "dark_theme";
    public static final String EXPLORER_MODE = "explorer_mode";
    public static final String COLORED_NAVBAR = "colored_navbar";
    public static final String GRID_MODE = "grid_mode";
    public static final String GRID_SIZE_PREFIX = "grid_size_";
    public static final String OVERVIEW_MODE = "overview_mode";
    public static final String FILTER_MODE = "filter_mode";

    public static boolean isDarkTheme(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(DARK_THEME, false);
    }

    public static boolean isExplorerMode(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(EXPLORER_MODE, false);
    }

    public static boolean isColoredNavBar(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(COLORED_NAVBAR, false);
    }

    public static boolean isGridMode(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(GRID_MODE, true);
    }

    public static void setGridMode(Context context, boolean gridMode) {
       PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(GRID_MODE, gridMode).apply();
    }

    public static int getGridColumns(Context context) {
        if (context == null  || !isGridMode(context)) return 1;

        final Resources r = context.getResources();
        final int defaultGrid = r.getInteger(R.integer.default_grid_width);
        final int orientation = r.getConfiguration().orientation;
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt("grid_size_" + orientation, defaultGrid);
    }

    public static void setGridColumns(Context context, int orientation, int gridSize) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(GRID_SIZE_PREFIX + orientation, gridSize).apply();
    }

    public static int getOverviewMode(Context context) {
        if (context == null) return 1;
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(OVERVIEW_MODE, 1);
    }

    public static MediaAdapter.FileFilterMode getFilterMode(Context context) {
        if (context == null) return MediaAdapter.FileFilterMode.ALL;
        int explorerMode = PreferenceManager.getDefaultSharedPreferences(context).getInt(FILTER_MODE, 0);
        return MediaAdapter.FileFilterMode.valueOf(explorerMode);
    }
}
