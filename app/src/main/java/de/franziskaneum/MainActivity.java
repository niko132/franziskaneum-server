package de.franziskaneum;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SwitchCompat;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.daimajia.slider.library.Indicators.PagerIndicator;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.DefaultSliderView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Calendar;

import de.franziskaneum.drawer.DrawerActivity;
import de.franziskaneum.news.NewsFragment;
import de.franziskaneum.settings.AppUpdateService;
import de.franziskaneum.settings.SettingsActivity;
import de.franziskaneum.settings.SettingsManager;
import de.franziskaneum.teacher.TeacherFragment;
import de.franziskaneum.teacher.TeacherManager;
import de.franziskaneum.timetable.TimetableFragment;
import de.franziskaneum.vplan.VPlanFragment;
import de.franziskaneum.vplan.VPlanNotificationManager;

/**
 * Created by Niko on 20.12.2015.
 */
public class MainActivity extends DrawerActivity {
    public static final String EXTRA_DRAWER_ITEM_ID =
            "de.franziskaneum.MainActivity.extra.DRAWER_ITEM_ID";
    private static final String KEY_SELECTED_DRAWER_ITEM_ID =
            "de.franziskianeum.MainActivity.key.SELECTED_DRAWER_ITEM_ID";

    private static final String FRAGMENT_TAG_NEWS = "fragment_tag_news";
    private static final String FRAGMENT_TAG_VPLAN = "fragment_tag_vplan";
    private static final String FRAGMENT_TAG_TIMETABLE = "fragment_tag_timetable";
    private static final String FRAGMENT_TAG_TEACHER = "fragment_tag_teacher_list";

    private NavigationView navigationView;
    private DrawerLayout drawerLayout;

    private int selectedDrawerItemID;
    private SettingsManager settings;

    private NewsFragment newsFragment;
    private VPlanFragment vplanFragment;
    private TimetableFragment timetableFragment;
    private TeacherFragment teacherFragment;

    private FirebaseAnalytics firebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        settings = SettingsManager.getInstance();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (!prefs.contains(SettingsManager.KEY_SCHOOL_CLASS_STEP) &&
                !prefs.contains(SettingsManager.KEY_SCHOOL_CLASS) &&
                !prefs.contains(SettingsManager.KEY_TEACHER_SHORTCUT))
            welcomeDialog();

        if (savedInstanceState != null) {
            selectedDrawerItemID = savedInstanceState.getInt(KEY_SELECTED_DRAWER_ITEM_ID);
        } else {
            if (getIntent().getDataString() != null) {
                String data = getIntent().getDataString().toLowerCase();

                if (!data.endsWith("/"))
                    data += "/";

                String newsUrl = getString(R.string.news_url).toLowerCase();
                String vplanUrl = getString(R.string.vplan_url).toLowerCase();
                String teacherUrl = getString(R.string.teacher_url).toLowerCase();

                if (data.equals(newsUrl))
                    selectedDrawerItemID = R.id.drawer_news;
                else if (data.equals(vplanUrl))
                    selectedDrawerItemID = R.id.drawer_vplan;
                else if (data.equals(teacherUrl))
                    selectedDrawerItemID = R.id.drawer_teacher;
            } else {
                switch (settings.getHomeCategory()) {
                    case "news":
                        selectedDrawerItemID = R.id.drawer_news;
                        break;
                    case "vplan":
                        selectedDrawerItemID = R.id.drawer_vplan;
                        break;
                    case "timetable":
                        selectedDrawerItemID = R.id.drawer_timetable;
                        break;
                    case "teacher":
                        selectedDrawerItemID = R.id.drawer_teacher;
                        break;
                    default:
                        selectedDrawerItemID = R.id.drawer_news;
                        break;
                }
            }
        }

        final FragmentManager fm = getSupportFragmentManager();

        navigationView = (NavigationView) findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.drawer_news:
                        if (newsFragment == null) {
                            newsFragment = (NewsFragment) fm.findFragmentByTag(FRAGMENT_TAG_NEWS);
                            if (newsFragment != null)
                                break;

                            newsFragment = new NewsFragment();
                        }

                        fm.beginTransaction().replace(R.id.container, newsFragment, FRAGMENT_TAG_NEWS).commit();

                        break;
                    case R.id.drawer_vplan:
                        if (vplanFragment == null) {
                            vplanFragment = (VPlanFragment) fm.findFragmentByTag(FRAGMENT_TAG_VPLAN);
                            if (vplanFragment != null)
                                break;

                            vplanFragment = new VPlanFragment();
                        }

                        fm.beginTransaction().replace(R.id.container, vplanFragment, FRAGMENT_TAG_VPLAN).commit();
                        break;
                    case R.id.drawer_timetable:
                        if (timetableFragment == null) {
                            timetableFragment = (TimetableFragment) fm.findFragmentByTag(FRAGMENT_TAG_TIMETABLE);
                            if (timetableFragment != null)
                                break;

                            timetableFragment = new TimetableFragment();
                        }

                        fm.beginTransaction().replace(R.id.container, timetableFragment, FRAGMENT_TAG_TIMETABLE).commit();
                        break;
                    case R.id.drawer_teacher:
                        if (teacherFragment == null) {
                            teacherFragment = (TeacherFragment) fm.findFragmentByTag(FRAGMENT_TAG_TEACHER);
                            if (teacherFragment != null)
                                break;

                            teacherFragment = new TeacherFragment();
                        }

                        fm.beginTransaction().replace(R.id.container, teacherFragment, FRAGMENT_TAG_TEACHER).commit();
                        break;
                    case R.id.drawer_feedback:
                        Intent feedbackIntent = new Intent(Intent.ACTION_SENDTO);
                        feedbackIntent.setData(Uri.parse("mailto:app@franziskaneum.de"));
                        feedbackIntent.putExtra(Intent.EXTRA_SUBJECT,
                                getString(R.string.feedback_franziskaneum_app));
                        if (feedbackIntent.resolveActivity(getPackageManager()) != null)
                            startActivity(feedbackIntent);
                        break;
                    case R.id.drawer_settings:
                        Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                        startActivity(settingsIntent);
                        break;
                }

                if (item.getGroupId() == R.id.drawer_items_group)
                    selectedDrawerItemID = item.getItemId();

                fm.executePendingTransactions();
                drawerLayout.closeDrawers();

                return item.getGroupId() == R.id.drawer_items_group;
            }
        });

        drawerLayout = (DrawerLayout) findViewById(R.id.navigationDrawer);
        setDrawerLayout(drawerLayout);

        selectedDrawerItemID = getIntent().getIntExtra(EXTRA_DRAWER_ITEM_ID, selectedDrawerItemID);

        navigationView.setCheckedItem(selectedDrawerItemID);
        navigationView.getMenu().performIdentifierAction(selectedDrawerItemID, 0);

        setupDrawerSlider((SliderLayout) navigationView.inflateHeaderView(R.layout.drawer_header));
        setupAppUpdate();
        refreshTeacherList();
        updateSchoolClassIfNeeded();

        VPlanNotificationManager.getInstance().makeNotificationAsync(VPlanNotificationManager.Mode.SHOW_ONLY);

        firebaseAnalytics = FirebaseAnalytics.getInstance(this);
        FirebaseMessaging.getInstance().subscribeToTopic("vplan");

        Log.d("main", "Token: " + FirebaseInstanceId.getInstance().getToken());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        selectedDrawerItemID = intent.getIntExtra(EXTRA_DRAWER_ITEM_ID, selectedDrawerItemID);

        navigationView.setCheckedItem(selectedDrawerItemID);
        navigationView.getMenu().performIdentifierAction(selectedDrawerItemID, 0);
    }

    private void welcomeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogTheme);
        builder.setTitle(R.string.welcome);
        builder.setCancelable(false);

        View root = LayoutInflater.from(this).inflate(R.layout.dialog_welcome, null);

        final TextView message = (TextView) root.findViewById(android.R.id.message);

        final View schoolClassContainer = root
                .findViewById(R.id.dialog_welcome_school_class_container);
        final Spinner schoolClassStep = (Spinner) root
                .findViewById(R.id.dialog_welcome_school_class_step);
        final Spinner schoolClass = (Spinner) root.findViewById(R.id.dialog_welcome_school_class);

        ArrayAdapter<CharSequence> schoolClassStepAdapter = ArrayAdapter
                .createFromResource(this, R.array.school_class_step,
                        android.R.layout.simple_spinner_item);
        schoolClassStepAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        schoolClassStep.setAdapter(schoolClassStepAdapter);

        ArrayAdapter<CharSequence> schoolClassAdapter = ArrayAdapter
                .createFromResource(this, R.array.school_class,
                        android.R.layout.simple_spinner_item);
        schoolClassAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        schoolClass.setAdapter(schoolClassAdapter);

        final View teacherShortcutContainer = root
                .findViewById(R.id.dialog_welcome_teacher_shortcut_container);
        final EditText teacherShortcut = (EditText) root
                .findViewById(R.id.dialog_welcome_teacher_shortcut);

        final SwitchCompat teacherMode = (SwitchCompat) root
                .findViewById(R.id.dialog_welcome_teacher_mode);
        teacherMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                schoolClassContainer.setVisibility(isChecked ? View.GONE : View.VISIBLE);
                teacherShortcutContainer.setVisibility(isChecked ? View.VISIBLE : View.GONE);

                message.setText(isChecked ? R.string.enter_shortcut
                        : R.string.enter_school_class);
            }

        });

        builder.setView(root);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                settings.setIsTeacher(teacherMode.isChecked());
                settings.setSchoolClassStep(schoolClassStep.getSelectedItemPosition() + 5);
                settings.setSchoolClass(schoolClass.getSelectedItemPosition() + 1);
                settings.setTeacherShortcut(teacherShortcut.getText().toString());
            }

        });

        schoolClassStep.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                schoolClass.setEnabled(position < 6);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }

        });

        builder.show();
    }

    private void setupDrawerSlider(SliderLayout sliderLayout) {
        DefaultSliderView sliderView;

        sliderView = new DefaultSliderView(this);
        sliderView.image(R.drawable.banner1);
        sliderView.setScaleType(BaseSliderView.ScaleType.CenterCrop);
        sliderLayout.addSlider(sliderView);

        sliderView = new DefaultSliderView(this);
        sliderView.image(R.drawable.banner3);
        sliderView.setScaleType(BaseSliderView.ScaleType.CenterCrop);
        sliderLayout.addSlider(sliderView);

        sliderView = new DefaultSliderView(this);
        sliderView.image(R.drawable.banner4);
        sliderView.setScaleType(BaseSliderView.ScaleType.CenterCrop);
        sliderLayout.addSlider(sliderView);

        sliderView = new DefaultSliderView(this);
        sliderView.image(R.drawable.banner2);
        sliderView.setScaleType(BaseSliderView.ScaleType.CenterCrop);
        sliderLayout.addSlider(sliderView);

        sliderView = new DefaultSliderView(this);
        sliderView.image(R.drawable.banner5);
        sliderView.setScaleType(BaseSliderView.ScaleType.CenterCrop);
        sliderLayout.addSlider(sliderView);

        sliderLayout.setIndicatorVisibility(PagerIndicator.IndicatorVisibility.Invisible);
    }

    private void setupAppUpdate() {
        AlarmManager am = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);

        Intent appUpdateIntent = new Intent(this, AppUpdateService.class);
        PendingIntent appUpdatePendingIntent = PendingIntent.getService(this,
                Constants.PENDING_INTENT_ALARM_APP_UPDATE_SERVICE, appUpdateIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 19);
        calendar.set(Calendar.MINUTE, 0);

        am.cancel(appUpdatePendingIntent);
        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, appUpdatePendingIntent);

        startService(appUpdateIntent);
    }

    private void refreshTeacherList() {
        long teacherListLastRefresh = settings.getTeacherListLastRefresh();
        Calendar teacherListLastRefreshDate = Calendar.getInstance();
        teacherListLastRefreshDate.setTimeInMillis(teacherListLastRefresh);

        Calendar maxDate = Calendar.getInstance();
        maxDate.setTimeInMillis(maxDate.getTimeInMillis() - 7 * 24 * 60 * 60 * 1000);

        if (teacherListLastRefreshDate.before(maxDate))
            TeacherManager.getInstance().getTeacherListAsync(true, new FranzCallback() {
                @Override
                public void onCallback(int status, Object... objects) {
                    if (Status.OK == status && objects.length > 0 && objects[0] != null)
                        settings.setTeacherListLastRefresh(Calendar.getInstance().getTimeInMillis());
                }
            });
    }

    private void updateSchoolClassIfNeeded() {
        if (!settings.isTeacher()) {
            Calendar schoolClassLastModifiedExpiringDate = Calendar.getInstance();
            schoolClassLastModifiedExpiringDate.setTimeInMillis(settings.getSchoolClassLastModified());
            schoolClassLastModifiedExpiringDate.add(Calendar.WEEK_OF_YEAR, 6); // add 6 (holiday length)

            Calendar schoolClassUpdateDate = Calendar.getInstance();
            schoolClassUpdateDate.setTimeInMillis(schoolClassLastModifiedExpiringDate.getTimeInMillis());
            schoolClassUpdateDate.set(Calendar.MILLISECOND, 0);
            schoolClassUpdateDate.set(Calendar.SECOND, 0);
            schoolClassUpdateDate.set(Calendar.MINUTE, 0);
            schoolClassUpdateDate.set(Calendar.HOUR_OF_DAY, 0);
            schoolClassUpdateDate.set(Calendar.DAY_OF_MONTH, 31);
            schoolClassUpdateDate.set(Calendar.MONTH, Calendar.JULY);

            if (schoolClassUpdateDate.before(schoolClassLastModifiedExpiringDate))
                schoolClassUpdateDate.add(Calendar.YEAR, 1);

            Calendar now = Calendar.getInstance();

            int diff = now.get(Calendar.YEAR) - schoolClassUpdateDate.get(Calendar.YEAR);

            schoolClassUpdateDate.set(Calendar.YEAR, now.get(Calendar.YEAR));

            if (now.after(schoolClassUpdateDate))
                diff++;

            int newSchoolClassStep = settings.getSchoolClassStep() + diff;
            if (newSchoolClassStep > 12)
                newSchoolClassStep = 12;

            if (diff > 0) {
                settings.setSchoolClassStep(newSchoolClassStep);
                settings.setSchoolClassLastModified(now.getTimeInMillis());

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setTitle(R.string.changed_school_class).
                        setPositiveButton(android.R.string.ok, null).
                        setNegativeButton(R.string.change, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                                startActivity(settingsIntent);
                            }
                        });

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    alertDialogBuilder.setMessage(Html.fromHtml(getString(
                            R.string.your_school_class_was_automatically_changed_to,
                            newSchoolClassStep, settings.getSchoolClass()),
                            Html.FROM_HTML_MODE_LEGACY));
                } else
                    alertDialogBuilder.setMessage(Html.fromHtml(getString(
                            R.string.your_school_class_was_automatically_changed_to,
                            newSchoolClassStep, settings.getSchoolClass())));

                alertDialogBuilder.show();

                // update notifications
                VPlanNotificationManager.getInstance().makeNotificationAsync(VPlanNotificationManager.Mode.SHOW_ONLY);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(KEY_SELECTED_DRAWER_ITEM_ID, selectedDrawerItemID);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(navigationView))
            drawerLayout.closeDrawers();
        else
            super.onBackPressed();
    }
}