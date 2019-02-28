package de.franziskaneum.vplan;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import de.franziskaneum.BroadcastReceiver;
import de.franziskaneum.Constants;
import de.franziskaneum.FranzCallback;
import de.franziskaneum.MainActivity;
import de.franziskaneum.R;
import de.franziskaneum.ReturnValue;
import de.franziskaneum.Status;
import de.franziskaneum.settings.SettingsManager;
import de.franziskaneum.teacher.TeacherList;
import de.franziskaneum.timetable.Timetable;
import de.franziskaneum.timetable.TimetableManager;

/**
 * Created by Niko on 19.12.2016.
 */

public class VPlanNotificationManager {
    public static final String ACTION_NEW_VPLAN_AVAILABLE =
            "de.franziskaneum.vplan.VPlanNotificationManager.action.NEW_VPLAN_AVAILABLE";
    public static final String ACTION_NOTIFICATION_DELETED =
            "de.franziskaneum.vplan.VPlanNotificationManager.action.NOTIFICATION_DELETED";
    private static final String VPLAN_NOTIFICATION_FILENAME = "vplan_notification.json";
    private static final String NOTIFICATION_CHANNEL_ID_VPLAN = "vplan";

    private static VPlanNotificationManager instance;

    private Context context;
    private SettingsManager settings;

    public enum Mode {
        DOWNLOAD, CACHE, SHOW_ONLY
    }

    private VPlanNotificationManager(Context context) {
        this.context = context;
        this.settings = SettingsManager.getInstance();
    }

    public static void initInstance(Context context) {
        instance = new VPlanNotificationManager(context);
    }

    public static VPlanNotificationManager getInstance() {
        if (instance == null)
            throw new NullPointerException("You have to init the instance");

        return instance;
    }

    void getSavedNotificationsAsync(@NonNull final FranzCallback callback) {
        final Handler handler = new Handler(Looper.getMainLooper());
        new Thread(new Runnable() {
            @Override
            public void run() {
                final VPlanNotification vplanNotification = getSavedNotifications();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onCallback(Status.OK, vplanNotification);
                    }
                });
            }
        }).start();
    }

    @Nullable
    VPlanNotification getSavedNotifications() {
        File cachedVPlanNotificationFile = new File(context.getFilesDir(), VPLAN_NOTIFICATION_FILENAME);
        return VPlanNotification.readFromFile(cachedVPlanNotificationFile);
    }

    void saveNotificationsAsync(@Nullable final VPlanNotification vplanNotification) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                saveNotifications(vplanNotification);
            }
        }).start();
    }

    void saveNotifications(@Nullable VPlanNotification vplanNotification) {
        File cachedVPlanNotificationFile = new File(context.getFilesDir(), VPLAN_NOTIFICATION_FILENAME);
        VPlanNotification.writeToFile(vplanNotification, cachedVPlanNotificationFile);

        // update AppWidget every time notifications are saved
        int[] appWidgetIds = AppWidgetManager.getInstance(context).getAppWidgetIds(
                new ComponentName(context, VPlanAppWidgetProvider.class));
        Intent appWidgetIntent = new Intent(context, VPlanAppWidgetProvider.class);
        appWidgetIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        appWidgetIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        context.sendBroadcast(appWidgetIntent);
    }

    @Nullable
    VPlanNotification removeOldDays(@Nullable VPlanNotification vplanNotification) {
        if (vplanNotification != null) {
            Calendar maxDate = Calendar.getInstance();
            maxDate.set(Calendar.MINUTE, 0);
            maxDate.set(Calendar.SECOND, 0);
            maxDate.set(Calendar.MILLISECOND, 0);
            maxDate.setTimeInMillis(maxDate.getTimeInMillis() - 15 * 60 * 60 * 1000);

            for (int i = vplanNotification.size() - 1; i >= 0; i--) {
                Calendar notificationDayDate = vplanNotification.get(i).getCalendarDate();
                if (notificationDayDate != null)
                    if (!notificationDayDate.after(maxDate))
                        vplanNotification.remove(i);
            }
        }

        return vplanNotification;
    }

    void getNotificationFromVPlanAsync(@Nullable final VPlan vplan, @NonNull final FranzCallback callback) {
        final Handler handler = new Handler(Looper.getMainLooper());
        new Thread(new Runnable() {
            @Override
            public void run() {
                final ReturnValue rv = getNotificationFromVPlan(vplan);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onCallback(rv.status, rv.objects);
                    }
                });
            }
        }).start();
    }

    private ReturnValue getNotificationFromVPlan(@Nullable final VPlan vplan) {
        if (vplan != null) {
            if (settings.isTeacher()) {
                String teacherShortcut = settings.getTeacherShortcut();

                if (teacherShortcut != null && !teacherShortcut.isEmpty()) {
                    VPlanNotification vplanNotification = null;

                    for (VPlan.VPlanDayData vplanDay : vplan) {
                        VPlanNotification.VPlanNotificationDay vplanNotificationDay = null;

                        if (vplanDay.getTableData() != null) {
                            for (VPlan.VPlanDayData.VPlanTableData tableRow : vplanDay.getTableData()) {
                                if (TeacherList.TeacherData.teacherShortcutInString(tableRow.getTeacher() + " " + tableRow.getInfo(), teacherShortcut) != null) {
                                    if (vplanNotification == null)
                                        vplanNotification = new VPlanNotification();
                                    if (vplanNotificationDay == null) {
                                        vplanNotificationDay = new VPlanNotification.VPlanNotificationDay();
                                        vplanNotificationDay.setTitle(vplanDay.getTitle());
                                        vplanNotification.add(vplanNotificationDay);
                                    }

                                    vplanNotificationDay.addNotification(tableRow.getNotificationText(context));
                                }
                            }
                        }

                        if (vplanDay.getChangesSupervision() != null && TeacherList.TeacherData.teacherShortcutInString(vplanDay.getChangesSupervision(), teacherShortcut) != null) {
                            for (String line : vplanDay.getChangesSupervision().split("\n")) {
                                if (TeacherList.TeacherData.teacherShortcutInString(line, teacherShortcut) != null) {
                                    if (vplanNotification == null)
                                        vplanNotification = new VPlanNotification();
                                    if (vplanNotificationDay == null) {
                                        vplanNotificationDay = new VPlanNotification.VPlanNotificationDay();
                                        vplanNotificationDay.setTitle(vplanDay.getTitle());
                                        vplanNotification.add(vplanNotificationDay);
                                    }

                                    vplanNotificationDay.addNotification(line);
                                }
                            }
                        }
                    }

                    return new ReturnValue(Status.OK, vplanNotification);
                } else {
                    return new ReturnValue(Status.UNKNOWN_ERROR);
                }
            } else {
                final int schoolClassStep = settings.getSchoolClassStep();

                if (schoolClassStep > 10) {
                    ReturnValue rv = TimetableManager.getInstance().getTimetable();

                    if (rv.status == Status.OK && rv.objects != null && rv.objects.length > 0) {
                        Timetable timetable = (Timetable) rv.objects[0];
                        VPlanNotification vplanNotification = null;

                        for (VPlan.VPlanDayData vplanDay : vplan) {
                            VPlanNotification.VPlanNotificationDay vplanNotificationDay = null;

                            if (vplanDay.getTableData() != null)
                                for (VPlan.VPlanDayData.VPlanTableData tableRow : vplanDay.getTableData()) {
                                    if (tableRow.getSchoolClass() != null &&
                                            tableRow.getSchoolClass().contains(String.valueOf(schoolClassStep)) &&
                                            timetable.hasCourse(tableRow.getSchoolClass())) {
                                        if (vplanNotification == null)
                                            vplanNotification = new VPlanNotification();
                                        if (vplanNotificationDay == null) {
                                            vplanNotificationDay = new VPlanNotification.VPlanNotificationDay();
                                            vplanNotificationDay.setTitle(vplanDay.getTitle());
                                            vplanNotification.add(vplanNotificationDay);
                                        }

                                        vplanNotificationDay.addNotification(tableRow.getNotificationText(context));
                                    }
                                }
                        }

                        return new ReturnValue(Status.OK, vplanNotification);
                    } else
                        return new ReturnValue(Status.UNKNOWN_ERROR);
                } else {
                    int schoolClass = settings.getSchoolClass();
                    VPlanNotification vplanNotification = null;

                    for (VPlan.VPlanDayData vplanDay : vplan) {
                        VPlanNotification.VPlanNotificationDay vplanNotificationDay = null;

                        if (vplanDay.getTableData() != null)
                            for (VPlan.VPlanDayData.VPlanTableData tableRow : vplanDay.getTableData()) {
                                if (tableRow.getSchoolClass() != null &&
                                        tableRow.getSchoolClass().contains(schoolClassStep + "/" + schoolClass)) {
                                    if (vplanNotification == null)
                                        vplanNotification = new VPlanNotification();
                                    if (vplanNotificationDay == null) {
                                        vplanNotificationDay = new VPlanNotification.VPlanNotificationDay();
                                        vplanNotificationDay.setTitle(vplanDay.getTitle());
                                        vplanNotification.add(vplanNotificationDay);
                                    }

                                    vplanNotificationDay.addNotification(tableRow.getNotificationText(context));
                                }
                            }
                    }

                    return new ReturnValue(Status.OK, vplanNotification);
                }
            }
        } else
            return new ReturnValue(Status.UNKNOWN_ERROR);
    }

    void getFilteredVPlanAsync(@Nullable final VPlan vplan, @NonNull final FranzCallback callback) {
        final Handler handler = new Handler(Looper.getMainLooper());
        new Thread(new Runnable() {
            @Override
            public void run() {
                final ReturnValue rv = getFilteredVPlan(vplan);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onCallback(rv.status, rv.objects);
                    }
                });
            }
        }).start();
    }

    ReturnValue getFilteredVPlan(@Nullable final VPlan vplan) {
        if (vplan != null) {
            final VPlan filteredVPlan = vplan.cloneContent();

            if (settings.isTeacher()) {
                String teacherShortcut = settings.getTeacherShortcut();
                for (int i = filteredVPlan.size() - 1; i >= 0; i--) {
                    VPlan.VPlanDayData vplanDay = filteredVPlan.get(i);

                    if (vplanDay.getChangesSupervision() != null && TeacherList.TeacherData.teacherShortcutInString(vplanDay.getChangesSupervision(), teacherShortcut) != null) {
                        StringBuilder changesSupervision = new StringBuilder();

                        for (String line : vplanDay.getChangesSupervision().split("\n")) {
                            if (TeacherList.TeacherData.teacherShortcutInString(line, teacherShortcut) != null) {
                                changesSupervision.append(line);
                                changesSupervision.append("\n");
                            }
                        }

                        vplanDay.setChangesSupervision(changesSupervision.toString().trim());
                    } else {
                        vplanDay.setChangesSupervision(null);
                    }

                    if (vplanDay.getTableData() != null && !vplanDay.getTableData().isEmpty() && teacherShortcut != null && !teacherShortcut.isEmpty()) {
                        for (int j = vplanDay.getTableData().size() - 1; j >= 0; j--) {
                            VPlan.VPlanDayData.VPlanTableData vplanTableRow =
                                    vplanDay.getTableData().get(j);

                            if (TeacherList.TeacherData.teacherShortcutInString(
                                    vplanTableRow.getTeacher() + " " +
                                            vplanTableRow.getInfo(),
                                    teacherShortcut) == null)
                                vplanDay.removeTableData(j);
                        }
                    } else
                        vplanDay.setTableData(null);

                    if (vplanDay.getExamData() != null && !vplanDay.getExamData().isEmpty() && teacherShortcut != null && !teacherShortcut.isEmpty()) {
                        for (int j = vplanDay.getExamData().size() - 1; j >= 0; j--) {
                            VPlan.VPlanDayData.VPlanExamData vplanExamData =
                                    vplanDay.getExamData().get(j);

                            if (TeacherList.TeacherData.teacherShortcutInString(
                                    vplanExamData.getTeacher() + " " + vplanExamData.getInfo(),
                                    teacherShortcut) == null)
                                vplanDay.removeExamData(j);
                        }
                    } else
                        vplanDay.setExamData(null);

                    if (vplanDay.getTableData() == null && vplanDay.getExamData() == null)
                        filteredVPlan.remove(i);
                }
            } else {
                final int schoolClassStep = settings.getSchoolClassStep();

                if (schoolClassStep >= 11) {
                    ReturnValue rv = TimetableManager.getInstance().getTimetable();

                    if (Status.OK == rv.status && rv.objects != null && rv.objects.length > 0) {
                        Timetable timetable = (Timetable) rv.objects[0];

                        for (int i = filteredVPlan.size() - 1; i >= 0; i--) {
                            VPlan.VPlanDayData vplanDay = filteredVPlan.get(i);

                            if (vplanDay.getTableData() != null) {
                                for (int j = vplanDay.getTableData().size() - 1; j >= 0; j--) {
                                    VPlan.VPlanDayData.VPlanTableData
                                            notificationTableRow =
                                            vplanDay.getTableData().get(j);

                                    if (notificationTableRow.getSchoolClass() == null
                                            || !notificationTableRow.getSchoolClass().
                                            contains(String.valueOf(schoolClassStep)) ||
                                            !timetable.hasCourse(
                                                    notificationTableRow.
                                                            getSchoolClass()))
                                        vplanDay.removeTableData(j);
                                }
                            }

                            if (vplanDay.getExamData() != null) {
                                for (int j = vplanDay.getExamData().size() - 1; j >= 0; j--) {
                                    VPlan.VPlanDayData.VPlanExamData vplanExamData =
                                            vplanDay.getExamData().get(j);

                                    if (vplanExamData.getSchoolClass() == null ||
                                            !vplanExamData.getSchoolClass().contains(
                                                    String.valueOf(schoolClassStep)) ||
                                            vplanExamData.getCourse() == null ||
                                            !timetable.hasCourse(vplanExamData.getCourse()))
                                        vplanDay.removeExamData(j);
                                }
                            }

                            if (vplanDay.getTableData() == null && vplanDay.getExamData() == null)
                                filteredVPlan.remove(i);
                        }
                    }
                } else {
                    int schoolClass = settings.getSchoolClass();

                    for (int i = filteredVPlan.size() - 1; i >= 0; i--) {
                        VPlan.VPlanDayData vplanDay = filteredVPlan.get(i);

                        if (vplanDay.getTableData() != null) {
                            for (int j = vplanDay.getTableData().size() - 1; j >= 0; j--) {
                                VPlan.VPlanDayData.VPlanTableData notificationTableRow = vplanDay.getTableData().get(j);
                                if (notificationTableRow.getSchoolClass() == null ||
                                        !notificationTableRow.getSchoolClass().
                                                contains(schoolClassStep + "/" + schoolClass))
                                    vplanDay.removeTableData(j);
                            }
                        }

                        vplanDay.setExamData(null);

                        if (vplanDay.getTableData() == null)
                            filteredVPlan.remove(i);
                    }
                }
            }

            return new ReturnValue(Status.OK, filteredVPlan);
        }

        return new ReturnValue(Status.UNKNOWN_ERROR);
    }

    public void makeNotificationAsync(@Nullable final VPlan vplan) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                makeNotification(vplan);
            }
        }).start();
    }

    public void makeNotification(@Nullable VPlan vplan) {
        if (!settings.isVPlanNotificationEnabled())
            removeNotification(Constants.NOTIFICATION_ID_VPLAN);

        boolean isTeacher = settings.isTeacher();
        String teacherShortcut = settings.getTeacherShortcut();

        if (isTeacher && (teacherShortcut == null || teacherShortcut.isEmpty())) {
            removeNotification(Constants.NOTIFICATION_ID_VPLAN);
            saveNotifications(null);
            return;
        }

        ReturnValue rv = getNotificationFromVPlan(vplan);

        if (Status.OK == rv.status && rv.objects != null && rv.objects.length > 0) {
            VPlanNotification downloadedVPlanNotification = (VPlanNotification) rv.objects[0];
            final VPlanNotification shortDownloadedVPlanNotification = removeOldDays(downloadedVPlanNotification);

            if (shortDownloadedVPlanNotification != null && !shortDownloadedVPlanNotification.isEmpty()) {
                VPlanNotification shortSavedVPlanNotification = removeOldDays(getSavedNotifications());

                if (!shortDownloadedVPlanNotification.notificationEquals(shortSavedVPlanNotification))
                    makeNotification(shortDownloadedVPlanNotification, true);
                else if (!settings.isVPlanNotificationDeleted())
                    makeNotification(shortDownloadedVPlanNotification, false);

                saveNotifications(shortDownloadedVPlanNotification);
            } else {
                removeNotification(Constants.NOTIFICATION_ID_VPLAN);
                saveNotifications(null);
            }
        } else {
            removeNotification(Constants.NOTIFICATION_ID_VPLAN);
            saveNotifications(null);
        }
    }

    public void makeNotificationAsync(final Mode mode) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                makeNotification(mode);
            }
        }).start();
    }

    public void makeNotification(final Mode mode) {
        if (!settings.isVPlanNotificationEnabled())
            removeNotification(Constants.NOTIFICATION_ID_VPLAN);

        boolean isTeacher = settings.isTeacher();
        String teacherShortcut = settings.getTeacherShortcut();

        if (isTeacher && (teacherShortcut == null || teacherShortcut.isEmpty())) {
            removeNotification(Constants.NOTIFICATION_ID_VPLAN);
            saveNotificationsAsync(null);
            return;
        }

        if (mode == Mode.SHOW_ONLY) {
            if (!settings.isVPlanNotificationDeleted()) {
                VPlanNotification vplanNotification = removeOldDays(getSavedNotifications());
                makeNotification(vplanNotification, false);
            }
        } else {
            VPlanManager.Mode vplanMode = mode == Mode.DOWNLOAD ? VPlanManager.Mode.DOWNLOAD : VPlanManager.Mode.CACHE;

            ReturnValue rv = VPlanManager.getInstance().getVPlan(vplanMode);
            if (rv.objects.length > 1 && rv.objects[1] != null)
                makeNotification((VPlan) rv.objects[1]);
        }
    }

    private void removeNotification(int id) {
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(id);
    }

    private void makeNotification(@Nullable VPlanNotification vplanNotification, boolean alert) {
        if (!settings.isVPlanNotificationEnabled() || vplanNotification == null || vplanNotification.isEmpty())
            removeNotification(Constants.NOTIFICATION_ID_VPLAN);
        else {
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                String name = context.getString(R.string.vplan);
                NotificationChannel vplanChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID_VPLAN,
                        name, NotificationManager.IMPORTANCE_HIGH);
                nm.createNotificationChannel(vplanChannel);
            }

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID_VPLAN);
            builder.setSmallIcon(R.drawable.ic_notification_vplan);
            builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.ic_launcher));
            builder.setColor(ContextCompat.getColor(context, R.color.ColorAccent));

            Intent deleteIntent = new Intent(context, BroadcastReceiver.class);
            deleteIntent.setAction(ACTION_NOTIFICATION_DELETED);
            PendingIntent deletePendingIntent = PendingIntent.getBroadcast(context,
                    Constants.PENDING_INTENT_NOTIFICATION_VPLAN_DELETE,
                    deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setDeleteIntent(deletePendingIntent);

            if (vplanNotification.size() == 1) {
                VPlanNotification.VPlanNotificationDay vplanDay = vplanNotification.get(0);

                if (vplanDay.getNotifications() != null && !vplanDay.getNotifications().isEmpty()) {
                    builder.setContentTitle(vplanDay.getTitle());

                    if (vplanDay.getNotifications().size() == 1) {
                        builder.setContentText(vplanDay.getNotifications().get(0));
                    } else if (vplanDay.getNotifications().size() > 1) {
                        NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle(builder);

                        for (String notification : vplanDay.getNotifications()) {
                            style.addLine(notification);
                        }

                        style.setBigContentTitle(vplanDay.getTitle());

                        String summaryText = String.format(context.getString(R.string.changes), vplanDay.getNotifications().size());

                        style.setSummaryText(summaryText);
                        builder.setContentText(summaryText);
                    }

                    Intent contentIntent = new Intent(context, VPlanDayActivity.class);
                    contentIntent.putExtra(VPlanDayActivity.EXTRA_DAY_TITLE, vplanDay.getTitle());

                    PendingIntent contentPendingIntent = TaskStackBuilder
                            .create(context)
                            .addNextIntentWithParentStack(contentIntent)
                            .getPendingIntent(
                                    Constants.PENDING_INTENT_NOTIFICATION_VPLAN_CONTENT,
                                    PendingIntent.FLAG_UPDATE_CURRENT);

                    builder.setContentIntent(contentPendingIntent);
                }
            } else if (vplanNotification.size() > 1) {
                builder.setContentTitle(context.getString(R.string.app_name));

                NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle(builder);
                style.setBigContentTitle(context.getString(R.string.app_name));

                int numberOfChanges = 0;

                for (VPlanNotification.VPlanNotificationDay vplanDay : vplanNotification) {
                    if (vplanDay.getNotifications() != null && !vplanDay.getNotifications().isEmpty()) {
                        String dayName;

                        if (vplanDay.getCalendarDate() != null) {
                            SimpleDateFormat sdf = new SimpleDateFormat("EEE",
                                    Locale.getDefault());
                            dayName = sdf.format(vplanDay.getCalendarDate().getTime());
                            if (dayName.endsWith("."))
                                dayName = dayName.substring(0, dayName.length() - 1);
                        } else
                            dayName = "...:";

                        for (String notification : vplanDay.getNotifications()) {
                            notification = dayName + ": " + notification;
                            style.addLine(notification);
                        }

                        numberOfChanges += vplanDay.getNotifications().size();
                    }
                }

                String summaryText = String.format(context.getString(R.string.changes_at_days), numberOfChanges,
                        vplanNotification.size());

                style.setSummaryText(summaryText);
                builder.setContentText(summaryText);

                Intent contentIntent = new Intent(context, MainActivity.class);
                contentIntent.putExtra(MainActivity.EXTRA_DRAWER_ITEM_ID,
                        R.id.drawer_vplan);
                PendingIntent contentPendingIntent = PendingIntent.getActivity(context,
                        Constants.PENDING_INTENT_NOTIFICATION_VPLAN_CONTENT,
                        contentIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                builder.setContentIntent(contentPendingIntent);
            }

            if (alert) {
                int light = settings.getNotificationLight();
                builder.setLights(light, 500, 1000);

                int vibration = settings.getVibrationLength();
                builder.setVibrate(new long[]{0, vibration});
            }

            nm.notify(Constants.NOTIFICATION_ID_VPLAN, builder.build());

            // notification is new (not deleted)
            settings.setVPlanNotificationDeleted(false);
        }
    }
}
