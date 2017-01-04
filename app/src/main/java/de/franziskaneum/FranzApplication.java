package de.franziskaneum;

import android.app.Application;

import de.franziskaneum.news.NewsManager;
import de.franziskaneum.settings.SettingsManager;
import de.franziskaneum.teacher.TeacherManager;
import de.franziskaneum.timetable.TimetableManager;
import de.franziskaneum.vplan.VPlanManager;
import de.franziskaneum.vplan.VPlanNotificationManager;

/**
 * Created by Niko on 20.12.2015.
 */
public class FranzApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        initSingletons();
    }

    private void initSingletons() {
        SettingsManager.initInstance(this);
        NewsManager.initInstance(this);
        VPlanManager.initInstance(this);
        VPlanNotificationManager.initInstance(this);
        TimetableManager.initInstance(this);
        TeacherManager.initInstance(this);
    }

}
