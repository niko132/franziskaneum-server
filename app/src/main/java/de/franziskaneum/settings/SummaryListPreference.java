package de.franziskaneum.settings;

import android.annotation.TargetApi;
import android.content.Context;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.util.AttributeSet;

public class SummaryListPreference extends ListPreference {

    public SummaryListPreference(Context context) {
        super(context);
    }

    public SummaryListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @TargetApi(21)
    public SummaryListPreference(Context context, AttributeSet attrs,
                                 int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(21)
    public SummaryListPreference(Context context, AttributeSet attrs,
                                 int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                setSummary(getEntries()[findIndexOfValue(o.toString())]);
                return true;
            }
        });
    }

    @Override
    public void onAttached() {
        super.onAttached();
        setSummary(getEntry());
    }
}