package de.franziskaneum.timetable;

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
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Niko on 16.02.2016.
 */
public class Timetable extends ArrayList<List<List<Timetable.TimetableData>>> {

    public Timetable() {
        super();
    }

    @Nullable
    static Timetable readFromFile(File file) {
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
            return gson.fromJson(json, Timetable.class);
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

    static void writeToFile(@Nullable Timetable timetable, File file) {
        Gson gson = new Gson();
        String s = gson.toJson(timetable);

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

    static int getHourForIndex(@NonNull List<TimetableData> timetableDay, int index) {
        int hourCount = 1;

        for (int i = 0; i < timetableDay.size(); i++) {
            if (i == index)
                return hourCount;

            if (timetableDay.get(i).isDoubleHour)
                hourCount += 2;
            else
                hourCount += 1;
        }

        return hourCount;
    }

    static int getIndexForHour(@NonNull List<TimetableData> timetableDay, int hour) {
        for (int i = 0; i < timetableDay.size(); i++) {
            TimetableData timetableData = timetableDay.get(i);

            if (timetableData.hour == hour ||
                    (timetableData.isDoubleHour && timetableData.hour + 1 == hour))
                return i;
        }

        return timetableDay.size();
    }

    public static void correctHours(@NonNull List<TimetableData> timetableDay) {
        int hourCount = 1;

        for (TimetableData timetableData : timetableDay) {
            timetableData.hour = hourCount;

            hourCount += timetableData.isDoubleHour ? 2 : 1;
        }
    }

    public boolean hasCourse(@Nullable String course) {
        if (course == null || course.isEmpty())
            return false;

        for (List<List<TimetableData>> timetableWeek : this) {
            for (List<TimetableData> timetableDay : timetableWeek) {
                for (TimetableData subject : timetableDay) {
                    if (subject.subject != null) {
                        String trimmedSubject = subject.subject.trim();
                        if (!trimmedSubject.isEmpty() && course.contains(trimmedSubject))
                            return true;
                    }
                }
            }
        }

        return false;
    }

    public static class TimetableData implements Parcelable {

        private int hour;
        @Nullable
        private String subject;
        @Nullable
        private String room;
        @Nullable
        private String teacherOrSchoolClass;
        private boolean isDoubleHour;

        public TimetableData() {
        }

        ;

        public TimetableData(Parcel in) {
            hour = in.readInt();
            subject = in.readString();
            room = in.readString();
            teacherOrSchoolClass = in.readString();
            isDoubleHour = in.readInt() == 1;
        }

        public int getHour() {
            return hour;
        }

        public void setHour(int hour) {
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
        public String getRoom() {
            return room;
        }

        public void setRoom(@Nullable String room) {
            this.room = room;
        }

        @Nullable
        public String getTeacherOrSchoolClass() {
            return teacherOrSchoolClass;
        }

        public void setTeacherOrSchoolClass(@Nullable String teacherOrSchoolClass) {
            this.teacherOrSchoolClass = teacherOrSchoolClass;
        }

        public boolean isDoubleHour() {
            return isDoubleHour;
        }

        public void setIsDoubleHour(boolean isDoubleHour) {
            this.isDoubleHour = isDoubleHour;
        }

        @Override
        public String toString() {
            return getHour() + ". " + getSubject() + " " + getRoom() + " " +
                    getTeacherOrSchoolClass() + " " + isDoubleHour;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeInt(hour);
            parcel.writeString(subject);
            parcel.writeString(room);
            parcel.writeString(teacherOrSchoolClass);
            parcel.writeInt(isDoubleHour ? 1 : 0);
        }

        public static final Parcelable.Creator<TimetableData> CREATOR = new Parcelable.Creator<TimetableData>() {

            @Override
            public TimetableData createFromParcel(Parcel source) {
                return new TimetableData(source);
            }

            @Override
            public TimetableData[] newArray(int size) {
                return new TimetableData[size];
            }
        };

        public TimetableData cloneContent() {
            TimetableData clone = new TimetableData();
            clone.hour = hour;
            clone.subject = subject;
            clone.room = room;
            clone.teacherOrSchoolClass = teacherOrSchoolClass;
            clone.isDoubleHour = isDoubleHour;

            return clone;
        }

    }

}
