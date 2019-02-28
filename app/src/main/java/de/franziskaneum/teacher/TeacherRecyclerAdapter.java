package de.franziskaneum.teacher;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;

import de.franziskaneum.R;
import de.franziskaneum.utils.ClickableViewHolder;

/**
 * Created by Niko on 23.02.2016.
 */
public class TeacherRecyclerAdapter extends RecyclerView.Adapter<TeacherRecyclerAdapter.TeacherViewHolder> implements Filterable {

    private TeacherList teacherList;
    private TeacherList filteredTeacherList;
    private TeacherFilter filter;

    public class TeacherViewHolder extends ClickableViewHolder {

        public ImageView image;
        public TextView name;
        public TextView subjects;

        public TeacherViewHolder(View itemView) {
            super(itemView);

            name = (TextView) itemView.findViewById(R.id.teacher_item_name);
            subjects = (TextView) itemView.findViewById(R.id.teacher_item_subjects);
            image = (ImageView) itemView.findViewById(R.id.teacher_item_image);
        }

        @Override
        public void onClick(int position, @Nullable Context context) {
            if (filteredTeacherList != null && position < filteredTeacherList.size() && context != null) {
                TeacherList.TeacherData teacher = filteredTeacherList.get(position);

                Intent teacherDetailIntent = new Intent(context, TeacherDetailActivity.class);
                teacherDetailIntent.putExtra(TeacherDetailActivity.EXTRA_TEACHER, teacher);
                context.startActivity(teacherDetailIntent);
            }
        }
    }

    public TeacherRecyclerAdapter() {
        super();
        filter = new TeacherFilter();
    }

    public void setTeacherList(TeacherList teacherList) {
        this.teacherList = teacherList;
        this.filteredTeacherList = teacherList;
        notifyDataSetChanged();
    }

    @Override
    public TeacherViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new TeacherViewHolder(inflater.inflate(R.layout.teacher_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(TeacherViewHolder holder, int position) {
        if (position < filteredTeacherList.size()) {
            TeacherList.TeacherData teacher = filteredTeacherList.get(position);

            holder.name.setText(teacher.getForename() + " " + teacher.getName());

            String subjects = null;
            if (teacher.getSubjects() != null)
                for (String subject : teacher.getSubjects()) {
                    if (subjects == null) {
                        subjects = subject;
                    } else
                        subjects += ", " + subject;
                }

            holder.subjects.setText(subjects);

            int textSize = (int) (18.0f * holder.itemView.getContext()
                    .getResources().getDisplayMetrics().density);

            TextDrawable textDrawable = TextDrawable
                    .builder()
                    .beginConfig()
                    .bold()
                    .fontSize(textSize)
                    .endConfig()
                    .buildRound(
                            teacher.getShortcut() == null ? "" : teacher.getShortcut(), teacher.getColor());
            holder.image.setImageDrawable(textDrawable);
        }
    }

    @Override
    public int getItemCount() {
        return filteredTeacherList == null ? 0 : filteredTeacherList.size();
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    private class TeacherFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();

            if (teacherList != null)
                if (constraint == null || constraint.length() == 0) {
                    results.values = teacherList;
                    results.count = teacherList.size();
                } else {
                    TeacherList filtered = new TeacherList();

                    for (TeacherList.TeacherData teacher : teacherList) {
                        StringBuilder teacherStringBuilder = new StringBuilder();
                        teacherStringBuilder.append(teacher.getForename());
                        teacherStringBuilder.append(" ");
                        teacherStringBuilder.append(teacher.getName());
                        teacherStringBuilder.append(" ");
                        teacherStringBuilder.append(teacher.getShortcut());
                        teacherStringBuilder.append(" ");

                        if (teacher.getSubjects() != null)
                            for (String subject : teacher.getSubjects()) {
                                teacherStringBuilder.append(subject);
                                teacherStringBuilder.append(" ");
                            }

                        if (teacher.getSpecificTasks() != null)
                            for (String specificTask : teacher.getSpecificTasks()) {
                                teacherStringBuilder.append(specificTask);
                                teacherStringBuilder.append(" ");
                            }

                        String teacherString = teacherStringBuilder.toString().toLowerCase();
                        String[] keyWords = constraint.toString().toLowerCase().split(" ");
                        boolean contains = true;

                        for (String keyWord : keyWords) {
                            if (!teacherString.contains(keyWord)) {
                                contains = false;
                                break;
                            }
                        }

                        if (contains)
                            filtered.add(teacher);
                    }

                    results.values = filtered;
                    results.count = filtered.size();
                }

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            if (results.values instanceof TeacherList) {
                filteredTeacherList = (TeacherList) results.values;
                notifyDataSetChanged();
            }
        }

    }
}
