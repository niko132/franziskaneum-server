package de.franziskaneum.timetable;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import de.franziskaneum.Constants;
import de.franziskaneum.FranzCallback;
import de.franziskaneum.R;
import de.franziskaneum.Status;
import de.franziskaneum.drawer.DrawerFragment;
import de.franziskaneum.settings.SettingsManager;

/**
 * Created by Niko on 16.02.2016.
 */
public class TimetableFragment extends DrawerFragment implements
        View.OnClickListener, AdapterView.OnItemSelectedListener {
    private static final String KEY_SHOW_TIMES =
            "de.franziskaneum.timetable.TimetableFragment.key.SHOW_TIMES";

    private TimetableManager timetableManager;
    private Timetable timetable;
    private int week = 0;
    private boolean hasABWeek = false;
    private boolean showTimes = false;

    private ViewPager viewPager;
    private TimetablePagerAdapter pagerAdapter;
    private Spinner abWeekSpinner;
    private FloatingActionButton floatingActionButton;

    private ArrayAdapter<CharSequence> abWeekAdapter;

    private FranzCallback timetableCallback = new FranzCallback() {
        @Override
        public void onCallback(int status, Object... objects) {
            if (Status.OK == status && objects.length > 0 && objects[0] != null) { // this should be always true
                Timetable timetable = (Timetable) objects[0];

                TimetableFragment.this.timetable = timetable;

                if (pagerAdapter != null && timetable.size() > week)
                    pagerAdapter.setTimetableWeek(timetable.get(week));
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        timetableManager = TimetableManager.getInstance();

        if (timetable == null) {
            timetableManager.getTimetableAsync(timetableCallback);
        }

        if (savedInstanceState != null)
            showTimes = savedInstanceState.getBoolean(KEY_SHOW_TIMES, showTimes);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_timetable, container, false);

        viewPager = (ViewPager) root.findViewById(R.id.timetable_view_pager);

        // create new adapter because of fragmentManager (old activity reference)
        pagerAdapter = new TimetablePagerAdapter(getChildFragmentManager());
        if (timetable != null && timetable.size() > week)
            pagerAdapter.setTimetableWeek(timetable.get(week));
        pagerAdapter.setShowTimes(showTimes);

        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 2);

        TabLayout tabLayout = (TabLayout) root.findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);

        Toolbar toolbar = (Toolbar) root.findViewById(R.id.toolbar);
        setToolbar(toolbar);

        floatingActionButton = (FloatingActionButton) root.findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(this);

        abWeekSpinner = (Spinner) root.findViewById(R.id.timetable_a_b_week_spinner);

        if (abWeekAdapter == null) {
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (actionBar != null) {
                abWeekAdapter = ArrayAdapter.createFromResource(actionBar.getThemedContext(),
                        R.array.ab_week, android.R.layout.simple_spinner_item);
                abWeekAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
            }
        }
        abWeekSpinner.setAdapter(abWeekAdapter);
        abWeekSpinner.setOnItemSelectedListener(this);

        toggleABWeek();

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        toggleABWeek();
        if (floatingActionButton != null)
            floatingActionButton.show();

        setHasOptionsMenu(false);
        setHasOptionsMenu(true);
    }

    private void toggleABWeek() {
        SettingsManager settings = SettingsManager.getInstance();
        hasABWeek = settings.hasABWeek();
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();

        if (actionBar != null)
            if (hasABWeek) {
                actionBar.setDisplayShowTitleEnabled(false);
                abWeekSpinner.setVisibility(View.VISIBLE);
            } else {
                abWeekSpinner.setVisibility(View.GONE);
                actionBar.setDisplayShowTitleEnabled(true);
            }
    }

    @Override
    public void onPause() {
        super.onPause();
        timetableManager.saveTimetableAsync(timetable);
        if (floatingActionButton != null)
            floatingActionButton.hide();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(KEY_SHOW_TIMES, showTimes);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.timetable, menu);
        MenuItem transferWeekItem = menu.findItem(R.id.timetable_transfer_week);
        transferWeekItem.setVisible(hasABWeek);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.timetable_show_times:
                showTimes = !showTimes;
                if (pagerAdapter != null)
                    pagerAdapter.setShowTimes(showTimes);
                break;
            case R.id.timetable_delete_week:
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AlertDialogTheme);
                builder.setTitle(R.string.delete_week);
                builder.setMessage(R.string.are_you_sure_you_want_to_delete_the_current_week);
                builder.setNegativeButton(android.R.string.cancel, null);
                builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        if (timetable != null && timetable.size() > week) {
                            List<List<Timetable.TimetableData>> timetableWeek = timetable.get(week);
                            for (List<Timetable.TimetableData> timetableDay : timetableWeek) {
                                timetableDay.clear();
                            }

                            pagerAdapter.notifyDataSetChanged();
                        }
                    }
                });
                builder.show();
                return true;
            case R.id.timetable_delete_all:
                builder = new AlertDialog.Builder(getActivity(), R.style.AlertDialogTheme);
                builder.setTitle(R.string.delete_all);
                builder.setMessage(R.string.are_you_sure_you_want_to_delete_your_whole_timetable);
                builder.setNegativeButton(android.R.string.cancel, null);
                builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        if (timetable != null)
                            for (List<List<Timetable.TimetableData>> timetableWeek : timetable) {
                                for (List<Timetable.TimetableData> timetableDay : timetableWeek) {
                                    timetableDay.clear();
                                }
                            }

                        pagerAdapter.notifyDataSetChanged();
                    }
                });
                builder.show();
                return true;
            case R.id.timetable_transfer_week:
                builder = new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme);
                builder.setTitle(R.string.transfer_week);
                builder.setMessage(R.string.all_subjects_of_the_target_week_will_be_overwritten_during_the_transfer);
                View dialogView = LayoutInflater.from(getActivity()).inflate(
                        R.layout.dialog_timetable_transfer_week, null);
                final TextView leftTextView = (TextView) dialogView.findViewById(
                        R.id.dialog_timetable_transfer_week_left_text_view);
                leftTextView.setTag(0);
                final TextView rightTextView = (TextView) dialogView.findViewById(
                        R.id.dialog_timetable_transfer_week_right_text_view);
                rightTextView.setTag(1);
                dialogView.findViewById(R.id.dialog_timetable_transfer_week_switch_image_button).
                        setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        CharSequence leftText = leftTextView.getText();
                        leftTextView.setText(rightTextView.getText());
                        rightTextView.setText(leftText);

                        Object leftTag = leftTextView.getTag();
                        leftTextView.setTag(rightTextView.getTag());
                        rightTextView.setTag(leftTag);
                    }
                });
                builder.setView(dialogView);
                builder.setNegativeButton(android.R.string.cancel, null);
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (timetable != null && timetable.size() >= 2) {
                            int leftTag = (int) leftTextView.getTag();
                            int rightTag = (int) rightTextView.getTag();
                            List<List<Timetable.TimetableData>> timetableWeek =
                                    timetable.get(leftTag);
                            List<List<Timetable.TimetableData>> destinationTimetableWeek =
                                    timetable.get(rightTag);

                            for (int j = 0; j < 5; j++) {
                                if (timetableWeek.size() > j && destinationTimetableWeek.size() > j) {
                                    List<Timetable.TimetableData> timetableDay = timetableWeek.get(j);
                                    List<Timetable.TimetableData> destinationTimetableDay =
                                            destinationTimetableWeek.get(j);

                                    destinationTimetableDay.clear();
                                    destinationTimetableDay.addAll(timetableDay);
                                }
                            }

                            pagerAdapter.notifyDataSetChanged();
                        }
                    }
                });
                builder.show();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.fab) {
            if (timetable != null && timetable.size() > week) {
                List<List<Timetable.TimetableData>> timetableWeek = timetable.get(week);
                int dayIndex = viewPager.getCurrentItem();
                if (timetableWeek != null && timetableWeek.size() > dayIndex) {
                    List<Timetable.TimetableData> timetableDay = timetableWeek.get(dayIndex);
                    Calendar selectedDay = Calendar.getInstance();
                    selectedDay.set(Calendar.DAY_OF_WEEK, 2 + dayIndex);

                    Intent editSubjectIntent = new Intent(getActivity(),
                            TimetableEditSubjectActivity.class);
                    editSubjectIntent.putExtra(TimetableEditSubjectActivity.EXTRA_DAY_OF_WEEK,
                            selectedDay.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG,
                                    Locale.getDefault()));
                    editSubjectIntent.putExtra(TimetableEditSubjectActivity.EXTRA_HOUR,
                            Timetable.getHourForIndex(timetableDay, timetableDay.size()));
                    startActivityForResult(editSubjectIntent,
                            Constants.ACTIVITY_REQUEST_CODE_TIMETABLE_EDIT_SUBJECT);
                }
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
        week = position;
        if (timetable != null && timetable.size() > week) {
            pagerAdapter.setTimetableWeek(timetable.get(week));
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.ACTIVITY_REQUEST_CODE_TIMETABLE_EDIT_SUBJECT) {
            if (resultCode == Activity.RESULT_OK && timetable != null && timetable.size() > week &&
                    timetable.get(week).size() > viewPager.getCurrentItem()) {
                Timetable.TimetableData timetableData =
                        data.getParcelableExtra(TimetableEditSubjectActivity.EXTRA_SUBJECT_TO_EDIT);
                List<Timetable.TimetableData> timetableDay =
                        timetable.get(week).get(viewPager.getCurrentItem());
                int index = Timetable.getIndexForHour(timetableDay, timetableData.getHour());

                if (index < timetableDay.size()) {
                    timetableDay.set(index, timetableData);
                    Timetable.correctHours(timetableDay);

                } else {
                    timetableDay.add(timetableData);
                }

                pagerAdapter.notifyDataSetChanged();
            }
        } else
            super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.timetable);
    }

}