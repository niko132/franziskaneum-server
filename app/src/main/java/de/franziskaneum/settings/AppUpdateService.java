package de.franziskaneum.settings;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import com.dropbox.core.DbxDownloader;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import de.franziskaneum.Constants;
import de.franziskaneum.R;
import de.franziskaneum.utils.ProgressOutputStream;

/**
 * Created by Niko on 19.01.2017.
 */

public class AppUpdateService extends Service {
    private static final String ACCESS_TOKEN = "_Rjs2Zx1EoAAAAAAAAAATEWpFaqBJHjarkVDb8S1sEijFQXolZXD-tVfJa1s_lKF";
    private static final String EXTRA_IGNORE_APP_UPDATE_VERSION =
            "de.franziskaneum.settings.AppUpdateService.extra.IGNORE_APP_UPDATE_VERSION";
    public static final String KEY_CHECKED_FOR_UPDATE = "checked_for_update";

    private IBinder binder = new AppUpdateBinder();
    private DbxClientV2 client;
    private SettingsManager settings;

    @Override
    public void onCreate() {
        super.onCreate();

        DbxRequestConfig config = new DbxRequestConfig("franziskaneum/app");
        client = new DbxClientV2(config, ACCESS_TOKEN);
        settings = SettingsManager.getInstance();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, final int startId) {
        if (intent.getExtras() != null && intent.getExtras().containsKey(EXTRA_IGNORE_APP_UPDATE_VERSION)) {
            int version = intent.getIntExtra(EXTRA_IGNORE_APP_UPDATE_VERSION, 0);
            settings.setIgnoredAppUpdateVersion(version);
            stopSelf(startId);
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int appUpdateVersion = getAppUpdateVersion();
                    if (isAppUpdateNew(appUpdateVersion) && appUpdateVersion >
                            settings.getIgnoredAppUpdateVersion()) {
                        makeNotification(appUpdateVersion);
                    }

                    stopSelf(startId);
                }
            }).start();
        }

        return Service.START_NOT_STICKY;
    }

    int getAppUpdateVersion() {
        int version = 0;
        InputStream is = null;
        try {
            is = client.files().download("/app/version").getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            String line;
            String versionString = "";

            while ((line = br.readLine()) != null) {
                versionString += line;
            }

            version = Integer.parseInt(versionString);
        } catch (DbxException | IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null)
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }

        return version;
    }

    boolean isAppUpdateNew(int appUpdateVersion) {
        try {
            return appUpdateVersion >
                    getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    String downloadAppUpdate(@Nullable ProgressOutputStream.Listener listener) {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return null;
        } else {
            File file = new File(Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + "/Franziskaneum/app");
            if (file.exists() || file.mkdirs()) {
                file = new File(file, "/Franziskaneum.apk");
                FileOutputStream fos = null;
                String filePath = null;
                try {
                    fos = new FileOutputStream(file);

                    DbxDownloader<FileMetadata> dl = client.files().download("/app/Franziskaneum.apk");
                    long size = dl.getResult().getSize();

                    dl.download(new ProgressOutputStream(size, fos, listener));

                    filePath = file.getAbsolutePath();
                } catch (DbxException | IOException e) {
                    e.printStackTrace();
                    filePath = null;
                } finally {
                    if (fos != null)
                        try {
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                }

                return filePath;
            }

        }

        return null;
    }

    private void makeNotification(int appUpdateVersion) {
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
        contentIntent.putExtra(KEY_CHECKED_FOR_UPDATE, true);
        PendingIntent contentPendingIntent = PendingIntent.getActivity(this,
                Constants.PENDING_INTENT_NOTIFICATION_APP_UPDATE_CONTENT,
                contentIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentPendingIntent);

        Intent deleteIntent = new Intent(this, AppUpdateService.class);
        deleteIntent.putExtra(EXTRA_IGNORE_APP_UPDATE_VERSION, appUpdateVersion);
        PendingIntent deletePendingIntent = PendingIntent.getService(this,
                Constants.PENDING_INTENT_NOTIFICATION_APP_UPDATE_DELETE,
                deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setDeleteIntent(deletePendingIntent);

        NotificationManager nm = (NotificationManager) this
                .getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(Constants.NOTIFICATION_ID_APP_UPDATE, builder.build());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class AppUpdateBinder extends Binder {
        AppUpdateService getService() {
            // Return this instance of LocalService so clients can call public methods
            return AppUpdateService.this;
        }
    }
}
