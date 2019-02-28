package de.franziskaneum.vplan;

import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.support.v4.view.GravityCompat;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.franziskaneum.R;
import de.franziskaneum.teacher.TeacherList;
import de.franziskaneum.views.TeacherLinkTextView;

/**
 * Created by Niko on 28.04.2016.
 */
class VPlanDayRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static class VPlanDayTitleViewHolder extends RecyclerView.ViewHolder {
        TextView title;

        VPlanDayTitleViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.vplan_day_list_item_title);
            setIsRecyclable(false);
        }
    }

    private static class VPlanDayHeaderViewHolder extends RecyclerView.ViewHolder {
        TextView property;
        TeacherLinkTextView value;

        VPlanDayHeaderViewHolder(View itemView) {
            super(itemView);
            property = (TextView) itemView.findViewById(R.id.vplan_day_list_item_property);
            value = (TeacherLinkTextView) itemView.findViewById(R.id.vplan_day_list_item_value);
        }
    }

    private static class VPlanDaySubtitleViewHolder extends RecyclerView.ViewHolder {
        TextView subtitle;

        VPlanDaySubtitleViewHolder(View itemView) {
            super(itemView);
            subtitle = (TextView) itemView.findViewById(R.id.vplan_day_list_item_subtitle);
            setIsRecyclable(false);
        }
    }

    private static class VPlanDayTableRowViewHolder extends RecyclerView.ViewHolder {
        TextView schoolClass;
        TextView hour;
        TextView subject;
        TeacherLinkTextView teacher;
        TextView room;
        TeacherLinkTextView info;

        VPlanDayTableRowViewHolder(View itemView) {
            super(itemView);
            schoolClass = (TextView) itemView.findViewById(R.id.vplan_school_class);
            hour = (TextView) itemView.findViewById(R.id.vplan_hour);
            subject = (TextView) itemView.findViewById(R.id.vplan_subject);
            teacher = (TeacherLinkTextView) itemView.findViewById(R.id.vplan_teacher);
            room = (TextView) itemView.findViewById(R.id.vplan_room);
            info = (TeacherLinkTextView) itemView.findViewById(R.id.vplan_info);
        }
    }

    private static class VPlanDayExamRowViewHolder extends RecyclerView.ViewHolder {
        TextView schoolClass;
        TextView course;
        TextView hour;
        TeacherLinkTextView teacher;
        TextView startTime;
        TextView duration;
        TextView info;

        VPlanDayExamRowViewHolder(View itemView) {
            super(itemView);
            schoolClass = (TextView) itemView.findViewById(R.id.vplan_exam_school_class);
            course = (TextView) itemView.findViewById(R.id.vplan_exam_course);
            hour = (TextView) itemView.findViewById(R.id.vplan_exam_hour);
            teacher = (TeacherLinkTextView) itemView.findViewById(R.id.vplan_exam_teacher);
            startTime = (TextView) itemView.findViewById(R.id.vplan_exam_start_time);
            duration = (TextView) itemView.findViewById(R.id.vplan_exam_duration);
            info = (TextView) itemView.findViewById(R.id.vplan_exam_info);
        }
    }

    private static class VPlanDayDateViewHolder extends RecyclerView.ViewHolder {
        TextView date;

        VPlanDayDateViewHolder(View itemView) {
            super(itemView);
            date = (TextView) itemView.findViewById(R.id.vplan_day_list_item_date);
            setIsRecyclable(false);
        }
    }

    private static final int VIEW_TYPE_TITLE = 0;
    private static final int VIEW_TYPE_HEADER_LEFT_RIGHT = 1;
    private static final int VIEW_TYPE_HEADER_TOP_BOTTOM = 2;
    private static final int VIEW_TYPE_SUBTITLE = 3;
    private static final int VIEW_TYPE_TABLE_ROW = 4;
    private static final int VIEW_TYPE_EXAM_ROW = 5;
    private static final int VIEW_TYPE_DATE = 6;

    private static final int VPLAN_HEADER_ABSENT_TEACHER = 0;
    private static final int VPLAN_HEADER_ABSENT_CLASSES = 1;
    private static final int VPLAN_HEADER_NOT_AVAILABLE_ROOMS = 2;
    private static final int VPLAN_HEADER_CHANGES_TEACHER = 3;
    private static final int VPLAN_HEADER_CHANGES_CLASSES = 4;
    private static final int VPLAN_HEADER_CHANGES_SUPERVISION = 5;
    private static final int VPLAN_HEADER_ADDITIONAL_INFO = 6;

    @Nullable
    private VPlan.VPlanDayData vplanDay;
    @Nullable
    private TeacherList teacherList;

    private List<Integer> headerInformation = new ArrayList<>();

    void setVPlanDay(@Nullable VPlan.VPlanDayData vplanDay) {
        this.vplanDay = vplanDay;

        if (vplanDay != null) {
            if (vplanDay.getAbsentTeacher() != null)
                headerInformation.add(VPLAN_HEADER_ABSENT_TEACHER);
            if (vplanDay.getAbsentClasses() != null)
                headerInformation.add(VPLAN_HEADER_ABSENT_CLASSES);
            if (vplanDay.getNotAvailableRooms() != null)
                headerInformation.add(VPLAN_HEADER_NOT_AVAILABLE_ROOMS);
            if (vplanDay.getChangesTeacher() != null)
                headerInformation.add(VPLAN_HEADER_CHANGES_TEACHER);
            if (vplanDay.getChangesClasses() != null)
                headerInformation.add(VPLAN_HEADER_CHANGES_CLASSES);
            if (vplanDay.getChangesSupervision() != null)
                headerInformation.add(VPLAN_HEADER_CHANGES_SUPERVISION);
            if (vplanDay.getAdditionalInfo() != null)
                headerInformation.add(VPLAN_HEADER_ADDITIONAL_INFO);
        } else
            headerInformation.clear();

        notifyDataSetChanged();
    }

    public void setTeacherList(@Nullable TeacherList teacherList) {
        this.teacherList = teacherList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0)
            return VIEW_TYPE_TITLE;
        else if (position == getItemCount() - 1)
            return VIEW_TYPE_DATE;
        else if (vplanDay != null) {
            if (position < headerInformation.size() + 1) {
                int headerType = headerInformation.get(position - 1);

                if (headerType < 5)
                    return VIEW_TYPE_HEADER_LEFT_RIGHT;
                else
                    return VIEW_TYPE_HEADER_TOP_BOTTOM;
            } else {
                int vplanTableSize = 0;
                if (vplanDay.getTableData() != null) {
                    vplanTableSize = vplanDay.getTableData().size() + 2;

                    if (position == headerInformation.size() + 1)
                        return VIEW_TYPE_SUBTITLE;
                    else if (position < headerInformation.size() + vplanTableSize + 1)
                        return VIEW_TYPE_TABLE_ROW;
                }

                if (vplanDay.getExamData() != null) {
                    if (position >= headerInformation.size() + vplanTableSize + 1) {
                        if (position == headerInformation.size() + vplanTableSize + 1)
                            return VIEW_TYPE_SUBTITLE;
                        else if (position < getItemCount() - 1)
                            return VIEW_TYPE_EXAM_ROW;
                    }
                }
            }
        }

        return -1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View root;

        switch (viewType) {
            case VIEW_TYPE_TITLE:
                root = inflater.inflate(R.layout.vplan_day_list_item_title, parent, false);
                return new VPlanDayTitleViewHolder(root);
            case VIEW_TYPE_HEADER_LEFT_RIGHT:
                root = inflater.inflate(R.layout.vplan_day_list_item_header_left_right, parent, false);
                return new VPlanDayHeaderViewHolder(root);
            case VIEW_TYPE_HEADER_TOP_BOTTOM:
                root = inflater.inflate(R.layout.vplan_day_list_item_header_top_bottom, parent, false);
                return new VPlanDayHeaderViewHolder(root);
            case VIEW_TYPE_SUBTITLE:
                root = inflater.inflate(R.layout.vplan_day_list_item_subtitle, parent, false);
                return new VPlanDaySubtitleViewHolder(root);
            case VIEW_TYPE_TABLE_ROW:
                root = inflater.inflate(R.layout.vplan_day_list_item_table_row, parent, false);
                return new VPlanDayTableRowViewHolder(root);
            case VIEW_TYPE_EXAM_ROW:
                root = inflater.inflate(R.layout.vplan_day_list_item_exam_row, parent, false);
                return new VPlanDayExamRowViewHolder(root);
            case VIEW_TYPE_DATE:
                root = inflater.inflate(R.layout.vplan_day_list_item_date, parent, false);
                return new VPlanDayDateViewHolder(root);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (vplanDay != null) {
            if (position == 0 && holder instanceof VPlanDayTitleViewHolder) {
                VPlanDayTitleViewHolder viewHolder = (VPlanDayTitleViewHolder) holder;
                viewHolder.title.setText(vplanDay.getTitle());
            } else if (position == getItemCount() - 1 && holder instanceof VPlanDayDateViewHolder) {
                VPlanDayDateViewHolder viewHolder = (VPlanDayDateViewHolder) holder;
                viewHolder.date.setText(vplanDay.getModified());
            } else if (position < headerInformation.size() + 1 && holder instanceof VPlanDayHeaderViewHolder) {
                VPlanDayHeaderViewHolder viewHolder = (VPlanDayHeaderViewHolder) holder;
                int headerType = headerInformation.get(position - 1);
                switch (headerType) {
                    case VPLAN_HEADER_ABSENT_TEACHER:
                        viewHolder.property.setText(R.string.absent_teacher);
                        viewHolder.value.setText(vplanDay.getAbsentTeacher());
                        break;
                    case VPLAN_HEADER_ABSENT_CLASSES:
                        viewHolder.property.setText(R.string.absent_school_classes);
                        viewHolder.value.setText(vplanDay.getAbsentClasses());
                        break;
                    case VPLAN_HEADER_NOT_AVAILABLE_ROOMS:
                        viewHolder.property.setText(R.string.not_available_rooms);
                        viewHolder.value.setText(vplanDay.getNotAvailableRooms());
                        break;
                    case VPLAN_HEADER_CHANGES_TEACHER:
                        viewHolder.property.setText(R.string.teacher_with_changes);
                        viewHolder.value.setText(vplanDay.getChangesTeacher());
                        break;
                    case VPLAN_HEADER_CHANGES_CLASSES:
                        viewHolder.property.setText(R.string.classes_with_changes);
                        viewHolder.value.setText(vplanDay.getChangesClasses());
                        break;
                    case VPLAN_HEADER_CHANGES_SUPERVISION:
                        viewHolder.property.setText(R.string.changes_supervision);
                        viewHolder.value.setText(vplanDay.getChangesSupervision());
                        break;
                    case VPLAN_HEADER_ADDITIONAL_INFO:
                        viewHolder.property.setText(R.string.additional_info);
                        viewHolder.value.setText(vplanDay.getAdditionalInfo());
                        break;
                }

                if (teacherList != null) {
                    viewHolder.value.setTeacherList(teacherList);
                }
            } else {
                int vplanTableSize = 0;
                if (vplanDay.getTableData() != null) {
                    vplanTableSize = vplanDay.getTableData().size() + 2;

                    if (position == headerInformation.size() + 1 && holder instanceof VPlanDaySubtitleViewHolder) {
                        VPlanDaySubtitleViewHolder viewHolder = (VPlanDaySubtitleViewHolder) holder;
                        viewHolder.subtitle.setText(R.string.changed_lessons);
                    } else if (position < headerInformation.size() + vplanTableSize + 1 && holder instanceof VPlanDayTableRowViewHolder) {
                        VPlanDayTableRowViewHolder viewHolder = (VPlanDayTableRowViewHolder) holder;

                        if (position == headerInformation.size() + 2) {
                            viewHolder.schoolClass.setTypeface(null, Typeface.BOLD);
                            viewHolder.hour.setTypeface(null, Typeface.BOLD);
                            viewHolder.subject.setTypeface(null, Typeface.BOLD);
                            viewHolder.teacher.setTypeface(null, Typeface.BOLD);
                            viewHolder.room.setTypeface(null, Typeface.BOLD);
                            viewHolder.info.setTypeface(null, Typeface.BOLD);

                            viewHolder.schoolClass.setGravity(Gravity.CENTER);
                            viewHolder.hour.setGravity(Gravity.CENTER);
                            viewHolder.subject.setGravity(Gravity.CENTER);
                            viewHolder.teacher.setGravity(Gravity.CENTER);
                            viewHolder.room.setGravity(Gravity.CENTER);
                            viewHolder.info.setGravity(Gravity.CENTER);

                            viewHolder.schoolClass.setText(R.string.school_class_course);
                            viewHolder.hour.setText(R.string.hour);
                            viewHolder.subject.setText(R.string.subject);
                            viewHolder.teacher.setText(R.string.teacher);
                            viewHolder.room.setText(R.string.room);
                            viewHolder.info.setText(R.string.info);
                        } else {
                            VPlan.VPlanDayData.VPlanTableData tableRow = vplanDay.getTableData().get(position - headerInformation.size() - 3);

                            viewHolder.schoolClass.setTypeface(null, Typeface.NORMAL);
                            viewHolder.hour.setTypeface(null, Typeface.NORMAL);
                            viewHolder.subject.setTypeface(null, Typeface.NORMAL);
                            viewHolder.teacher.setTypeface(null, Typeface.NORMAL);
                            viewHolder.room.setTypeface(null, Typeface.NORMAL);
                            viewHolder.info.setTypeface(null, Typeface.NORMAL);

                            viewHolder.schoolClass.setGravity(GravityCompat.START | Gravity.TOP);
                            viewHolder.hour.setGravity(GravityCompat.START | Gravity.TOP);
                            viewHolder.subject.setGravity(GravityCompat.START | Gravity.TOP);
                            viewHolder.teacher.setGravity(GravityCompat.START | Gravity.TOP);
                            viewHolder.room.setGravity(GravityCompat.START | Gravity.TOP);
                            viewHolder.info.setGravity(GravityCompat.START | Gravity.TOP);

                            viewHolder.schoolClass.setText(tableRow.getSchoolClass());
                            viewHolder.hour.setText(tableRow.getHour());
                            viewHolder.subject.setText(tableRow.getSubject());
                            viewHolder.teacher.setText(tableRow.getTeacher());
                            viewHolder.room.setText(tableRow.getRoom());
                            viewHolder.info.setText(tableRow.getInfo());

                            if (teacherList != null) {
                                viewHolder.teacher.setTeacherList(teacherList);
                                viewHolder.info.setTeacherList(teacherList);
                            }
                        }
                    }
                }

                if (vplanDay.getExamData() != null) {
                    if (position >= headerInformation.size() + vplanTableSize + 1) {
                        if (position == headerInformation.size() + vplanTableSize + 1 && holder instanceof VPlanDaySubtitleViewHolder) {
                            VPlanDaySubtitleViewHolder viewHolder = (VPlanDaySubtitleViewHolder) holder;
                            viewHolder.subtitle.setText(R.string.exams);
                        } else if (position < headerInformation.size() + vplanTableSize + vplanDay.getExamData().size() + 3 && holder instanceof VPlanDayExamRowViewHolder) {
                            VPlanDayExamRowViewHolder viewHolder = (VPlanDayExamRowViewHolder) holder;

                            if (position == headerInformation.size() + vplanTableSize + 2) {
                                viewHolder.schoolClass.setTypeface(null, Typeface.BOLD);
                                viewHolder.course.setTypeface(null, Typeface.BOLD);
                                viewHolder.hour.setTypeface(null, Typeface.BOLD);
                                viewHolder.teacher.setTypeface(null, Typeface.BOLD);
                                viewHolder.startTime.setTypeface(null, Typeface.BOLD);
                                viewHolder.duration.setTypeface(null, Typeface.BOLD);
                                viewHolder.info.setTypeface(null, Typeface.BOLD);

                                viewHolder.schoolClass.setGravity(Gravity.CENTER);
                                viewHolder.course.setGravity(Gravity.CENTER);
                                viewHolder.hour.setGravity(Gravity.CENTER);
                                viewHolder.teacher.setGravity(Gravity.CENTER);
                                viewHolder.startTime.setGravity(Gravity.CENTER);
                                viewHolder.duration.setGravity(Gravity.CENTER);
                                viewHolder.info.setGravity(Gravity.CENTER);

                                viewHolder.schoolClass.setText(R.string.exam_school_class);
                                viewHolder.course.setText(R.string.course);
                                viewHolder.hour.setText(R.string.hour);
                                viewHolder.teacher.setText(R.string.course_leader);
                                viewHolder.startTime.setText(R.string.start_time);
                                viewHolder.duration.setText(R.string.duration);
                                viewHolder.info.setText(R.string.info);
                            } else {
                                VPlan.VPlanDayData.VPlanExamData examData = vplanDay.getExamData().get(position - headerInformation.size() - vplanTableSize - 3);

                                viewHolder.schoolClass.setTypeface(null, Typeface.NORMAL);
                                viewHolder.course.setTypeface(null, Typeface.NORMAL);
                                viewHolder.hour.setTypeface(null, Typeface.NORMAL);
                                viewHolder.teacher.setTypeface(null, Typeface.NORMAL);
                                viewHolder.startTime.setTypeface(null, Typeface.NORMAL);
                                viewHolder.duration.setTypeface(null, Typeface.NORMAL);
                                viewHolder.info.setTypeface(null, Typeface.NORMAL);

                                viewHolder.schoolClass.setGravity(GravityCompat.START | Gravity.TOP);
                                viewHolder.course.setGravity(GravityCompat.START | Gravity.TOP);
                                viewHolder.hour.setGravity(GravityCompat.START | Gravity.TOP);
                                viewHolder.teacher.setGravity(GravityCompat.START | Gravity.TOP);
                                viewHolder.startTime.setGravity(GravityCompat.START | Gravity.TOP);
                                viewHolder.duration.setGravity(GravityCompat.START | Gravity.TOP);
                                viewHolder.info.setGravity(GravityCompat.START | Gravity.TOP);

                                viewHolder.schoolClass.setText(examData.getSchoolClass());
                                viewHolder.course.setText(examData.getCourse());
                                viewHolder.hour.setText(examData.getHour());
                                viewHolder.teacher.setText(examData.getTeacher());
                                viewHolder.startTime.setText(examData.getStartTime());
                                viewHolder.duration.setText(examData.getDuration());
                                viewHolder.info.setText(examData.getInfo());

                                if (teacherList != null)
                                    viewHolder.teacher.setTeacherList(teacherList);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        int count = 0;

        if (vplanDay != null) {
            count += 2;
            count += headerInformation.size();

            if (vplanDay.getTableData() != null && !vplanDay.getTableData().isEmpty())
                count += vplanDay.getTableData().size() + 2;

            if (vplanDay.getExamData() != null && !vplanDay.getExamData().isEmpty())
                count += vplanDay.getExamData().size() + 2;
        }

        return count;
    }
}
