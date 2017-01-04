package de.franziskaneum.vplan;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import de.franziskaneum.R;

/**
 * Created by Niko on 28.03.2016.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class VPlanAppWidgetListProvider implements RemoteViewsService.RemoteViewsFactory {
    private Context context;
    private VPlanNotification vplanNotification;

    public VPlanAppWidgetListProvider(Context context) {
        this.context = context;
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {
        vplanNotification = VPlanNotificationManager.getInstance().getSavedNotifications();
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return vplanNotification != null ? vplanNotification.size() : 0;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        if (vplanNotification != null && position < vplanNotification.size()) {
            VPlanNotification.VPlanNotificationDay notificationDay = vplanNotification.get(position);

            if (notificationDay.getNotifications() != null && !notificationDay.getNotifications().isEmpty()) {
                RemoteViews rootView = new RemoteViews(context.getPackageName(), R.layout.appwidget_vplan_list_item);

                Intent fillInIntent = new Intent();
                fillInIntent.putExtra(VPlanDayActivity.EXTRA_DAY_TITLE, notificationDay.getTitle());
                rootView.setOnClickFillInIntent(R.id.appwidget_vplan_content_list_item_container, fillInIntent);

                rootView.setTextViewText(R.id.appwidget_vplan_content_list_item_title, notificationDay.getTitle());

                StringBuilder contentStringBuilder = null;
                for (String line  : notificationDay.getNotifications()) {
                    if (contentStringBuilder == null)
                        contentStringBuilder = new StringBuilder(line);
                    else {
                        contentStringBuilder.append("\n");
                        contentStringBuilder.append(line);
                    }
                }

                if (contentStringBuilder != null)
                    rootView.setTextViewText(R.id.appwidget_vplan_content_list_item_content, contentStringBuilder.toString());

                return rootView;
            }
        }

        return null;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
