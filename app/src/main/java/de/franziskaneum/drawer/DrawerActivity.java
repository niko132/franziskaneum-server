package de.franziskaneum.drawer;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Niko on 20.12.2015.
 */
public class DrawerActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private OnDrawerInflatedListener listener;

    protected void setDrawerLayout(DrawerLayout drawerLayout) {
        this.drawerLayout = drawerLayout;
        if (drawerLayout != null && listener != null)
            listener.onDrawerInflated(drawerLayout);
    }

    public void setOnDrawerInflatedListener(OnDrawerInflatedListener listener) {
        this.listener = listener;
        if (listener != null && drawerLayout != null)
            listener.onDrawerInflated(drawerLayout);
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();
        for (Fragment fragment : fm.getFragments()) {
            if (fragment instanceof DrawerFragment) {
                DrawerFragment df = (DrawerFragment) fragment;

                if (df.onBackPressed())
                    return;
            }
        }

        super.onBackPressed();
    }
}
