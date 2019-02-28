package de.franziskaneum.timetable;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import de.franziskaneum.Constants;
import de.franziskaneum.R;
import de.franziskaneum.settings.SettingsManager;
import de.franziskaneum.utils.ClickableViewHolder;

/**
 * Created by Niko on 16.02.2016.
 */
public class TimetableDayRecyclerAdapter extends RecyclerView.Adapter<TimetableDayRecyclerAdapter.TimetableSingleHourViewHolder> {

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

    private List<Timetable.TimetableData> timetableDay;
    private boolean showTimes = false;
    private SettingsManager settings;
    private int schoolClassStep = 5;
    private RecyclerView recyclerView;
    private int dayIndex = 0;
    private Fragment fragment;

    public class TimetableSingleHourViewHolder extends ClickableViewHolder {
        public View contentContainer;
        public TextView hour;
        public TextView subject;
        public TextView room;
        public TextView teacherOrSchoolClass;
        public View timesContainer;
        public TextView startTime;
        public TextView endTime;

        public TimetableSingleHourViewHolder(final View itemView) {
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

        @Override
        public void onClick(final int position, @Nullable final Context context) {
            if (context != null && timetableDay != null && timetableDay.size() > position) {
                final Timetable.TimetableData timetableData = timetableDay.get(position);
                AlertDialog.Builder builder = new AlertDialog.Builder(context,
                        R.style.AlertDialogTheme);

                // inflate the dialog main view
                View dialogView = LayoutInflater.from(context).
                        inflate(R.layout.dialog_timetable_detail, null);

                // set the view to the dialog builder
                builder.setView(dialogView);

                ((TextView) dialogView.findViewById(R.id.dialog_timetable_detail_title))
                        .setText(timetableData.getHour() + "." + context.getString(R.string.hour));
                ((TextView) dialogView.findViewById(R.id.dialog_timetable_detail_subject))
                        .setText(timetableData.getSubject());
                ((TextView) dialogView.findViewById(R.id.dialog_timetable_detail_room))
                        .setText(timetableData.getRoom());
                ((TextView) dialogView.findViewById(R.id.dialog_timetable_detail_teacher))
                        .setText(timetableData.getTeacherOrSchoolClass());

                final AlertDialog dialog = builder.create();

                dialogView.findViewById(R.id.dialog_timetable_detail_edit)
                        .setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();

                                Calendar selectedDay = Calendar.getInstance();
                                selectedDay.set(Calendar.DAY_OF_WEEK, 2 + dayIndex);

                                Intent editSubjectIntent = new Intent(context,
                                        TimetableEditSubjectActivity.class);
                                editSubjectIntent.putExtra(
                                        TimetableEditSubjectActivity.EXTRA_DAY_OF_WEEK,
                                        selectedDay.getDisplayName(Calendar.DAY_OF_WEEK,
                                                Calendar.LONG, Locale.getDefault()));
                                editSubjectIntent.putExtra(TimetableEditSubjectActivity.EXTRA_HOUR,
                                        Timetable.getHourForIndex(timetableDay, position));
                                editSubjectIntent.putExtra(
                                        TimetableEditSubjectActivity.EXTRA_SUBJECT_TO_EDIT,
                                        timetableData);

                                // workaround for not called onActivityResult in nested Fragments (Android Bug)
                                fragment.getParentFragment().startActivityForResult(editSubjectIntent,
                                        Constants.ACTIVITY_REQUEST_CODE_TIMETABLE_EDIT_SUBJECT);
                            }

                        });

                dialogView.findViewById(R.id.dialog_timetable_detail_delete)
                        .setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();

                                timetableDay.remove(position);
                                Timetable.correctHours(timetableDay);

                                notifyDataSetChanged();
                            }

                        });

                dialog.show();
            }
        }

    }

    public class TimetableDoubleHourViewHolder extends TimetableSingleHourViewHolder {
        public TextView hour2;

        public TimetableDoubleHourViewHolder(View itemView) {
            super(itemView);

            contentContainer = itemView.findViewById(R.id.timetable_item_content_container);
            hour = (TextView) itemView.findViewById(R.id.timetable_item_hour1);
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

    public TimetableDayRecyclerAdapter(RecyclerView recyclerView, int dayIndex, Fragment fragment) {
        settings = SettingsManager.getInstance();
        schoolClassStep = settings.getSchoolClassStep();
        this.recyclerView = recyclerView;
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHandlerCallback());
        itemTouchHelper.attachToRecyclerView(recyclerView);
        this.dayIndex = dayIndex;
        this.fragment = fragment;
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
    public TimetableSingleHourViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
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
    public void onBindViewHolder(TimetableSingleHourViewHolder holder, int position) {
        String freehour = holder.itemView.getContext().getString(R.string._freehour_);

        if (timetableDay != null && timetableDay.size() > position) {
            Timetable.TimetableData timetableData = timetableDay.get(position);

            holder.hour.setText(timetableData.getHour() + ".");
            String subject = timetableData.getSubject();

            holder.subject.setText(timetableData.isFreehour() ? freehour : subject);
            holder.room.setText(timetableData.getRoom());
            holder.teacherOrSchoolClass.setText(timetableData.getTeacherOrSchoolClass());
            holder.timesContainer.setVisibility(showTimes ? View.VISIBLE : View.GONE);

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
                holder.startTime.setText(subjectTimes[0]);
                holder.endTime.setText(subjectTimes[1]);
            }


            if (holder instanceof TimetableDoubleHourViewHolder) {
                TimetableDoubleHourViewHolder viewHolder = (TimetableDoubleHourViewHolder) holder;
                viewHolder.hour2.setText((timetableData.getHour() + 1) + ".");

                if (hourIndex + 1 < times.length) {
                    String[] subjectTimes1 = times[hourIndex];
                    String[] subjectTimes2 = times[hourIndex + 1];
                    viewHolder.startTime.setText(subjectTimes1[0]);
                    viewHolder.endTime.setText(subjectTimes2[1]);
                }
            } else {
                if (hourIndex < times.length) {
                    String[] subjectTimes = times[hourIndex];
                    holder.startTime.setText(subjectTimes[0]);
                    holder.endTime.setText(subjectTimes[1]);
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
            onBindViewHolder((TimetableSingleHourViewHolder) viewHolder, toPosition);
            onBindViewHolder((TimetableSingleHourViewHolder) target, fromPosition);
            notifyItemMoved(fromPosition, toPosition);
            return true;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        }
    }
}
