package de.franziskaneum.vplan;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.franziskaneum.FranzCallback;
import de.franziskaneum.R;
import de.franziskaneum.Status;
import de.franziskaneum.teacher.TeacherList;
import de.franziskaneum.teacher.TeacherManager;

/**
 * Created by Niko on 06.03.2016.
 */
public class VPlanDayFragment extends Fragment {

    private VPlan.VPlanDayData vplanDay;
    private TeacherList teacherList;

    private boolean noChangesAvailable = false;

    private RecyclerView recycler;
    private View noChanges;

    private VPlanDayRecyclerAdapter recyclerAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TeacherManager.getInstance().getTeacherListAsync(false, new FranzCallback() {
            @Override
            public void onCallback(int status, Object... objects) {
                if (Status.OK == status && objects.length > 0 && objects[0] != null) {
                    TeacherList teacherList = (TeacherList) objects[0];

                    VPlanDayFragment.this.teacherList = teacherList;
                    if (recyclerAdapter != null)
                        recyclerAdapter.setTeacherList(teacherList);
                }
            }
        });
    }

    public void setVPlanDay(VPlan.VPlanDayData vplanDay) {
        this.vplanDay = vplanDay;
        toggleNoChangesView();
        if (vplanDay != null && recyclerAdapter != null)
            recyclerAdapter.setVPlanDay(vplanDay);
    }

    public void setNoChangesAvailable(boolean noChangesAvailable) {
        this.noChangesAvailable = noChangesAvailable;
        toggleNoChangesView();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_vplan_day, container, false);

        recycler = (RecyclerView) root.findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(getActivity()));

        if (recyclerAdapter == null)
            recyclerAdapter = new VPlanDayRecyclerAdapter();
        recyclerAdapter.setVPlanDay(vplanDay);
        recyclerAdapter.setTeacherList(teacherList);
        recycler.setAdapter(recyclerAdapter);

        noChanges = root.findViewById(R.id.vplan_day_no_changes);

        toggleNoChangesView();

        return root;
    }

    private void toggleNoChangesView() {
        if (recycler != null && noChanges != null)
            if ((vplanDay == null || vplanDay.getTableData() == null ||
                    vplanDay.getTableData().size() == 0) && noChangesAvailable) {
                recycler.setVisibility(View.GONE);
                noChanges.setVisibility(View.VISIBLE);
            } else {
                noChanges.setVisibility(View.GONE);
                recycler.setVisibility(View.VISIBLE);
            }
    }
}
