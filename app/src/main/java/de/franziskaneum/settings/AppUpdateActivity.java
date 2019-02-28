package de.franziskaneum.settings;

import android.Manifest;
import android.app.Service;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import de.franziskaneum.R;
import de.franziskaneum.utils.Network;
import de.franziskaneum.utils.ProgressOutputStream;

import static de.franziskaneum.settings.AppUpdateService.KEY_CHECKED_FOR_UPDATE;

/**
 * Created by Niko on 19.01.2017.
 */

public class AppUpdateActivity extends AppCompatActivity {
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 0;

    private TextView text;
    private ProgressBar progress;
    private TextView progressText;

    private boolean bound = false;
    private AppUpdateService appUpdateService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_update);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        text = (TextView) findViewById(R.id.app_update_text);
        progress = (ProgressBar) findViewById(R.id.app_update_progress);
        progressText = (TextView) findViewById(R.id.app_update_progress_text);

        Intent appUpdateServiceIntent = new Intent(this, AppUpdateService.class);
        bindService(appUpdateServiceIntent, serviceConnection, Service.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceConnection != null)
            unbindService(serviceConnection);
    }

    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            appUpdateService = ((AppUpdateService.AppUpdateBinder) iBinder).getService();
            bound = true;

            checkForUpdate();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            appUpdateService = null;
            bound = false;
        }
    };

    private void checkForUpdate() {
        if (getIntent().getBooleanExtra(KEY_CHECKED_FOR_UPDATE, false)) {
            requestPermissions();
        } else {
            new UpdateCheckerTask().execute();
        }
    }

    private void requestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE },
                    REQUEST_WRITE_EXTERNAL_STORAGE);
        } else {
            new DownloadAppUpdateTask().execute();
        }
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                new DownloadAppUpdateTask().execute();
            else
                finish();
        }
    }

    private class UpdateCheckerTask extends AsyncTask<Void, Void, Integer> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            text.setText(getString(R.string.loading) + "...");
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            if (Network.isConnected(AppUpdateActivity.this)) {
                if (bound && appUpdateService != null) {
                    int version = appUpdateService.getAppUpdateVersion();
                    if (appUpdateService.isAppUpdateNew(version))
                        return version;
                    else
                        return de.franziskaneum.Status.OK;
                } else
                    return de.franziskaneum.Status.UNKNOWN_ERROR;
            } else
                return de.franziskaneum.Status.NO_CONNECTION;
        }

        @Override
        protected void onPostExecute(final Integer result) {
            super.onPostExecute(result);

            if (result > 0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        AppUpdateActivity.this, R.style.AlertDialogTheme);
                builder.setMessage(R.string.a_new_app_update_is_available);
                builder.setPositiveButton(R.string.download,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                requestPermissions();
                            }

                        });
                builder.setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                SettingsManager.getInstance().setIgnoredAppUpdateVersion(result);
                                finish();
                            }

                        });
                builder.show();
            } else if (result == de.franziskaneum.Status.NO_CONNECTION) {
                Toast.makeText(AppUpdateActivity.this, R.string.no_connection,
                        Toast.LENGTH_LONG).show();
                finish();
            } else {
                Toast.makeText(AppUpdateActivity.this,
                        R.string.the_newest_version_is_already_installed, Toast.LENGTH_LONG)
                        .show();
                finish();
            }
        }
    }

    private class DownloadAppUpdateTask extends AsyncTask<Void, Integer, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            text.setText(R.string.download);
            progress.setIndeterminate(false);
            progress.setMax(100);
        }

        @Override
        protected String doInBackground(Void... voids) {
            if (Network.isConnected(AppUpdateActivity.this)) {
                if (bound && appUpdateService != null)
                    return appUpdateService.downloadAppUpdate(new ProgressOutputStream.Listener() {
                        @Override
                        public void progress(long completed, long totalSize) {
                            publishProgress((int) completed, (int) totalSize);
                        }
                    });
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            int p = (int) (values[0] * 100f / values[1]);
            progress.setProgress(p);
            progressText.setText(String.valueOf(p) + "%");
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result != null) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse("file:///" + result),
                        "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } else {
                Toast.makeText(AppUpdateActivity.this, R.string.sd_card_required, Toast.LENGTH_LONG).show();
            }
            finish();
        }
    }

}
