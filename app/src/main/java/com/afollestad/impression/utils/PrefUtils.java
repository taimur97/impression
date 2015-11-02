package com.afollestad.impression.utils;

import android.content.Context;
import android.preference.PreferenceManager;

public class PrefUtils {
    public static final String DARK_THEME = "dark_theme";
    public static final String EXPLORER_MODE = "explorer_mode";

    public static boolean isDarkTheme(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(DARK_THEME, false);
    }

    public static boolean isExplorerMode(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(EXPLORER_MODE, false);
    }
}
