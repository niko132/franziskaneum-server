package de.franziskaneum.timetable;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import de.franziskaneum.R;

/**
 * Created by Niko on 16.02.2016.
 */
public class TimetableDayFragment extends Fragment {

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

        if (recyclerAdapter != null) {
            recyclerAdapter.invalidateSchoolClass();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_timetable_day, container, false);

        RecyclerView recyclerView = (RecyclerView) root.findViewById(R.id.recycler);
        recyclerView.setHasFixedSize(true);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        if (recyclerAdapter == null)
            recyclerAdapter = new TimetableDayRecyclerAdapter(recyclerView, dayIndex, this);

        recyclerAdapter.setTimetableDay(timetableDay);
        recyclerAdapter.setShowTimes(showTimes);
        recyclerView.setAdapter(recyclerAdapter);

        return root;
    }
}