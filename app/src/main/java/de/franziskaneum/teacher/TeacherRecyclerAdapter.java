package de.franziskaneum.teacher;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;

import de.franziskaneum.R;

/**
 * Created by Niko on 23.02.2016.
 */
public class TeacherRecyclerAdapter extends RecyclerView.Adapter<TeacherRecyclerAdapter.TeacherViewHolder> {

    private TeacherList teacherList;
    private View.OnClickListener onClickListener;

    public static class TeacherViewHolder extends RecyclerView.ViewHolder {

        public ImageView image;
        public TextView name;
        public TextView subjects;

        public TeacherViewHolder(View itemView) {
            super(itemView);

            name = (TextView) itemView.findViewById(R.id.teacher_item_name);
            subjects = (TextView) itemView.findViewById(R.id.teacher_item_subjects);
            image = (ImageView) itemView.findViewById(R.id.teacher_item_image);
        }

    }

    public TeacherRecyclerAdapter(@Nullable View.OnClickListener onClickListener) {
        super();
        this.onClickListener = onClickListener;
    }

    public void setTeacherList(TeacherList teacherList) {
        this.teacherList = teacherList;
        notifyDataSetChanged();
    }

    @Override
    public TeacherViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new TeacherViewHolder(inflater.inflate(R.layout.teacher_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(TeacherViewHolder holder, int position) {
        if (position < teacherList.size()) {
            TeacherList.TeacherData teacher = teacherList.get(position);

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
                            teacher.getShortcut(), teacher.getColor());
            holder.image.setImageDrawable(textDrawable);

            if (onClickListener != null) {
                holder.itemView.setOnClickListener(onClickListener);
                holder.itemView.setTag(R.string.recycler_item_position, position);
            }
        }
    }

    @Override
    public int getItemCount() {
        return teacherList == null ? 0 : teacherList.size();
    }
}
