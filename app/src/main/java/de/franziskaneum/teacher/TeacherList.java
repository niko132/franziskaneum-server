package de.franziskaneum.teacher;

import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by Niko on 23.02.2016.
 */
public class TeacherList extends ArrayList<TeacherList.TeacherData> {

    public TeacherList() {
        super();
    }

    public TeacherList(TeacherList teacherList) {
        super(teacherList);
    }

    @Nullable
    static TeacherList readFromFile(File file) {
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
            return gson.fromJson(json, TeacherList.class);
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

    static void writeToFile(@Nullable TeacherList teacherList, File file) {
        Gson gson = new Gson();
        String s = gson.toJson(teacherList);

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

    boolean existsTeacherWithSameName(TeacherData teacher) {
        for (TeacherData teacherData : this) {
            if (teacherData.sameName(teacher) && !teacherData.equals(teacher))
                return true;
        }

        return false;
    }

    public static class TeacherData implements Parcelable {

        private String name;
        private String forename;
        private String shortcut;
        private String[] subjects;
        private String[] specificTasks;

        public TeacherData() {
            super();
        }

        TeacherData(Parcel in) {
            name = in.readString();
            forename = in.readString();
            shortcut = in.readString();
            subjects = in.createStringArray();
            specificTasks = in.createStringArray();
        }

        @Nullable
        public String getName() {
            return name;
        }

        public void setName(@Nullable String name) {
            this.name = name;
        }

        @Nullable
        public String getForename() {
            return forename;
        }

        void setForename(@Nullable String forename) {
            this.forename = forename;
        }

        @Nullable
        public String getShortcut() {
            return shortcut;
        }

        public void setShortcut(@Nullable String shortcut) {
            this.shortcut = shortcut;
        }

        @Nullable
        public String[] getSubjects() {
            return subjects;
        }

        void setSubjects(@Nullable String[] subjects) {
            this.subjects = subjects;
        }

        @Nullable
        public String[] getSpecificTasks() {
            return specificTasks;
        }

        void setSpecificTasks(@Nullable String[] specificTasks) {
            this.specificTasks = specificTasks;
        }

        public int getColor() {
            String string = getForename() + getName();

            int totalHue = 360;

            for (int i = string.length() - 1; i >= 0; i--) {
                totalHue += (int) (string.charAt(i));
            }

            float[] hsv = {(totalHue % 360), 0.66f, 0.66f};

            return Color.HSVToColor(hsv);
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof TeacherData) {
                TeacherData rhs = (TeacherData) o;

                return (forename != null && !forename.isEmpty() && forename.equals(rhs.forename))
                        && (name != null && !name.isEmpty() && name.equals(rhs.name));
            }

            return false;
        }

        boolean sameName(TeacherData rhs) {
            if (rhs != null)
                if (name != null && !name.isEmpty() && name.equals(rhs.name))
                    return true;

            return false;
        }

        @Nullable
        public static int[] teacherShortcutInString(@Nullable String string,
                                                    @Nullable String teacherShortcut) {
            return teacherShortcutInString(string, 0, teacherShortcut);
        }

        @Nullable
        static int[] teacherShortcutInString(@Nullable String string, int start,
                                             @Nullable String teacherShortcut) {
            if (string != null && teacherShortcut != null) {
                int teacherShortcutIndex = string.indexOf(teacherShortcut, start);
                int end = teacherShortcutIndex + teacherShortcut.length();

                if (teacherShortcutIndex != -1)
                    if ((teacherShortcutIndex == 0 ||
                            (!Character.isLetter(string.charAt(teacherShortcutIndex - 1)) &&
                                    string.charAt(teacherShortcutIndex - 1) != '.')) &&
                            (end == string.length() || (!Character.isLetter(string.charAt(end)) &&
                                    string.charAt(end) != '.')))
                        return new int[]{teacherShortcutIndex, end};
            }

            return null;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(name);
            dest.writeString(forename);
            dest.writeString(shortcut);
            dest.writeStringArray(subjects);
            dest.writeStringArray(specificTasks);
        }

        public static final Parcelable.Creator<TeacherData> CREATOR = new Parcelable.Creator<TeacherData>() {

            @Override
            public TeacherData createFromParcel(Parcel source) {
                return new TeacherData(source);
            }

            @Override
            public TeacherData[] newArray(int size) {
                return new TeacherData[size];
            }

        };

    }

}
