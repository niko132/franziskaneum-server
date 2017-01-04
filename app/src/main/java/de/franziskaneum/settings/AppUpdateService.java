package de.franziskaneum.settings;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AppKeyPair;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import de.franziskaneum.Constants;
import de.franziskaneum.Network;
import de.franziskaneum.R;

public class AppUpdateService extends IntentService {

    private static final String APP_KEY = "wz8193x713voom0";
    private static final String APP_SECRET = "0whfktq16rm73xw";
    private static final String OAUTH2_ACCESS_TOKEN =
            "_Rjs2Zx1EoAAAAAAAAAALEghRdatD7IeSqHFAteiBCuZnK0qgAAipEkt1z_LeKkl";
    public static final String KEY_IGNORE_UPDATE = "ignore_update";
    private static final String EXTRA_IGNORE_UPDATE =
            "de.franziskaneum.settings.AppUpdateService.extra.IGNORE_UPDATE";

    private SharedPreferences prefs;

    public AppUpdateService() {
        super(AppUpdateService.class.getName());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (!Network.isConnected(this))
            return;

        try {
            AndroidAuthSession session = new AndroidAuthSession(new AppKeyPair(
                    APP_KEY, APP_SECRET));
            session.setOAuth2AccessToken(OAUTH2_ACCESS_TOKEN);

            DropboxAPI<AndroidAuthSession> DBApi = new DropboxAPI<>(session);

            InputStream is = DBApi.getFileStream("/app/version", null);

            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            String line;
            String versionString = "";

            while ((line = br.readLine()) != null) {
                versionString += line;
            }

            is.close();

            int versionCode = Integer.parseInt(versionString);

            if (intent.getBooleanExtra(EXTRA_IGNORE_UPDATE, false)) {
                Editor editor = prefs.edit();
                editor.putInt(KEY_IGNORE_UPDATE, versionCode);
                editor.apply();
            } else {
                if (versionCode > this.getPackageManager().getPackageInfo(
                        getPackageName(), 0).versionCode
                        && versionCode > prefs.getInt(KEY_IGNORE_UPDATE, 0)) {
                    makeNotification();
                }
            }
        } catch (NameNotFoundException | IOException | DropboxException e) {
            e.printStackTrace();
        }
    }

    private void makeNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                this);
        builder.setContentTitle(this.getString(R.string.app_update));
        builder.setContentText(this.getString(R.string.new_app_update_available));
        builder.setSmallIcon(R.drawable.ic_notification_app_update);
        builder.setLargeIcon(BitmapFactory.decodeResource(this.getResources(),
                R.drawable.ic_launcher));
        builder.setAutoCancel(true);
        builder.setColor(ContextCompat.getColor(this, R.color.ColorAccent));
        builder.setLights(ContextCompat.getColor(this, R.color.ColorPrimary),
                500, 1000);

        long[] pattern = {0, 500};
        builder.setVibrate(pattern);

        Intent contentIntent = new Intent(this, AppUpdateActivity.class);
        contentIntent.putExtra(AppUpdateActivity.KEY_CHECKED_FOR_UPDATE, true);
        PendingIntent contentPendingIntent = PendingIntent.getActivity(this,
                Constants.PENDING_INTENT_NOTIFICATION_APP_UPDATE_CONTENT,
                contentIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentPendingIntent);

        Intent deleteIntent = new Intent(this, AppUpdateService.class);
        deleteIntent.putExtra(EXTRA_IGNORE_UPDATE, true);
        PendingIntent deletePendingIntent = PendingIntent.getService(this,
                Constants.PENDING_INTENT_NOTIFICATION_APP_UPDATE_DELETE,
                deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setDeleteIntent(deletePendingIntent);

        NotificationManager nm = (NotificationManager) this
                .getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(Constants.NOTIFICATION_ID_APP_UPDATE, builder.build());
    }
}