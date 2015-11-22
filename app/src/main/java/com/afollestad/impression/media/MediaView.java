package com.afollestad.impression.media;

import android.content.Context;
import android.os.Bundle;

import com.afollestad.impression.MvpView;
import com.afollestad.impression.widget.breadcrumbs.Crumb;

interface MediaView extends MvpView {

    void initializeRecyclerView(boolean gridMode, int size, MediaAdapter adapter);

    void updateGridModeOn(boolean gridMode);

    void updateGridColumns(int size);

    MediaAdapter getAdapter();

    Context getContextCompat();

    Bundle getArguments();

    void setCrumb(Crumb crumb);

    void reload();

    void saveScrollPosition();

}
