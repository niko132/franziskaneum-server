package de.franziskaneum.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.Preference;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import net.xpece.android.support.preference.XpPreferenceDialogFragment;

import de.franziskaneum.R;

/**
 * Created by Niko on 07.04.2016.
 */
public class SchoolClassOrTeacherShortcutPreferenceDialogFragmentCompat extends
        XpPreferenceDialogFragment {

    private Spinner spinnerSchoolClassStep, spinnerSchoolClass;
    private EditText shortcut;

    private SettingsManager settings;
    private SchoolClassOrTeacherShortcutPreference preference;

    private int schoolClassStep, schoolClass;
    private String teacherShortcut;
    private boolean isTeacher;

    public static SchoolClassOrTeacherShortcutPreferenceDialogFragmentCompat newInstance(Preference preference) {
        final SchoolClassOrTeacherShortcutPreferenceDialogFragmentCompat
                fragment = new SchoolClassOrTeacherShortcutPreferenceDialogFragmentCompat();
        final Bundle b = new Bundle(1);
        b.putString(ARG_KEY, preference.getKey());
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settings = SettingsManager.getInstance();
        preference = (SchoolClassOrTeacherShortcutPreference) getPreference();
        schoolClassStep = preference.getSchoolClassStep();
        schoolClass = preference.getSchoolClass();
        teacherShortcut = preference.getTeacherShortcut();
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);
        builder.setTitle(getTitleID());
    }

    @Override
    protected View onCreateDialogView(Context context) {
        isTeacher = settings.isTeacher();

        View root;

        if (isTeacher) {
            root = LayoutInflater.from(context).inflate(
                    R.layout.preference_dialog_teacher_shortcut, null);

            shortcut = (EditText) root
                    .findViewById(R.id.preference_dialog_teacher_shortcut);
        } else {
            root = LayoutInflater.from(context).inflate(
                    R.layout.preference_dialog_school_class, null);

            spinnerSchoolClassStep = (Spinner) root
                    .findViewById(R.id.preference_dialog_school_class_step);
            spinnerSchoolClass = (Spinner) root
                    .findViewById(R.id.preference_dialog_school_class_suffix);

            ArrayAdapter<CharSequence> schoolClassStepAdapter = ArrayAdapter
                    .createFromResource(context, R.array.school_class_step,
                            android.R.layout.simple_spinner_item);
            schoolClassStepAdapter
                    .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerSchoolClassStep.setAdapter(schoolClassStepAdapter);

            ArrayAdapter<CharSequence> schoolClassAdapter = ArrayAdapter
                    .createFromResource(context, R.array.school_class,
                            android.R.layout.simple_spinner_item);
            schoolClassAdapter
                    .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerSchoolClass.setAdapter(schoolClassAdapter);

            spinnerSchoolClassStep
                    .setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                        @Override
                        public void onItemSelected(AdapterView<?> parent,
                                                   View view, int position, long id) {
                            spinnerSchoolClass.setEnabled(position <= 5);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }

                    });
        }

        return root;
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        if (isTeacher) {
            shortcut.setText(teacherShortcut);
        } else {
            spinnerSchoolClassStep.setSelection(schoolClassStep - 5);
            spinnerSchoolClass.setSelection(schoolClass - 1);
        }
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            if (isTeacher) {
                teacherShortcut = shortcut.getText().toString();
                preference.setTeacherShortcut(teacherShortcut);
            } else {
                schoolClassStep = spinnerSchoolClassStep.getSelectedItemPosition() + 5;
                schoolClass = spinnerSchoolClass.getSelectedItemPosition() + 1;

                preference.setSchoolClassStep(schoolClassStep);
                preference.setSchoolClass(schoolClass);
            }

            preference.setSummary();
        }
    }

    private int getTitleID() {
        if (isTeacher)
            return R.string.shortcut;
        else
            return R.string.school_class_course;
    }
}
