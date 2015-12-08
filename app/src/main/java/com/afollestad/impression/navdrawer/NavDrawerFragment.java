package com.afollestad.impression.navdrawer;

import android.Manifest;
import android.app.Fragment;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.impression.R;
import com.afollestad.impression.accounts.base.Account;
import com.afollestad.impression.api.MediaFolderEntry;
import com.afollestad.impression.base.ThemedActivity;
import com.afollestad.impression.media.MainActivity;
import com.afollestad.impression.media.MediaAdapter;
import com.afollestad.impression.providers.AccountProvider;
import com.afollestad.impression.providers.ExcludedFolderProvider;
import com.afollestad.impression.utils.PrefUtils;
import com.afollestad.impression.utils.Utils;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.folderselector.FolderChooserDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import rx.SingleSubscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class NavDrawerFragment extends Fragment implements NavDrawerAdapter.Callback {

    private RecyclerView mRecyclerView;

    //private Account mCurrentAccount;

    private NavDrawerAdapter mAdapter;
    //private LinearLayout mAccountsFrame;

    //private List<Account> mAccounts;

    /*private int mSelectedColor;
    private int mRegularColor;*/

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*mSelectedColor = Utils.resolveColor(getActivity(), R.attr.colorAccent);
        mRegularColor = Utils.resolveColor(getActivity(), android.R.attr.textColorPrimary);*/
    }

    public void notifyClosed() {
        View v = getView();
        if (v == null) {
            return;
        }
        View dropdown = v.findViewById(R.id.dropdown);
        if (dropdown.getTag() != null) {
            dropdown.performClick();
        }
    }

    public void reloadAlbums() {
        if (mAdapter.getCurrentAccount() == null) {
            reload();
        } else {
            loadMediaFolders(mAdapter.getCurrentAccount());
        }
    }

    /*private void invalidateAccountViews(View itemView, int currentIndex, int selectedIndex) {
        if (itemView == null) {
            for (int i = 0; i < mAccountsFrame.getChildCount() - 1; i++) {
                invalidateAccountViews(mAccountsFrame.getChildAt(i), i, selectedIndex);
            }
        } else {
            TextView title = (TextView) itemView.findViewById(R.id.title);
            ImageView icon = (ImageView) itemView.findViewById(R.id.icon);

            Account acc = mAccounts.get(currentIndex);
            switch (acc.type()) {
                default:
                    icon.setImageResource(R.drawable.ic_folder);
                    break;
                case Account.TYPE_GOOGLE_DRIVE:
                    icon.setImageResource(R.drawable.ic_drive);
                    break;
                case Account.TYPE_DROPBOX:
                    icon.setImageResource(R.drawable.ic_dropbox);
                    break;
            }

            boolean activated = currentIndex == selectedIndex;
            itemView.setActivated(activated);
            icon.getDrawable().mutate().setColorFilter(activated ? mSelectedColor : mRegularColor, PorterDuff.Mode.SRC_ATOP);
            itemView.setTag(currentIndex);
            title.setText(acc.name());
            title.setTextColor(activated ? mSelectedColor : mRegularColor);
        }
    }*/

    /*private View getAccountView(int index, int selectedIndex, ViewGroup container) {
        RelativeLayout view = (RelativeLayout) getActivity().getLayoutInflater().inflate(R.layout.list_item_drawer_account, container, false);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Integer index = (Integer) v.getTag();
                Account a = mAccounts.get(index);
                if (a.type() == Account.TYPE_GOOGLE_DRIVE) {
                    // TODO
                }
            }
        });
        invalidateAccountViews(view, index, selectedIndex);
        return view;
    }*/

    /*private void showAccountAddDialog() {
        if (getActivity() == null) {
            return;
        }
        new MaterialDialog.Builder(getActivity())
                .title(R.string.add_account)
                .items(R.array.account_options)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog materialDialog, View view, int i, CharSequence s) {
                        // TODO
                        Toast.makeText(getActivity(), "Not implemented", Toast.LENGTH_SHORT).show();
                    }
                }).build().show();
    }*/

    /*private void setupHeader(View view) {
        View addAccountFrame = ((ViewStub) view.findViewById(R.id.addAccountStub)).inflate();
        ((ImageView) addAccountFrame.findViewById(R.id.icon)).getDrawable().mutate().setColorFilter(mRegularColor, PorterDuff.Mode.SRC_ATOP);

        mAccountsFrame = (LinearLayout) view.findViewById(R.id.accountsFrame);
        mAccountsFrame.findViewById(R.id.addAccountFrame).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAccountAddDialog();
            }
        });
        ImageButton dropdown = (ImageButton) view.findViewById(R.id.dropdown);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            dropdown.setBackgroundResource(Utils.resolveDrawable(getActivity(), R.attr.menu_selector));
        }
        dropdown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getTag() == null) {
                    ((ImageButton) v).setImageResource(R.drawable.ic_uparrow);
                    v.setTag("GOUP");
                    View root = getView();
                    if (root != null) {
                        ((TextView) root.findViewById(R.id.accountHeader)).setText(R.string.accounts);
                        root.findViewById(R.id.accountsFrame).setVisibility(View.VISIBLE);
                        root.findViewById(R.id.list).setVisibility(View.GONE);
                    }
                } else {
                    ((ImageButton) v).setImageResource(R.drawable.ic_downarrow);
                    v.setTag(null);
                    View root = getView();
                    if (root != null) {
                        ((TextView) root.findViewById(R.id.accountHeader)).setText(R.string.local);
                        root.findViewById(R.id.accountsFrame).setVisibility(View.GONE);
                        root.findViewById(R.id.list).setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }*/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_navdrawer, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.list);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);
        DefaultItemAnimator animator = new DefaultItemAnimator() {
            @Override
            public boolean canReuseUpdatedViewHolder(RecyclerView.ViewHolder viewHolder) {
                return true;
            }
        };
        animator.setSupportsChangeAnimations(false);
        mRecyclerView.setItemAnimator(animator);

        mAdapter = new NavDrawerAdapter(getActivity(), this);
        mRecyclerView.setAdapter(mAdapter);

        /*setupHeader(view);*/
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final ThemedActivity act = (ThemedActivity) getActivity();

        if (ContextCompat.checkSelfPermission(act, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(act, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 69);
            return;
        }
        reload();
    }

    public void reload() {
        if (getActivity() == null) {
            return;
        }

        final List<Account> allAccounts = new ArrayList<>();

        /*View addAccountFrame = mAccountsFrame.findViewById(R.id.addAccountFrame);
        mAccountsFrame.removeAllViews();
        mAccountsFrame.addView(addAccountFrame);*/
        int currentAccountId = PrefUtils.getActiveAccountId(getActivity());

        if (currentAccountId == -1) {
            Account acc = AccountProvider.add(getActivity(), null, Account.TYPE_LOCAL);
            allAccounts.add(acc);
            PrefUtils.setActiveAccountId(getActivity(), acc.id());
            currentAccountId = acc.id();
        }

        mAdapter.setCurrentAccountId(currentAccountId);

        final int fCurrentAccountId = currentAccountId;
        Account.getAll(getActivity())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Account[]>() {
                    @Override
                    public void call(Account[] accounts) {
                        if (accounts == null || !isAdded()) {
                            return;
                        }
                        for (Account a : accounts) {
                            allAccounts.add(a);
                            boolean selected = a.id() == fCurrentAccountId;
                            //mAccountsFrame.addView(getAccountView(i, selected ? i : -1, mAccountsFrame), i);
                            if (selected) {
                                loadMediaFolders(a);
                            }
                        }
                        mAdapter.setAccounts(allAccounts);

                        /*mAccountsFrame.requestLayout();
                        mAccountsFrame.invalidate();*/
                    }
                });
    }

    public void loadMediaFolders(final Account account) {
        /*if (account != null) {
            mCurrentAccount = account;
        }*/
        mAdapter.clear();
        mAdapter.add(new NavDrawerAdapter.Entry
                (MediaFolderEntry.OVERVIEW_PATH, NavDrawerAdapter.OVERVIEW_ID, false, false));

        account.getMediaFolders(MediaAdapter.SORT_NAME_DESC, MediaAdapter.FILTER_ALL)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleSubscriber<Set<MediaFolderEntry>>() {
                    @Override
                    public void onSuccess(Set<MediaFolderEntry> folderEntries) {
                        for (MediaFolderEntry f : folderEntries) {
                            mAdapter.add(new NavDrawerAdapter.Entry(f.data(), f.id(), false, false));
                        }
                        if (account.supportsIncludedFolders()) {
                            loadIncludedFolders();
                        } else {
                            mAdapter.notifyDataSetChangedAndSort();
                        }
                    }

                    @Override
                    public void onError(Throwable error) {
                        if (getActivity() == null) {
                            return;
                        }
                        Utils.showErrorDialog(getActivity(), error);
                    }
                });
    }

    private void loadIncludedFolders() {
        //TODO
        /*mCurrentAccount.getIncludedFolders(preAlbums, new Account.AlbumCallback() {
            @Override
            public void onAlbums(OldAlbumEntry[] albums) {
                for (OldAlbumEntry a : albums)
                    mAdapter.update(new NavDrawerAdapter.Entry(a.data(), false, true));
                mAdapter.add(new NavDrawerAdapter.Entry("", true, false));
                mAdapter.notifyDataSetChangedAndSort();
            }

            @Override
            public void onError(Exception e) {
                if (getActivity() == null) return;
                Utils.showErrorDialog(getActivity(), e);
            }
        });*/


        //mAdapter.add(new NavDrawerAdapter.Entry("", true, false));
        mAdapter.notifyDataSetChangedAndSort();
    }

    @Override
    public void onEntrySelected(final int index, final NavDrawerAdapter.Entry entry, boolean longClick) {
        if (entry.isAdd()) {
            new FolderChooserDialog.Builder((MainActivity) getActivity())
                    .chooseButton(R.string.choose)
                    .show();
        } else if (longClick) {
            /*if (entry.isIncluded()) {
                new MaterialDialog.Builder(getActivity())
                        .content(Html.fromHtml(getString(R.string.confirm_folder_remove, entry.getPath())))
                        .positiveText(R.string.yes)
                        .negativeText(R.string.no)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog materialDialog) {
                                IncludedFolderProvider.removeMediaEntry(getActivity(), entry.getPath());
                                getMediaFolders(mCurrentAccount); // reload albums

                                if (getActivity() == null) {
                                    return;
                                }
                                MainActivity act = (MainActivity) getActivity();
                                *//*act.notifyFoldersChanged();*//*

                                if (entry.getId() == mAdapter.getSelectedId()) {
                                    if (mCurrentSelectedPosition > mAdapter.getItemCount() - 1) {
                                        mCurrentSelectedPosition = mAdapter.getItemCount() - 1;
                                    }
                                    if (mAdapter.get(mCurrentSelectedPosition).isAdd()) {
                                        mCurrentSelectedPosition--;
                                    }
                                    NavDrawerAdapter.Entry newPath = mAdapter.get(mCurrentSelectedPosition);
                                    act.navDrawerSwitchAlbum(newPath.getPath());
                                    mAdapter.setCheckedItemId(mCurrentSelectedPosition);
                                }
                            }
                        }).show();
            } else {*/
            new MaterialDialog.Builder(getActivity())
                    .content(Html.fromHtml(getString(R.string.confirm_exclude_album, entry.getPath())))
                    .positiveText(R.string.yes)
                    .negativeText(R.string.no)
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog materialDialog) {

                            ExcludedFolderProvider.add(getActivity(), entry.getPath());
                            mAdapter.removeMediaEntry(index);

                            if (getActivity() == null) {
                                return;
                            }
                            MainActivity act = (MainActivity) getActivity();
                                /*act.notifyFoldersChanged();*/

                            NavDrawerAdapter.Entry newPath = mAdapter.getSelectedEntry();
                            act.navDrawerSwitchAlbum(newPath.getPath());
                        }
                    }).show();
            /*}*/
        } else {
            ((MainActivity) getActivity()).navDrawerSwitchAlbum(entry.getPath());
        }
    }

    @Override
    public void onAccountSelected(Account account) {


    }
}