package com.afollestad.impression.viewer;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.SharedElementCallback;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.print.PrintHelper;
import android.support.v4.view.ViewPager;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.transition.ArcMotion;
import android.transition.ChangeBounds;
import android.transition.ChangeClipBounds;
import android.transition.ChangeImageTransform;
import android.transition.ChangeTransform;
import android.transition.Transition;
import android.transition.TransitionSet;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.afollestad.impression.BuildConfig;
import com.afollestad.impression.R;
import com.afollestad.impression.api.MediaEntry;
import com.afollestad.impression.base.ThemedActivity;
import com.afollestad.impression.media.CurrentMediaEntriesSingleton;
import com.afollestad.impression.media.MainActivity;
import com.afollestad.impression.providers.SortMemoryProvider;
import com.afollestad.impression.utils.PrefUtils;
import com.afollestad.impression.utils.ScrimUtil;
import com.afollestad.impression.utils.TimeUtils;
import com.afollestad.impression.utils.Utils;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Aidan Follestad (afollestad)
 */
public class ViewerActivity extends ThemedActivity implements SlideshowInitDialog.SlideshowCallback {

    public static final String EXTRA_WIDTH = "com.afollestad.impression.Width";
    public static final String EXTRA_HEIGHT = "com.afollestad.impression.Height";
    public static final String EXTRA_ITEMS_READY = "com.afollestad.impression.ItemsReady";
    public static final String EXTRA_PATH = "com.afollestad.impression.Path";
    public static final String EXTRA_INIT_CURRENT_ITEM_POSITION = "com.afollestad.impression.CurrentItemPosition";

    public static final int UI_FADE_DELAY = 2750;
    public static final int UI_FADE_DURATION = 400;
    public static final int SHARED_ELEMENT_TRANSITION_DURATION = 200;
    private static final int EDIT_REQUEST = 1000;
    private static final String STATE_CURRENT_POSITION = "state_current_position";
    private static final String TAG = "ViewerActivity";
    public Toolbar mToolbar;
    private boolean mFinishedTransition;

    private ViewPager mPager;
    private ViewerPagerAdapter mAdapter;

    private Timer mTimer;
    private int mCurrentPosition;
    private boolean mStartedPostponedTransition;

    private int mStatusBarHeight;
    private boolean mIsReturningToMain;

    private boolean mAllVideos;

    private long mSlideshowDelay;
    private boolean mSlideshowLoop;
    private Timer mSlideshowTimer;

    private View mTopScrim;
    private View mBottomScrim;

    private AnimatorSet mUiAnimatorSet;

    private ViewPager.OnPageChangeListener mPagerListener = new ViewPager.OnPageChangeListener() {

        int previousState;
        boolean userScrollChange;

        private ViewerPagerFragment mActive;
        private boolean mInitialized;

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
            if (userScrollChange) {
                stopSlideshow();
            }

            mCurrentPosition = position;

            invalidateOptionsMenu();
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            if (!mInitialized) {
                mActive = getViewerPagerFragment(mCurrentPosition);

                mInitialized = true;
            }

            if (state == ViewPager.SCROLL_STATE_DRAGGING) {
                if (mActive != null) {
                    mActive.setIsActive(false);
                    mActive = null;
                }
            }

            if (previousState == ViewPager.SCROLL_STATE_DRAGGING
                    && state == ViewPager.SCROLL_STATE_SETTLING) {
                userScrollChange = true;
            } else if (previousState == ViewPager.SCROLL_STATE_SETTLING
                    && state == ViewPager.SCROLL_STATE_IDLE) {
                userScrollChange = false;
            }

            if (state == ViewPager.SCROLL_STATE_IDLE) {
                mActive = getViewerPagerFragment(mCurrentPosition);
                if (mActive != null) {
                    mActive.setIsActive(true);
                }
            }

            previousState = state;
        }
    };

    private static void logSharedElementTransition(String message, boolean isReturning) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, String.format("%s: %s", isReturning ? "RETURNING" : "ENTERING", message));
        }
    }

    private ViewerPagerFragment getViewerPagerFragment(int index) {
        return (ViewerPagerFragment) getFragmentManager().findFragmentByTag("page:" + index);
    }

    @Override
    protected int darkTheme() {
        return R.style.AppTheme_Viewer_Dark;
    }

    @Override
    protected int lightTheme() {
        return R.style.AppTheme_Viewer;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void invalidateTransition() {
        if (mStartedPostponedTransition || Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return;
        }
        mStartedPostponedTransition = true;
        startPostponedEnterTransition();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setupSharedElementCallback() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return;
        }
        final SharedElementCallback enterCallback = new SharedElementCallback() {
            @Override
            public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                logSharedElementTransition("onMapSharedElements(List<String>, Map<String, View>)", mIsReturningToMain);

                if (mIsReturningToMain) {
                    View sharedView = getViewerPagerFragment(mCurrentPosition).getSharedElement();
                    names.clear();
                    sharedElements.clear();

                    if (sharedView != null) {
                        final String transName = sharedView.getTransitionName();
                        names.add(transName);
                        sharedElements.put(transName, sharedView);
                    }
                }

                //To "register" backgrounds
                if (!mIsReturningToMain) {
                    getWindow().setStatusBarColor(primaryColor());
                    getWindow().setNavigationBarColor(Color.BLACK);
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

                if (mToolbar != null && !sharedElements.containsKey(mToolbar.getTransitionName())) {
                    if (!names.contains(mToolbar.getTransitionName())) {
                        names.add(mToolbar.getTransitionName());
                    }
                    sharedElements.put(mToolbar.getTransitionName(), mToolbar);
                }

                if (statusBar != null && !sharedElements.containsKey(Window.STATUS_BAR_BACKGROUND_TRANSITION_NAME)) {
                    if (!names.contains(Window.STATUS_BAR_BACKGROUND_TRANSITION_NAME)) {
                        names.add(Window.STATUS_BAR_BACKGROUND_TRANSITION_NAME);
                    }
                    sharedElements.put(Window.STATUS_BAR_BACKGROUND_TRANSITION_NAME, statusBar);
                }

                logSharedElementTransition("=== names: " + names.toString(), mIsReturningToMain);
                logSharedElementTransition("=== sharedElements: " + Utils.setToString(sharedElements.keySet()), mIsReturningToMain);
            }

            @Override
            public void onSharedElementStart(List<String> sharedElementNames, List<View> sharedElements,
                                             List<View> sharedElementSnapshots) {

                logSharedElementTransition("onSharedElementStart(List<String>, List<View>, List<View>)", mIsReturningToMain);
                logSharedElementsInfo(sharedElementNames, sharedElements);

                View decor = getWindow().getDecorView();
                View navigationBar = decor.findViewById(android.R.id.navigationBarBackground);
                View statusBar = decor.findViewById(android.R.id.statusBarBackground);

                if (!mIsReturningToMain) {

                    if (mToolbar != null) {
                        ObjectAnimator.ofArgb(mToolbar, "backgroundColor", primaryColorDark(), Color.BLACK)
                                .setDuration(SHARED_ELEMENT_TRANSITION_DURATION)
                                .start();
                    }

                    if (navigationBar != null) {
                        if (PrefUtils.isColoredNavBar(ViewerActivity.this)) {
                            ObjectAnimator.ofArgb(navigationBar, "backgroundColor", Color.BLACK, Color.BLACK)
                                    .setDuration(SHARED_ELEMENT_TRANSITION_DURATION)
                                    .start();
                        }
                    }

                    if (statusBar != null) {
                        ObjectAnimator.ofArgb(statusBar, "backgroundColor", primaryColor(), Color.BLACK)
                                .setDuration(SHARED_ELEMENT_TRANSITION_DURATION)
                                .start();
                    }
                }
            }

            @Override
            public void onSharedElementEnd(List<String> sharedElementNames, List<View> sharedElements,
                                           List<View> sharedElementSnapshots) {
                logSharedElementTransition("onSharedElementEnd(List<String>, List<View>, List<View>)", mIsReturningToMain);
                logSharedElementsInfo(sharedElementNames, sharedElements);

                View decor = getWindow().getDecorView();
                View navigationBar = decor.findViewById(android.R.id.navigationBarBackground);
                View statusBar = decor.findViewById(android.R.id.statusBarBackground);

                if (mIsReturningToMain) {
                    if (mToolbar != null) {
                        ObjectAnimator.ofArgb(mToolbar, "backgroundColor", Color.BLACK, primaryColor())
                                .setDuration(SHARED_ELEMENT_TRANSITION_DURATION)
                                .start();
                    }

                    if (navigationBar != null) {
                        if (PrefUtils.isColoredNavBar(ViewerActivity.this)) {
                            ObjectAnimator.ofArgb(navigationBar, "backgroundColor", Color.BLACK, primaryColorDark())
                                    .setDuration(SHARED_ELEMENT_TRANSITION_DURATION)
                                    .start();
                        }
                    }
                    if (statusBar != null) {
                        ObjectAnimator.ofArgb(statusBar, "backgroundColor", Color.BLACK, primaryColorDark())
                                .setDuration(SHARED_ELEMENT_TRANSITION_DURATION)
                                .start();
                    }
                }
            }

            private void logSharedElementsInfo(List<String> names, List<View> sharedElements) {
                logSharedElementTransition("=== names: " + names.toString(), mIsReturningToMain);
                logSharedElementTransition("=== infos:", mIsReturningToMain);
                for (View view : sharedElements) {
                    int[] loc = new int[2];
                    //noinspection ResourceType
                    view.getLocationInWindow(loc);
                    logSharedElementTransition("====== " + view.getTransitionName() + ": " + "(" + loc[0] + ", " + loc[1] + ")", mIsReturningToMain);
                }
            }
        };
        setEnterSharedElementCallback(enterCallback);

        getWindow().getSharedElementEnterTransition().addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {

            }

            @Override
            public void onTransitionEnd(Transition transition) {
                mToolbar.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //For DrawerLayout transparency
                        mToolbar.setBackgroundColor(Color.TRANSPARENT);
                        getWindow().setStatusBarColor(ContextCompat.getColor(
                                ViewerActivity.this, android.R.color.transparent));
                        getWindow().setNavigationBarColor(ContextCompat.getColor(
                                ViewerActivity.this, android.R.color.transparent));
                    }
                }, 150);

                getWindow().getSharedElementEnterTransition().removeListener(this);
            }

            @Override
            public void onTransitionCancel(Transition transition) {

            }

            @Override
            public void onTransitionPause(Transition transition) {

            }

            @Override
            public void onTransitionResume(Transition transition) {

            }
        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_CURRENT_POSITION, mCurrentPosition);
    }

    private int getStatusBarHeight() {
        return mStatusBarHeight = Utils.getStatusBarHeight(this);
    }

    @Override
    protected boolean hasColoredBars() {
        return false;
    }

    public int getNavigationBarHeight(boolean portraitOnly, boolean landscapeOnly) {
        final Configuration config = getResources().getConfiguration();
        final Resources r = getResources();
        int id;
        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (portraitOnly) {
                return 0;
            }
            id = r.getIdentifier("navigation_bar_height_landscape", "dimen", "android");
        } else {
            if (landscapeOnly) {
                return 0;
            }
            id = r.getIdentifier("navigation_bar_height", "dimen", "android");
        }
        if (id > 0) {
            return r.getDimensionPixelSize(id);
        }
        return 0;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mStartedPostponedTransition = false;
            postponeEnterTransition();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewer);

        setTransition();

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, getStatusBarHeight(), 0, 0);
        mToolbar.setLayoutParams(params);

        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mTopScrim = findViewById(R.id.top_scrim);
        mTopScrim.setBackground(ScrimUtil.makeCubicGradientScrimDrawable(
                Color.argb(200, 0, 0, 0), 8, Gravity.TOP));

        mBottomScrim = findViewById(R.id.bottom_scrim);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBottomScrim.setBackground(ScrimUtil.makeCubicGradientScrimDrawable(
                    Color.argb(200, 0, 0, 0), 8, Gravity.BOTTOM));
        } else {
            mBottomScrim.setVisibility(View.GONE);
        }

        if (savedInstanceState == null) {
            if (getIntent() != null && getIntent().getExtras() != null) {
                mCurrentPosition = getIntent().getExtras().getInt(EXTRA_INIT_CURRENT_ITEM_POSITION);
            }
        } else {
            mCurrentPosition = savedInstanceState.getInt(STATE_CURRENT_POSITION);
        }

        //mRemovedEntryIds = new ArrayList<>();

        boolean dontSetPos = false;


        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        if (getIntent() != null && getIntent().hasExtra(EXTRA_PATH)) {
            //noinspection ResourceType
            path = getIntent().getStringExtra(EXTRA_PATH);
        }

        if (getIntent() != null && (!getIntent().hasExtra(EXTRA_ITEMS_READY) || !CurrentMediaEntriesSingleton.instanceExists()) && getIntent().getData() != null) {
            path = reload();
            if (path == null) return;
        }

        mAdapter = new ViewerPagerAdapter(this, getFragmentManager(),
                getIntent().getIntExtra(EXTRA_WIDTH, -1),
                getIntent().getIntExtra(EXTRA_HEIGHT, -1),
                mCurrentPosition,
                SortMemoryProvider.getSortMode(this, path));
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setOffscreenPageLimit(1);
        mPager.setAdapter(mAdapter);


        mAllVideos = true;
        for (MediaEntry e : mAdapter.getEntries()) {
            if (!e.isVideo()) {
                mAllVideos = false;
                break;
            }
        }
        //TODO
        /*if (!dontSetPos)
            mCurrentPosition = translateToViewerIndex(mCurrentPosition);*/
        mPager.setCurrentItem(mCurrentPosition);


        // When the view pager is swiped, fragments are notified if they're active or not
        // And the menu updates based on the color mode (light or dark).

        mPager.addOnPageChangeListener(mPagerListener);

        mFinishedTransition = getIntent().getData() != null || Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP;
        setupSharedElementCallback();

        // Android Beam stuff
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 &&
                getPackageManager().hasSystemFeature(PackageManager.FEATURE_NFC)) {
            NfcAdapter mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
            mNfcAdapter.setBeamPushUrisCallback(new FileBeamCallback(), this);
        }

        // Callback used to know when the user swipes up to show system UI
        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if (visibility == View.VISIBLE) {
                    uiTapped(false);
                }
            }
        });

        // Prevents nav bar from overlapping toolbar options in landscape
        mToolbar.setPadding(
                mToolbar.getPaddingLeft(),
                mToolbar.getPaddingTop(),
                getNavigationBarHeight(false, true),
                mToolbar.getPaddingBottom()
        );
    }

    @Nullable
    private String reload() {
        boolean dontSetPos;

        String path = null;

        List<MediaEntry> entries = new ArrayList<>();
        Uri data = getIntent().getData();
        if (data.getScheme() != null) {
            path = data.toString();
            if (data.getScheme().equals("file")) {
                path = data.getPath();
                if (!new File(path).exists()) {
                    path = null;
                } else {
                    final File file = new File(path);
                    //TODO
                    final List<MediaEntry> brothers = null/*Utils.getEntriesFromFolder(this, file.getParentFile(), false, false, MediaAdapter.FileFilterMode.FILTER_ALL)*/;
                    entries.addAll(brothers);
                    for (int i = 0; i < brothers.size(); i++) {
                        if (brothers.get(i).data().equals(file.getAbsolutePath())) {
                            mCurrentPosition = i;
                            dontSetPos = true;
                            break;
                        }
                    }
                }
            } else {
                String tempPath = null;
                try {
                    Cursor cursor = getContentResolver().query(data, new String[]{"_data"}, null, null, null);
                    if (cursor.moveToFirst()) {
                        tempPath = cursor.getString(0);
                    }
                    cursor.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (tempPath != null) {
                    // @author Viswanath Lekshmanan
                    // #282 Fix to load all other photos in the same album when loading using URI
                    final File file = new File(tempPath);
                    //TODO
                    final List<MediaEntry> brothers = null/*Utils.getEntriesFromFolder(this, file.getParentFile(), false, false, MediaAdapter.FileFilterMode.FILTER_ALL)*/;
                    entries.addAll(brothers);
                    for (int i = 0; i < brothers.size(); i++) {
                        if (brothers.get(i).data().equals(file.getAbsolutePath())) {
                            mCurrentPosition = i;
                            dontSetPos = true;
                            break;
                        }
                    }
                } else {
                    path = null;
                }
            }
        }

        CurrentMediaEntriesSingleton.getInstance().set(entries);

        if (path == null) {
            new MaterialDialog.Builder(this)
                    .title(R.string.error)
                    .content(R.string.invalid_file_path_error)
                    .positiveText(android.R.string.ok)
                    .cancelable(false)
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            super.onPositive(dialog);
                            finish();
                        }
                    }).show();
            return null;
        }
        return path;
    }

    private void setTransition() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return;
        }

        final TransitionSet transition = new TransitionSet();

        ChangeBounds transition1 = new ChangeBounds();
        transition.addTransition(transition1);
        ChangeTransform transition2 = new ChangeTransform();
        transition.addTransition(transition2);
        ChangeClipBounds transition3 = new ChangeClipBounds();
        transition.addTransition(transition3);
        ChangeImageTransform transition4 = new ChangeImageTransform();
        transition.addTransition(transition4);

        transition.setDuration(SHARED_ELEMENT_TRANSITION_DURATION);

        //Android framework bug? Interpolator set on TransitionSet doesn't work
        FastOutSlowInInterpolator interpolator = new FastOutSlowInInterpolator();
        transition1.setInterpolator(interpolator);
        transition2.setInterpolator(interpolator);
        transition3.setInterpolator(interpolator);
        transition4.setInterpolator(interpolator);

        final ArcMotion pathMotion = new ArcMotion();
        pathMotion.setMaximumAngle(50);
        transition.setPathMotion(pathMotion);

        getWindow().setSharedElementEnterTransition(transition);
        getWindow().setSharedElementReturnTransition(transition);
    }

    private int translateToViewerIndex(int remote) {
        //TODO
        /*for (int i = 0; i < mEntries.size(); i++) {
            if (mEntries.get(i).realIndex() == remote) {
                if (mEntries.size() - 1 < i) {
                    return 0;
                } else {
                    return i;
                }
            }
        }*/
        return 0;
    }

    private void hideSystemUi() {
        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    // This snippet shows the system bars. It does this by removing all the flags
    // except for the ones that make the content appear under the system bars.
    private void showSystemUi() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    private void postSystemUiHide() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer.purge();
        }
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideSystemUi();
                    }
                });
            }
        }, UI_FADE_DELAY);
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        uiTapped(true);
        return super.onMenuOpened(featureId, menu);
    }

    @Override
    public void onOptionsMenuClosed(Menu menu) {
        super.onOptionsMenuClosed(menu);
        // Resume the fade animation
        uiTapped(false);
    }

    private void uiTapped(boolean tapped) {
        uiTapped(tapped, null);
    }

    public void uiTapped(boolean tapped, final ToolbarFadeListener listener) {
        if (mUiAnimatorSet != null) {
            mUiAnimatorSet.cancel();
        }

        ObjectAnimator toolbar = ObjectAnimator.ofFloat(mToolbar, View.ALPHA, 0f);
        ObjectAnimator topScrim = ObjectAnimator.ofFloat(mTopScrim, View.ALPHA, 0f);
        ObjectAnimator bottomScrim = ObjectAnimator.ofFloat(mBottomScrim, View.ALPHA, 0f);

        mUiAnimatorSet = new AnimatorSet()
                .setDuration(UI_FADE_DURATION);

        if (tapped && mToolbar.getAlpha() > 0f) {
            // User tapped to hide the toolbar immediately

            mUiAnimatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    if (listener != null) {
                        listener.onFade();
                    }
                }
            });
            hideSystemUi();
        } else {
            mToolbar.setAlpha(1f);
            mTopScrim.setAlpha(1f);
            mBottomScrim.setAlpha(1f);

            showSystemUi();
            postSystemUiHide();

            mUiAnimatorSet.setStartDelay(UI_FADE_DELAY);
        }

        mUiAnimatorSet.play(toolbar).with(topScrim).with(bottomScrim);
        mUiAnimatorSet.start();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Start the toolbar fader
        uiTapped(false);
        showSystemUi();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mUiAnimatorSet != null) {
            mUiAnimatorSet.cancel();
        }
    }

    private Uri getCurrentUri() {
        return Uri.fromFile(new File(mAdapter.getEntries().get(mCurrentPosition).data()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_viewer, menu);
        if (mAdapter.getEntries().size() > 0) {
            MediaEntry currentEntry = mAdapter.getEntries().get(mCurrentPosition);
            if (currentEntry == null || currentEntry.isVideo()) {
                menu.findItem(R.id.print).setVisible(false);
                menu.findItem(R.id.edit).setVisible(false);
                menu.findItem(R.id.set_as).setVisible(false);
            } else {
                menu.findItem(R.id.print).setVisible(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT);
                menu.findItem(R.id.edit).setVisible(true);
                menu.findItem(R.id.set_as).setVisible(true);
            }
        }
        menu.findItem(R.id.slideshow).setVisible(!mAllVideos && mSlideshowTimer == null);

        return super.onCreateOptionsMenu(menu);
    }

    private Bitmap loadBitmap(File file) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inSampleSize = 1;
        Bitmap bitmap;
        while (true) {
            Log.v("ViewerActivity", "loadBitmap(" + file.getAbsolutePath() + "), sample size: " + options.inSampleSize);
            try {
                bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
                break;
            } catch (OutOfMemoryError e) {
                options.inSampleSize += 1;
            }
        }
        return bitmap;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.share) {
            try {
                final String mime = mAdapter.getEntries().get(mCurrentPosition).isVideo() ? "video/*" : "image/*";
                startActivity(new Intent(Intent.ACTION_SEND)
                        .setType(mime)
                        .putExtra(Intent.EXTRA_STREAM, getCurrentUri()));
            } catch (ActivityNotFoundException e) {
                Toast.makeText(getApplicationContext(), R.string.no_app_complete_action, Toast.LENGTH_SHORT).show();
            }
            return true;
        } else if (item.getItemId() == R.id.edit) {
            try {
                startActivityForResult(new Intent(Intent.ACTION_EDIT)
                        .setDataAndType(getCurrentUri(), "image/*"), EDIT_REQUEST);
                setResult(RESULT_OK); // signals that list should reload on returning
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
            }
            return true;
        } else if (item.getItemId() == R.id.set_as) {
            try {
                startActivity(new Intent(Intent.ACTION_ATTACH_DATA)
                        .setDataAndType(getCurrentUri(), "image/*")
                        .putExtra("mimeType", "image/*"));
            } catch (ActivityNotFoundException e) {
                Toast.makeText(getApplicationContext(), R.string.no_app_complete_action, Toast.LENGTH_SHORT).show();
            }
            return true;
        } else if (item.getItemId() == R.id.print) {
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
            PrintHelper photoPrinter = new PrintHelper(ViewerActivity.this);
            photoPrinter.setScaleMode(PrintHelper.SCALE_MODE_FIT);
            final File currentFile = new File(mAdapter.getEntries().get(mCurrentPosition).data());
            Bitmap bitmap = loadBitmap(currentFile);
            photoPrinter.printBitmap(currentFile.getName(), bitmap);
//                    bitmap.recycle();
//                }
//            }).start();
        } else if (item.getItemId() == R.id.details) {
            final MediaEntry entry = mAdapter.getEntries().get(mCurrentPosition);
            final File file = new File(entry.data());
            final Calendar cal = new GregorianCalendar();
            cal.setTimeInMillis(entry.dateTaken());
            new MaterialDialog.Builder(this)
                    .title(R.string.details)
                    .content(Html.fromHtml(getString(R.string.details_contents,
                            TimeUtils.toStringLong(cal),
                            //TODO
                            ""/*entry.width() + " x " + entry.height()*/,
                            file.getName(),
                            Utils.readableFileSize(file.length()),
                            file.getAbsolutePath())))
                    .contentLineSpacing(1.6f)
                    .positiveText(R.string.dismiss)
                    .show();
        } else if (item.getItemId() == R.id.delete) {
            final MediaEntry currentEntry = mAdapter.getEntries().get(mCurrentPosition);
            new MaterialDialog.Builder(this)
                    .content(currentEntry.isVideo() ? R.string.delete_confirm_video : R.string.delete_confirm_photo)
                    .positiveText(R.string.yes)
                    .negativeText(R.string.no)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                            MediaEntry entry = mAdapter.getEntries().get(mCurrentPosition);
                            entry.delete(ViewerActivity.this);

                            CurrentMediaEntriesSingleton.getInstance().remove(entry);

                            mAdapter.updateEntries();
                            if (mAdapter.getEntries().size() == 0) {
                                finish();
                            }
                        }
                    }).build().show();
        } else if (item.getItemId() == R.id.slideshow) {
            new SlideshowInitDialog().show(this);
        } else if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStartSlideshow(long delay, boolean loop) {
        mSlideshowDelay = delay;
        mSlideshowLoop = loop;
        mSlideshowTimer = new Timer();
        incrementSlideshow();
        invalidateOptionsMenu();

        while (true) {
            MediaEntry e = mAdapter.getEntries().get(mCurrentPosition);
            if (e.isVideo()) {
                mCurrentPosition += 1;
                if (mCurrentPosition > mAdapter.getEntries().size() - 1) {
                    mCurrentPosition = 0;
                }
            } else {
                mPager.setCurrentItem(mCurrentPosition);
                break;
            }
        }
    }

    private void incrementSlideshow() {
        mSlideshowTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                performSlide();
            }
        }, mSlideshowDelay);
    }

    private void performSlide() {
        int nextPage = mPager.getCurrentItem() + 1;
        if (nextPage > mAdapter.getEntries().size() - 1) {
            nextPage = mSlideshowLoop ? 0 : -1;
        } else {
            MediaEntry nextEntry = mAdapter.getEntries().get(nextPage);
            if (nextEntry.isVideo()) {
                final int fNextPage = nextPage;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mPager.setCurrentItem(fNextPage);
                        performSlide();
                    }
                });
                return;
            }
        }
        final int fNextPage = nextPage;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (fNextPage != -1) {
                    mPager.setCurrentItem(fNextPage);
                    incrementSlideshow();
                } else {
                    stopSlideshow();
                }
            }
        });
    }

    private void stopSlideshow() {
        if (mSlideshowTimer != null) {
            mSlideshowDelay = 0;
            mSlideshowLoop = false;
            mSlideshowTimer.cancel();
            mSlideshowTimer.purge();
            mSlideshowTimer = null;
            invalidateOptionsMenu();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSlideshowTimer != null) {
            mSlideshowTimer.cancel();
            mSlideshowTimer.purge();
            mSlideshowTimer = null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        // When you edit a photo, the result it will be inserted as the first page so you can scroll to the new image
        if (requestCode == EDIT_REQUEST && resultCode == RESULT_OK) {
            Uri data = intent.getData();
            if (data != null) {
                //TODO
                /*if (data.getScheme() == null || data.getScheme().equals("file")) {
                    MediaEntry pic = new PhotoEntry().load(new File(data.getPath()));
                    mAdapter.add(pic);
                    mPager.setCurrentItem(mPager.getCurrentItem() + 1);
                } else {
                    try {
                        Cursor cursor = getContentResolver().query(data, new PhotoEntry().projection(), null, null, null);
                        if (cursor != null && cursor.moveToFirst()) {
                            MediaEntry pic = new PhotoEntry().load(cursor);
                            mAdapter.add(pic);
                            mPager.setCurrentItem(mPager.getCurrentItem() + 1);
                            cursor.close();
                        }
                    } catch (SecurityException e) {
                        new MaterialDialog.Builder(this)
                                .title(R.string.error)
                                .content(R.string.open_permission_error)
                                .positiveText(android.R.string.ok)
                                .cancelable(false)
                                .callback(new MaterialDialog.ButtonCallback() {
                                    @Override
                                    public void onPositive(MaterialDialog dialog) {
                                        super.onPositive(dialog);
                                        finish();
                                    }
                                }).show();
                    }
                }*/
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void finishAfterTransition() {
        getViewerPagerFragment(mCurrentPosition).finish();

        mIsReturningToMain = true;
        Intent data = new Intent();
        if (getIntent() != null) {
            data.putExtra(MainActivity.EXTRA_OLD_ITEM_POSITION, getIntent().getIntExtra(MainActivity.EXTRA_CURRENT_ITEM_POSITION, 0));
        }
        data.putExtra(MainActivity.EXTRA_CURRENT_ITEM_POSITION, mCurrentPosition);
        //data.putExtra(MainActivity.EXTRA_REMOVED_ITEMS, mRemovedEntryIds.toArray(new Long[mRemovedEntryIds.size()]));
        setResult(RESULT_OK, data);
        super.finishAfterTransition();
    }

    public boolean isFinishedTransition() {
        return mFinishedTransition;
    }

    public void setFinishedTransition(boolean mFinishedTransition) {
        this.mFinishedTransition = mFinishedTransition;
    }

    public interface ToolbarFadeListener {
        void onFade();
    }

    private class FileBeamCallback implements NfcAdapter.CreateBeamUrisCallback {

        public FileBeamCallback() {
        }

        @Override
        public Uri[] createBeamUris(NfcEvent event) {
            if (mCurrentPosition == -1) {
                return null;
            }
            return new Uri[]{
                    Utils.getImageContentUri(ViewerActivity.this,
                            new File(mAdapter.getEntries().get(mCurrentPosition).data()))
            };
        }
    }
}