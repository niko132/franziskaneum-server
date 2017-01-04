package de.franziskaneum.settings;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.ProgressListener;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AppKeyPair;

import android.Manifest;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
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

import de.franziskaneum.Network;
import de.franziskaneum.R;

public class AppUpdateActivity extends AppCompatActivity {
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 0;

    private static final String APP_KEY = "wz8193x713voom0";
    private static final String APP_SECRET = "0whfktq16rm73xw";
    private static final String OAUTH2_ACCESS_TOKEN =
            "_Rjs2Zx1EoAAAAAAAAAALEghRdatD7IeSqHFAteiBCuZnK0qgAAipEkt1z_LeKkl";

    public static final String KEY_CHECKED_FOR_UPDATE = "checked_for_update";

    private static String fileDirectory;

    private DropboxAPI<AndroidAuthSession> DBApi;

    private TextView text;
    private ProgressBar progress;
    private TextView progressText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_update);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        AndroidAuthSession session = new AndroidAuthSession(new AppKeyPair(
                APP_KEY, APP_SECRET));
        session.setOAuth2AccessToken(OAUTH2_ACCESS_TOKEN);

        DBApi = new DropboxAPI<>(session);

        text = (TextView) findViewById(R.id.app_update_text);
        progress = (ProgressBar) findViewById(R.id.app_update_progress);
        progressText = (TextView) findViewById(R.id.app_update_progress_text);

        if (getIntent().getBooleanExtra(KEY_CHECKED_FOR_UPDATE, false)) {
            requestPermissions();
        } else {
            new CheckForUpdate().execute();
        }
    }

    private void requestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE },
                    REQUEST_WRITE_EXTERNAL_STORAGE);
        } else {
            new DownloadAppUpdate().execute();
        }
    }

    private class CheckForUpdate extends AsyncTask<Void, Void, Integer> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            text.setText(getString(R.string.loading) + "...");
        }

        @Override
        protected Integer doInBackground(Void... params) {
            if (!Network.isConnected(AppUpdateActivity.this))
                return de.franziskaneum.Status.NO_CONNECTION;

            try {
                InputStream is = DBApi.getFileStream("/app/version", null);

                BufferedReader br = new BufferedReader(new InputStreamReader(is));

                String line;
                String versionString = "";

                while ((line = br.readLine()) != null) {
                    versionString += line;
                }

                is.close();

                int versionCode = Integer.parseInt(versionString);

                if (versionCode > AppUpdateActivity.this.getPackageManager()
                        .getPackageInfo(getPackageName(), 0).versionCode)
                    return versionCode;
                else
                    return -1;
            } catch (DropboxException | IOException | NameNotFoundException e) {
                e.printStackTrace();
            }

            return -1;
        }

        @Override
        protected void onPostExecute(final Integer result) {
            super.onPostExecute(result);

            if (result > 0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        AppUpdateActivity.this, R.style.AlertDialogTheme);
                builder.setMessage(R.string.a_new_app_update_is_available);
                builder.setPositiveButton(R.string.download,
                        new OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                requestPermissions();
                            }

                        });
                builder.setNegativeButton(android.R.string.cancel,
                        new OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                SharedPreferences prefs = PreferenceManager
                                        .getDefaultSharedPreferences(AppUpdateActivity.this);
                                Editor editor = prefs.edit();
                                editor.putInt(AppUpdateService.KEY_IGNORE_UPDATE, result);
                                editor.apply();
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

    private class DownloadAppUpdate extends AsyncTask<Void, Integer, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            text.setText(R.string.download);
            progress.setIndeterminate(false);
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            try {
                if (!Environment.getExternalStorageState().equals(
                        Environment.MEDIA_MOUNTED)) {
                    Toast.makeText(AppUpdateActivity.this,
                            R.string.sd_card_required, Toast.LENGTH_LONG).show();
                    return false;
                }

                File file = new File(Environment.getExternalStorageDirectory()
                        .getAbsolutePath() + "/Franziskaneum/app");

                file.mkdirs();

                file = new File(file, "/Franziskaneum.apk");

                fileDirectory = file.getAbsolutePath();

                FileOutputStream fos = new FileOutputStream(file);

                publishProgress(0);
                DBApi.getFile("/app/Franziskaneum.apk", null, fos,
                        new ProgressListener() {

                            @Override
                            public void onProgress(long bytes, long total) {
                                publishProgress((int) (100 * bytes / total));
                            }

                        });
                publishProgress(100);

                fos.flush();
                fos.close();

                return true;
            } catch (DropboxException | IOException e) {
                e.printStackTrace();
            }

            return false;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progress.setProgress(values[0]);
            progressText.setText(String.valueOf(values[0]) + "%");
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (result) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse("file:///" + fileDirectory),
                        "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
            finish();
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
                new DownloadAppUpdate().execute();
            else
                finish();
        }
    }
}