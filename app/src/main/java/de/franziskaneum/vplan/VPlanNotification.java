package de.franziskaneum.vplan;

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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Created by Niko on 19.12.2016.
 */

class VPlanNotification extends ArrayList<VPlanNotification.VPlanNotificationDay> {

    @Nullable
    static VPlanNotification readFromFile(File file) {
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
            return gson.fromJson(json, VPlanNotification.class);
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

    static void writeToFile(@Nullable VPlanNotification vplanNotification, File file) {
        Gson gson = new Gson();
        String s = gson.toJson(vplanNotification);

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

    boolean notificationEquals(@Nullable VPlanNotification rhs) {
        if (rhs == null || rhs.size() != size())
            return false;

        for (int i = 0; i < size(); i++) {
            VPlanNotificationDay lhsDay = get(i);
            VPlanNotificationDay rhsDay = rhs.get(i);

            if ((lhsDay.title != null && rhsDay.title != null &&
                    !lhsDay.title.equals(rhsDay.title)) || lhsDay.getNotifications() == null ||
                    rhsDay.getNotifications() == null || lhsDay.getNotifications().size() !=
                    rhsDay.getNotifications().size())
                return false;

            for (int j = 0; j < lhsDay.getNotifications().size(); j++) {
                String lhsNotification = lhsDay.getNotifications().get(j);
                String rhsNotification = rhsDay.getNotifications().get(j);

                if (!lhsNotification.equals(rhsNotification))
                    return false;
            }
        }

        return true;
    }

    VPlanNotification cloneContent() {
        VPlanNotification clone = new VPlanNotification();
        for (VPlanNotificationDay vplanNotificationDay : this) {
            clone.add(vplanNotificationDay.cloneContent());
        }

        return clone;
    }

    static class VPlanNotificationDay implements Parcelable {
        @Nullable
        private String title;
        @Nullable
        private List<String> notifications;

        VPlanNotificationDay() {
            super();
        }

        VPlanNotificationDay(Parcel in) {
            title = in.readString();
            notifications = in.createStringArrayList();
        }

        @Nullable
        public String getTitle() {
            return title;
        }

        public void setTitle(@Nullable String title) {
            this.title = title;
        }

        @Nullable
        public List<String> getNotifications() {
            return notifications;
        }

        public void setNotifications(@Nullable List<String> notifications) {
            this.notifications = notifications;
        }

        void addNotification(String notification) {
            if (notifications == null)
                notifications = new ArrayList<>();

            while(notification.contains("  "))
                notification = notification.replace("  ", "");

            notifications.add(notification);
        }

        public void removeNotification(int index) {
            if (notifications != null) {
                notifications.remove(index);

                if (notifications.isEmpty())
                    notifications = null;
            }
        }

        @Nullable
        Calendar getCalendarDate() {
            if (title != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("cccc, d. MMMM yyyy", Locale.GERMANY);
                Calendar date = Calendar.getInstance();
                try {
                    date.setTime(sdf.parse(title));
                } catch (ParseException e) {
                    e.printStackTrace();
                    date = null;
                }

                return date;
            }

            return null;
        }

        VPlanNotificationDay cloneContent() {
            VPlanNotificationDay clone = new VPlanNotificationDay();
            clone.setTitle(title);
            if (notifications != null)
                for (String notification : notifications) {
                    clone.addNotification(notification);
                }

            return clone;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(title);
            dest.writeStringList(notifications);
        }

        public static final Creator<VPlanNotificationDay> CREATOR = new Creator<VPlanNotificationDay>() {
            @Override
            public VPlanNotificationDay createFromParcel(Parcel in) {
                return new VPlanNotificationDay(in);
            }

            @Override
            public VPlanNotificationDay[] newArray(int size) {
                return new VPlanNotificationDay[size];
            }
        };
    }
}
