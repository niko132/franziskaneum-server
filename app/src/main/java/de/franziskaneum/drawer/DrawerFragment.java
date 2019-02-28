package de.franziskaneum.drawer;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import de.franziskaneum.R;

/**
 * Created by Niko on 20.12.2015.
 */
public abstract class DrawerFragment extends Fragment implements OnDrawerInflatedListener {

    private DrawerActivity activity;
    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private String title = "";

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Activity) {
            Activity activity = (Activity) context;

            if (activity instanceof DrawerActivity) {
                this.activity = (DrawerActivity) activity;
                this.activity.setOnDrawerInflatedListener(this);
            }
        }

        title = getTitle(context);

        if (title == null || title.isEmpty())
            title = context.getString(R.string.app_name);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (activity != null)
            activity.setOnDrawerInflatedListener(null);
    }

    protected void setToolbar(Toolbar toolbar) {
        this.toolbar = toolbar;
        if (toolbar != null) {
            if (activity != null)
                activity.setSupportActionBar(toolbar);
            setActionBarTitle();

            if (drawerLayout != null)
                setupDrawerToggle();
        }
    }

    @Override
    public void onDrawerInflated(DrawerLayout drawerLayout) {
        this.drawerLayout = drawerLayout;
        setupDrawerToggle();
    }

    private void setupDrawerToggle() {
        if (drawerLayout != null && toolbar != null) {
            ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(activity, drawerLayout,
                    toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {

                @Override
                public void onDrawerOpened(View drawerView) {
                    super.onDrawerOpened(drawerView);
                }

                @Override
                public void onDrawerClosed(View drawerView) {
                    super.onDrawerClosed(drawerView);
                }

            };
            drawerLayout.addDrawerListener(drawerToggle);
            drawerToggle.syncState();
        }
    }

    protected void setTitle(String title) {
        this.title = title;
        setActionBarTitle();
    }

    private void setActionBarTitle() {
        if (activity != null) {
            ActionBar actionBar = activity.getSupportActionBar();
            if (actionBar != null)
                activity.getSupportActionBar().setTitle(title);
        }
    }

    public abstract String getTitle(Context context);

    public boolean onBackPressed() {
        return false;
    }

}
