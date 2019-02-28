package de.franziskaneum.vplan;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import de.franziskaneum.R;
import de.franziskaneum.utils.ClickableViewHolder;

/**
 * Created by Niko on 21.12.2015.
 */
public class VPlanRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_NORMAL = 1;

    public class VPlanViewHolder extends ClickableViewHolder {

        TextView title;
        TextView subtitle;

        public VPlanViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.vplan_list_item_title);
            subtitle = (TextView) itemView.findViewById(R.id.vplan_list_item_subtitle);
        }

        @Override
        public void onClick(int position, @Nullable Context context) {
            if (context != null && position - 1 >= 0 && vplanData.size() >= position) {
                VPlan.VPlanDayData vplanDay = vplanData.get(position - 1);

                Intent vplanDayIntent = new Intent(context, VPlanDayActivity.class);
                vplanDayIntent.putExtra(VPlanDayActivity.EXTRA_VPLAN_DAY_DATA, vplanDay);
                context.startActivity(vplanDayIntent);
            }
        }
    }

    public class VPlanHeaderViewHolder extends RecyclerView.ViewHolder {

        TextView text;

        public VPlanHeaderViewHolder(View itemView) {
            super(itemView);
            text = (TextView) itemView.findViewById(R.id.vplan_list_item_header_text);
        }
    }

    private VPlan vplanData;
    @Nullable
    private VPlanNotification vplanNotification;

    public VPlanRecyclerAdapter() {
        super();
    }

    public void setVPlanData(VPlan vplanData) {
        this.vplanData = vplanData;
        notifyDataSetChanged();
    }

    public void setNotificationData(@Nullable VPlanNotification vplanNotification) {
        this.vplanNotification = vplanNotification;
        notifyDataSetChanged();
    }

    public boolean hasData() {
        return vplanData != null;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == VIEW_TYPE_HEADER) {
            View root = inflater.inflate(R.layout.vplan_list_item_header, parent, false);
            return new VPlanHeaderViewHolder(root);
        } else {
            View root = inflater.inflate(R.layout.vplan_list_item, parent, false);
            return new VPlanViewHolder(root);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof VPlanHeaderViewHolder) {
            VPlanHeaderViewHolder viewHolder = (VPlanHeaderViewHolder) holder;
            viewHolder.text.setText(R.string.vplans);
        } else {
            VPlan.VPlanDayData vplanDay = vplanData.get(position - 1);
            VPlanNotification.VPlanNotificationDay vplanNotificationDay = null;

            if (vplanNotification != null)
                for (VPlanNotification.VPlanNotificationDay notificationDay : vplanNotification) {
                    if (notificationDay.getTitle() != null &&
                            notificationDay.getTitle().equals(vplanDay.getTitle())) {
                        vplanNotificationDay = notificationDay;
                        break;
                    }
                }

            VPlanViewHolder viewHolder = (VPlanViewHolder) holder;
            viewHolder.title.setText(vplanDay.getTitle());
            if (vplanNotificationDay != null && vplanNotificationDay.getNotifications() != null && !vplanNotificationDay.getNotifications().isEmpty()) {
                viewHolder.subtitle.setText(viewHolder.itemView.getContext().getString(R.string.changes, vplanNotificationDay.getNotifications().size()));
                viewHolder.subtitle.setTextColor(Color.RED);
            } else {
                viewHolder.subtitle.setText(vplanDay.getModified());
                viewHolder.subtitle.setTextColor(ContextCompat.getColor(viewHolder.itemView.getContext(), R.color.DarkGray));
            }
        }
    }

    @Override
    public int getItemCount() {
        return vplanData != null ? (vplanData.isEmpty() ? 0 : vplanData.size() + 1) : 0;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0)
            return VIEW_TYPE_HEADER;

        return VIEW_TYPE_NORMAL;
    }

}