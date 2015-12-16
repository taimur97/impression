package com.afollestad.impression.navdrawer;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import rx.Single;
import rx.SingleSubscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class NavDrawerFragment extends Fragment implements NavDrawerAdapter.Callback {

    public static final String SCOPE_PICASA = "https://picasaweb.google.com/data/";
    private static final int REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR = 1024;
    private static final int REQUEST_ADD_PICASA_ACCOUNT_PICKER = 1337;
    private RecyclerView mRecyclerView;

    //private Account mCurrentAccount;

    private NavDrawerAdapter mAdapter;
    private String mPicasaAccountEmailAttempt;
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
        /*View v = getView();
        if (v == null) {
            return;
        }
        View dropdownButton = v.findViewById(R.id.dropdownButton);
        if (dropdownButton.getTag() != null) {
            dropdownButton.performClick();
        }*/
    }

    public void reloadAlbums() {
        if (mAdapter.getCurrentAccount() == null) {
            reload(null);
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

    private void showAddAccountDialog() {
        if (getActivity() == null) {
            return;
        }
        new MaterialDialog.Builder(getActivity())
                .title(R.string.add_account)
                .items(R.array.account_options)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog materialDialog, View view, int i, CharSequence s) {
                        if (i == 0) {
                            //Google Photos
                            String[] accountTypes = new String[]{"com.google"};
                            Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                                    accountTypes, false, null, null, null, null);
                            startActivityForResult(intent, REQUEST_ADD_PICASA_ACCOUNT_PICKER);
                        } else {
                            // TODO - Other web accounts
                            Toast.makeText(getActivity(), "Not implemented", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).build().show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ADD_PICASA_ACCOUNT_PICKER) {
            if (resultCode == Activity.RESULT_OK) {
                mPicasaAccountEmailAttempt = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                getPicasaAccountAuthToken(mPicasaAccountEmailAttempt);
            } else if (resultCode == Activity.RESULT_CANCELED) {
                //TODO: String res
                Snackbar.make(mRecyclerView, "You must select an account to add", Snackbar.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR && resultCode == Activity.RESULT_OK) {
            // Receiving a result that follows a GoogleAuthException, try auth again
            getPicasaAccountAuthToken(mPicasaAccountEmailAttempt);
        }
    }

    private void getPicasaAccountAuthToken(final String email) {
        if (isDeviceOnline()) {
            Single.create(new Single.OnSubscribe<String>() {
                @Override
                public void call(SingleSubscriber<? super String> subscriber) {
                    try {
                        subscriber.onSuccess(GoogleAuthUtil.getToken(getActivity(), email, "oauth2:" + SCOPE_PICASA));
                    } catch (IOException | GoogleAuthException e) {
                        subscriber.onError(e);
                    }
                }
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleSubscriber<String>() {
                        @Override
                        public void onSuccess(String value) {
                            //TODO: Web request time!
                            Toast.makeText(getActivity(), "OAuth2 token: " + value, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(Throwable error) {
                            if (error instanceof GooglePlayServicesAvailabilityException) {
                                // The Google Play services APK is old, disabled, or not present.
                                // Show a dialog created by Google Play services that allows
                                // the user to update the APK
                                int statusCode = ((GooglePlayServicesAvailabilityException) error)
                                        .getConnectionStatusCode();
                                Dialog dialog = GooglePlayServicesUtil.getErrorDialog(statusCode,
                                        getActivity(), REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR);
                                dialog.show();
                            } else if (error instanceof UserRecoverableAuthException) {
                                // Unable to authenticate, such as when the user has not yet granted
                                // the app access to the account, but the user can fix this.
                                // Forward the user to an activity in Google Play services.
                                Intent intent = ((UserRecoverableAuthException) error).getIntent();
                                startActivityForResult(intent, REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR);
                            }
                        }
                    });
        } else {
            //TODO: String res
            Snackbar.make(mRecyclerView, "Cannot add account when offline", Snackbar.LENGTH_SHORT).show();
        }
    }

    private boolean isDeviceOnline() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    /*private void setupHeader(View view) {
        View addAccountFrame = ((ViewStub) view.findViewById(R.id.addAccountStub)).inflate();
        ((ImageView) addAccountFrame.findViewById(R.id.icon)).getDrawable().mutate().setColorFilter(mRegularColor, PorterDuff.Mode.SRC_ATOP);

        mAccountsFrame = (LinearLayout) view.findViewById(R.id.accountsFrame);
        mAccountsFrame.findViewById(R.id.addAccountFrame).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddAccountDialog();
            }
        });
        ImageButton dropdownButton = (ImageButton) view.findViewById(R.id.dropdownButton);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            dropdownButton.setBackgroundResource(Utils.resolveDrawable(getActivity(), R.attr.menu_selector));
        }
        dropdownButton.setOnClickListener(new View.OnClickListener() {
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
        reload(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        mAdapter.saveInstanceState(outState);

        super.onSaveInstanceState(outState);
    }

    public void reload(final Bundle instanceState) {
        if (getActivity() == null) {
            return;
        }

        final List<Account> allAccounts = new ArrayList<>();

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
                            if (selected) {
                                loadMediaFolders(a);
                            }
                        }
                        mAdapter.setAccounts(allAccounts);

                        if (instanceState != null) {
                            mAdapter.restoreInstanceState(instanceState);
                        }
                    }
                });
    }

    public void loadMediaFolders(final Account account) {
        /*if (account != null) {
            mCurrentAccount = account;
        }*/
        mAdapter.clear();
        mAdapter.add(new NavDrawerAdapter.Entry(MediaFolderEntry.OVERVIEW_PATH, NavDrawerAdapter.OVERVIEW_ID));

        account.getMediaFolders(MediaAdapter.SORT_NAME_DESC, MediaAdapter.FILTER_ALL)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleSubscriber<Set<MediaFolderEntry>>() {
                    @Override
                    public void onSuccess(Set<MediaFolderEntry> folderEntries) {
                        for (MediaFolderEntry f : folderEntries) {
                            mAdapter.add(new NavDrawerAdapter.Entry(f.data(), f.id()));
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
        /*if (entry.isAdd()) {
            new FolderChooserDialog.Builder((MainActivity) getActivity())
                    .chooseButton(R.string.choose)
                    .show();
        } else */
        if (longClick) {
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

    @Override
    public void onAddAccountPressed() {
        showAddAccountDialog();
    }

    @Override
    public void onSpecialItemPressed(long id) {
        if (id == NavDrawerAdapter.SETTINGS_ID) {
            ((MainActivity) getActivity()).openSettings();
        } else if (id == NavDrawerAdapter.ABOUT_ID) {
            ((MainActivity) getActivity()).showAboutDialog();
        }
    }
}