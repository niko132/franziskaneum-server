package de.franziskaneum.timetable;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import de.franziskaneum.R;
import de.franziskaneum.settings.SettingsManager;

/**
 * Created by Niko on 17.02.2016.
 */
public class TimetableEditSubjectActivity extends AppCompatActivity {

    public static final String EXTRA_DAY_OF_WEEK = "de.franziskaneum.timetable.extra.DAY_OF_WEEK";
    public static final String EXTRA_HOUR = "de.franziskaneum.timetable.extra.HOUR";
    public static final String EXTRA_SUBJECT_TO_EDIT =
            "de.franziskaneum.timetable.extra.SUBJECT_TO_EDIT";

    private EditText subject;
    private EditText room;
    private EditText teacherOrSchoolClass;
    private CheckBox isDoubleHour;

    private String dayOfWeek = "";
    private int hour = 1;
    private Timetable.TimetableData timetableData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timetable_edit_subject);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        subject = (EditText) findViewById(R.id.timetable_edit_subject_subject);
        room = (EditText) findViewById(R.id.timetable_edit_subject_room);
        teacherOrSchoolClass = (EditText) findViewById(R.id.timetable_edit_subject_teacher_or_school_class);
        isDoubleHour = (CheckBox) findViewById(R.id.timetable_edit_subject_is_double_hour);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            dayOfWeek = extras.getString(EXTRA_DAY_OF_WEEK);
            if (dayOfWeek == null)
                dayOfWeek = "";
            hour = extras.getInt(EXTRA_HOUR, 1);
            timetableData = extras.getParcelable(EXTRA_SUBJECT_TO_EDIT);

            if (timetableData != null) {
                subject.setText(timetableData.getSubject());
                room.setText(timetableData.getRoom());
                teacherOrSchoolClass.setText(timetableData.getTeacherOrSchoolClass());
                isDoubleHour.setChecked(timetableData.isDoubleHour());
            }
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_cancel);
            actionBar.setTitle(dayOfWeek);
            actionBar.setSubtitle(hour + ". " + getString(R.string.hour));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        SettingsManager settings = SettingsManager.getInstance();

        findViewById(R.id.timetable_edit_subject_exact_spelling_note).setVisibility(
                (!settings.isTeacher() && settings.getSchoolClassStep() > 10) ? View.VISIBLE :
                        View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.timetable_edit_subject, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.timetable_save_action:
                if (timetableData == null)
                    timetableData = new Timetable.TimetableData();

                timetableData.setHour(hour);
                timetableData.setSubject(subject.getText().toString());
                timetableData.setRoom(room.getText().toString());
                timetableData.setTeacherOrSchoolClass(teacherOrSchoolClass.getText().toString());
                timetableData.setIsDoubleHour(isDoubleHour.isChecked());

                Intent intent = getIntent();
                intent.putExtra(EXTRA_SUBJECT_TO_EDIT, timetableData);
                setResult(Activity.RESULT_OK, intent);
                finish();
                break;
            case android.R.id.home:
                setResult(Activity.RESULT_CANCELED);
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
