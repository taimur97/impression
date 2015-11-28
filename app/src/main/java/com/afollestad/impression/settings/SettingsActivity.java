package com.afollestad.impression.settings;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.afollestad.impression.BuildConfig;
import com.afollestad.impression.R;
import com.afollestad.impression.base.ThemedActivity;
import com.afollestad.impression.excludedfolder.ExcludedFolderActivity;
import com.afollestad.impression.utils.PrefUtils;
import com.afollestad.impression.utils.Utils;
import com.afollestad.impression.widget.ImpressionPreference;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.color.ColorChooserDialog;

/**
 * @author Aidan Follestad (afollestad)
 */
public class SettingsActivity extends ThemedActivity implements ColorChooserDialog.ColorCallback {

    private static final int EXCLUDED_REQUEST = 8000;

    @Override
    protected int darkTheme() {
        return R.style.AppTheme_Settings_Dark;
    }

    @Override
    protected int lightTheme() {
        return R.style.AppTheme_Settings;
    }

    @Override
    protected boolean allowStatusBarColoring() {
        return true;
    }

    @SuppressLint("CommitPrefEdits")
    @Override
    public void onColorSelection(@NonNull ColorChooserDialog colorChooserDialog, @ColorInt int color) {
        if (colorChooserDialog.isAccentMode()) {
            accentColor(color);
        } else {
            primaryColor(color);
        }
        recreate();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preference_activity_custom);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(primaryColor());
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction().replace(R.id.content_frame, new SettingsFragment()).commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EXCLUDED_REQUEST) {
            setResult(resultCode);
        }
    }

    public static class SettingsFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings);

            findPreference(PrefUtils.OPEN_ABOUT).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Activity act = getActivity();
                    new MaterialDialog.Builder(act)
                            .title(getString(R.string.about_dialog_title, BuildConfig.VERSION_NAME))
                            .positiveText(R.string.dismiss)
                            .content(Html.fromHtml(getString(R.string.about_body)))
                            .iconRes(R.drawable.ic_launcher)
                            .linkColor(((SettingsActivity) getActivity()).accentColor())
                            .show();
                    return true;
                }
            });

            findPreference(PrefUtils.OPEN_EXCLUDED_FOLDERS).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    getActivity().startActivityForResult(new Intent(getActivity(), ExcludedFolderActivity.class), EXCLUDED_REQUEST);
                    return false;
                }
            });

            findPreference(PrefUtils.DARK_THEME).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if (getActivity() != null) {
                        getActivity().recreate();
                    }
                    return true;
                }
            });

            Preference colorNavBar = findPreference(PrefUtils.COLORED_NAVBAR);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                colorNavBar.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        int color = ((Boolean) newValue) ? ((ThemedActivity) getActivity()).primaryColor() : Color.BLACK;
                        getActivity().getWindow().setNavigationBarColor(color);
                        return true;
                    }
                });
            } else {
                colorNavBar.setEnabled(false);
                colorNavBar.setSummary(R.string.only_available_api21);
            }

            findPreference(PrefUtils.INCLUDE_SUBFOLDERS).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    getActivity().setResult(RESULT_OK);
                    return true;
                }
            });

            ImpressionPreference primaryColor = (ImpressionPreference) findPreference(PrefUtils.PRIMARY_COLOR_PREFIX);
            primaryColor.setColor(((ThemedActivity) getActivity()).primaryColor(), Utils.resolveColor(getActivity(), R.attr.colorAccent));
            primaryColor.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    SettingsActivity act = (SettingsActivity) getActivity();
                    if (act == null) {
                        return false;
                    }
                    new ColorChooserDialog.Builder(act, preference.getTitleRes())
                            .preselect(act.primaryColor())
                            .backButton(R.string.back)
                            .doneButton(R.string.done)
                            .cancelButton(android.R.string.cancel)
                            .show();
                    return true;
                }
            });


            ImpressionPreference accentColor = (ImpressionPreference) findPreference(PrefUtils.ACCENT_COLOR_PREFIX);
            accentColor.setColor(((ThemedActivity) getActivity()).accentColor(), Utils.resolveColor(getActivity(), R.attr.colorAccent));
            accentColor.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    SettingsActivity act = (SettingsActivity) getActivity();
                    if (act == null) {
                        return false;
                    }
                    new ColorChooserDialog.Builder(act, preference.getTitleRes())
                            .preselect(act.accentColor())
                            .accentMode(true)
                            .backButton(R.string.back)
                            .doneButton(R.string.done)
                            .cancelButton(android.R.string.cancel)
                            .show();
                    return true;
                }
            });
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            ListView list = (ListView) view.findViewById(android.R.id.list);
            list.setDivider(null);
            list.setDividerHeight(0);
            int listTopBottomPadding = getResources().getDimensionPixelSize(R.dimen.list_top_bottom_padding);
            list.setPadding(0, listTopBottomPadding, 0, listTopBottomPadding);
            list.setClipToPadding(false);
        }
    }
}