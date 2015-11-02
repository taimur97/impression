package com.afollestad.impression.media;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.afollestad.impression.MvpPresenter;

public abstract class ImpressionListPresenter<V extends ImpressionListView> extends MvpPresenter<V> {

    abstract String getTitle();

    abstract int getEmptyText();

    abstract GridLayoutManager createLayoutManager();

    abstract RecyclerView.Adapter createAdapter();
}
