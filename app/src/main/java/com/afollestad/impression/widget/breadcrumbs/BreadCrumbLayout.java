package com.afollestad.impression.widget.breadcrumbs;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
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
    private String mTopPath;

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

    public String getTopPath() {
        return mTopPath;
    }

    public void setTopPath(String topPath) {
        mTopPath = topPath;
    }

    public boolean isTopPath(String path) {
        return mTopPath.equals(path);
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

    private void addCrumb(@NonNull Crumb crumb, boolean refreshLayout) {
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

        mChildFrame.addView(view);
        mCrumbs.add(crumb);
        if (refreshLayout) {
            mActive = mCrumbs.size() - 1;
            requestLayout();
            invalidateActivatedAll();
        }
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

    private void clearCrumbs() {
        try {
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

    void updateIndices() {
        for (int i = 0; i < mChildFrame.getChildCount(); i++)
            mChildFrame.getChildAt(i).setTag(i);
    }

    public void setActiveOrAdd(@NonNull Crumb crumb, boolean forceRecreate) {
        if (forceRecreate || !setActive(crumb)) {
            clearCrumbs();
            final List<String> newPathSet = new ArrayList<>();


            if (!isTopPath(crumb.getPath())) {
                File p = new File(crumb.getPath());
                newPathSet.add(p.getAbsolutePath());

                while ((p = p.getParentFile()) != null) {
                    newPathSet.add(0, p.getAbsolutePath());
                    if (isTopPath(p.getPath()))
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
            if (isTopPath(crumb.getPath())) {
                Crumb c = mCrumbs.get(0);
                while (c != null && !isTopPath(c.getPath())) {
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
/*
    public SavedState getStateWrapper() {
        return new SavedState(mActive,);
    }*/
/*
    public void restoreFromStateWrapper(SavedState mSavedState) {
        if (mSavedState != null) {
            mActive = mSavedState.active;
            for (Crumb c : mSavedState.crumbs) {
                c.setContext(getContext());
                addCrumb(c, false);
            }
            requestLayout();
            setVisibility(mSavedState.visibility);
        }
    }*/

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();

        return new SavedState(superState, this);
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());

        mActive = ss.active;
        mTopPath = ss.topPath;

        for (Crumb c : ss.crumbs) {
            c.setContext(getContext());
            addCrumb(c, false);
        }

        mHistory.addAll(Arrays.asList(ss.history));


        //setVisibility(visibility);
    }

    public interface SelectionCallback {
        void onCrumbSelection(Crumb crumb, int index);
    }

    static class SavedState extends View.BaseSavedState {

        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        int active;
        Crumb[] crumbs;
        int visibility;
        String topPath;
        Crumb[] history;

        SavedState(Parcelable superState, BreadCrumbLayout view) {
            super(superState);

            this.active = view.mActive;
            this.crumbs = view.mCrumbs.toArray(new Crumb[view.mCrumbs.size()]);
            this.visibility = view.getVisibility();
            this.topPath = view.mTopPath;
            this.history = view.mHistory.toArray(new Crumb[view.mHistory.size()]);
        }

        private SavedState(Parcel source) {
            super(source);
            this.active = source.readInt();
            this.crumbs = source.createTypedArray(Crumb.CREATOR);
            this.visibility = source.readInt();
            this.topPath = source.readString();
            this.history = source.createTypedArray(Crumb.CREATOR);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(active);
            out.writeTypedArray(crumbs, 0);
            out.writeInt(visibility);
            out.writeString(topPath);
            out.writeTypedArray(history, 0);
        }
    }
}