package de.franziskaneum.teacher;

import android.animation.Animator;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.miguelcatalan.materialsearchview.MaterialSearchView;

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

    private MaterialSearchView searchView;

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

        searchView = (MaterialSearchView) root.findViewById(R.id.search_view);
        searchView.findViewById(R.id.action_up_btn).setOnClickListener(this);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            Drawable icon = getResources().getDrawable(R.drawable.ic_action_navigation_arrow_back, null);
            icon.setColorFilter(ContextCompat.getColor(getContext(), R.color.ColorPrimary), PorterDuff.Mode.SRC_IN);

            searchView.setBackIcon(icon);
        }

        swipeRefreshLayout = (SwipeRefreshLayout) root
                .findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(R.color.ColorAccent);

        RecyclerView recyclerView = (RecyclerView) root.findViewById(R.id.recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        if (recyclerAdapter == null)
            recyclerAdapter = new TeacherRecyclerAdapter();

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
    public void onResume() {
        super.onResume();

        setHasOptionsMenu(false);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.teacher, menu);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            /*
            final MenuItem menuItem = menu.findItem(R.id.action_search);
            final SearchView searchView = (SearchView) menuItem.getActionView();
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);

                    if (recyclerAdapter != null)
                        recyclerAdapter.getFilter().filter(query);

                    return true;
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    if (recyclerAdapter != null)
                        recyclerAdapter.getFilter().filter(s);

                    return true;
                }
            });
            */

            MenuItem searchItem = menu.findItem(R.id.action_search);
            searchView.setMenuItem(searchItem);

            searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);

                    if (recyclerAdapter != null)
                        recyclerAdapter.getFilter().filter(query);

                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    if (recyclerAdapter != null)
                        recyclerAdapter.getFilter().filter(newText);

                    return true;
                }
            });
        }
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
            case R.id.action_up_btn:
                closeSearch();
                break;
        }
    }

    @Override
    public boolean onBackPressed() {
        if (searchView.isSearchOpen()) {
            closeSearch();
            return true;
        }

        return super.onBackPressed();
    }

    private void closeSearch() {
        View view = searchView;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int cx = view.getWidth() - (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 24, view.getResources().getDisplayMetrics());
            int cy = view.getHeight() / 2;
            int startRadius = Math.max(view.getWidth(), view.getHeight());

            Animator anim = ViewAnimationUtils.createCircularReveal(view, cx, cy, startRadius, 0);
            anim.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    searchView.closeSearch();
                }

                @Override
                public void onAnimationCancel(Animator animator) {
                }

                @Override
                public void onAnimationRepeat(Animator animator) {
                }
            });

            anim.start();
        } else {
            ViewCompat.animate(view).alpha(0.0f).setDuration(400).setListener(new ViewPropertyAnimatorListener() {
                @Override
                public void onAnimationStart(View view) {

                }

                @Override
                public void onAnimationEnd(View view) {
                    searchView.closeSearch();
                }

                @Override
                public void onAnimationCancel(View view) {

                }
            });
        }
    }

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.teacher_list);
    }
}
