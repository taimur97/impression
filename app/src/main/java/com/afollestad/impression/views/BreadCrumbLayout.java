package com.afollestad.impression.views;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.impression.R;
import com.afollestad.impression.api.AlbumEntry;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author Aidan Follestad (afollestad)
 */
public class BreadCrumbLayout extends HorizontalScrollView implements View.OnClickListener {

    // Stores currently visible crumbs
    private List<Crumb> mCrumbs;
    // Used in setActiveOrAdd() between clearing crumbs and adding the new set, nullified afterwards
    private List<Crumb> mOldCrumbs;
    // Stores user's navigation history, like a fragment back stack
    private List<Crumb> mHistory;
    private LinearLayout mChildFrame;
    private int mActive;
    private SelectionCallback mCallback;
    private FragmentManager mFragmentManager;
    public BreadCrumbLayout(Context context) {
        super(context);
        init();
    }

    public BreadCrumbLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    public BreadCrumbLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public static boolean isStorage(String path) {
        return path == null ||
                path.equals(AlbumEntry.ALBUM_OVERVIEW) ||
                path.equals(Environment.getExternalStorageDirectory().getAbsolutePath());
    }

    private void init() {
        setMinimumHeight((int) getResources().getDimension(R.dimen.breadcrumb_height));
        setClipToPadding(false);
        setHorizontalScrollBarEnabled(false);
        mCrumbs = new ArrayList<>();
        mHistory = new ArrayList<>();
        mChildFrame = new LinearLayout(getContext());
        addView(mChildFrame, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    public void addHistory(Crumb crumb) {
        mHistory.add(crumb);
    }

    public Crumb lastHistory() {
        if (mHistory.size() == 0) return null;
        return mHistory.get(mHistory.size() - 1);
    }

    public boolean popHistory() {
        if (mHistory.size() == 0) return false;
        mHistory.remove(mHistory.size() - 1);
        return mHistory.size() != 0;
    }

    public void clearHistory() {
        mHistory.clear();
    }

//    public int historySize() {
//        return mHistory.size();
//    }

    public void reverseHistory() {
        Collections.reverse(mHistory);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void setAlpha(View view, int alpha) {
        if (view instanceof ImageView && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            ((ImageView) view).setImageAlpha(alpha);
        } else {
            ViewCompat.setAlpha(view, alpha);
        }
    }

    public void setFragmentManager(FragmentManager fm) {
        this.mFragmentManager = fm;
    }

    public void addCrumb(@NonNull Crumb crumb, boolean refreshLayout) {
        LinearLayout view = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.bread_crumb, this, false);
        view.setTag(mCrumbs.size());
        view.setOnClickListener(this);

        ImageView iv = (ImageView) view.getChildAt(1);
        Drawable arrow = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_right_arrow, null);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            assert arrow != null;
            arrow.setAutoMirrored(true);
        }

        iv.setImageDrawable(arrow);
        iv.setVisibility(View.GONE);

        mChildFrame.addView(view, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mCrumbs.add(crumb);
        if (refreshLayout) {
            mActive = mCrumbs.size() - 1;
            requestLayout();
        }
        invalidateActivatedAll();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        //RTL works fine like this
        View child = mChildFrame.getChildAt(mActive);
        if (child != null)
            smoothScrollTo(child.getLeft(), 0);
    }

    public Crumb findCrumb(@NonNull String forDir) {
        for (int i = 0; i < mCrumbs.size(); i++) {
            if (mCrumbs.get(i).getPath().equals(forDir))
                return mCrumbs.get(i);
        }
        return null;
    }

    public void clearCrumbs() {
        try {
            mFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            mOldCrumbs = new ArrayList<>(mCrumbs);
            mCrumbs.clear();
            mChildFrame.removeAllViews();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public Crumb getCrumb(int index) {
        return mCrumbs.get(index);
    }

    public void setCallback(SelectionCallback callback) {
        mCallback = callback;
    }

    private boolean setActive(Crumb newActive) {
        mActive = mCrumbs.indexOf(newActive);
        invalidateActivatedAll();
        boolean success = mActive > -1;
        if (success)
            requestLayout();
        return success;
    }

    void invalidateActivatedAll() {
        for (int i = 0; i < mCrumbs.size(); i++) {
            Crumb crumb = mCrumbs.get(i);
            invalidateActivated(mChildFrame.getChildAt(i), mActive == mCrumbs.indexOf(crumb), false, i < mCrumbs.size() - 1)
                    .setText(crumb.getTitle());
        }
    }

    void removeCrumbAt(int index) {
        mCrumbs.remove(index);
        mChildFrame.removeViewAt(index);
    }

    public boolean trim(String str, boolean dir) {
        if (!dir) return false;
        int index = -1;
        for (int i = mCrumbs.size() - 1; i >= 0; i--) {
            String path = mCrumbs.get(i).getPath();
            if (path.equals(str)) {
                index = i;
                break;
            }
        }

        boolean removedActive = index >= mActive;
        if (index > -1) {
            while (index <= mCrumbs.size() - 1)
                removeCrumbAt(index);
            if (mChildFrame.getChildCount() > 0) {
                int lastIndex = mCrumbs.size() - 1;
                invalidateActivated(mChildFrame.getChildAt(lastIndex), mActive == lastIndex, false, false);
            }
        }
        return removedActive || mCrumbs.size() == 0;
    }

    public boolean trim(String path) {
        return trim(path, new java.io.File(path).isDirectory());
    }

    void updateIndices() {
        for (int i = 0; i < mChildFrame.getChildCount(); i++)
            mChildFrame.getChildAt(i).setTag(i);
    }

    public void setActiveOrAdd(@NonNull Crumb crumb, boolean forceRecreate) {
        if (forceRecreate || !setActive(crumb)) {
            clearCrumbs();
            final List<String> newPathSet = new ArrayList<>();

            File p = new File(crumb.getPath());
            newPathSet.add(p.getAbsolutePath());
            if (!isStorage(p.getAbsolutePath())) {
                while ((p = p.getParentFile()) != null) {
                    newPathSet.add(0, p.getAbsolutePath());
                    if (isStorage(p.getPath()))
                        break;
                }
            }

            for (int index = 0; index < newPathSet.size(); index++) {
                final String fi = newPathSet.get(index);
                crumb = new Crumb(getContext(), fi);

                // Restore scroll positions saved before clearing
                if (mOldCrumbs != null) {
                    for (Iterator<Crumb> iterator = mOldCrumbs.iterator(); iterator.hasNext(); ) {
                        Crumb old = iterator.next();
                        if (old.equals(crumb)) {
                            crumb.setScrollPosition(old.getScrollPosition());
                            crumb.setScrollOffset(old.getScrollOffset());
                            crumb.setQuery(old.getQuery());
                            iterator.remove(); // minimize number of linear passes by removing un-used crumbs from history
                            break;
                        }
                    }
                }

                addCrumb(crumb, true);
            }

            // History no longer needed
            mOldCrumbs = null;
        } else {
            if (isStorage(crumb.getPath())) {
                Crumb c = mCrumbs.get(0);
                while (c != null && !isStorage(c.getPath())) {
                    removeCrumbAt(0);
                    if (mCrumbs.size() > 0)
                        c = mCrumbs.get(0);
                }
                updateIndices();
                requestLayout();
            }
        }
    }

    public int size() {
        return mCrumbs.size();
    }

    private TextView invalidateActivated(View view, boolean isActive, boolean noArrowIfAlone, boolean allowArrowVisible) {
        LinearLayout child = (LinearLayout) view;
        TextView tv = (TextView) child.getChildAt(0);
        tv.setTextColor(ContextCompat.getColor(getContext(), isActive ? R.color.crumb_active : R.color.crumb_inactive));
        ImageView iv = (ImageView) child.getChildAt(1);
        setAlpha(iv, isActive ? 255 : 109);
        if (noArrowIfAlone && getChildCount() == 1)
            iv.setVisibility(View.GONE);
        else if (allowArrowVisible)
            iv.setVisibility(View.VISIBLE);
        else
            iv.setVisibility(View.GONE);
        return tv;
    }

    public int getActiveIndex() {
        return mActive;
    }

    @Override
    public void onClick(View v) {
        if (mCallback != null) {
            int index = (Integer) v.getTag();
            mCallback.onCrumbSelection(mCrumbs.get(index), index);
        }
    }

    public SavedStateWrapper getStateWrapper() {
        return new SavedStateWrapper(this);
    }

    public void restoreFromStateWrapper(SavedStateWrapper mSavedState, Activity context) {
        if (mSavedState != null) {
            mActive = mSavedState.mActive;
            for (Crumb c : mSavedState.mCrumbs) {
                c.mContext = getContext();
                addCrumb(c, false);
            }
            requestLayout();
            setVisibility(mSavedState.mVisibility);
        }
    }


    public interface SelectionCallback {
        void onCrumbSelection(Crumb crumb, int index);
    }

    public static class Crumb implements Serializable {

        private final String mPath;
        private transient Context mContext;
        private int mScrollPos;
        private int mScrollOffset;
        private String mQuery;

        public Crumb(Context context, String path) {
            mContext = context;
            mPath = path;
        }

        public String getQuery() {
            return mQuery;
        }

        public void setQuery(String query) {
            this.mQuery = query;
        }

        public int getScrollPosition() {
            return mScrollPos;
        }

        public void setScrollPosition(int scrollY) {
            this.mScrollPos = scrollY;
        }

        public int getScrollOffset() {
            return mScrollOffset;
        }

        public void setScrollOffset(int scrollOffset) {
            this.mScrollOffset = scrollOffset;
        }

        public String getTitle() {
            if (mPath.equals("/"))
                return mContext.getString(R.string.root);
            else if (mPath.equals(Environment.getExternalStorageDirectory().getAbsolutePath()))
                return mContext.getString(R.string.internal_storage);
            return new java.io.File(mPath).getName();
        }

        public String getPath() {
            return mPath;
        }

        @Override
        public boolean equals(Object o) {
            return (o instanceof Crumb) && ((Crumb) o).getPath().equals(getPath());
        }

        @Override
        public String toString() {
            return getPath();
        }
    }

    public static class SavedStateWrapper implements Serializable {

        public final int mActive;
        public final List<Crumb> mCrumbs;
        public final int mVisibility;

        public SavedStateWrapper(BreadCrumbLayout view) {
            mActive = view.mActive;
            mCrumbs = view.mCrumbs;
            mVisibility = view.getVisibility();
        }
    }
}