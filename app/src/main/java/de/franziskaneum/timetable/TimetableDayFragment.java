package de.franziskaneum.timetable;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import de.franziskaneum.Constants;
import de.franziskaneum.R;

/**
 * Created by Niko on 16.02.2016.
 */
public class TimetableDayFragment extends Fragment implements View.OnClickListener {

    private int dayIndex;
    @Nullable
    private List<Timetable.TimetableData> timetableDay;
    private boolean showTimes = false;

    private TimetableDayRecyclerAdapter recyclerAdapter;

    public void setTimetableDay(@Nullable List<Timetable.TimetableData> timetableDay, int dayIndex) {
        this.timetableDay = timetableDay;
        this.dayIndex = dayIndex;

        if (recyclerAdapter != null)
            recyclerAdapter.setTimetableDay(timetableDay);
    }

    public void setShowTimes(boolean showTimes) {
        this.showTimes = showTimes;
        if (recyclerAdapter != null)
            recyclerAdapter.setShowTimes(showTimes);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (recyclerAdapter != null)
            recyclerAdapter.invalidateSchoolClass();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_timetable_day, container, false);

        RecyclerView recyclerView = (RecyclerView) root.findViewById(R.id.recycler);
        recyclerView.setHasFixedSize(true);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        if (recyclerAdapter == null)
            recyclerAdapter = new TimetableDayRecyclerAdapter(this, recyclerView);

        recyclerAdapter.setTimetableDay(timetableDay);
        recyclerAdapter.setShowTimes(showTimes);
        recyclerView.setAdapter(recyclerAdapter);

        return root;
    }

    @Override
    public void onClick(View view) {
        Object itemPositionTag = view.getTag(R.string.recycler_item_position);

        if (itemPositionTag != null && timetableDay != null) {
            final int itemPosition = (int) itemPositionTag;

            if (itemPosition >= 0 && itemPosition < timetableDay.size()) {
                final Timetable.TimetableData timetableData = timetableDay.get(itemPosition);
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),
                        R.style.AlertDialogTheme);

                // inflate the dialog main view
                View dialogView = LayoutInflater.from(getActivity()).
                        inflate(R.layout.dialog_timetable_detail, null);

                // set the view to the dialog builder
                builder.setView(dialogView);

                ((TextView) dialogView.findViewById(R.id.dialog_timetable_detail_title))
                        .setText(timetableData.getHour() + "." + getActivity().getString(R.string.hour));
                ((TextView) dialogView.findViewById(R.id.dialog_timetable_detail_subject))
                        .setText(timetableData.getSubject());
                ((TextView) dialogView.findViewById(R.id.dialog_timetable_detail_room))
                        .setText(timetableData.getRoom());
                ((TextView) dialogView.findViewById(R.id.dialog_timetable_detail_teacher))
                        .setText(timetableData.getTeacherOrSchoolClass());

                final AlertDialog dialog = builder.create();

                dialogView.findViewById(R.id.dialog_timetable_detail_edit)
                        .setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();

                                Calendar selectedDay = Calendar.getInstance();
                                selectedDay.set(Calendar.DAY_OF_WEEK, 2 + dayIndex);

                                Intent editSubjectIntent = new Intent(getActivity(),
                                        TimetableEditSubjectActivity.class);
                                editSubjectIntent.putExtra(
                                        TimetableEditSubjectActivity.EXTRA_DAY_OF_WEEK,
                                        selectedDay.getDisplayName(Calendar.DAY_OF_WEEK,
                                                Calendar.LONG, Locale.getDefault()));
                                editSubjectIntent.putExtra(TimetableEditSubjectActivity.EXTRA_HOUR,
                                        Timetable.getHourForIndex(timetableDay, itemPosition));
                                editSubjectIntent.putExtra(
                                        TimetableEditSubjectActivity.EXTRA_SUBJECT_TO_EDIT,
                                        timetableData);

                                // workaround for not called onActivityResult in nested Fragments (Android Bug)
                                getParentFragment().startActivityForResult(editSubjectIntent,
                                        Constants.ACTIVITY_REQUEST_CODE_TIMETABLE_EDIT_SUBJECT);
                            }

                        });

                dialogView.findViewById(R.id.dialog_timetable_detail_delete)
                        .setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();

                                timetableDay.remove(itemPosition);
                                Timetable.correctHours(timetableDay);

                                if (recyclerAdapter != null)
                                    recyclerAdapter.notifyDataSetChanged();
                            }

                        });

                dialog.show();
            }
        }
    }
}