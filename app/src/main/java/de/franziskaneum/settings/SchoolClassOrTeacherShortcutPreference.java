package de.franziskaneum.settings;

import android.content.Context;
import android.util.AttributeSet;

import net.xpece.android.support.preference.DialogPreference;

import de.franziskaneum.R;

public class SchoolClassOrTeacherShortcutPreference extends DialogPreference {

    private SettingsManager settings;

    private int schoolClassStep, schoolClass;
    private String teacherShortcut;
    private boolean isTeacher;

    public SchoolClassOrTeacherShortcutPreference(Context context) {
        super(context);
        init(context);
    }

    public SchoolClassOrTeacherShortcutPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SchoolClassOrTeacherShortcutPreference(Context context, AttributeSet attrs,
                                                  int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public SchoolClassOrTeacherShortcutPreference(Context context, AttributeSet attrs,
                                                  int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);

    }

    private void init(Context context) {
        settings = SettingsManager.getInstance();

        isTeacher = settings.isTeacher();
        schoolClassStep = settings.getSchoolClassStep();
        schoolClass = settings.getSchoolClass();
        teacherShortcut = settings.getTeacherShortcut();

        setSummary();
        setTitle();
    }

    public void setSchoolClassStep(int schoolClassStep) {
        this.schoolClassStep = schoolClassStep;
        settings.setSchoolClassStep(schoolClassStep);

        OnPreferenceChangeListener listener = getOnPreferenceChangeListener();
        if (listener != null)
            listener.onPreferenceChange(this, schoolClassStep);
    }

    public int getSchoolClassStep() {
        return schoolClassStep;
    }

    public int getSchoolClass() {
        return schoolClass;
    }

    public String getTeacherShortcut() {
        return teacherShortcut;
    }

    public void setSchoolClass(int schoolClass) {
        this.schoolClass = schoolClass;
        settings.setSchoolClass(schoolClass);

        OnPreferenceChangeListener listener = getOnPreferenceChangeListener();
        if (listener != null)
            listener.onPreferenceChange(this, schoolClass);
    }

    public void setTeacherShortcut(String teacherShortcut) {
        this.teacherShortcut = teacherShortcut;
        settings.setTeacherShortcut(teacherShortcut);

        OnPreferenceChangeListener listener = getOnPreferenceChangeListener();
        if (listener != null)
            listener.onPreferenceChange(this, teacherShortcut);
    }

    public void isTeacherChanged(boolean isTeacher) {
        this.isTeacher = isTeacher;
        setSummary();
        setTitle();
    }

    public void setSummary() {
        if (isTeacher) {
            setSummary(teacherShortcut != null ? teacherShortcut : "");
        } else {
            if (schoolClassStep >= 11)
                setSummary(String.valueOf(schoolClassStep));
            else
                setSummary(String.valueOf(schoolClassStep) + "/"
                        + String.valueOf(schoolClass));
        }
    }

    private void setTitle() {
        setTitle(getTitleID());
    }

    private int getTitleID() {
        if (isTeacher)
            return R.string.shortcut;
        else
            return R.string.school_class_course;
    }
}