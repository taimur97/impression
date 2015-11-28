package com.afollestad.impression.media;

import android.Manifest;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.app.SharedElementCallback;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.view.menu.BaseMenuPresenter;
import android.support.v7.view.menu.ListMenuItemView;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.afollestad.impression.BuildConfig;
import com.afollestad.impression.R;
import com.afollestad.impression.api.MediaFolderEntry;
import com.afollestad.impression.base.ThemedActivity;
import com.afollestad.impression.navdrawer.NavDrawerFragment;
import com.afollestad.impression.providers.IncludedFolderProvider;
import com.afollestad.impression.providers.SortMemoryProvider;
import com.afollestad.impression.settings.SettingsActivity;
import com.afollestad.impression.utils.PrefUtils;
import com.afollestad.impression.utils.Utils;
import com.afollestad.impression.widget.breadcrumbs.BreadCrumbLayout;
import com.afollestad.impression.widget.breadcrumbs.Crumb;
import com.afollestad.inquiry.Inquiry;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.folderselector.FolderChooserDialog;
import com.afollestad.materialdialogs.internal.MDTintHelper;
import com.afollestad.materialdialogs.internal.ThemeSingleton;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * @author Aidan Follestad (afollestad)
 */
public class MainActivity extends ThemedActivity
        implements FolderChooserDialog.FolderCallback {

    public static final String EXTRA_CURRENT_ITEM_POSITION = "com.afollestad.impression.extra_current_item_position";
    public static final String EXTRA_OLD_ITEM_POSITION = "com.afollestad.impression.extra_old_item_position";

    public static final String ACTION_SELECT_ALBUM = BuildConfig.APPLICATION_ID + ".SELECT_FOLDER";

    public static final String NAV_DRAWER_FRAGMENT = "NAV_DRAWER";

    private static final int SETTINGS_REQUEST = 9000;

    private static final String TAG = "MainActivity";

    private DrawerLayout mDrawerLayout;
    private AnimatedDrawerToggle mAnimatedDrawerToggle;

    private boolean mPickMode;

    private SelectAlbumMode mSelectAlbumMode = SelectAlbumMode.NONE;
    private MediaCab mMediaCab;
    private Toolbar mToolbar;

    private BreadCrumbLayout mBreadCrumbLayout;
    private CharSequence mTitle;

    private Bundle mTmpState;
    private boolean mIsReenteringFromViewer;

    private static void logSharedElementTransition(String message, boolean isReentering) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, String.format("%s: %s", isReentering ? "REENTERING" : "EXITING", message));
        }
    }

    public DrawerLayout getDrawerLayout() {
        return mDrawerLayout;
    }

    public boolean isPickMode() {
        return mPickMode;
    }

    public void setIsReentering(boolean isReentering) {
        mIsReenteringFromViewer = isReentering;
    }

    public Bundle getTmpState() {
        return mTmpState;
    }

    public void setTmpState(Bundle tmpState) {
        mTmpState = tmpState;
    }

    public MediaCab getMediaCab() {
        return mMediaCab;
    }

    public void setMediaCab(MediaCab mediaCab) {
        mMediaCab = mediaCab;
    }

    public BreadCrumbLayout getBreadCrumbLayout() {
        return mBreadCrumbLayout;
    }

    public void invalidateMenuArrow(final String albumPath) {
        mBreadCrumbLayout.post(new Runnable() {
            @Override
            public void run() {
                if (albumPath == null || albumPath.equals(mBreadCrumbLayout.getTopPath()) || PrefUtils.isExplorerMode(MainActivity.this)) {
                    animateDrawerArrow(true);
                } else {
                    animateDrawerArrow(false);
                }
            }
        });
    }

    public void animateDrawerArrow(boolean close) {
        float currentOffset;
        if (mAnimatedDrawerToggle == null || (currentOffset = mAnimatedDrawerToggle.getOffset()) == (close ? 0 : 1)) {
            return;
        }
        ValueAnimator anim;
        if (close) {
            anim = ValueAnimator.ofFloat(currentOffset, 0f);
        } else {
            anim = ValueAnimator.ofFloat(currentOffset, 1f);
        }
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float slideOffset = (Float) valueAnimator.getAnimatedValue();
                mAnimatedDrawerToggle.setOffset(slideOffset);
            }
        });
        anim.setInterpolator(new FastOutSlowInInterpolator());
        anim.setDuration(300);
        anim.start();
    }

    public void setStatus(String status) {
        TextView view = (TextView) findViewById(R.id.status);
        if (status == null) {
            view.setVisibility(View.GONE);
        } else {
            view.setVisibility(View.VISIBLE);
            view.setText(status);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setupSharedElementCallback() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return;
        }
        final SharedElementCallback mCallback = new SharedElementCallback() {
            @Override
            public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                logSharedElementTransition("onMapSharedElements(List<String>, Map<String, View>)", mIsReenteringFromViewer);


                int oldPosition = mTmpState != null ? mTmpState.getInt(EXTRA_OLD_ITEM_POSITION) : 0;
                int currentPosition = mTmpState != null ? mTmpState.getInt(EXTRA_CURRENT_ITEM_POSITION) : 0;
                mTmpState = null;

                boolean shouldAddSharedImageView = !mIsReenteringFromViewer || currentPosition != oldPosition;

                final RecyclerView recyclerView = findMediaFragment().getRecyclerView();
                if (shouldAddSharedImageView && recyclerView != null) {
                    View newSharedView = ((MediaAdapter.ViewHolder) recyclerView.findViewHolderForLayoutPosition(currentPosition)).image;
                    if (newSharedView != null) {
                        newSharedView = newSharedView.findViewById(R.id.image);
                        final String transName = newSharedView.getTransitionName();
                        names.clear();
                        names.add(transName);
                        sharedElements.clear();
                        sharedElements.put(transName, newSharedView);
                    }
                }

                if (!mIsReenteringFromViewer) {
                    getWindow().setStatusBarColor(primaryColorDark());
                }

                View decor = getWindow().getDecorView();
                View navigationBar = decor.findViewById(android.R.id.navigationBarBackground);
                View statusBar = decor.findViewById(android.R.id.statusBarBackground);

                if (navigationBar != null && !sharedElements.containsKey(Window.NAVIGATION_BAR_BACKGROUND_TRANSITION_NAME)) {
                    if (!names.contains(Window.NAVIGATION_BAR_BACKGROUND_TRANSITION_NAME)) {
                        names.add(Window.NAVIGATION_BAR_BACKGROUND_TRANSITION_NAME);
                    }
                    sharedElements.put(Window.NAVIGATION_BAR_BACKGROUND_TRANSITION_NAME, navigationBar);
                }

                View toolbarFrame = findViewById(R.id.toolbar_frame);
                if (toolbarFrame != null && !sharedElements.containsKey(toolbarFrame.getTransitionName())) {
                    if (!names.contains(toolbarFrame.getTransitionName())) {
                        names.add(toolbarFrame.getTransitionName());
                    }
                    sharedElements.put(toolbarFrame.getTransitionName(), toolbarFrame);
                }

                if (statusBar != null && !sharedElements.containsKey(Window.STATUS_BAR_BACKGROUND_TRANSITION_NAME)) {
                    if (!names.contains(Window.STATUS_BAR_BACKGROUND_TRANSITION_NAME)) {
                        names.add(Window.STATUS_BAR_BACKGROUND_TRANSITION_NAME);
                    }
                    sharedElements.put(Window.STATUS_BAR_BACKGROUND_TRANSITION_NAME, statusBar);
                }

                logSharedElementTransition("=== names: " + names.toString(), mIsReenteringFromViewer);
                logSharedElementTransition("=== sharedElements: " + Utils.setToString(sharedElements.keySet()), mIsReenteringFromViewer);
            }

            @Override
            public void onSharedElementStart(List<String> sharedElementNames, List<View> sharedElements,
                                             List<View> sharedElementSnapshots) {
                logSharedElementTransition("onSharedElementStart(List<String>, List<View>, List<View>)", mIsReenteringFromViewer);
                logSharedElementsInfo(sharedElementNames, sharedElements);
            }

            @Override
            public void onSharedElementEnd(List<String> sharedElementNames, List<View> sharedElements,
                                           List<View> sharedElementSnapshots) {
                logSharedElementTransition("onSharedElementEnd(List<String>, List<View>, List<View>)", mIsReenteringFromViewer);
                logSharedElementsInfo(sharedElementNames, sharedElements);

                if (mIsReenteringFromViewer) {
                    View statusBar = getWindow().getDecorView().findViewById(android.R.id.statusBarBackground);
                    if (statusBar != null) {
                        statusBar.post(new Runnable() {
                            @Override
                            public void run() {
                                //For DrawerLayout transparency
                                getWindow().setStatusBarColor(Color.TRANSPARENT);
                            }
                        });
                    }
                }
            }

            private void logSharedElementsInfo(List<String> names, List<View> sharedElements) {
                logSharedElementTransition("=== names: " + names.toString(), mIsReenteringFromViewer);
                logSharedElementTransition("=== infos:", mIsReenteringFromViewer);
                for (View view : sharedElements) {
                    int[] loc = new int[2];
                    //noinspection ResourceType
                    view.getLocationInWindow(loc);
                    logSharedElementTransition("====== " + view.getTransitionName() + ": " + "(" + loc[0] + ", " + loc[1] + ")", mIsReenteringFromViewer);
                }
            }
        };
        setExitSharedElementCallback(mCallback);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == -1) {
            new MaterialDialog.Builder(this)
                    .title(R.string.permission_needed)
                    .content(R.string.permission_needed_desc)
                    .cancelable(false)
                    .positiveText(android.R.string.ok)
                    .dismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            finish();
                        }
                    }).show();
        } else {
            switchAlbum(null);
            NavDrawerFragment nav = (NavDrawerFragment) getFragmentManager().findFragmentByTag(NAV_DRAWER_FRAGMENT);
            if (nav != null) {
                nav.reloadAccounts();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Inquiry.init(this);

        setupSharedElementCallback();

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setSubtitleTextAppearance(this, R.style.ToolbarSubtitleStyle);
        setSupportActionBar(mToolbar);
        findViewById(R.id.toolbar_frame).setBackgroundColor(primaryColor());

        processIntent(getIntent());

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        if (!isSelectAlbumMode()) {
            mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
            mAnimatedDrawerToggle = new AnimatedDrawerToggle();
            mAnimatedDrawerToggle.syncState();
            mAnimatedDrawerToggle.setOffset(0f);

            mDrawerLayout.setDrawerListener(new DrawerLayout.SimpleDrawerListener() {
                @Override
                public void onDrawerClosed(View drawerView) {
                    super.onDrawerClosed(drawerView);
                    Fragment nav = getFragmentManager().findFragmentByTag(NAV_DRAWER_FRAGMENT);
                    if (nav != null) {
                        ((NavDrawerFragment) nav).notifyClosed();
                    }
                }
            });
            mDrawerLayout.setStatusBarBackgroundColor(primaryColorDark());

            FrameLayout navDrawerFrame = (FrameLayout) findViewById(R.id.nav_drawer_frame);
            int navDrawerMargin = getResources().getDimensionPixelSize(R.dimen.nav_drawer_margin);
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            int navDrawerWidthLimit = getResources().getDimensionPixelSize(R.dimen.nav_drawer_width_limit);
            int navDrawerWidth = displayMetrics.widthPixels - navDrawerMargin;
            if (navDrawerWidth > navDrawerWidthLimit) {
                navDrawerWidth = navDrawerWidthLimit;
            }
            navDrawerFrame.setLayoutParams(new DrawerLayout.LayoutParams(navDrawerWidth, DrawerLayout.LayoutParams.MATCH_PARENT, Gravity.START));
            navDrawerFrame.setBackgroundColor(primaryColorDark());

            if (getIntent().getAction() != null &&
                    (getIntent().getAction().equals(Intent.ACTION_GET_CONTENT) ||
                            getIntent().getAction().equals(Intent.ACTION_PICK))) {
                mTitle = getTitle();
                setTitle(R.string.pick_something);
                mPickMode = true;
            }
        } else {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_action_discard);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                // The drawer layout would handle this if album selection mode wasn't active
                getWindow().setStatusBarColor(primaryColorDark());
            }
        }

        mBreadCrumbLayout = (BreadCrumbLayout) findViewById(R.id.breadCrumbs);
        mBreadCrumbLayout.setCallback(new BreadCrumbLayout.SelectionCallback() {
            @Override
            public void onCrumbSelection(Crumb crumb, int index) {
                if (index == -1) {
                    onBackPressed();
                } else {
                    String activeFile = null;
                    if (mBreadCrumbLayout.getActiveIndex() > -1) {
                        activeFile = mBreadCrumbLayout.getCrumb(mBreadCrumbLayout.getActiveIndex()).getPath();
                    }
                    if (crumb.getPath() != null && activeFile != null &&
                            crumb.getPath().equals(activeFile)) {
                        Fragment frag = getFragmentManager().findFragmentById(R.id.content_frame);
                        ((MediaFragment) frag).jumpToTop(true);
                    } else {
                        switchAlbum(crumb, crumb.getPath() == null, true);
                    }
                }
            }
        });

        if (savedInstanceState == null) {
            // Show initial page (overview)
            switchAlbum(null);
        } else {
            if (!isSelectAlbumMode()) {
                if (mTitle != null) {
                    getSupportActionBar().setTitle(mTitle);
                }
                mMediaCab = MediaCab.restoreState(savedInstanceState, this);
            }
        }

        SortMemoryProvider.cleanup(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Inquiry.deinit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        invalidateExplorerMode();
    }

    public void invalidateExplorerMode() {
        final boolean explorerMode = PrefUtils.isExplorerMode(this);
        mBreadCrumbLayout.setVisibility(explorerMode ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mMediaCab != null) {
            mMediaCab.saveState(outState);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        processIntent(intent);
    }

    private void processIntent(final Intent intent) {
        String action = intent.getAction();
        if (action != null && action.equals(ACTION_SELECT_ALBUM)) {
            switch (intent.getIntExtra("mode", -1)) {
                default:
                    setSelectAlbumMode(SelectAlbumMode.CHOOSE);
                    break;
                case R.id.copyTo:
                    setSelectAlbumMode(SelectAlbumMode.COPY);
                    break;
                case R.id.moveTo:
                    setSelectAlbumMode(SelectAlbumMode.MOVE);
                    break;
            }
        }
    }

    public boolean isSelectAlbumMode() {
        return mSelectAlbumMode != SelectAlbumMode.NONE;
    }

    private void setSelectAlbumMode(SelectAlbumMode mode) {
        mSelectAlbumMode = mode;
        switch (mSelectAlbumMode) {
            default:
                setTitle(R.string.choose_album);
                break;
            case COPY:
                setTitle(R.string.copy_to);
                break;
            case MOVE:
                setTitle(R.string.move_to);
                break;
        }
        invalidateOptionsMenu();
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        if (!mPickMode && mSelectAlbumMode == SelectAlbumMode.NONE && getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        super.onActivityReenter(resultCode, data);
        mIsReenteringFromViewer = true;
        mTmpState = new Bundle(data.getExtras());
        int oldPosition = mTmpState.getInt(EXTRA_OLD_ITEM_POSITION);
        int currentPosition = mTmpState.getInt(EXTRA_CURRENT_ITEM_POSITION);

        final RecyclerView recyclerView = findMediaFragment().getRecyclerView();
        if (recyclerView != null) {
            if (oldPosition != currentPosition) {
                recyclerView.scrollToPosition(currentPosition);
            }

            postponeEnterTransition();
            recyclerView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    recyclerView.getViewTreeObserver().removeOnPreDrawListener(this);

                    startPostponedEnterTransition();
                    return true;
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        if (mMediaCab != null) {
            mMediaCab.finish();
            mMediaCab = null;
        } else {
            if (mDrawerLayout != null && mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                mDrawerLayout.closeDrawer(GravityCompat.START);
            } else if (mBreadCrumbLayout.popHistory()) {
                // Go to previous crumb in history
                final Crumb crumb = mBreadCrumbLayout.lastHistory();
                switchAlbum(crumb, false, false);
            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        menu.findItem(R.id.settings).setVisible(!mPickMode && mSelectAlbumMode == SelectAlbumMode.NONE);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        invalidateMenuTint();

        if (item.getItemId() == R.id.settings) {
            startActivityForResult(new Intent(this, SettingsActivity.class), SETTINGS_REQUEST);
            return true;
        } else if (item.getItemId() == android.R.id.home) {
            if (isSelectAlbumMode()) {
                finish();
                return true;
            } else if (mAnimatedDrawerToggle.getOffset() == 1) {
                onBackPressed();
                return true;
            }
        }
        return mDrawerLayout != null && (mDrawerLayout.getDrawerLockMode(GravityCompat.START) == DrawerLayout.LOCK_MODE_LOCKED_CLOSED ||
                mAnimatedDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SETTINGS_REQUEST && resultCode == Activity.RESULT_OK) {
            MediaFragment content = findMediaFragment();
            if (content != null) {
                content.getPresenter().reload();
            }
            reloadNavDrawerAlbums();
        }
    }

    private MediaFragment findMediaFragment() {
        return (MediaFragment) getFragmentManager().findFragmentById(R.id.content_frame);
    }

    public boolean navDrawerSwitchAlbum(String path) {
        mDrawerLayout.closeDrawers();

        if (path.equals(MediaFolderEntry.OVERVIEW_PATH) && PrefUtils.isExplorerMode(this)) {
            path = Environment.getExternalStorageDirectory().getAbsolutePath();
        }

        mBreadCrumbLayout.setTopPath(path);

        MediaFragment frag = findMediaFragment();
        if (!path.equals(frag.getPresenter().getPath())) {
            mBreadCrumbLayout.clearHistory();
            Crumb crumb = new Crumb(this, path);
            switchAlbum(crumb, true, true);
            return true;
        }
        return false;
    }

    public void switchAlbum(String path) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            return;
        }

        boolean initialCreate = (path == null);
        if (initialCreate) {
            // Initial directory
            path = PrefUtils.isExplorerMode(this) ?
                    Environment.getExternalStorageDirectory().getAbsolutePath() :
                    MediaFolderEntry.OVERVIEW_PATH;
            mBreadCrumbLayout.setTopPath(path);
        }

        Crumb crumb = new Crumb(this, path);
        switchAlbum(crumb, initialCreate, true);
    }

    public void switchAlbum(Crumb crumb, boolean forceRecreate, boolean addToHistory) {
        if (forceRecreate) {
            // Rebuild artificial history, most likely first time load
            mBreadCrumbLayout.clearHistory();
            String path = crumb.getPath();
            while (path != null) {
                mBreadCrumbLayout.addHistory(new Crumb(this, path));
                if (mBreadCrumbLayout.isTopPath(path)) {
                    break;
                }
                path = new File(path).getParent();
            }
            mBreadCrumbLayout.reverseHistory();
        } else if (addToHistory) {
            mBreadCrumbLayout.addHistory(crumb);
        }
        mBreadCrumbLayout.setActiveOrAdd(crumb, forceRecreate);

        final String to = crumb.getPath();
        MediaFragment frag = findMediaFragment();

        if (frag == null) {
            frag = MediaPresenter.newInstance(to);
            getFragmentManager().beginTransaction().replace(R.id.content_frame, frag).commit();
        } else {
            MediaPresenter presenter = frag.getPresenter();
            String albumPath = presenter.getPath();
            if (!albumPath.equals(to)) {
                presenter.setPath(to);
            }
        }
    }

    @Override
    public void onFolderSelection(File folder) {
        IncludedFolderProvider.add(this, folder);
        reloadNavDrawerAlbums();
    }

    public void reloadNavDrawerAlbums() {
        NavDrawerFragment nav = (NavDrawerFragment) getFragmentManager().findFragmentByTag(NAV_DRAWER_FRAGMENT);
        if (nav != null) {
            if (nav.mCurrentAccount == null) {
                nav.reloadAccounts();
            } else {
                nav.getMediaFolders(nav.mCurrentAccount);
            }
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        invalidateMenuTint();
        invalidateMenuIcons(menu);
        return super.onPrepareOptionsMenu(menu);
    }

    public void invalidateMenuIcons(final Menu menu) {
        if (menu != null && menu.getClass().getSimpleName().equals("MenuBuilder")) {
            try {
                Field field = menu.getClass().getDeclaredField("mOptionalIconsVisible");
                field.setAccessible(true);
                field.setBoolean(menu, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void invalidateMenuTint() {
        mToolbar.post(new Runnable() {
            @Override
            public void run() {
                try {
                    Field f1 = Toolbar.class.getDeclaredField("mMenuView");
                    f1.setAccessible(true);
                    ActionMenuView actionMenuView = (ActionMenuView) f1.get(mToolbar);

                    Field f2 = ActionMenuView.class.getDeclaredField("mPresenter");
                    f2.setAccessible(true);

                    //Actually ActionMenuPresenter
                    BaseMenuPresenter presenter = (BaseMenuPresenter) f2.get(actionMenuView);

                    Field f3 = presenter.getClass().getDeclaredField("mOverflowPopup");
                    f3.setAccessible(true);
                    MenuPopupHelper overflowMenuPopupHelper = (MenuPopupHelper) f3.get(presenter);
                    setTintForMenuPopupHelper(overflowMenuPopupHelper);

                    Field f4 = presenter.getClass().getDeclaredField("mActionButtonPopup");
                    f4.setAccessible(true);
                    MenuPopupHelper subMenuPopupHelper = (MenuPopupHelper) f4.get(presenter);
                    setTintForMenuPopupHelper(subMenuPopupHelper);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void setTintForMenuPopupHelper(MenuPopupHelper menuPopupHelper) {
        if (menuPopupHelper != null) {
            final ListView listView = menuPopupHelper.getPopup().getListView();
            listView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    try {
                        Field checkboxField = ListMenuItemView.class.getDeclaredField("mCheckBox");
                        checkboxField.setAccessible(true);
                        Field radioButtonField = ListMenuItemView.class.getDeclaredField("mRadioButton");
                        radioButtonField.setAccessible(true);

                        for (int i = 0; i < listView.getChildCount(); i++) {
                            View v = listView.getChildAt(i);
                            if (!(v instanceof ListMenuItemView)) {
                                continue;
                            }
                            ListMenuItemView iv = (ListMenuItemView) v;

                            CheckBox check = (CheckBox) checkboxField.get(iv);
                            if (check != null) {
                                MDTintHelper.setTint(check, ThemeSingleton.get().widgetColor);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    check.setBackground(null);
                                }
                            }

                            RadioButton radioButton = (RadioButton) radioButtonField.get(iv);
                            if (radioButton != null) {
                                MDTintHelper.setTint(radioButton, ThemeSingleton.get().widgetColor);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    radioButton.setBackground(null);
                                }
                            }
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        listView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    } else {
                        //noinspection deprecation
                        listView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    }
                }
            });
        }
    }

    public enum SelectAlbumMode {
        NONE,
        COPY,
        MOVE,
        CHOOSE
    }

    private class AnimatedDrawerToggle extends ActionBarDrawerToggle {

        private float mOffset;

        public AnimatedDrawerToggle() {
            super(MainActivity.this, MainActivity.this.mDrawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        }

        public float getOffset() {
            return mOffset;
        }

        public void setOffset(float slideOffset) {
            super.onDrawerSlide(null, slideOffset);
            mOffset = slideOffset;
        }
    }
}