package com.afollestad.impression.media;

import android.content.Context;
import android.os.Bundle;

import com.afollestad.impression.MvpView;
import com.afollestad.impression.adapters.MediaAdapter;
import com.afollestad.impression.widget.BreadCrumbLayout;

interface MediaView extends MvpView {

    void initializeRecyclerView(boolean gridMode, int size, MediaAdapter adapter);

    void updateGridModeOn(boolean gridMode);

    void updateGridColumns(int size);

    MediaAdapter getAdapter();

    Context getContextCompat();

    Bundle getArguments();

    void setCrumb(BreadCrumbLayout.Crumb crumb);

    void reload();

    void saveScrollPosition();

}
