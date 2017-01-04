package de.franziskaneum;

import java.util.Calendar;

import de.franziskaneum.settings.AppUpdateService;
import de.franziskaneum.settings.SettingsManager;
import de.franziskaneum.vplan.VPlanNotificationManager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class BroadcastReceiver extends android.content.BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null) {
            String action = intent.getAction();

            if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
                AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

                VPlanNotificationManager.getInstance().makeNotificationAsync(VPlanNotificationManager.Mode.SHOW_ONLY);

                // set the alarm for appUpdate
                Intent updateIntent = new Intent(context,
                        AppUpdateService.class);
                PendingIntent pendingIntent = PendingIntent.getService(context,
                        Constants.PENDING_INTENT_ALARM_APP_UPDATE_SERVICE,
                        updateIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, 19);
                calendar.set(Calendar.MINUTE, 0);

                am.cancel(pendingIntent);
                am.setRepeating(AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY,
                        pendingIntent);
            } else if (action.equals(VPlanNotificationManager.ACTION_NOTIFICATION_DELETED)) {
                SettingsManager.getInstance().setVPlanNotificationDeleted(true);
            }
        }
    }
}