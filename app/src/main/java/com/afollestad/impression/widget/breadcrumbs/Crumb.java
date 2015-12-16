package com.afollestad.impression.widget.breadcrumbs;

import android.content.Context;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;

import com.afollestad.impression.R;

public class Crumb implements Parcelable {

    public static final Creator<Crumb> CREATOR = new Creator<Crumb>() {
        public Crumb createFromParcel(Parcel source) {
            return new Crumb(source);
        }

        public Crumb[] newArray(int size) {
            return new Crumb[size];
        }
    };
    private final String mPath;
    private transient Context mContext;
    private int mScrollPos;
    private int mScrollOffset;
    private String mQuery;

    public Crumb(Context context, String path) {
        mContext = context;
        mPath = path;
    }

    protected Crumb(Parcel in) {
        this.mPath = in.readString();
        this.mScrollPos = in.readInt();
        this.mScrollOffset = in.readInt();
        this.mQuery = in.readString();
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
        if (mPath.equals("/")) {
            return mContext.getString(R.string.root);
        } else if (mPath.equals(Environment.getExternalStorageDirectory().getAbsolutePath())) {
            return mContext.getString(R.string.internal_storage);
        }
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

    void setContext(Context context) {
        mContext = context;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mPath);
        dest.writeInt(this.mScrollPos);
        dest.writeInt(this.mScrollOffset);
        dest.writeString(this.mQuery);
    }
}
