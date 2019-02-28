package de.franziskaneum.settings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import java.util.Calendar;

/**
 * Created by Niko on 18.02.2016.
 */
public class SettingsManager {
    public static final String ACTION_SETTINGS_CHANGED = "de.franziskaneum.settings.action.SETTINGS_CHANGED";

    // preference keys
    public static final String KEY_IS_TEACHER_MODE_ENABLED = "teacher_mode_enabled";
    public static final String KEY_SCHOOL_CLASS_STEP = "class_step";
    public static final String KEY_SCHOOL_CLASS = "class_suffix";
    public static final String KEY_TEACHER_SHORTCUT = "teacher_shortcut";
    public static final String KEY_HOME_CATEGORY = "home_category";
    public static final String KEY_HAS_AB_WEEK = "timetable_a_b_week";
    public static final String KEY_VIBRATION_LENGTH = "notification_vibration";
    public static final String KEY_NOTIFICATION_LIGHT = "notification_light";
    public static final String KEY_CREATE_ACTIVITY_LOG_ENABLED = "create_activity_log_enabled";
    public static final String KEY_VPLAN_LAST_MODIFIED = "vplan_last_modified";
    public static final String KEY_VPLAN_AUTHENTICATION_PASSWORD = "vplan_authentication_password";
    public static final String KEY_VPLAN_NOTIFICATION_ENABLED = "vplan_notification_enabled";
    public static final String KEY_VPLAN_NOTIFICATION_DELETED = "vplan_notification_deleted";
    public static final String KEY_TEACHER_LIST_LAST_REFRESH = "teacher_list_last_refresh";
    public static final String KEY_SCHOOL_CLASS_LAST_MODIFIED = "school_class_last_modified";
    public static final String KEY_IGNORED_APP_UPDATE_VERSION = "ignore_update";
    public static final String KEY_TIMETABLE_SHOW_TIMES = "timetable_show_times";

    private static SettingsManager instance;

    private SharedPreferences prefs;

    // preference values
    private boolean isTeacher;
    private int schoolClassStep;
    private int schoolClass;
    private String teacherShortcut;
    private String homeCategory;
    private boolean hasABWeek;
    private int vibrationLength;
    private int notificationLight;
    private boolean createActivityLogEnabled;
    private long vplanLastModified;
    private String vplanAuthenticationPassword;
    private boolean vplanNotificationEnabled;
    private boolean vplanNotificationDeleted;
    private long teacherListLastRefresh;
    private long schoolClassLastModified;
    private int ignoredAppUpdateVersion;
    private boolean timetableShowTimes;

    // preference listener
    private SharedPreferences.OnSharedPreferenceChangeListener listener;

    private SettingsManager(final Context context) {
        prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());

        isTeacher = prefs.getBoolean(KEY_IS_TEACHER_MODE_ENABLED, false);
        schoolClassStep = prefs.getInt(KEY_SCHOOL_CLASS_STEP, 5);
        schoolClass = prefs.getInt(KEY_SCHOOL_CLASS, 1);
        teacherShortcut = prefs.getString(KEY_TEACHER_SHORTCUT, null);
        homeCategory = prefs.getString(KEY_HOME_CATEGORY, "news");
        hasABWeek = prefs.getBoolean(KEY_HAS_AB_WEEK, false);
        vibrationLength = Integer.parseInt(prefs.getString(KEY_VIBRATION_LENGTH, "500"));
        notificationLight = Integer.parseInt(prefs.getString(KEY_NOTIFICATION_LIGHT, "-65281"));
        createActivityLogEnabled = prefs.getBoolean(KEY_CREATE_ACTIVITY_LOG_ENABLED, false);
        vplanLastModified = prefs.getLong(KEY_VPLAN_LAST_MODIFIED, -1);
        vplanAuthenticationPassword = prefs.getString(KEY_VPLAN_AUTHENTICATION_PASSWORD, null);
        vplanNotificationEnabled = prefs.getBoolean(KEY_VPLAN_NOTIFICATION_ENABLED, true);
        vplanNotificationDeleted = prefs.getBoolean(KEY_VPLAN_NOTIFICATION_DELETED, false);
        teacherListLastRefresh = prefs.getLong(KEY_TEACHER_LIST_LAST_REFRESH, 0);
        schoolClassLastModified = prefs.getLong(KEY_SCHOOL_CLASS_LAST_MODIFIED,
                Calendar.getInstance().getTimeInMillis());
        ignoredAppUpdateVersion = prefs.getInt(KEY_IGNORED_APP_UPDATE_VERSION, 0);
        timetableShowTimes = prefs.getBoolean(KEY_TIMETABLE_SHOW_TIMES, false);

        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                switch(key) {
                    case KEY_IS_TEACHER_MODE_ENABLED:
                        isTeacher = prefs.getBoolean(KEY_IS_TEACHER_MODE_ENABLED, isTeacher);
                        break;
                    case KEY_SCHOOL_CLASS_STEP:
                        schoolClassStep = prefs.getInt(KEY_SCHOOL_CLASS_STEP, schoolClassStep);

                        // change last the last school class update
                        setSchoolClassLastModified(Calendar.getInstance().getTimeInMillis());
                        break;
                    case KEY_SCHOOL_CLASS:
                        schoolClass = prefs.getInt(KEY_SCHOOL_CLASS, schoolClass);
                        break;
                    case KEY_TEACHER_SHORTCUT:
                        teacherShortcut = prefs.getString(KEY_TEACHER_SHORTCUT, teacherShortcut);
                        break;
                    case KEY_HOME_CATEGORY:
                        homeCategory = prefs.getString(KEY_HOME_CATEGORY, homeCategory);
                        break;
                    case KEY_HAS_AB_WEEK:
                        hasABWeek = prefs.getBoolean(KEY_HAS_AB_WEEK, hasABWeek);
                        break;
                    case KEY_VIBRATION_LENGTH:
                        vibrationLength = Integer.parseInt(prefs.getString(
                                KEY_VIBRATION_LENGTH, String.valueOf(vibrationLength)));
                        break;
                    case KEY_NOTIFICATION_LIGHT:
                        notificationLight = Integer.parseInt(prefs.getString(
                                KEY_NOTIFICATION_LIGHT, String.valueOf(notificationLight)));
                        break;
                    case KEY_CREATE_ACTIVITY_LOG_ENABLED:
                        createActivityLogEnabled = prefs.getBoolean(
                                KEY_CREATE_ACTIVITY_LOG_ENABLED, createActivityLogEnabled);
                        break;
                    case KEY_VPLAN_LAST_MODIFIED:
                        vplanLastModified = prefs.getLong(KEY_VPLAN_LAST_MODIFIED,
                                vplanLastModified);
                        break;
                    case KEY_VPLAN_AUTHENTICATION_PASSWORD:
                        vplanAuthenticationPassword =
                                prefs.getString(KEY_VPLAN_AUTHENTICATION_PASSWORD,
                                        vplanAuthenticationPassword);
                        break;
                    case KEY_VPLAN_NOTIFICATION_ENABLED:
                        vplanNotificationEnabled = prefs.getBoolean(KEY_VPLAN_NOTIFICATION_ENABLED,
                                vplanNotificationEnabled);
                        // notifications were just enabled so it there can`t be a deleted notification
                        setVPlanNotificationDeleted(false);
                        break;
                    case KEY_VPLAN_NOTIFICATION_DELETED:
                        vplanNotificationDeleted = prefs.getBoolean(KEY_VPLAN_NOTIFICATION_DELETED,
                                vplanNotificationDeleted);
                        break;
                    case KEY_TEACHER_LIST_LAST_REFRESH:
                        teacherListLastRefresh = prefs.getLong(KEY_TEACHER_LIST_LAST_REFRESH,
                                teacherListLastRefresh);
                        break;
                    case KEY_SCHOOL_CLASS_LAST_MODIFIED:
                        schoolClassLastModified = prefs.getLong(KEY_SCHOOL_CLASS_LAST_MODIFIED,
                                schoolClassLastModified);
                        break;
                    case KEY_IGNORED_APP_UPDATE_VERSION:
                        ignoredAppUpdateVersion = prefs.getInt(KEY_IGNORED_APP_UPDATE_VERSION,
                                ignoredAppUpdateVersion);
                        break;
                    case KEY_TIMETABLE_SHOW_TIMES:
                        timetableShowTimes = prefs.getBoolean(KEY_TIMETABLE_SHOW_TIMES,
                                timetableShowTimes);
                        break;
                }

                Intent settingsChangedBroadcastIntent = new Intent(ACTION_SETTINGS_CHANGED);
                context.sendBroadcast(settingsChangedBroadcastIntent);
            }
        };

        prefs.registerOnSharedPreferenceChangeListener(listener);
    }

    public static void initInstance(Context context) {
        instance = new SettingsManager(context.getApplicationContext());
    }

    public static SettingsManager getInstance() {
        if (instance == null)
            throw new NullPointerException("You have to init the instance");

        return instance;
    }

    public boolean isTeacher() {
        return isTeacher;
    }

    public void setIsTeacher(boolean isTeacher) {
        this.isTeacher = isTeacher;
        prefs.edit().putBoolean(KEY_IS_TEACHER_MODE_ENABLED, isTeacher).apply();
    }

    public int getSchoolClassStep() {
        return schoolClassStep;
    }

    public void setSchoolClassStep(int schoolClassStep) {
        this.schoolClassStep = schoolClassStep;
        prefs.edit().putInt(KEY_SCHOOL_CLASS_STEP, schoolClassStep).apply();
    }

    public int getSchoolClass() {
        return schoolClass;
    }

    public void setSchoolClass(int schoolClass) {
        this.schoolClass = schoolClass;
        prefs.edit().putInt(KEY_SCHOOL_CLASS, schoolClass).apply();
    }

    @Nullable
    public String getTeacherShortcut() {
        return teacherShortcut;
    }

    public void setTeacherShortcut(@Nullable String teacherShortcut) {
        this.teacherShortcut = teacherShortcut;
        prefs.edit().putString(KEY_TEACHER_SHORTCUT, teacherShortcut).apply();
    }

    public String getHomeCategory() {
        return homeCategory;
    }

    public void setHomeCategory(String homeCategory) {
        this.homeCategory = homeCategory;
        prefs.edit().putString(KEY_HOME_CATEGORY, homeCategory).apply();
    }

    public boolean hasABWeek() {
        return hasABWeek;
    }

    public void setHasABWeek(boolean hasABWeek) {
        this.hasABWeek = hasABWeek;
        prefs.edit().putBoolean(KEY_HAS_AB_WEEK, hasABWeek).apply();
    }

    public int getVibrationLength() {
        return vibrationLength;
    }

    public void setVibrationLength(int vibrationLength) {
        this.vibrationLength = vibrationLength;
        prefs.edit().putInt(KEY_VIBRATION_LENGTH, vibrationLength).apply();
    }

    public int getNotificationLight() {
        return notificationLight;
    }

    public void setNotificationLight(int notificationLight) {
        this.notificationLight = notificationLight;
        prefs.edit().putInt(KEY_NOTIFICATION_LIGHT, notificationLight).apply();
    }

    public boolean isCreateActivityLogEnabled() {
        return createActivityLogEnabled;
    }

    public void setCreateActivityLogEnabled(boolean createActivityLogEnabled) {
        this.createActivityLogEnabled = createActivityLogEnabled;
        prefs.edit().putBoolean(KEY_CREATE_ACTIVITY_LOG_ENABLED, createActivityLogEnabled).apply();
    }

    public long getVPlanLastModified() {
        return vplanLastModified;
    }

    public void setVPlanLastModified(long vplanLastModified) {
        this.vplanLastModified = vplanLastModified;
        prefs.edit().putLong(KEY_VPLAN_LAST_MODIFIED, vplanLastModified).apply();
    }

    @Nullable
    public String getVPlanAuthenticationPassword() {
        return vplanAuthenticationPassword;
    }

    public void setVPlanAuthenticationPassword(@Nullable String vplanAuthenticationPassword) {
        this.vplanAuthenticationPassword = vplanAuthenticationPassword;
        prefs.edit().putString(KEY_VPLAN_AUTHENTICATION_PASSWORD, vplanAuthenticationPassword)
                .apply();
    }

    public boolean isVPlanNotificationEnabled() {
        return vplanNotificationEnabled;
    }

    public void setVPlanNotificationEnabled(boolean vplanNotificationEnabled) {
        this.vplanNotificationEnabled = vplanNotificationEnabled;
        prefs.edit().putBoolean(KEY_VPLAN_NOTIFICATION_ENABLED, vplanNotificationEnabled).apply();
    }

    public boolean isVPlanNotificationDeleted() {
        return vplanNotificationDeleted;
    }

    public void setVPlanNotificationDeleted(boolean vplanNotificationDeleted) {
        this.vplanNotificationDeleted = vplanNotificationDeleted;
        prefs.edit().putBoolean(KEY_VPLAN_NOTIFICATION_DELETED, vplanNotificationDeleted).apply();
    }

    public long getTeacherListLastRefresh() {
        return teacherListLastRefresh;
    }

    public void setTeacherListLastRefresh(long teacherListLastRefresh) {
        this.teacherListLastRefresh = teacherListLastRefresh;
        prefs.edit().putLong(KEY_TEACHER_LIST_LAST_REFRESH, teacherListLastRefresh).apply();
    }

    public long getSchoolClassLastModified() {
        return schoolClassLastModified;
    }

    public void setSchoolClassLastModified(long schoolClassLastModified) {
        this.schoolClassLastModified = schoolClassLastModified;
        prefs.edit().putLong(KEY_SCHOOL_CLASS_LAST_MODIFIED, schoolClassLastModified).apply();
    }

    public int getIgnoredAppUpdateVersion() {
        return ignoredAppUpdateVersion;
    }

    public void setIgnoredAppUpdateVersion(int ignoredAppUpdateVersion) {
        this.ignoredAppUpdateVersion = ignoredAppUpdateVersion;
        prefs.edit().putInt(KEY_IGNORED_APP_UPDATE_VERSION, ignoredAppUpdateVersion).apply();
    }

    public boolean getTimetableShowTimes() {
        return timetableShowTimes;
    }

    public void setTimetableShowTimes(boolean timetableShowTimes) {
        this.timetableShowTimes = timetableShowTimes;
        prefs.edit().putBoolean(KEY_TIMETABLE_SHOW_TIMES, timetableShowTimes).apply();
    }
}
