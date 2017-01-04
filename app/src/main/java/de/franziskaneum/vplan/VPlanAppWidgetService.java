package de.franziskaneum.vplan;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.widget.RemoteViewsService;

/**
 * Created by Niko on 28.03.2016.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class VPlanAppWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new VPlanAppWidgetListProvider(getApplicationContext());
    }
}
