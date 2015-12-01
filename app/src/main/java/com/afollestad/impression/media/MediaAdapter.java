package com.afollestad.impression.media;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.IntDef;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.impression.R;
import com.afollestad.impression.api.MediaEntry;
import com.afollestad.impression.utils.PrefUtils;
import com.afollestad.impression.utils.Utils;
import com.afollestad.impression.widget.ImpressionThumbnailImageView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Aidan Follestad (afollestad)
 */
public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.ViewHolder> {

    public static final int SORT_NAME_ASC = 0;
    public static final int SORT_NAME_DESC = 1;
    public static final int SORT_TAKEN_DATE_ASC = 2;
    public static final int SORT_TAKEN_DATE_DESC = 3;
    public static final int SORT_NOSORT = 4;
    public static final int FILTER_ALL = 0;
    public static final int FILTER_PHOTOS = 1;
    public static final int FILTER_VIDEOS = 2;
    public static final int VIEW_TYPE_GRID = 0;
    public static final int VIEW_TYPE_GRID_FOLDER = 1;
    public static final int VIEW_TYPE_LIST = 2;
    private static final int ACTIVATION_UPDATE = -42;
    private static final String TAG = "MediaAdapter";

    private final Context mContext;
    private final Callback mCallback;
    private final List<String> mCheckedPaths;
    private final boolean mSelectAlbumMode;
    private List<MediaEntry> mEntries;
    @SortMode
    private int mSortMode;
    private boolean mGridMode;
    private boolean mExplorerMode;
    private int mDefaultImageBackground;
    private int mEmptyImageBackground;

    public MediaAdapter(Context context, @SortMode int sort, Callback callback, boolean selectAlbumMode) {
        mContext = context;
        mSortMode = sort;
        mGridMode = PrefUtils.isGridMode(context);
        mExplorerMode = PrefUtils.isExplorerMode(context);
        mCallback = callback;
        mCheckedPaths = new ArrayList<>();
        mSelectAlbumMode = selectAlbumMode;

        mDefaultImageBackground = Utils.resolveColor(context, R.attr.default_image_background);
        mEmptyImageBackground = Utils.resolveColor(context, R.attr.empty_image_background);

        mEntries = CurrentMediaEntriesSingleton.getInstance().getMediaEntriesCopy(mContext, mSortMode);

        setHasStableIds(true);
    }

    @Override
    public int getItemViewType(int position) {
        if (!mGridMode) {
            return VIEW_TYPE_LIST;
        } else if (mEntries.get(position).isFolder() && mExplorerMode) {
            return VIEW_TYPE_GRID_FOLDER;
        } else {
            return VIEW_TYPE_GRID;
        }
    }

    public void setItemChecked(MediaEntry entry, boolean checked) {
        if (checked) {
            if (!mCheckedPaths.contains(entry.data())) {
                mCheckedPaths.add(entry.data());
            }
            for (int i = 0; i < mEntries.size(); i++) {
                if (mEntries.get(i).data() != null &&
                        mEntries.get(i).data().equals(entry.data())) {
                    notifyItemChanged(i, ACTIVATION_UPDATE);
                    break;
                }
            }
        } else {
            if (mCheckedPaths.contains(entry.data())) {
                mCheckedPaths.remove(entry.data());
            }
            for (int i = 0; i < mEntries.size(); i++) {
                if (mEntries.get(i).data().equals(entry.data())) {
                    notifyItemChanged(i, ACTIVATION_UPDATE);
                    break;
                }
            }
        }
    }

    public void clearChecked() {
        for (int i = 0; i < mEntries.size(); i++) {
            for (String path : mCheckedPaths) {
                if (mEntries.get(i).data().equals(path)) {
                    notifyItemChanged(i, ACTIVATION_UPDATE);
                    break;
                }
            }
        }
        mCheckedPaths.clear();
    }

    @Override
    public long getItemId(int position) {
        return mEntries.get(position).id();
    }

    public void clear() {
        mEntries.clear();
        notifyDataSetChanged();
    }

    public void updateEntriesAndSort() {
        mEntries = CurrentMediaEntriesSingleton.getInstance().getMediaEntriesCopy(mContext, mSortMode);
        notifyDataSetChanged();
    }

    public void updateGridModeOn() {
        mGridMode = PrefUtils.isGridMode(mContext);
        notifyDataSetChanged();
    }

    public void updateGridColumns() {
        notifyDataSetChanged();
    }

    public void updateSortMode(@SortMode int mode) {
        mSortMode = mode;
        updateEntriesAndSort();
    }

    public void updateExplorerMode() {
        mExplorerMode = PrefUtils.isExplorerMode(mContext);
        //Don't notifyDataSetChanged() because setPath() will be called and then reload().
    }

    public void updateTheme() {
        mDefaultImageBackground = Utils.resolveColor(mContext, R.attr.default_image_background);
        mEmptyImageBackground = Utils.resolveColor(mContext, R.attr.empty_image_background);
        notifyDataSetChanged();
    }

    /**
     * Use with caution. Don't modify.
     */
    public List<MediaEntry> getEntries() {
        return mEntries;
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

    /**
     * These payloads are awesome. More people should use these. USE THESE PEOPLE
     */
    @Override
    public void onBindViewHolder(ViewHolder holder, int position, List<Object> payloads) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads);
        } else {
            if (payloads.contains(ACTIVATION_UPDATE)) {
                holder.view.setActivated(mCheckedPaths.contains(mEntries.get(position).data()));
            }
        }
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        MediaEntry entry = mEntries.get(position);

        if (!mSelectAlbumMode || (entry.isFolder()/*|| entry.isAlbum()*/)) {
            holder.view.setActivated(mCheckedPaths.contains(entry.data()));
            if (!mSelectAlbumMode) {
                holder.view.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        int index = holder.getAdapterPosition();
                        if (index != RecyclerView.NO_POSITION) {
                            mCallback.onItemClick(index, v, mEntries.get(index), true);
                        }
                        return true;
                    }
                });
            }
            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int index = holder.getAdapterPosition();
                    if (index != RecyclerView.NO_POSITION) {
                        mCallback.onItemClick(index, v, mEntries.get(index), false);
                    }
                }
            });
            holder.image.setBackgroundColor(mDefaultImageBackground);
            ViewCompat.setTransitionName(holder.image, "view_" + position);
        } else {
            holder.view.setBackground(null);
        }

        if (entry.isFolder() && !mExplorerMode) {
            holder.titleFrame.setVisibility(View.VISIBLE);
            holder.title.setText(entry.displayName(mContext));
            if (entry.data() == null) {
                holder.image.setBackgroundColor(mEmptyImageBackground);
                if (holder.subTitle != null) {
                    holder.subTitle.setText("0");
                }
            }
            //TODO
            /*else if (entry.size() == 1) {
                if (holder.subTitle != null)
                    holder.subTitle.setText("1");
            } else if (holder.subTitle != null) {
                holder.subTitle.setText(String.valueOf(entry.size()));
            }*/
            holder.image.load(entry, holder.imageProgress);
        } else if (entry.isFolder() && mExplorerMode) {
            holder.image.setBackgroundColor(mEmptyImageBackground);
            holder.titleFrame.setVisibility(View.VISIBLE);
            holder.title.setText(entry.displayName(mContext));
            if (holder.imageProgress != null) {
                holder.imageProgress.setVisibility(View.GONE);
            }
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
                holder.title.setText(entry.displayName(mContext));
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

    @SuppressLint("UniqueConstants")
    @IntDef({SORT_NAME_ASC, SORT_NAME_DESC, SORT_TAKEN_DATE_ASC, SORT_TAKEN_DATE_DESC, SORT_NOSORT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface SortMode {
    }

    @IntDef({FILTER_ALL, FILTER_PHOTOS, FILTER_VIDEOS})
    @Retention(RetentionPolicy.SOURCE)
    public @interface FileFilterMode {
    }


    public interface Callback {
        void onItemClick(int index, View view, MediaEntry pic, boolean longClick);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public final View view;
        public final ImpressionThumbnailImageView image;
        public final View imageProgress;
        public final View titleFrame;
        public final TextView title;
        public final TextView subTitle;

        public ViewHolder(View v) {
            super(v);
            view = v;
            image = (ImpressionThumbnailImageView) v.findViewById(R.id.image);
            imageProgress = v.findViewById(R.id.imageProgress);
            titleFrame = v.findViewById(R.id.titleFrame);
            title = (TextView) v.findViewById(R.id.title);
            subTitle = (TextView) v.findViewById(R.id.subTitle);
        }
    }
}