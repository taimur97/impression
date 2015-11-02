package com.afollestad.impression.media;

import com.afollestad.impression.views.BreadCrumbLayout;

public interface LoaderView extends ImpressionListView {
    void setCrumb(BreadCrumbLayout.Crumb crumb);

    void reload();

    void saveScrollPosition();
}
