package de.franziskaneum.timetable;

import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import de.franziskaneum.R;
import de.franziskaneum.settings.SettingsManager;

/**
 * Created by Niko on 16.02.2016.
 */
public class TimetableDayRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SINGLE_HOUR = 0;
    private static final int VIEW_TYPE_DOUBLE_HOUR = 1;

    private static final String[][][] TIMES = new String[][][]{
            {
                    {"7:30", "8:15"},
                    {"8:25", "9:10"},
                    {"9:25", "10:10"},
                    {"10:10", "10:55"},
                    {"11:15", "12:00"},
                    {"12:40", "13:25"},
                    {"13:35", "14:20"},
                    {"14:30", "15:15"},
                    {"15:15", "16:00"}},
            {
                    {"7:30", "8:15"},
                    {"8:25", "9:10"},
                    {"9:25", "10:10"},
                    {"10:10", "10:55"},
                    {"11:15", "12:00"},
                    {"12:10", "12:55"},
                    {"13:35", "14:20"},
                    {"14:30", "15:15"},
                    {"15:15", "16:00"}
            }
    };

    @Nullable
    private View.OnClickListener onClickListener;
    private List<Timetable.TimetableData> timetableDay;
    private boolean showTimes = false;
    private SettingsManager settings;
    private int schoolClassStep = 5;
    private RecyclerView recyclerView;

    public static class TimetableSingleHourViewHolder extends RecyclerView.ViewHolder {
        public View contentContainer;
        public TextView hour;
        public TextView subject;
        public TextView room;
        public TextView teacherOrSchoolClass;
        public View timesContainer;
        public TextView startTime;
        public TextView endTime;

        public TimetableSingleHourViewHolder(View itemView) {
            super(itemView);

            contentContainer = itemView.findViewById(R.id.timetable_item_content_container);
            hour = (TextView) itemView.findViewById(R.id.timetable_item_hour1);
            subject = (TextView) itemView
                    .findViewById(R.id.timetable_item_subject);
            subject.setSelected(true);
            room = (TextView) itemView.findViewById(R.id.timetable_item_room);
            room.setSelected(true);
            teacherOrSchoolClass = (TextView) itemView
                    .findViewById(R.id.timetable_item_teacher_or_school_class);
            teacherOrSchoolClass.setSelected(true);
            timesContainer = itemView.findViewById(R.id.timetable_item_time_container);
            startTime = (TextView) itemView.findViewById(R.id.timetable_item_start_time);
            endTime = (TextView) itemView.findViewById(R.id.timetable_item_end_time);
        }

    }

    public static class TimetableDoubleHourViewHolder extends
            RecyclerView.ViewHolder {
        public View contentContainer;
        public TextView hour1;
        public TextView hour2;
        public TextView subject;
        public TextView room;
        public TextView teacherOrSchoolClass;
        public View timesContainer;
        public TextView startTime;
        public TextView endTime;

        public TimetableDoubleHourViewHolder(View itemView) {
            super(itemView);

            contentContainer = itemView.findViewById(R.id.timetable_item_content_container);
            hour1 = (TextView) itemView.findViewById(R.id.timetable_item_hour1);
            hour2 = (TextView) itemView.findViewById(R.id.timetable_item_hour2);
            subject = (TextView) itemView
                    .findViewById(R.id.timetable_item_subject);
            subject.setSelected(true);
            room = (TextView) itemView.findViewById(R.id.timetable_item_room);
            room.setSelected(true);
            teacherOrSchoolClass = (TextView) itemView
                    .findViewById(R.id.timetable_item_teacher_or_school_class);
            teacherOrSchoolClass.setSelected(true);
            timesContainer = itemView.findViewById(R.id.timetable_item_time_container);
            startTime = (TextView) itemView.findViewById(R.id.timetable_item_start_time);
            endTime = (TextView) itemView.findViewById(R.id.timetable_item_end_time);
        }

    }

    public TimetableDayRecyclerAdapter(@Nullable View.OnClickListener onClickListener,
                                       RecyclerView recyclerView) {
        this.onClickListener = onClickListener;
        settings = SettingsManager.getInstance();
        schoolClassStep = settings.getSchoolClassStep();
        this.recyclerView = recyclerView;
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHandlerCallback());
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    public void setTimetableDay(@Nullable List<Timetable.TimetableData> timetableDay) {
        this.timetableDay = timetableDay;
        notifyDataSetChanged();
    }

    public void setShowTimes(boolean showTimes) {
        this.showTimes = showTimes;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && recyclerView != null) {
            TransitionManager.beginDelayedTransition(recyclerView);
        }
        notifyDataSetChanged();
    }

    public void invalidateSchoolClass() {
        schoolClassStep = settings.getSchoolClassStep();
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (timetableDay != null && timetableDay.size() > position)
            return timetableDay.get(position).isDoubleHour() ? VIEW_TYPE_DOUBLE_HOUR :
                    VIEW_TYPE_SINGLE_HOUR;
        else
            return VIEW_TYPE_SINGLE_HOUR;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == VIEW_TYPE_SINGLE_HOUR) {
            return new TimetableSingleHourViewHolder(inflater.inflate(
                    R.layout.timetable_list_item_single_hour, parent, false));
        } else {
            return new TimetableDoubleHourViewHolder(inflater.inflate(
                    R.layout.timetable_list_item_double_hour, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        String freehour = holder.itemView.getContext().getString(R.string._freehour_);

        if (timetableDay != null && timetableDay.size() > position) {
            Timetable.TimetableData timetableData = timetableDay.get(position);

            if (holder instanceof TimetableSingleHourViewHolder) {
                TimetableSingleHourViewHolder viewHolder = (TimetableSingleHourViewHolder) holder;
                viewHolder.hour.setText(timetableData.getHour() + ".");
                String subject = timetableData.getSubject();
                viewHolder.subject.setText(subject == null || subject.isEmpty() ? freehour : subject);
                viewHolder.room.setText(timetableData.getRoom());
                viewHolder.teacherOrSchoolClass.setText(timetableData.getTeacherOrSchoolClass());
                viewHolder.timesContainer.setVisibility(showTimes ? View.VISIBLE : View.GONE);

                String[][] times = null;

                if (settings.isTeacher()) {
                    String schoolClass = timetableData.getTeacherOrSchoolClass();
                    if (schoolClass != null && !schoolClass.isEmpty()) {
                        if (schoolClass.contains("/")) {
                            String schoolClassStepString = schoolClass.substring(0, schoolClass.indexOf("/"));
                            try {
                                int schoolClassStep = Integer.parseInt(schoolClassStepString);
                                times = schoolClassStep >= 5 && schoolClassStep < 7 ? TIMES[0] : TIMES[1];
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            }
                        }

                        if (schoolClass.contains(" ") && times == null) {
                            String schoolClassStepString = schoolClass.substring(0, schoolClass.indexOf(" "));
                            try {
                                int schoolClassStep = Integer.parseInt(schoolClassStepString);
                                times = schoolClassStep >= 5 && schoolClassStep < 7 ? TIMES[0] : TIMES[1];
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    if (times == null)
                        times = TIMES[1];
                } else {
                    times = schoolClassStep < 7 ? TIMES[0] : TIMES[1];
                }

                int hourIndex = Timetable.getHourForIndex(timetableDay, position) - 1;
                if (hourIndex < times.length) {
                    String[] subjectTimes = times[hourIndex];
                    viewHolder.startTime.setText(subjectTimes[0]);
                    viewHolder.endTime.setText(subjectTimes[1]);
                }

                if (onClickListener != null) {
                    viewHolder.contentContainer.setOnClickListener(onClickListener);
                    viewHolder.contentContainer.setTag(R.string.recycler_item_position, position);
                }
            } else {
                TimetableDoubleHourViewHolder viewHolder = (TimetableDoubleHourViewHolder) holder;
                viewHolder.hour1.setText(timetableData.getHour() + ".");
                viewHolder.hour2.setText((timetableData.getHour() + 1) + ".");
                String subject = timetableData.getSubject();
                viewHolder.subject.setText(subject == null || subject.isEmpty() ? freehour : subject);
                viewHolder.room.setText(timetableData.getRoom());
                viewHolder.teacherOrSchoolClass.setText(timetableData.getTeacherOrSchoolClass());
                viewHolder.timesContainer.setVisibility(showTimes ? View.VISIBLE : View.GONE);

                String[][] times = null;

                if (settings.isTeacher()) {
                    String schoolClass = timetableData.getTeacherOrSchoolClass();
                    if (schoolClass != null && !schoolClass.isEmpty()) {
                        if (schoolClass.contains("/")) {
                            String schoolClassStepString = schoolClass.substring(0, schoolClass.indexOf("/"));
                            try {
                                int schoolClassStep = Integer.parseInt(schoolClassStepString);
                                times = schoolClassStep >= 5 && schoolClassStep < 7 ? TIMES[0] : TIMES[1];
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            }
                        }

                        if (schoolClass.contains(" ") && times == null) {
                            String schoolClassStepString = schoolClass.substring(0, schoolClass.indexOf(" "));
                            try {
                                int schoolClassStep = Integer.parseInt(schoolClassStepString);
                                times = schoolClassStep >= 5 && schoolClassStep < 7 ? TIMES[0] : TIMES[1];
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    if (times == null)
                        times = TIMES[1];
                } else {
                    times = schoolClassStep < 7 ? TIMES[0] : TIMES[1];
                }
				
                int hourIndex = Timetable.getHourForIndex(timetableDay, position) - 1;
                if (hourIndex + 1 < times.length) {
                    String[] subjectTimes1 = times[hourIndex];
                    String[] subjectTimes2 = times[hourIndex + 1];
                    viewHolder.startTime.setText(subjectTimes1[0]);
                    viewHolder.endTime.setText(subjectTimes2[1]);
                }

                if (onClickListener != null) {
                    viewHolder.contentContainer.setOnClickListener(onClickListener);
                    viewHolder.contentContainer.setTag(R.string.recycler_item_position, position);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return timetableDay == null ? 0 : timetableDay.size();
    }

    private class ItemTouchHandlerCallback extends ItemTouchHelper.Callback {

        @Override
        public boolean isLongPressDragEnabled() {
            return true;
        }

        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            return makeMovementFlags(dragFlags, 0);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                              RecyclerView.ViewHolder target) {
            int fromPosition = viewHolder.getAdapterPosition();
            int toPosition = target.getAdapterPosition();

            if (fromPosition < toPosition) {
                for (int i = fromPosition; i < toPosition; i++) {
                    Collections.swap(timetableDay, i, i + 1);
                }
            } else {
                for (int i = fromPosition; i > toPosition; i--) {
                    Collections.swap(timetableDay, i, i - 1);
                }
            }
            Timetable.correctHours(timetableDay);
            onBindViewHolder(viewHolder, toPosition);
            onBindViewHolder(target, fromPosition);
            notifyItemMoved(fromPosition, toPosition);
            return true;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        }
    }
}
