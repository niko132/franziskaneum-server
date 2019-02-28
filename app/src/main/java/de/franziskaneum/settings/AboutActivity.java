package de.franziskaneum.settings;

import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import de.franziskaneum.R;

public class AboutActivity extends AppCompatActivity {

    private static final String CHANGELOG_FILENAME = "changelog.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView versionTextView = (TextView) findViewById(R.id.about_version);
        try {
            versionTextView.setText(String.format(getString(R.string.version), getPackageManager()
                    .getPackageInfo(getPackageName(), 0).versionName));
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        /*
        Button changelogButton = (Button) findViewById(R.id.about_changelog);
        changelogButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(AboutActivity.this,
                        R.style.AlertDialogTheme);
                builder.setTitle(R.string.changelog);

                BufferedReader reader = null;
                try {
                    reader = new BufferedReader(new InputStreamReader(AboutActivity.this.
                            getAssets().open(CHANGELOG_FILENAME), "UTF-8"));

                    StringBuilder text = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        text.append(line);
                        text.append("\n");
                    }

                    builder.setMessage(text.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                builder.setPositiveButton(android.R.string.ok, null);
                builder.show();
            }

        });
        */

        Button licenses = (Button) findViewById(R.id.about_licenses);
        licenses.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent licensesIntent = new Intent(AboutActivity.this, LicensesActivity.class);
                startActivity(licensesIntent);
            }
        });
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