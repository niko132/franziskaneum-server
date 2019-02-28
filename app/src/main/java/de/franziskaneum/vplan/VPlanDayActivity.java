package de.franziskaneum.vplan;

import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import de.franziskaneum.FranzCallback;
import de.franziskaneum.R;
import de.franziskaneum.Status;

/**
 * Created by Niko on 23.02.2016.
 */
public class VPlanDayActivity extends AppCompatActivity {
    public static final String EXTRA_VPLAN_DAY_DATA =
            "de.franziskaneum.vplan.VPlanDayActivity.extra.VPLAN_DAY";
    public static final String EXTRA_DAY_TITLE =
            "de.franziskaneum.vplan.VPlanDayActivity.extra.DAY_TITLE";

    private VPlanDayPagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vplan_day);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ViewPager viewPager = (ViewPager) findViewById(R.id.vplan_day_view_pager);
        pagerAdapter = new VPlanDayPagerAdapter(this, getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);

        final AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.app_bar);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                appBarLayout.setExpanded(true, false); // expand the toolbar to be able to click the back button without scrolling
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            final VPlan.VPlanDayData dayData = extras.getParcelable(EXTRA_VPLAN_DAY_DATA);
            if (dayData != null) {
                pagerAdapter.setGeneralVPlanDay(dayData);

                VPlan notificationVPlan = new VPlan();
                notificationVPlan.add(dayData);

                VPlanNotificationManager.getInstance().getFilteredVPlanAsync(notificationVPlan, new FranzCallback() {
                    @Override
                    public void onCallback(int status, Object... objects) {
                        if (Status.OK == status && objects.length > 0 && objects[0] != null) {
                            VPlan filteredVPlan = (VPlan) objects[0];

                            for (VPlan.VPlanDayData filteredDay : filteredVPlan)
                                if (filteredDay.getTitle() != null &&
                                        dayData.getTitle() != null &&
                                        filteredDay.getTitle().equals(dayData.getTitle()))
                                    pagerAdapter.setMyChangesVPlanDay(filteredDay);
                        }
                    }
                });
            } else {
                final String dayTitle = extras.getString(EXTRA_DAY_TITLE);
                if (dayTitle != null) {
                    VPlanManager.getInstance().getVPlanAsync(VPlanManager.Mode.CACHE, new FranzCallback() {
                        @Override
                        public void onCallback(int status, Object... objects) {
                            if (Status.OK == status && objects.length > 1 && objects[1] != null) {
                                VPlan vplan = (VPlan) objects[1];

                                for (final VPlan.VPlanDayData dayData : vplan) {
                                    if (dayData.getTitle() != null &&
                                            dayData.getTitle().equals(dayTitle)) {
                                        pagerAdapter.setGeneralVPlanDay(dayData);

                                        VPlanNotificationManager.getInstance().getFilteredVPlanAsync(vplan, new FranzCallback() {
                                            @Override
                                            public void onCallback(int status, Object... objects) {
                                                if (Status.OK == status && objects.length > 0 && objects[0] != null) {
                                                    VPlan filteredVPlan = (VPlan) objects[0];

                                                    for (VPlan.VPlanDayData filteredDay : filteredVPlan)
                                                        if (filteredDay.getTitle() != null &&
                                                                filteredDay.getTitle().
                                                                        equals(dayData.
                                                                                getTitle()))
                                                            pagerAdapter.setMyChangesVPlanDay(
                                                                    filteredDay);
                                                }
                                            }
                                        });
                                        break;
                                    }
                                }
                            }
                        }
                    });
                } else
                    finish();
            }
        } else
            finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
