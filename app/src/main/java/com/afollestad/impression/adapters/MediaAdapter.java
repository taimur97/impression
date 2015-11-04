package com.afollestad.impression.adapters;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.impression.R;
import com.afollestad.impression.adapters.base.HybridCursorAdapter;
import com.afollestad.impression.api.AlbumEntry;
import com.afollestad.impression.api.FolderEntry;
import com.afollestad.impression.api.PhotoEntry;
import com.afollestad.impression.api.VideoEntry;
import com.afollestad.impression.api.base.MediaEntry;
import com.afollestad.impression.utils.PrefUtils;
import com.afollestad.impression.utils.Utils;
import com.afollestad.impression.viewer.ViewerActivity;
import com.afollestad.impression.widget.ImpressionImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Aidan Follestad (afollestad)
 */
public class MediaAdapter extends HybridCursorAdapter<MediaAdapter.ViewHolder> {

    public static final int VIEW_TYPE_GRID = 0;
    public static final int VIEW_TYPE_GRID_FOLDER = 1;
    public static final int VIEW_TYPE_LIST = 2;

    private final Context mContext;
    private final Callback mCallback;
    private final List<MediaEntry> mEntries;
    private final List<String> mCheckedPaths;
    private final boolean mSelectAlbumMode;
    private SortMode mSortMode;
    private boolean mGridMode;
    private int mDefaultImageBackground;
    private int mEmptyImageBackground;

    public MediaAdapter(Context context, SortMode sort, Callback callback, boolean selectAlbumMode) {
        mContext = context;
        mSortMode = sort;
        mGridMode = PrefUtils.isGridMode(context);
        mCallback = callback;
        mEntries = new ArrayList<>();
        mCheckedPaths = new ArrayList<>();
        mSelectAlbumMode = selectAlbumMode;

        mDefaultImageBackground = Utils.resolveColor(context, R.attr.default_image_background);
        mEmptyImageBackground = Utils.resolveColor(context, R.attr.empty_image_background);
    }

    @Override
    public int getItemViewType(int position) {
        if (!mGridMode) {
            return VIEW_TYPE_LIST;
        } else if (mEntries.get(position).isFolder()) {
            return VIEW_TYPE_GRID_FOLDER;
        } else {
            return VIEW_TYPE_GRID;
        }
    }

    public void setItemChecked(MediaEntry entry, boolean checked) {
        if (checked) {
            if (!mCheckedPaths.contains(entry.data()))
                mCheckedPaths.add(entry.data());
            for (int i = 0; i < mEntries.size(); i++) {
                if (mEntries.get(i).data() != null &&
                        mEntries.get(i).data().equals(entry.data())) {
                    notifyItemChanged(i);
                    break;
                }
            }
        } else {
            if (mCheckedPaths.contains(entry.data()))
                mCheckedPaths.remove(entry.data());
            for (int i = 0; i < mEntries.size(); i++) {
                if (mEntries.get(i).data().equals(entry.data())) {
                    notifyItemChanged(i);
                    break;
                }
            }
        }
    }

    public void clearChecked() {
        mCheckedPaths.clear();
        notifyDataSetChanged();
    }

    public ViewerActivity.MediaWrapper getMedia() {
        return new ViewerActivity.MediaWrapper(mEntries, false);
    }

    @Override
    public void clear() {
        mEntries.clear();
    }

    private void add(MediaEntry e) {
        if (e instanceof AlbumEntry) {
            synchronized (mEntries) {
                boolean found = false;
                for (int i = 0; i < mEntries.size(); i++) {
                    MediaEntry e2 = mEntries.get(i);
                    if (e2.data().equals(e.data())) {
                        found = true;
                        mEntries.set(i, e);
                        break;
                    }
                }
                if (!found)
                    mEntries.add(e);
            }
        } else {
            mEntries.add(e);
        }
    }

    @Override
    public void addAll(MediaEntry[] entries) {
        if (entries != null) {
            for (MediaEntry e : entries)
                add(e);
        }
        if (mEntries.size() > 0)
            Collections.sort(mEntries, getSorter());
        notifyDataSetChanged();
    }

    private Comparator<MediaEntry> getSorter() {
        switch (mSortMode) {
            default:
                return new MediaNameSorter(false);
            case NAME_DESC:
                return new MediaNameSorter(true);
            case MODIFIED_DATE_ASC:
                return new MediaModifiedSorter(false);
            case MODIFIED_DATE_DESC:
                return new MediaModifiedSorter(true);
        }
    }

    public void updateGridModeOn() {
        mGridMode = PrefUtils.isGridMode(mContext);
        notifyDataSetChanged();
    }

    public void updateGridColumns() {
        notifyDataSetChanged();
    }

    public void setSortMode(SortMode mode) {
        mSortMode = mode;
    }

    public void updateTheme() {
        mDefaultImageBackground = Utils.resolveColor(mContext, R.attr.default_image_background);
        mEmptyImageBackground = Utils.resolveColor(mContext, R.attr.empty_image_background);
        notifyDataSetChanged();
    }

    @Override
    public void changeContent(Cursor cursor, Uri from, boolean clear, boolean explorerMode) {
        if (cursor == null || from == null) {
            mEntries.clear();
            return;
        }
        if (clear) mEntries.clear();
        final boolean photos = from.toString().equals(MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString());
        while (cursor.moveToNext()) {
            MediaEntry pic = (photos ? new PhotoEntry().load(cursor) : new VideoEntry().load(cursor));
            mEntries.add(pic);
        }
        cursor.close();
        Collections.sort(mEntries, getSorter());
    }

    @Override
    public void changeContent(File[] content, boolean explorerMode, FileFilterMode mode) {
        mEntries.clear();
        if (content == null || content.length == 0) return;
        for (File fi : content) {
            if (!fi.isHidden()) {
                if (fi.isDirectory()) {
                    if (explorerMode)
                        mEntries.add(new FolderEntry(fi));
                } else {
                    String mime = Utils.getMimeType(Utils.getExtension(fi.getName()));
                    if (mime != null) {
                        if (mime.startsWith("image/") && mode != FileFilterMode.VIDEOS) {
                            mEntries.add(new PhotoEntry().load(fi));
                        } else if (mime.startsWith("video/") && mode != FileFilterMode.PHOTOS) {
                            mEntries.add(new VideoEntry().load(fi));
                        }
                    }
                }
            }
        }
        Collections.sort(mEntries, getSorter());
    }

    @Override
    public MediaAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layoutRes;
        switch (viewType) {
            case VIEW_TYPE_GRID_FOLDER:
                layoutRes = R.layout.grid_item_folder;
                break;
            case VIEW_TYPE_LIST:
                layoutRes = R.layout.list_item_media;
                break;
            case VIEW_TYPE_GRID:
            default:
                layoutRes = R.layout.grid_item_media;
                break;
        }
        View v = LayoutInflater.from(mContext).inflate(layoutRes, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        MediaEntry entry = mEntries.get(position);

        if (!mSelectAlbumMode || (entry.isFolder() || entry.isAlbum())) {
            holder.view.setActivated(mCheckedPaths.contains(entry.data()));
            if (!mSelectAlbumMode) {
                holder.view.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        int index = holder.getAdapterPosition();
                        mCallback.onItemClick(index, v, mEntries.get(index), true);
                        return true;
                    }
                });
            }
            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int index = holder.getAdapterPosition();
                    mCallback.onItemClick(index, v, mEntries.get(index), false);
                }
            });
            holder.image.setBackgroundColor(mDefaultImageBackground);
            ViewCompat.setTransitionName(holder.image, "view_" + position);
        } else {
            holder.view.setBackground(null);
        }

        if (entry.isAlbum()) {
            holder.titleFrame.setVisibility(View.VISIBLE);
            holder.title.setText(entry.displayName());
            if (((AlbumEntry) entry).mFirstPath == null) {
                holder.image.setBackgroundColor(mEmptyImageBackground);
                if (holder.subTitle != null)
                    holder.subTitle.setText("0");
            } else if (entry.size() == 1) {
                if (holder.subTitle != null)
                    holder.subTitle.setText("1");
            } else if (holder.subTitle != null) {
                holder.subTitle.setText("" + entry.size());
            }
            holder.image.load(entry, holder.imageProgress);
        } else if (entry.isFolder()) {
            holder.image.setBackground(null);
            holder.titleFrame.setVisibility(View.VISIBLE);
            holder.title.setText(entry.displayName());
            if (holder.imageProgress != null)
                holder.imageProgress.setVisibility(View.GONE);
            holder.image.setImageResource(R.drawable.ic_folder);

            if (!mGridMode) {
                if (holder.subTitle != null) {
                    holder.subTitle.setVisibility(View.VISIBLE);
                    holder.subTitle.setText(R.string.folder);
                }
            } else if (holder.subTitle != null) {
                holder.subTitle.setVisibility(View.GONE);
            }
        } else {
            if (mGridMode) {
                holder.titleFrame.setVisibility(View.GONE);
            } else {
                holder.titleFrame.setVisibility(View.VISIBLE);
                holder.title.setText(entry.displayName());
                if (holder.subTitle != null) {
                    holder.subTitle.setVisibility(View.VISIBLE);
                    holder.subTitle.setText(entry.mimeType());
                }
            }
            holder.image.load(entry, holder.imageProgress);
        }
    }

    @Override
    public int getItemCount() {
        return mEntries.size();
    }

    public enum FileFilterMode {
        ALL(0),
        PHOTOS(1),
        VIDEOS(2);

        private final int value;

        FileFilterMode(int value) {
            this.value = value;
        }

        public static FileFilterMode valueOf(int value) {
            switch (value) {
                default:
                    return ALL;
                case 1:
                    return PHOTOS;
                case 2:
                    return VIDEOS;
            }
        }

        public int value() {
            return value;
        }
    }

    public enum SortMode {
        NAME_ASC(0),
        NAME_DESC(1),
        MODIFIED_DATE_ASC(2),
        MODIFIED_DATE_DESC(3);

        public static final int DEFAULT = 2;
        private final int value;

        SortMode(int value) {
            this.value = value;
        }

        public static SortMode valueOf(int value) {
            switch (value) {
                default:
                    return NAME_ASC;
                case 1:
                    return NAME_DESC;
                case 2:
                    return MODIFIED_DATE_ASC;
                case 3:
                    return MODIFIED_DATE_DESC;
            }
        }

        public int value() {
            return value;
        }
    }

    public interface Callback {
        void onItemClick(int index, View view, MediaEntry pic, boolean longClick);
    }

    public static class MediaNameSorter implements Comparator<MediaEntry> {

        private final boolean desc;

        public MediaNameSorter(boolean desc) {
            this.desc = desc;
        }

        @Override
        public int compare(MediaEntry lhs, MediaEntry rhs) {
            String right = rhs.displayName();
            String left = lhs.displayName();
            if (right == null) right = "";
            if (left == null) left = "";
            if (desc) {
                return right.compareTo(left);
            } else {
                return left.compareTo(right);
            }
        }
    }

    public static class MediaModifiedSorter implements Comparator<MediaEntry> {

        private final boolean desc;

        public MediaModifiedSorter(boolean desc) {
            this.desc = desc;
        }

        @Override
        public int compare(MediaEntry lhs, MediaEntry rhs) {
            Long right;
            Long left;
            if (rhs != null) right = rhs.dateModified();
            else right = 0l;
            if (lhs != null) left = lhs.dateModified();
            else left = 0l;

            if (desc) {
                return left.compareTo(right);
            } else {
                return right.compareTo(left);
            }
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public final View view;
        public final ImpressionImageView image;
        public final View imageProgress;
        public final View titleFrame;
        public final TextView title;
        public final TextView subTitle;

        public ViewHolder(View v) {
            super(v);
            view = v;
            image = (ImpressionImageView) v.findViewById(R.id.image);
            imageProgress = v.findViewById(R.id.imageProgress);
            titleFrame = v.findViewById(R.id.titleFrame);
            title = (TextView) v.findViewById(R.id.title);
            subTitle = (TextView) v.findViewById(R.id.subTitle);
        }
    }
}