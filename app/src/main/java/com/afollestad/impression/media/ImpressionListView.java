package com.afollestad.impression.media;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;

import com.afollestad.impression.MvpView;

public interface ImpressionListView extends MvpView {
    void invalidateLayoutManagerAndAdapter();

    RecyclerView.Adapter getAdapter();

    RecyclerView getRecyclerView();

    Context getContextCompat();

    Bundle getArguments();
}
