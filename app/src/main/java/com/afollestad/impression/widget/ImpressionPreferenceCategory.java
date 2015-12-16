package com.afollestad.impression.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.preference.PreferenceCategory;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.afollestad.impression.R;
import com.afollestad.materialdialogs.internal.ThemeSingleton;

public class ImpressionPreferenceCategory extends PreferenceCategory {

    public ImpressionPreferenceCategory(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImpressionPreferenceCategory(Context context) {
        this(context, null, 0);
    }

    public ImpressionPreferenceCategory(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setLayoutResource(R.layout.preference_category_custom);
        setSelectable(false);
    }

    @Override
    protected void onBindView(@NonNull View view) {
        super.onBindView(view);
        ColorStateList positiveColor = ThemeSingleton.get().positiveColor;
        if (positiveColor != null) {
            ((TextView) view.findViewById(android.R.id.title)).setTextColor(positiveColor);
        }
    }
}
