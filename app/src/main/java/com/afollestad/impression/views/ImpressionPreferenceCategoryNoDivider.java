package com.afollestad.impression.views;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;

import com.afollestad.impression.R;

public class ImpressionPreferenceCategoryNoDivider extends ImpressionPreferenceCategory {
    public ImpressionPreferenceCategoryNoDivider(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ImpressionPreferenceCategoryNoDivider(Context context) {
        super(context);
    }

    public ImpressionPreferenceCategoryNoDivider(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onBindView(@NonNull View view) {
        super.onBindView(view);
        view.findViewById(R.id.divider).setVisibility(View.GONE);
        view.setPadding(0, 0, 0, 0);
    }
}
