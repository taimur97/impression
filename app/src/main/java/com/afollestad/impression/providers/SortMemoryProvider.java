package com.afollestad.impression.providers;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.afollestad.impression.BuildConfig;
import com.afollestad.impression.media.MediaAdapter;
import com.afollestad.impression.providers.base.ProviderBase;
import com.afollestad.impression.utils.PrefUtils;

import java.io.File;

/**
 * @author Shirwa Mohamed (shirwaM)
 */
public class SortMemoryProvider extends ProviderBase {

    private final static String COLUMNS = "_id INTEGER PRIMARY KEY AUTOINCREMENT, path TEXT, mode INTEGER";
    private final static Uri CONTENT_URI = Uri.parse("content://" + BuildConfig.APPLICATION_ID + ".sortmemory");

    public SortMemoryProvider() {
        super("sort_memory", COLUMNS);
    }

    public static void cleanup(Context context) {
        final ContentResolver r = context.getContentResolver();
        new Thread(new Runnable() {
            @Override
            public void run() {
                final Cursor cursor = r.query(CONTENT_URI, null, null, null, null);
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        final File fi = new File(cursor.getString(1));
                        if (!fi.exists()) {
                            r.delete(CONTENT_URI, "path = ?", new String[]{cursor.getString(1)});
                        }
                    }
                    cursor.close();
                }
            }
        }).start();
    }

    public static void save(Context context, String path, @MediaAdapter.SortMode int mode) {
        if (context == null) {
            return;
        }
        if (path == null) {
            PrefUtils.setSortMode(context, mode);
        } else {
            final ContentResolver r = context.getContentResolver();
            final Cursor cursor = r.query(CONTENT_URI,
                    null, "path = ?", new String[]{path}, null);
            boolean found = false;
            final ContentValues values = new ContentValues(2);
            values.put("path", path);
            values.put("mode", mode);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    found = true;
                    r.update(CONTENT_URI, values, "path = ?", new String[]{path});
                }
                cursor.close();
            }
            if (!found) {
                r.insert(CONTENT_URI, values);
            }
        }
    }

    @MediaAdapter.SortMode
    public static int getSortMode(Context context, String path) {
        if (context == null) {
            return MediaAdapter.SORT_DEFAULT;
        } else if (path == null) {
            //noinspection ResourceType
            return PrefUtils.getSortMode(context);
        }

        Cursor cursor = context.getContentResolver().query(CONTENT_URI,
                null, "path = ?", new String[]{path}, null);
        int mode = -1;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                mode = cursor.getInt(2);
            }
            cursor.close();
        }
        if (mode == -1) {
            mode = PrefUtils.getSortMode(context);
        }
        //noinspection ResourceType
        return mode;
    }

    public static void forget(Context context, String path) {
        if (context == null) {
            return;
        }
        context.getContentResolver().delete(CONTENT_URI, "path = ?", new String[]{path});
    }
}