package de.franziskaneum.vplan;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import de.franziskaneum.R;
import de.franziskaneum.settings.SettingsManager;

/**
 * Created by Niko on 20.12.2015.
 */
public class VPlan extends ArrayList<VPlan.VPlanDayData> implements Parcelable { // implement Parcelable to prevent Exception when casting to Parcelable
    public static final String EXTRA_VPLAN = "de.franziskaneum.vplan.extra.VPLAN";

    public VPlan() {
        super();
    }

    private VPlan(Parcel in) {
        super(in.createTypedArrayList(VPlanDayData.CREATOR));
    }

    public static final Creator<VPlan> CREATOR = new Creator<VPlan>() {
        @Override
        public VPlan createFromParcel(Parcel in) {
            return new VPlan(in);
        }

        @Override
        public VPlan[] newArray(int size) {
            return new VPlan[size];
        }
    };

    @Nullable
    static VPlan readFromFile(File file) {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            fis.close();

            String json = sb.toString();
            Gson gson = new Gson();
            return gson.fromJson(json, VPlan.class);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null)
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }

        return null;
    }

    static void writeToFile(@Nullable VPlan vplan, File file) {
        Gson gson = new Gson();
        String s = gson.toJson(vplan);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            fos.write(s.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null)
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

    VPlan cloneContent() {
        VPlan clone = new VPlan();
        for (VPlanDayData vplanDay : this) {
            clone.add(vplanDay.cloneContent());
        }

        return clone;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeTypedList(this);
    }


    public static class VPlanDayData implements Parcelable {

        @Nullable
        private String title;
        @Nullable
        private String absentTeacher;
        @Nullable
        private String absentClasses;
        @Nullable
        private String notAvailableRooms;
        @Nullable
        private String changesTeacher;
        @Nullable
        private String changesClasses;
        @Nullable
        private StringBuilder changesSupervision;
        @Nullable
        private StringBuilder additionalInfo;
        @Nullable
        private String modified;
        @Nullable
        private List<VPlanTableData> tableData;
        @Nullable
        private List<VPlanExamData> examData;

        public VPlanDayData() {
            super();
        }

        public VPlanDayData(Parcel in) {
            String tmp;

            title = in.readString();
            absentTeacher = in.readString();
            absentClasses = in.readString();
            notAvailableRooms = in.readString();
            changesTeacher = in.readString();
            changesClasses = in.readString();
            changesSupervision = (tmp = in.readString()) == null ? null : new StringBuilder(tmp);
            additionalInfo = (tmp = in.readString()) == null ? null : new StringBuilder(tmp);
            modified = in.readString();
            tableData = in.createTypedArrayList(VPlanTableData.CREATOR);
            examData = in.createTypedArrayList(VPlanExamData.CREATOR);
        }

        @Nullable
        public String getTitle() {
            return title;
        }

        public void setTitle(@Nullable String title) {
            this.title = title;
        }

        @Nullable
        String getAbsentTeacher() {
            return absentTeacher;
        }

        void setAbsentTeacher(@Nullable String absentTeacher) {
            this.absentTeacher = absentTeacher;
        }

        @Nullable
        String getAbsentClasses() {
            return absentClasses;
        }

        void setAbsentClasses(@Nullable String absentClasses) {
            this.absentClasses = absentClasses;
        }

        @Nullable
        String getNotAvailableRooms() {
            return notAvailableRooms;
        }

        void setNotAvailableRooms(@Nullable String notAvailableRooms) {
            this.notAvailableRooms = notAvailableRooms;
        }

        @Nullable
        String getChangesTeacher() {
            return changesTeacher;
        }

        void setChangesTeacher(@Nullable String changesTeacher) {
            this.changesTeacher = changesTeacher;
        }

        @Nullable
        String getChangesClasses() {
            return changesClasses;
        }

        void setChangesClasses(@Nullable String changesClasses) {
            this.changesClasses = changesClasses;
        }

        @Nullable
        String getChangesSupervision() {
            return changesSupervision != null ? changesSupervision.toString() : null;
        }

        void setChangesSupervision(@Nullable String changesSupervision) {
            if (changesSupervision != null) {
                this.changesSupervision = new StringBuilder(changesSupervision.trim());
            }
        }

        void addChangesSupervision(@Nullable String changesSupervision) {
            if (changesSupervision != null) {
                if (this.changesSupervision == null)
                    this.changesSupervision = new StringBuilder(changesSupervision.trim());
                else {
                    this.changesSupervision.append("\n");
                    this.changesSupervision.append(changesSupervision.trim());
                }
            }
        }

        @Nullable
        String getAdditionalInfo() {
            return additionalInfo != null ? additionalInfo.toString() : null;
        }

        void setAdditionalInfo(@Nullable String additionalInfo) {
            if (additionalInfo != null) {
                this.additionalInfo = new StringBuilder(additionalInfo);
            }
        }

        void addAdditionalInfo(@Nullable String additionalInfo) {
            if (additionalInfo != null) {
                if (this.additionalInfo == null)
                    this.additionalInfo = new StringBuilder(additionalInfo.trim());
                else {
                    this.additionalInfo.append("\n");
                    this.additionalInfo.append(additionalInfo.trim());
                }
            }
        }

        @Nullable
        public String getModified() {
            return modified;
        }

        public void setModified(@Nullable String modified) {
            this.modified = modified;
        }

        @Nullable
        public List<VPlanTableData> getTableData() {
            return tableData;
        }

        public void setTableData(@Nullable List<VPlanTableData> tableData) {
            this.tableData = tableData;
        }

        void addTableData(@Nullable VPlanTableData tableData) {
            if (tableData != null) {
                if (this.tableData == null)
                    this.tableData = new ArrayList<>();

                this.tableData.add(tableData);
            }
        }

        void removeTableData(int index) {
            if (tableData != null) {
                tableData.remove(index);

                if (tableData.size() == 0)
                    tableData = null;
            }
        }

        @Nullable
        List<VPlanExamData> getExamData() {
            return examData;
        }

        public void setExamData(@Nullable List<VPlanExamData> examData) {
            this.examData = examData;
        }

        void addExamData(@Nullable VPlanExamData examData) {
            if (examData != null) {
                if (this.examData == null)
                    this.examData = new ArrayList<>();

                this.examData.add(examData);
            }
        }

        void removeExamData(int index) {
            if (examData != null) {
                examData.remove(index);

                if (examData.size() == 0)
                    examData = null;
            }
        }

        @Nullable
        public Calendar getCalendarDate() {
            SimpleDateFormat sdf = new SimpleDateFormat("cccc, d. MMMM yyyy",
                    Locale.GERMANY);
            Calendar date = Calendar.getInstance();
            try {
                date.setTime(sdf.parse(title));
            } catch (ParseException e) {
                e.printStackTrace();
                date = null;
            }

            return date;
        }

        @Nullable
        public String getNameOfDay() {
            if (title != null && title.contains(",")) {
                return title.substring(0, title.indexOf(","));
            } else
                return null;
        }

        public int getCurrentWeek() {
            if (title != null && title.toLowerCase().contains("b-woche"))
                return 1;

            return 0;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(title);
            dest.writeString(absentTeacher);
            dest.writeString(absentClasses);
            dest.writeString(notAvailableRooms);
            dest.writeString(changesTeacher);
            dest.writeString(changesClasses);
            dest.writeString(changesSupervision != null ? changesSupervision.toString() : null);
            dest.writeString(additionalInfo != null ? additionalInfo.toString() : null);
            dest.writeString(modified);
            dest.writeTypedList(tableData);
            dest.writeTypedList(examData);
        }

        public static final Parcelable.Creator<VPlanDayData> CREATOR = new Parcelable.Creator<VPlanDayData>() {

            @Override
            public VPlanDayData createFromParcel(Parcel source) {
                return new VPlanDayData(source);
            }

            @Override
            public VPlanDayData[] newArray(int size) {
                return new VPlanDayData[size];
            }
        };

        VPlanDayData cloneContent() {
            VPlanDayData clone = new VPlanDayData();
            clone.setTitle(title);
            clone.setAbsentTeacher(absentTeacher);
            clone.setAbsentClasses(absentClasses);
            clone.setNotAvailableRooms(notAvailableRooms);
            clone.setChangesTeacher(changesTeacher);
            clone.setChangesClasses(changesClasses);
            clone.setChangesSupervision(getChangesSupervision());
            clone.setAdditionalInfo(getAdditionalInfo());
            clone.setModified(modified);

            if (tableData != null)
                for (VPlanTableData vplanTableRow : tableData) {
                    clone.addTableData(vplanTableRow.clone());
                }

            if (examData != null)
                for (VPlanExamData vPlanExamData : examData) {
                    clone.addExamData(vPlanExamData.clone());
                }

            return clone;
        }

        public static class VPlanTableData implements Parcelable, Cloneable {

            @Nullable
            private String schoolClass;
            @Nullable
            private String hour;
            @Nullable
            private String subject;
            @Nullable
            private String teacher;
            @Nullable
            private String room;
            @Nullable
            private String info;

            public VPlanTableData() {
            }

            public VPlanTableData(Parcel in) {
                schoolClass = in.readString();
                hour = in.readString();
                subject = in.readString();
                teacher = in.readString();
                room = in.readString();
                info = in.readString();
            }

            @Nullable
            public String getSchoolClass() {
                return schoolClass;
            }

            public void setSchoolClass(@Nullable String schoolClass) {
                this.schoolClass = schoolClass;
            }

            @Nullable
            public String getHour() {
                return hour;
            }

            public void setHour(@Nullable String hour) {
                this.hour = hour;
            }

            @Nullable
            public String getSubject() {
                return subject;
            }

            public void setSubject(@Nullable String subject) {
                this.subject = subject;
            }

            @Nullable
            public String getTeacher() {
                return teacher;
            }

            public void setTeacher(@Nullable String teacher) {
                this.teacher = teacher;
            }

            @Nullable
            public String getRoom() {
                return room;
            }

            public void setRoom(@Nullable String room) {
                this.room = room;
            }

            @Nullable
            public String getInfo() {
                return info;
            }

            public void setInfo(@Nullable String info) {
                this.info = info;
            }

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {
                dest.writeString(schoolClass);
                dest.writeString(hour);
                dest.writeString(subject);
                dest.writeString(teacher);
                dest.writeString(room);
                dest.writeString(info);
            }

            public static final Parcelable.Creator<VPlanTableData> CREATOR = new Parcelable.Creator<VPlanTableData>() {

                @Override
                public VPlanTableData createFromParcel(Parcel source) {
                    return new VPlanTableData(source);
                }

                @Override
                public VPlanTableData[] newArray(int size) {
                    return new VPlanTableData[size];
                }
            };

            public VPlanTableData clone() {
                try {
                    return (VPlanTableData) super.clone();
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                }

                return null;
            }

            @NonNull
            String getNotificationText(Context context) {
                if (SettingsManager.getInstance().isTeacher())
                    return String
                            .format(context.getString(R.string.hour_subject_teacher_or_school_class_room_info),
                                    getHour(), getSubject(), getSchoolClass(), getRoom(), getInfo())
                            .replaceAll("--- ", "")
                            .replaceAll("---", "");

                return String
                        .format(context.getString(R.string.hour_subject_teacher_or_school_class_room_info),
                                getHour(), getSubject(), getTeacher(), getRoom(), getInfo())
                        .replaceAll("--- ", "")
                        .replaceAll("---", "");
            }

            @Override
            public boolean equals(Object o) {
                if (o instanceof VPlanTableData) {
                    VPlanTableData rhs = (VPlanTableData) o;

                    return (rhs.schoolClass != null && schoolClass != null &&
                            rhs.schoolClass.equals(schoolClass)) && (rhs.hour != null &&
                            hour != null && rhs.hour.equals(hour)) && (rhs.subject != null &&
                            subject != null && rhs.subject.equals(subject)) &&
                            (rhs.teacher != null && teacher != null && rhs.teacher.equals(teacher)) &&
                            (rhs.room != null && room != null && rhs.room.equals(room)) &&
                            (rhs.info != null && info != null && rhs.info.equals(info));
                }

                return false;
            }
        }

        static class VPlanExamData implements Parcelable, Cloneable {
            @Nullable
            private String schoolClass;
            @Nullable
            private String course;
            @Nullable
            private String teacher;
            @Nullable
            private String hour;
            @Nullable
            private String startTime;
            @Nullable
            private String duration;
            @Nullable
            private String info;

            public VPlanExamData() {
            }

            public VPlanExamData(Parcel in) {
                schoolClass = in.readString();
                course = in.readString();
                teacher = in.readString();
                hour = in.readString();
                startTime = in.readString();
                duration = in.readString();
                info = in.readString();
            }

            @Nullable
            public String getSchoolClass() {
                return schoolClass;
            }

            public void setSchoolClass(@Nullable String schoolClass) {
                this.schoolClass = schoolClass;
            }

            @Nullable
            public String getCourse() {
                return course;
            }

            public void setCourse(@Nullable String course) {
                this.course = course;
            }

            @Nullable
            public String getTeacher() {
                return teacher;
            }

            public void setTeacher(@Nullable String teacher) {
                this.teacher = teacher;
            }

            @Nullable
            public String getHour() {
                return hour;
            }

            public void setHour(@Nullable String hour) {
                this.hour = hour;
            }

            @Nullable
            public String getStartTime() {
                return startTime;
            }

            public void setStartTime(@Nullable String startTime) {
                this.startTime = startTime;
            }

            @Nullable
            public String getDuration() {
                return duration;
            }

            public void setDuration(@Nullable String duration) {
                this.duration = duration;
            }

            @Nullable
            public String getInfo() {
                return info;
            }

            public void setInfo(@Nullable String info) {
                this.info = info;
            }

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {
                dest.writeString(schoolClass);
                dest.writeString(course);
                dest.writeString(teacher);
                dest.writeString(hour);
                dest.writeString(startTime);
                dest.writeString(duration);
                dest.writeString(info);
            }

            public static final Parcelable.Creator<VPlanExamData> CREATOR = new Parcelable.Creator<VPlanExamData>() {

                @Override
                public VPlanExamData createFromParcel(Parcel source) {
                    return new VPlanExamData(source);
                }

                @Override
                public VPlanExamData[] newArray(int size) {
                    return new VPlanExamData[size];
                }
            };

            public VPlanExamData clone() {
                try {
                    return (VPlanExamData) super.clone();
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                }

                return null;
            }
        }
    }

}
