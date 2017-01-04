package de.franziskaneum.vplan;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import de.franziskaneum.R;

/**
 * Created by Niko on 06.03.2016.
 */
public class VPlanDayPagerAdapter extends FragmentPagerAdapter {

    private VPlan.VPlanDayData generalVPlanDay;
    private VPlan.VPlanDayData myChangesVPlanDay;

    private VPlanDayFragment generalVPlanFragment;
    private VPlanDayFragment myChangesVPlanFragment;
    private Context context;

    public VPlanDayPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        this.context = context;
    }

    public void setGeneralVPlanDay(VPlan.VPlanDayData vplanDay) {
        this.generalVPlanDay = vplanDay;
        if (generalVPlanFragment != null)
            generalVPlanFragment.setVPlanDay(generalVPlanDay);
    }

    public void setMyChangesVPlanDay(VPlan.VPlanDayData vplanDay) {
        this.myChangesVPlanDay = vplanDay;
        if (myChangesVPlanFragment != null)
            myChangesVPlanFragment.setVPlanDay(myChangesVPlanDay);
    }

    @Override
    public Fragment getItem(int position) {
        return new VPlanDayFragment();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        switch(position) {
            case 0:
                generalVPlanFragment = (VPlanDayFragment) super.instantiateItem(container, position);
                generalVPlanFragment.setVPlanDay(generalVPlanDay);
                return generalVPlanFragment;
            case 1:
                myChangesVPlanFragment = (VPlanDayFragment) super.instantiateItem(container, position);
                myChangesVPlanFragment.setVPlanDay(myChangesVPlanDay);
                myChangesVPlanFragment.setNoChangesAvailable(true);
                return myChangesVPlanFragment;
            default:
                return super.instantiateItem(container, position);
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return context.getString(R.string.general);
            case 1:
                return context.getString(R.string.my_changes);
            default:
                return "";
        }
    }
}
