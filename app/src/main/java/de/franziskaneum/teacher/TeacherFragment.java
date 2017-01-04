package de.franziskaneum.teacher;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import de.franziskaneum.FranzCallback;
import de.franziskaneum.R;
import de.franziskaneum.Status;
import de.franziskaneum.drawer.DrawerFragment;
import de.franziskaneum.views.SwipeRefreshLayout;

/**
 * Created by Niko on 23.02.2016.
 */
public class TeacherFragment extends DrawerFragment implements
        android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener, View.OnClickListener {

    private TeacherManager teacherManager;
    private TeacherList teacherList;

    private SwipeRefreshLayout swipeRefreshLayout;
    private TeacherRecyclerAdapter recyclerAdapter;

    private RelativeLayout errorContainer;
    private ImageView errorImage;
    private TextView errorDescription;

    private FranzCallback teacherCallback = new FranzCallback() {
        @Override
        public void onCallback(int status, Object... objects) {
            if (errorContainer != null)
                errorContainer.setVisibility(View.GONE);

            if (Status.OK == status && objects.length > 0 && objects[0] != null) {
                TeacherList teacherList = (TeacherList) objects[0];

                if (swipeRefreshLayout != null)
                    swipeRefreshLayout.setRefreshing(false);

                TeacherFragment.this.teacherList = teacherList;
                if (recyclerAdapter != null)
                    recyclerAdapter.setTeacherList(teacherList);
            } else {
                switch (status) {
                    case Status.FILE_NOT_FOUND:
                        teacherManager.getTeacherListAsync(true, teacherCallback);
                        if (swipeRefreshLayout != null)
                            swipeRefreshLayout.setRefreshing(true);
                        break;
                    case Status.NO_CONNECTION:
                        if (swipeRefreshLayout != null)
                            swipeRefreshLayout.setRefreshing(false);

                        if (TeacherFragment.this.teacherList == null) {
                            errorDescription.setText(R.string.no_connection);
                            errorImage.setImageDrawable(ContextCompat.getDrawable(
                                    getContext(), R.drawable.ic_no_connection));

                            errorContainer.setVisibility(View.VISIBLE);
                        } else if (swipeRefreshLayout != null)
                            Snackbar.make(swipeRefreshLayout, R.string.no_connection, Snackbar.LENGTH_LONG).
                                    show();
                        break;
                    default:
                        if (swipeRefreshLayout != null) {
                            swipeRefreshLayout.setRefreshing(false);
                            Snackbar.make(swipeRefreshLayout, R.string.unknown_error,
                                    Snackbar.LENGTH_LONG).show();
                        }
                        break;
                }
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        teacherManager = TeacherManager.getInstance();

        if (teacherList == null) {
            teacherManager.getTeacherListAsync(false, teacherCallback);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_teacher_list, container, false);

        Toolbar toolbar = (Toolbar) root.findViewById(R.id.toolbar);
        setToolbar(toolbar);

        swipeRefreshLayout = (SwipeRefreshLayout) root
                .findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(R.color.ColorAccent);

        RecyclerView recyclerView = (RecyclerView) root.findViewById(R.id.recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        if (recyclerAdapter == null)
            recyclerAdapter = new TeacherRecyclerAdapter(this);

        recyclerAdapter.setTeacherList(teacherList);
        recyclerView.setAdapter(recyclerAdapter);

        errorContainer = (RelativeLayout) root
                .findViewById(R.id.error_container);
        errorImage = (ImageView) errorContainer.findViewById(R.id.error_image);
        errorDescription = (TextView) errorContainer
                .findViewById(R.id.error_description);

        errorContainer.setOnClickListener(this);

        return root;
    }

    @Override
    public void onRefresh() {
        teacherManager.getTeacherListAsync(true, teacherCallback);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.error_container:
                swipeRefreshLayout.setRefreshing(true);
                errorContainer.setVisibility(View.GONE);
                onRefresh();
                break;
            default:
                Object itemPositionTag = view.getTag(R.string.recycler_item_position);

                if (itemPositionTag != null) {
                    int itemPosition = (int) itemPositionTag;
                    if (itemPosition < teacherList.size()) {
                        TeacherList.TeacherData teacher = teacherList.get(itemPosition);

                        Intent teacherDetailIntent = new Intent(getActivity(),
                                TeacherDetailActivity.class);
                        teacherDetailIntent.putExtra(TeacherDetailActivity.EXTRA_TEACHER, teacher);
                        startActivity(teacherDetailIntent);
                    }
                }
                break;
        }
    }

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.teacher_list);
    }
}
