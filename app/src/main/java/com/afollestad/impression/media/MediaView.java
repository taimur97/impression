package com.afollestad.impression.media;

import android.content.Context;
import android.os.Bundle;

import com.afollestad.impression.MvpView;
import com.afollestad.impression.api.MediaEntry;
import com.afollestad.impression.widget.breadcrumbs.Crumb;

import java.util.List;

interface MediaView extends MvpView {

    void initializeRecyclerView(boolean gridMode, int size, MediaAdapter adapter);

    void updateGridModeOn(boolean gridMode);

    void updateGridColumns(int size);

    MediaAdapter getAdapter();

    Context getContextCompat();

    Bundle getArguments();

    void saveScrollPositionInto(Crumb crumb);

    void invalidateEmptyText();

    void setListShown(boolean listShown);

    void restoreScrollPositionFrom(Crumb crumb);

    void invalidateSubtitle(List<MediaEntry> allEntries);
}
