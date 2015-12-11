package com.afollestad.impression.navdrawer;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.impression.R;
import com.afollestad.impression.accounts.base.Account;
import com.afollestad.impression.api.MediaFolderEntry;
import com.afollestad.impression.base.ThemedActivity;
import com.afollestad.impression.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class NavDrawerAdapter extends RecyclerView.Adapter<NavDrawerAdapter.ViewHolder> {

    public static final long OVERVIEW_ID = -1;
    public static final long HEADER_ID = -2;
    public static final long ADD_ACCOUNT_ID = -3;
    public static final long SETTINGS_ID = -4;
    public static final long ABOUT_ID = -5;

    public static final long[] FOOTER_ITEM_IDS = {SETTINGS_ID, ABOUT_ID};
    public static final int[] FOOTER_ITEM_STRINGS = {R.string.settings, R.string.about};
    public static final int[] FOOTER_ITEM_ICONS = {R.drawable.ic_settings_white, R.drawable.ic_info_white};


    private static final String STATE_SELECTED_ID = "selected_navigation_drawer_id";

    private static final int VIEW_TYPE_MEDIA_FOLDERS = 42;
    private static final int VIEW_TYPE_ACCOUNTS = 23;
    private static final int VIEW_TYPE_HEADER = 13;

    private static final int PAYLOAD_UPDATE_ACTIVATED = 23;
    private static final int PAYLOAD_UPDATE_ACCOUNTS = 12;

    private final Context mContext;

    private final List<Entry> mEntries;
    private final List<Account> mAccounts;
    private final Callback mCallback;
    private long mCheckedId;
    private int mCurrentAccountId;
    private boolean mShowingAccounts;


    public NavDrawerAdapter(Context context, Callback callback) {
        mContext = context;
        mEntries = new ArrayList<>();
        mAccounts = new ArrayList<>();
        mCallback = callback;
        mCheckedId = OVERVIEW_ID;
        mShowingAccounts = false;

        setHasStableIds(true);
    }

    public void saveInstanceState(Bundle out) {
        out.putLong(STATE_SELECTED_ID, mCheckedId);
    }

    public void restoreInstanceState(Bundle in) {
        setCheckedItemId(in.getLong(STATE_SELECTED_ID));
    }

    public void clear() {
        mEntries.clear();
    }

    public void setCheckedItemId(long id) {
        for (int i = 0, entriesSize = mEntries.size(); i < entriesSize; i++) {
            Entry entry = mEntries.get(i);
            if (entry.getId() == id) {
                notifyItemChanged(i + 1, PAYLOAD_UPDATE_ACTIVATED);
            }
            if (entry.getId() == mCheckedId) {
                notifyItemChanged(i + 1, PAYLOAD_UPDATE_ACTIVATED);
            }
        }
        mCheckedId = id;
    }

    public void add(Entry entry) {
        mEntries.add(entry);
    }

    /*public void update(Entry entry) {
        boolean found = false;
        for (int i = 0; i < mEntries.size(); i++) {
            if (mEntries.get(i).getPath().equals(entry.getPath())) {
                mEntries.get(i).copy(entry);
                found = true;
                break;
            }
        }
        if (!found) {
            mEntries.add(entry);
        }
    }*/

    public Entry getEntryAtPosition(int index) {
        return mEntries.get(index);
    }

    public Entry getSelectedEntry() {
        for (Entry entry : mEntries) {
            if (entry.getId() == mCheckedId) {
                return entry;
            }
        }
        return null;
    }

    public long getSelectedId() {
        return mCheckedId;
    }

    public void removeMediaEntry(int index) {
        if (mEntries.get(index).getId() == mCheckedId) {
            int newSelectedPosition = index;
            if (newSelectedPosition == mEntries.size() - 1) {
                newSelectedPosition--;
            }

            mCheckedId = mEntries.get(newSelectedPosition).getId();
        }

        mEntries.remove(index);
        notifyDataSetChanged();
    }

    public void setShowingAccounts(boolean show) {
        mShowingAccounts = show;
        notifyDataSetChanged();
    }

    public void setAccounts(List<Account> accounts) {
        mAccounts.clear();
        mAccounts.addAll(accounts);
    }

    public void setCurrentAccountId(int currentAccountId) {
        mCurrentAccountId = currentAccountId;
    }

    public Account getCurrentAccount() {
        for (Account account : mAccounts) {
            if (account.id() == mCurrentAccountId) {
                return account;
            }
        }
        return null;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return VIEW_TYPE_HEADER;
        } else {
            return mShowingAccounts ? VIEW_TYPE_ACCOUNTS : VIEW_TYPE_MEDIA_FOLDERS;
        }
    }

    @Override
    public NavDrawerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int view = R.layout.list_item_drawer;
        switch (viewType) {
            case VIEW_TYPE_HEADER:
                view = R.layout.list_item_drawer_header;
                break;
            case VIEW_TYPE_ACCOUNTS:
            case VIEW_TYPE_MEDIA_FOLDERS:
                view = R.layout.list_item_drawer;
                break;
        }
        View v = LayoutInflater.from(parent.getContext()).inflate(view, parent, false);
        return new ViewHolder(v, viewType);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position, List<Object> payloads) {
        if (payloads.contains(PAYLOAD_UPDATE_ACTIVATED)) {
            updateFolderEntryActivation(holder, position - 1);
        } else {
            super.onBindViewHolder(holder, position, payloads);
        }

    }

    private void updateFolderEntryActivation(ViewHolder holder, int entryPosition) {
        holder.viewFrame.setActivated(mCheckedId == mEntries.get(entryPosition).getId());
        if (holder.viewFrame.isActivated()) {
            holder.textView.setTextColor(((ThemedActivity) mContext).accentColor());
        } else {
            holder.textView.setTextColor(Utils.resolveColor(mContext, android.R.attr.textColorPrimary));
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (holder.isViewTypeHeader) {


        } else if (holder.isViewTypeAccount) {
            position = position - 1;

            if (position < mAccounts.size()) {
                Account account = mAccounts.get(position);
                holder.textView.setText(account.name());
                holder.icon.setVisibility(View.GONE);
                holder.divider.setVisibility(View.GONE);
            } else {
                holder.textView.setText(R.string.add_account);
                holder.icon.setImageResource(R.drawable.ic_add_white);
                holder.icon.setVisibility(View.VISIBLE);
                holder.divider.setVisibility(View.VISIBLE);
            }
        } else {
            position = position - 1;

            if (position < mEntries.size()) {
                Entry entry = mEntries.get(position);

                holder.icon.setVisibility(View.GONE);

                holder.divider.setVisibility(View.GONE);
                holder.textView.setText(entry.getName(mContext));

                updateFolderEntryActivation(holder, position);
            } else {
                int footerPosition = position - mEntries.size();
                holder.textView.setText(FOOTER_ITEM_STRINGS[(footerPosition)]);
                holder.icon.setImageResource(FOOTER_ITEM_ICONS[(footerPosition)]);
                holder.icon.setVisibility(View.VISIBLE);
                holder.divider.setVisibility(footerPosition == 0 ? View.VISIBLE : View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        //+1 for the header, +1 in accounts for add account, +2 for settings/about
        return 1 + (mShowingAccounts ? mAccounts.size() + 1 : mEntries.size() + FOOTER_ITEM_IDS.length);
    }

    public void notifyDataSetChangedAndSort() {
        Collections.sort(mEntries, new NavDrawerSorter());
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        if (position == 0) {
            return HEADER_ID;
        } else if (mShowingAccounts) {
            position = position - 1;
            if (position < mAccounts.size()) {
                return mAccounts.get(position).id();
            } else {
                return ADD_ACCOUNT_ID;
            }
        } else {
            position = position - 1;
            if (position < mEntries.size()) {
                return mEntries.get(position).getId();
            } else {
                return FOOTER_ITEM_IDS[position - mEntries.size()];
            }
        }
    }

    public interface Callback {
        void onEntrySelected(int index, Entry entry, boolean longClick);

        void onAccountSelected(Account account);

        void onAddAccountPressed();

        void onSpecialItemPressed(long id);
    }

    public static class Entry {

        private final long mId;
        private final String mPath;

        public Entry(String path, long id) {
            mPath = path;
            mId = id;
        }

        public String getName(Context context) {
            if (mPath.equals(MediaFolderEntry.OVERVIEW_PATH)) {
                return context.getString(R.string.overview);
            } else if (mPath.equals(Environment.getExternalStorageDirectory().getAbsolutePath())) {
                return context.getString(R.string.internal_storage);
            } else if (mPath.contains(File.separator)) {
                return mPath.substring(mPath.lastIndexOf(File.separatorChar) + 1);
            } else {
                return mPath;
            }
        }

        public String getPath() {
            return mPath;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Entry)) {
                return false;
            }
            Entry oe = (Entry) o;
            return oe.mPath.equals(mPath);
        }

        public long getId() {
            return mId;
        }
    }

    private class NavDrawerSorter implements Comparator<Entry> {
        @Override
        public int compare(Entry lhs, Entry rhs) {
            if (lhs.getPath().equals(MediaFolderEntry.OVERVIEW_PATH)) {
                return -1;
            } else if (rhs.getPath().equals(MediaFolderEntry.OVERVIEW_PATH)) {
                return 1;
            } else {
                return lhs.getName(mContext).compareTo(rhs.getName(mContext));
            }
        }
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {

        final boolean isViewTypeHeader;
        final boolean isViewTypeAccount;

        final View viewFrame;

        final ImageView headerImage;
        final TextView headerSubtitle;
        final ImageButton dropdownButton;

        final View divider;
        final ImageView icon;
        final TextView textView;

        public ViewHolder(View v, int viewType) {
            super(v);
            viewFrame = v.findViewById(R.id.viewFrame);

            if (viewType == VIEW_TYPE_HEADER) {
                headerImage = (ImageView) v.findViewById(R.id.accountHeaderImage);
                divider = null;
                icon = null;
                headerSubtitle = (TextView) v.findViewById(R.id.subtitle);
                isViewTypeHeader = true;
                isViewTypeAccount = false;
                dropdownButton = (ImageButton) v.findViewById(R.id.dropdown);
            } else {
                headerImage = null;
                divider = ((ViewGroup) v).getChildAt(0);
                icon = (ImageView) v.findViewById(R.id.icon);
                headerSubtitle = null;
                isViewTypeHeader = false;
                isViewTypeAccount = viewType == VIEW_TYPE_ACCOUNTS;
                dropdownButton = null;
            }
            textView = (TextView) v.findViewById(R.id.title);


            if (viewType == VIEW_TYPE_MEDIA_FOLDERS) {
                viewFrame.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int index = getAdapterPosition();
                        if (index == RecyclerView.NO_POSITION) {
                            return;
                        }

                        index = index - 1;

                        if (index < mEntries.size()) {
                            Entry entry = mEntries.get(index);
                            setCheckedItemId(entry.getId());

                            mCallback.onEntrySelected(index, entry, false);
                        } else {
                            mCallback.onSpecialItemPressed(getItemId());
                        }
                    }
                });

                viewFrame.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        int index = getAdapterPosition();
                        if (index == RecyclerView.NO_POSITION) {
                            return false;
                        }

                        index = index - 1;

                        if (index < mEntries.size() && mCallback != null) {
                            Entry entry = mEntries.get(index);

                            mCallback.onEntrySelected(index, entry, true);
                            return true;
                        }
                        return false;
                    }
                });
            } else if (viewType == VIEW_TYPE_ACCOUNTS) {
                viewFrame.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int index = getAdapterPosition();
                        if (getAdapterPosition() == RecyclerView.NO_POSITION) {
                            return;
                        }
                        index = index - 1;

                        if (index < mAccounts.size()) {
                            mCallback.onAccountSelected(mAccounts.get(index));
                        } else {
                            mCallback.onAddAccountPressed();
                        }
                    }
                });
            } else if (viewType == VIEW_TYPE_HEADER) {
                dropdownButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setShowingAccounts(!mShowingAccounts);
                    }
                });
            }
        }
    }
}