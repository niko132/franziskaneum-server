package de.franziskaneum.vplan;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.TaskStackBuilder;
import android.widget.RemoteViews;

import de.franziskaneum.Constants;
import de.franziskaneum.MainActivity;
import de.franziskaneum.R;

/**
 * Created by Niko on 14.08.2016.
 */
public class VPlanAppWidgetProvider extends AppWidgetProvider {

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        RemoteViews rootView = new RemoteViews(context.getPackageName(), R.layout.appwidget_vplan);

        Intent appIntent = new Intent(context, MainActivity.class);
        appIntent.putExtra(MainActivity.EXTRA_DRAWER_ITEM_ID, R.id.drawer_vplan);
        PendingIntent appPendingIntent = PendingIntent.getActivity(context,
                Constants.PENDING_INTENT_APPWIDGET_VPLAN_APP, appIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        rootView.setOnClickPendingIntent(R.id.appwidget_vplan_header, appPendingIntent);

        Intent vplanDayIntent = new Intent(context, VPlanDayActivity.class);
        PendingIntent vplanDayPendingIntent = TaskStackBuilder.create(context).
                addNextIntentWithParentStack(vplanDayIntent).getPendingIntent(
                Constants.PENDING_INTENT_APPWIDGET_VPLAN_DAY, PendingIntent.FLAG_UPDATE_CURRENT);
        rootView.setPendingIntentTemplate(R.id.appwidget_vplan_content_list, vplanDayPendingIntent);

        Intent listIntent = new Intent(context, VPlanAppWidgetService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            rootView.setRemoteAdapter(R.id.appwidget_vplan_content_list, listIntent);
        } else
            for (int appWidgetId : appWidgetIds)
                rootView.setRemoteAdapter(appWidgetId, R.id.appwidget_vplan_content_list, listIntent);
        rootView.setEmptyView(R.id.appwidget_vplan_content_list, R.id.appwidget_vplan_empty_view);

        appWidgetManager.updateAppWidget(appWidgetIds, rootView);
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.appwidget_vplan_content_list);
    }
}
