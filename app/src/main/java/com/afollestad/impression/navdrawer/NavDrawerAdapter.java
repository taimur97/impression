package com.afollestad.impression.navdrawer;

import android.content.Context;
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
    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";
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

    public void clear() {
        mEntries.clear();
    }

    /*public void setItemChecked(String path) {
        if (path == null)
            path = MediaFolderEntry.OVERVIEW_PATH;
        for (int i = 0; i < mEntries.size(); i++) {
            String entryPath = mEntries.get(i).getPath();
            if (entryPath.equals(path)) {
                setItemChecked(i);
                break;
            }
        }
    }*/

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
            updateActivation(holder, position);
        } else {
            super.onBindViewHolder(holder, position, payloads);
        }

    }

    private void updateActivation(ViewHolder holder, int position) {
        holder.viewFrame.setActivated(mCheckedId == mEntries.get(position - 1).getId());
        if (holder.viewFrame.isActivated()) {
            holder.textView.setTextColor(((ThemedActivity) mContext).accentColor());
        } else {
            holder.textView.setTextColor(Utils.resolveColor(mContext, android.R.attr.textColorPrimary));
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (holder.isHeader) {
            /*ViewGroup.LayoutParams params  = holder.headerImage.getLayoutParams();
            int headerImageHeight = Utils.getNavDrawerWidth(mContext) * 9 / 16;
            int headerImageAppliedHeight= headerImageHeight;
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP){
                int statusBarHeight = Utils.getStatusBarHeight(mContext);
                headerImageAppliedHeight-=statusBarHeight;
            }
            params.height = headerImageAppliedHeight;
            holder.headerImage.setLayoutParams(params);

            ViewGroup.LayoutParams viewFrame  = holder.viewFrame.getLayoutParams();
            params.height = headerImageHeight + mContext.getResources().getDimensionPixelSize(R.dimen.nav_drawer_header_image_bottom_margin);
            holder.viewFrame.setLayoutParams(viewFrame);*/


        } else if (holder.isAccount) {
            Account account = mAccounts.get(position - 1);
            holder.textView.setText(account.name());
            holder.icon.setVisibility(View.GONE);
            holder.divider.setVisibility(View.GONE);
        } else {

            Entry entry = mEntries.get(position - 1);
            /*if (entry.isAdd()) {
                holder.textView.setText(R.string.include_folder);
                holder.divider.setVisibility(position > 0 && !mEntries.get(position - 1)
                        .isIncluded() ? View.VISIBLE : View.GONE);
                holder.icon.setVisibility(View.VISIBLE);
                holder.icon.getDrawable().mutate().setColorFilter(
                        Utils.resolveColor(mContext, android.R.attr.textColorPrimary), PorterDuff.Mode.SRC_ATOP);
            } else if (entry.getPath().equals(MediaFolderEntry.OVERVIEW_PATH)) {
                holder.textView.setText(R.string.overview);
                holder.divider.setVisibility(View.GONE);
                holder.icon.setVisibility(View.GONE);
            } else {*/
            holder.icon.setVisibility(View.GONE);
            /*if (entry.isIncluded()) {
                holder.divider.setVisibility(position > 0 && !mEntries.get(position - 1)
                        .isIncluded() ? View.VISIBLE : View.GONE);
            } else {*/
            holder.divider.setVisibility(View.GONE);
            /*}*/
            holder.textView.setText(entry.getName(mContext));
            //}
            updateActivation(holder, position);
        }
    }

    @Override
    public int getItemCount() {
        //+1 for the header
        return (mShowingAccounts ? mAccounts.size() : mEntries.size()) + 1;
    }

    public void notifyDataSetChangedAndSort() {
        Collections.sort(mEntries, new NavDrawerSorter());
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        if (position == 0) return HEADER_ID;
        return mShowingAccounts ? mAccounts.get(position - 1).id() : mEntries.get(position - 1).getId();
    }

    public interface Callback {
        void onEntrySelected(int index, Entry entry, boolean longClick);

        void onAccountSelected(Account account);
    }

    public static class Entry {

        private final long mId;
        private final String mPath;
       /* private boolean mIsAddIncludedFolderEntry;
        private boolean mIsIncludedFolder;*/

        public Entry(String path, long id, boolean add, boolean included) {
            mPath = path;
            mId = id;
            /*mIsAddIncludedFolderEntry = add;
            mIsIncludedFolder = included;*/
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

        public boolean isAdd() {
            /*return mIsAddIncludedFolderEntry;*/
            return false;
        }

        public boolean isIncluded() {
            /*return mIsIncludedFolder;*/
            return false;
        }

        public void copy(Entry other) {
            /*this.mIsAddIncludedFolderEntry = other.isAdd();
            this.mIsIncludedFolder = other.isIncluded();*/
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Entry)) {
                return false;
            }
            Entry oe = (Entry) o;
            return oe.mPath.equals(mPath) /*&& oe.mIsAddIncludedFolderEntry == mIsAddIncludedFolderEntry && oe.mIsIncludedFolder == mIsIncludedFolder*/;
        }

        public long getId() {
            return mId;
        }
    }

    private class NavDrawerSorter implements Comparator<Entry> {
        @Override
        public int compare(Entry lhs, Entry rhs) {
            if (lhs.isAdd()) {
                return 1;
            } else if (rhs.isAdd()) {
                return -1;
            } else if (lhs.getPath().equals(MediaFolderEntry.OVERVIEW_PATH)) {
                return -1;
            } else if (rhs.getPath().equals(MediaFolderEntry.OVERVIEW_PATH)) {
                return 1;
            } else if (lhs.isIncluded() && !rhs.isIncluded()) {
                return 1;
            } else if (!lhs.isIncluded() && rhs.isIncluded()) {
                return -1;
            } else {
                return lhs.getName(mContext).compareTo(rhs.getName(mContext));
            }
        }
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {

        final boolean isHeader;
        final boolean isAccount;

        final View viewFrame;

        final ImageView headerImage;
        final TextView headerSubtitle;
        final ImageButton dropdown;

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
                isHeader = true;
                isAccount = false;
                dropdown = (ImageButton) v.findViewById(R.id.dropdown);
            } else {
                headerImage = null;
                divider = ((ViewGroup) v).getChildAt(0);
                icon = (ImageView) v.findViewById(R.id.icon);
                headerSubtitle = null;
                isHeader = false;
                isAccount = viewType == VIEW_TYPE_ACCOUNTS;
                dropdown = null;
            }
            textView = (TextView) v.findViewById(R.id.title);


            if (viewType == VIEW_TYPE_MEDIA_FOLDERS) {
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        int index = getAdapterPosition();
                        index = index - 1;

                        Entry entry = mEntries.get(index);
                        setCheckedItemId(entry.getId());


                        if (mCallback != null) {
                            mCallback.onEntrySelected(index, entry, false);
                        }
                    }
                });

                v.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        int index = getAdapterPosition();
                        index = index - 1;

                        if (mCallback != null && index > 0) {
                            Entry entry = mEntries.get(index);


                            if (entry.isAdd()) {
                                return false;
                            }
                            mCallback.onEntrySelected(index, entry, true);
                            return true;
                        }
                        return false;
                    }
                });
            } else if (viewType == VIEW_TYPE_ACCOUNTS) {
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
            } else if (viewType == VIEW_TYPE_HEADER) {
                dropdown.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setShowingAccounts(!mShowingAccounts);
                    }
                });
            }
        }
    }
}