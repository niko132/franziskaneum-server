package de.franziskaneum.settings;

import de.franziskaneum.R;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

public class SettingsActivity extends AppCompatActivity implements
        PreferenceFragmentCompat.OnPreferenceDisplayDialogCallback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SettingsFragment settingsFragment = new SettingsFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.settings_fragment_container,
                settingsFragment).commit();
    }

    @Override
    public boolean onPreferenceDisplayDialog(PreferenceFragmentCompat preferenceFragmentCompat,
                                             Preference preference) {
        DialogFragment fragment;
        if (preference instanceof SchoolClassOrTeacherShortcutPreference) {
            fragment =
                    SchoolClassOrTeacherShortcutPreferenceDialogFragmentCompat.newInstance(preference);
        } else
            return false;

        fragment.setTargetFragment(preferenceFragmentCompat, 0);
        fragment.show(getSupportFragmentManager(),
                "android.support.v7.preference.PreferenceFragment.DIALOG");

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}