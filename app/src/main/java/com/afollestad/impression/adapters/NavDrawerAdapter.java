package com.afollestad.impression.adapters;

import android.content.Context;
import android.graphics.PorterDuff;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.impression.R;
import com.afollestad.impression.api.AlbumEntry;
import com.afollestad.impression.ui.base.ThemedActivity;
import com.afollestad.impression.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Aidan Follestad (afollestad)
 */
public class NavDrawerAdapter extends RecyclerView.Adapter<NavDrawerAdapter.ViewHolder> implements View.OnClickListener, View.OnLongClickListener {

    private final Context mContext;
    private final List<Entry> mEntries;
    private final Callback mCallback;
    private int mCheckedItem;

    public NavDrawerAdapter(Context context, Callback callback) {
        mContext = context;
        mEntries = new ArrayList<>();
        mCallback = callback;
    }

    public void clear() {
        mEntries.clear();
    }

    public void setItemChecked(String path) {
        if (path == null)
            path = AlbumEntry.ALBUM_OVERVIEW;
        for (int i = 0; i < mEntries.size(); i++) {
            String entryPath = mEntries.get(i).getPath();
            if (entryPath.equals(path)) {
                setItemChecked(i);
                break;
            }
        }
    }

    public void setItemChecked(int index) {
        mCheckedItem = index;
        notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        if (mCallback != null) {
            Integer index = (Integer) v.getTag();
            Entry entry = mEntries.get(index);
            mCallback.onEntrySelected(index, entry, false);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (mCallback != null) {
            Integer index = (Integer) v.getTag();
            Entry entry = mEntries.get(index);
            if (entry.isAdd()) return false;
            mCallback.onEntrySelected(index, entry, true);
            return true;
        }
        return false;
    }

    public void add(Entry entry) {
        if (mEntries.contains(entry)) return;
        mEntries.add(entry);
    }

    public void update(Entry entry) {
        boolean found = false;
        for (int i = 0; i < mEntries.size(); i++) {
            if (mEntries.get(i).getPath().equals(entry.getPath())) {
                mEntries.get(i).copy(entry);
                found = true;
                break;
            }
        }
        if (!found)
            mEntries.add(entry);
    }

    public Entry get(int index) {
        return mEntries.get(index);
    }

    public void remove(int index) {
        mEntries.remove(index);
    }

    @Override
    public NavDrawerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_drawer, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Entry entry = mEntries.get(position);
        if (entry.isAdd()) {
            holder.textView.setText(R.string.include_folder);
            holder.vivider.setVisibility(position > 0 && !mEntries.get(position - 1)
                    .isIncluded() ? View.VISIBLE : View.GONE);
            holder.icon.setVisibility(View.VISIBLE);
            holder.icon.getDrawable().mutate().setColorFilter(
                    Utils.resolveColor(mContext, android.R.attr.textColorPrimary), PorterDuff.Mode.SRC_ATOP);
        } else if (entry.getPath().equals(AlbumEntry.ALBUM_OVERVIEW)) {
            holder.textView.setText(R.string.overview);
            holder.vivider.setVisibility(View.GONE);
            holder.icon.setVisibility(View.GONE);
        } else {
            holder.icon.setVisibility(View.GONE);
            if (entry.isIncluded()) {
                holder.vivider.setVisibility(position > 0 && !mEntries.get(position - 1)
                        .isIncluded() ? View.VISIBLE : View.GONE);
            } else {
                holder.vivider.setVisibility(View.GONE);
            }
            holder.textView.setText(entry.getName());
        }
        holder.view.setTag(position);
        holder.view.setActivated(mCheckedItem == position);
        if (holder.view.isActivated()) {
            holder.textView.setTextColor(((ThemedActivity) mContext).accentColor());
        } else {
            holder.textView.setTextColor(Utils.resolveColor(mContext, android.R.attr.textColorPrimary));
        }
        holder.view.setOnClickListener(this);
        if (position > 0)
            holder.view.setOnLongClickListener(this);
        else holder.view.setOnLongClickListener(null);
    }

    @Override
    public int getItemCount() {
        return mEntries.size();
    }

    public void notifyDataSetChangedAndSort() {
        Collections.sort(mEntries, new NavDrawerSorter());
        super.notifyDataSetChanged();
    }

    public interface Callback {
        void onEntrySelected(int index, Entry entry, boolean longClick);
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {

        final View view;
        final View vivider;
        final ImageView icon;
        final TextView textView;

        public ViewHolder(View v) {
            super(v);
            view = v.findViewById(R.id.viewFrame);
            vivider = ((ViewGroup) v).getChildAt(0);
            icon = (ImageView) v.findViewById(R.id.icon);
            textView = (TextView) v.findViewById(R.id.title);
        }
    }

    private static class NavDrawerSorter implements Comparator<Entry> {
        @Override
        public int compare(Entry lhs, Entry rhs) {
            if (lhs.isAdd()) {
                return 1;
            } else if (rhs.isAdd()) {
                return -1;
            } else if (lhs.getPath().equals(AlbumEntry.ALBUM_OVERVIEW)) return -1;
            else if (rhs.getPath().equals(AlbumEntry.ALBUM_OVERVIEW)) return 1;
            else if (lhs.isIncluded() && !rhs.isIncluded()) {
                return 1;
            } else if (!lhs.isIncluded() && rhs.isIncluded()) {
                return -1;
            } else {
                return lhs.getName().compareTo(rhs.getName());
            }
        }
    }

    public static class Entry {

        private final String mPath;
        private boolean mAdd;
        private boolean mIncluded;

        public Entry(String path, boolean add, boolean included) {
            mPath = path;
            mAdd = add;
            mIncluded = included;
        }

        public String getName() {
            if (mPath.contains(File.separator)) {
                return mPath.substring(mPath.lastIndexOf(File.separatorChar) + 1);
            } else return mPath;
        }

        public String getPath() {
            return mPath;
        }

        public boolean isAdd() {
            return mAdd;
        }

        public boolean isIncluded() {
            return mIncluded;
        }

        public void copy(Entry other) {
            this.mAdd = other.isAdd();
            this.mIncluded = other.isIncluded();
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Entry)) return false;
            Entry oe = (Entry) o;
            return oe.mPath.equals(mPath) && oe.mAdd == mAdd && oe.mIncluded == mIncluded;
        }
    }
}