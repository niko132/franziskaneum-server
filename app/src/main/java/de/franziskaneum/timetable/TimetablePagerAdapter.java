package de.franziskaneum.timetable;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.ViewGroup;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Created by Niko on 16.02.2016.
 */
public class TimetablePagerAdapter extends FragmentPagerAdapter {
    private FragmentManager fragmentManager;
    private String dayTitles[] = new String[5];
    public TimetableDayFragment[] fragments = new TimetableDayFragment[5];

    private List<List<Timetable.TimetableData>> timetableWeek;
    private boolean showTimes = false;

    public TimetablePagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
        this.fragmentManager = fragmentManager;

        SimpleDateFormat sdf = new SimpleDateFormat("EEE", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();

        for (int i = 0; i < 5; i++) {
            calendar.set(Calendar.DAY_OF_WEEK, i + 2);
            dayTitles[i] = sdf.format(calendar.getTime());
        }
    }

    public void setTimetableWeek(List<List<Timetable.TimetableData>> timetableWeek) {
        this.timetableWeek = timetableWeek;
        notifyDataSetChanged();
    }

    public void setShowTimes(boolean showTimes) {
        this.showTimes = showTimes;
        notifyDataSetChanged();
    }

    @Override
    public Fragment getItem(int position) {
        if (position < fragments.length) {
            if (fragments[position] == null)
                fragments[position] = new TimetableDayFragment();

            if (timetableWeek != null && timetableWeek.size() > position)
                fragments[position].setTimetableDay(timetableWeek.get(position), position);
            fragments[position].setShowTimes(showTimes);

            return fragments[position];
        } else
            return null;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment fragment = getItem(position);
        FragmentTransaction trans = fragmentManager.beginTransaction();
        trans.add(container.getId(), fragment, "fragment:" + position);
        trans.commit();
        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        if (position < fragments.length) {
            FragmentTransaction trans = fragmentManager.beginTransaction();
            trans.remove(fragments[position]);
            trans.commit();
            fragments[position] = null;
        }
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return ((Fragment) object).getView() == view;
    }

    @Override
    public int getCount() {
        return 5;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return dayTitles[position];
    }

    @Override
    public void notifyDataSetChanged() {
        for (int i = 0; i < fragments.length; i++) {
            TimetableDayFragment fragment = fragments[i];

            if (fragment != null) {
                if (timetableWeek != null && i < timetableWeek.size())
                    fragment.setTimetableDay(timetableWeek.get(i), i);

                fragment.setShowTimes(showTimes);
            }
        }
        super.notifyDataSetChanged();
    }
}
