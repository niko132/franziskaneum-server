package de.franziskaneum.teacher;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import de.franziskaneum.FranzCallback;
import de.franziskaneum.R;
import de.franziskaneum.Status;
import de.franziskaneum.Utils;

/**
 * Created by Niko on 23.02.2016.
 */
public class TeacherDetailActivity extends AppCompatActivity {

    public static final String EXTRA_TEACHER =
            "de.franziskaneum.teacher.TeacherDetailActivity.extra.TEACHER";
    public static final String EXTRA_TEACHER_SHORTCUT =
            "de.franziskaneum.teacher.TeacherDetailActivity.extra.TEACHER_SHORTCUT";
    public static final String EXTRA_TEACHER_NAME =
            "de.franziskaneum.teacher.TeacherDetailActivity.extra.TEACHER_NAME";

    private TextView name;
    private TextView shortcut;
    private TextView subjects;
    private TextView specificTasks;
    private TextView email;

    private TeacherManager teacherManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_detail);

        teacherManager = TeacherManager.getInstance();

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        name = (TextView) findViewById(R.id.teacher_name);
        shortcut = (TextView) findViewById(R.id.teacher_shortcut);
        subjects = (TextView) findViewById(R.id.teacher_subjects);
        specificTasks = (TextView) findViewById(R.id.teacher_specific_tasks);
        email = (TextView) findViewById(R.id.teacher_email);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            TeacherList.TeacherData teacher = extras.getParcelable(EXTRA_TEACHER);
            if (teacher != null)
                displayContent(teacher);
            else {
                final String teacherShortcut = extras.getString(EXTRA_TEACHER_SHORTCUT);
                if (teacherShortcut != null)
                    teacherManager.getTeacherListAsync(false, new FranzCallback() {
                        @Override
                        public void onCallback(int status, Object... objects) {
                            if (Status.OK == status && objects.length > 0 && objects[0] != null) {
                                TeacherList teacherList = (TeacherList) objects[0];

                                for (TeacherList.TeacherData teacher : teacherList) {
                                    if (teacher.getShortcut() != null &&
                                            teacher.getShortcut().equals(teacherShortcut)) {
                                        displayContent(teacher);
                                        break;
                                    }
                                }
                            } else
                                finish();
                        }
                    });
                else{
                    final String teacherName = extras.getString(EXTRA_TEACHER_NAME);
                    if (teacherName != null)
                        teacherManager.getTeacherListAsync(false, new FranzCallback() {
                            @Override
                            public void onCallback(int status, Object... objects) {
                                if (Status.OK == status && objects.length > 0 && objects[0] != null) {
                                    TeacherList teacherList = (TeacherList) objects[0];

                                    for (TeacherList.TeacherData teacher : teacherList) {
                                        if (teacher.getName() != null &&
                                                teacher.getName().equals(teacherName)) {
                                            displayContent(teacher);
                                            break;
                                        }
                                    }
                                } else
                                    finish();
                            }
                        });
                    else
                        finish();
                }
            }
        } else
            finish();
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

    private void displayContent(@NonNull final TeacherList.TeacherData teacher) {
        if (teacher.getName() != null && !teacher.getName().isEmpty()) {
            if (teacher.getForename() != null && !teacher.getForename().isEmpty())
                name.setText(teacher.getForename() + " " + teacher.getName());
            else
                name.setText(teacher.getName());

            if (getSupportActionBar() != null)
                getSupportActionBar().setTitle(teacher.getName());
        } else {
            name.setText("---");
            if (getSupportActionBar() != null)
                getSupportActionBar().setTitle(getString(R.string.teacher));
        }

        String teacherShortcut = teacher.getShortcut() == null ? "" : teacher.getShortcut();
        shortcut.setText(teacherShortcut.isEmpty() ? "---" : teacherShortcut);

        String teacherSubjects = null;
        if (teacher.getSubjects() != null && teacher.getSubjects().length != 0)
            for (String subject : teacher.getSubjects()) {
                if (subject == null || subject.isEmpty())
                    continue;

                if (teacherSubjects == null)
                    teacherSubjects = subject;
                else
                    teacherSubjects += ", " + subject;
            }
        subjects.setText(teacherSubjects == null ? "---" : teacherSubjects);

        String teacherSpecificTasks = null;
        if (teacher.getSpecificTasks() != null && teacher.getSpecificTasks().length != 0)
            for (String specificTask : teacher.getSpecificTasks()) {
                if (specificTask.isEmpty())
                    continue;

                if (teacherSpecificTasks == null)
                    teacherSpecificTasks = "• " + specificTask;
                else
                    teacherSpecificTasks += "\n" + "• " + specificTask;
            }
        specificTasks.setText(teacherSpecificTasks == null ? "---" : teacherSpecificTasks);

        teacherManager.getTeacherListAsync(false, new FranzCallback() {
            @Override
            public void onCallback(int status, Object... objects) {
                if (Status.OK == status && objects.length > 0 && objects[0] != null) {
                    TeacherList teacherList = (TeacherList) objects[0];

                    String teacherName = teacher.getName();
                    if (teacherName == null || teacherName.isEmpty()) {
                        email.setText("---");
                    } else {
                        String escapedName = Utils.escapeString(teacherName);
                        String lowerCaseEscaptedName = escapedName.toLowerCase();

                        String teacherForename = teacher.getForename();

                        if ((teacherForename != null && !teacherForename.isEmpty()) &&
                                teacherList.existsTeacherWithSameName(teacher)) {
                            String forenameWithoutWhitespace;
                            if (teacherForename.contains(" "))
                                forenameWithoutWhitespace = teacherForename.substring(0,
                                        teacherForename.indexOf(" "));
                            else
                                forenameWithoutWhitespace = teacherForename;

                            String escaptedForename = Utils.escapeString(forenameWithoutWhitespace);
                            String lowerCaseEscaptedForename = escaptedForename.toLowerCase();

                            email.setText(lowerCaseEscaptedForename + "." + lowerCaseEscaptedName +
                                    "@franziskaneum.de");
                        } else
                            email.setText(lowerCaseEscaptedName + "@franziskaneum.de");
                    }

                    email.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                            emailIntent.setData(Uri.parse("mailto:" + email.getText()));
                            if (emailIntent.resolveActivity(getPackageManager()) != null)
                                startActivity(emailIntent);
                        }
                    });
                }
            }
        });
    }
}
