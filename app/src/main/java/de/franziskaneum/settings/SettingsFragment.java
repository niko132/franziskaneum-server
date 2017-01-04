package de.franziskaneum.settings;

import de.franziskaneum.R;
import de.franziskaneum.vplan.VPlanNotificationManager;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.XpPreferenceFragment;

public class SettingsFragment extends XpPreferenceFragment {

    @Override
    public void onCreatePreferences2(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.settings);

        final Preference.OnPreferenceChangeListener vplanNotificationListener = new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                // change the notification when the settings change
                VPlanNotificationManager.getInstance().makeNotificationAsync(VPlanNotificationManager.Mode.CACHE);
                return true;
            }
        };

        final SchoolClassOrTeacherShortcutPreference schoolClassOrTeacherShortcut = (SchoolClassOrTeacherShortcutPreference) findPreference("school_class_or_teacher_shortcut");
        schoolClassOrTeacherShortcut.setOnPreferenceChangeListener(vplanNotificationListener);

        Preference teacherMode = findPreference(SettingsManager.KEY_IS_TEACHER_MODE_ENABLED);
        teacherMode.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                schoolClassOrTeacherShortcut.isTeacherChanged((boolean) o);

                // notify our notification listener
                vplanNotificationListener.onPreferenceChange(preference, o);

                return true;
            }
        });

        findPreference(SettingsManager.KEY_VPLAN_NOTIFICATION_ENABLED).setOnPreferenceChangeListener(vplanNotificationListener);
    }
}