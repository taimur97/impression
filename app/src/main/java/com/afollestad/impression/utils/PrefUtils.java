package com.afollestad.impression.utils;

import android.content.Context;
import android.content.res.Resources;
import android.preference.PreferenceManager;

import com.afollestad.impression.R;
import com.afollestad.impression.api.MediaEntry;
import com.afollestad.impression.media.MediaAdapter;
import com.afollestad.impression.media.MediaModifiedSorter;
import com.afollestad.impression.media.MediaNameSorter;

import java.util.Comparator;

public abstract class PrefUtils {
    public static final String OPEN_ABOUT = "about";
    public static final String OPEN_EXCLUDED_FOLDERS = "excluded_folders";

    public static final String DARK_THEME = "dark_theme";
    public static final String EXPLORER_MODE = "explorer_mode";
    public static final String COLORED_NAVBAR = "colored_navbar";
    public static final String GRID_MODE = "grid_mode";
    public static final String GRID_SIZE_PREFIX = "grid_size_";
    public static final String OVERVIEW_MODE = "overview_mode";
    public static final String FILTER_MODE = "filter_mode";
    public static final String INCLUDE_SUBFOLDERS = "include_subfolders";
    public static final String ACTIVE_ACCOUNT_ID = "active_account";
    public static final String SORT_MODE = "sort_mode";
    public static final String PRIMARY_COLOR_PREFIX = "primary_color";
    public static final String ACCENT_COLOR_PREFIX = "accent_color";
    public static final String EXCLUDE_SUBFOLDERS = "exclude_subfolders";

    public static boolean isDarkTheme(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(DARK_THEME, false);
    }

    public static void setExplorerMode(Context context, boolean explorerMode) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(EXPLORER_MODE, explorerMode).apply();
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
        if (context == null || !isGridMode(context)) {
            return 1;
        }

        final Resources r = context.getResources();
        final int defaultGrid = r.getInteger(R.integer.default_grid_width);
        final int orientation = r.getConfiguration().orientation;
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(GRID_SIZE_PREFIX + orientation, defaultGrid);
    }

    public static void setGridColumns(Context context, int orientation, int gridSize) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(GRID_SIZE_PREFIX + orientation, gridSize).apply();
    }

    public static boolean getOverviewAllMediaMode(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(OVERVIEW_MODE, false);
    }

    public static
    @MediaAdapter.FileFilterMode
    int getFilterMode(Context context) {
        if (context == null) {
            return MediaAdapter.FILTER_ALL;
        }
        //noinspection ResourceType
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(FILTER_MODE, MediaAdapter.FILTER_ALL);
    }

    public static void setFilterMode(Context context, @MediaAdapter.FileFilterMode int mode) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(FILTER_MODE, mode).apply();
    }

    public static boolean isSubfoldersIncluded(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(INCLUDE_SUBFOLDERS, true);
    }

    public static int getActiveAccountId(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(ACTIVE_ACCOUNT_ID, -1);
    }

    public static void setActiveAccountId(Context context, int accountId) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(ACTIVE_ACCOUNT_ID, accountId).apply();
    }

    @MediaAdapter.SortMode
    public static int getSortMode(Context context) {
        //noinspection ResourceType
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(SORT_MODE, MediaAdapter.SORT_DEFAULT);
    }

    public static void setSortMode(Context context, @MediaAdapter.SortMode int mode) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(SORT_MODE, mode).apply();
    }

    public static Comparator<MediaEntry> getSortComparator(Context context, @MediaAdapter.SortMode int mode) {
        switch (mode) {
            default:
                return new MediaNameSorter(context, false);
            case MediaAdapter.SORT_NAME_DESC:
                return new MediaNameSorter(context, true);
            case MediaAdapter.SORT_MODIFIED_DATE_ASC:
                return new MediaModifiedSorter(false);
            case MediaAdapter.SORT_MODIFIED_DATE_DESC:
                return new MediaModifiedSorter(true);
        }
    }

    public static boolean isExcludeSubfolders(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(EXCLUDE_SUBFOLDERS, true);
    }
}
