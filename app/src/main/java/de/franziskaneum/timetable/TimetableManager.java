package de.franziskaneum.timetable;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import de.franziskaneum.FranzCallback;
import de.franziskaneum.ReturnValue;
import de.franziskaneum.Status;

/**
 * Created by Niko on 13.08.2016.
 */
public class TimetableManager {
    private static final String TIMETABLE_A_FILENAME = "timetable_A.xml";
    private static final String TIMETABLE_B_FILENAME = "timetable_B.xml";
    private static final String TIMETABLE_FILENAME_XML = "timetable.xml";
    private static final String TIMETABLE_FILENAME = "timetable.json";

    private static TimetableManager instance;

    private Context context;
    private Timetable timetable;

    private TimetableManager(Context context) {
        this.context = context;
    }

    public static void initInstance(Context context) {
        instance = new TimetableManager(context);
    }

    public static TimetableManager getInstance() {
        if (instance == null)
            throw new NullPointerException("You have to init the instance");

        return instance;
    }

    public void getTimetableAsync(@NonNull final FranzCallback callback) {
        final Handler handler = new Handler(Looper.getMainLooper());

        new Thread(new Runnable() {
            @Override
            public void run() {
                final ReturnValue rv = getTimetable();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onCallback(rv.status, rv.objects);
                    }
                });
            }
        }).start();
    }

    public void saveTimetableAsync(@NonNull final Timetable timetable) {
        TimetableManager.this.timetable = timetable;

        new Thread(new Runnable() {
            @Override
            public void run() {
                File cachedTimetableFile = new File(context.getFilesDir(), TIMETABLE_FILENAME);
                Timetable.writeToFile(timetable, cachedTimetableFile);
            }
        }).start();
    }

    public ReturnValue getTimetable() {
        if (timetable != null && !timetable.isEmpty())
            return new ReturnValue(Status.OK, timetable);
        else {
            ReturnValue rv = cacheTimetable();
            if (Status.OK == rv.status && rv.objects.length > 0 && rv.objects[0] != null)
                TimetableManager.this.timetable = (Timetable) rv.objects[0];
            return rv;
        }
    }

    private boolean isCachedTimetableAAvailable() {
        File cachedTimetableAFile = new File(context.getFilesDir(), TIMETABLE_A_FILENAME);
        return cachedTimetableAFile.exists() && cachedTimetableAFile.length() > 0;
    }

    private boolean isCachedTimetableAvailable() {
        File cachedTimetableFile = new File(context.getFilesDir(), TIMETABLE_FILENAME);
        if (cachedTimetableFile.exists() && cachedTimetableFile.length() > 0)
            return true;
        else {
            cachedTimetableFile = new File(context.getFilesDir(), TIMETABLE_FILENAME_XML);
            return cachedTimetableFile.exists() && cachedTimetableFile.length() > 0;
        }
    }

    @NonNull
    private Timetable fillTimetable(@Nullable Timetable timetable) {
        if (timetable == null)
            timetable = new Timetable();

        for (int week = 0; week < 2; week++) {
            List<List<Timetable.TimetableData>> timetableWeek;

            if (week >= timetable.size()) {
                timetableWeek = new ArrayList<>();
                timetable.add(timetableWeek);
            } else
                timetableWeek = timetable.get(week);

            for (int day = 0; day < 5; day++) {
                List<Timetable.TimetableData> timetableDay;

                if (day >= timetableWeek.size()) {
                    timetableDay = new ArrayList<>();
                    timetableWeek.add(timetableDay);
                }
            }
        }

        return timetable;
    }

    private ReturnValue cacheTimetable() {
        Timetable timetable = null;

        if (isCachedTimetableAvailable()) {
            File cachedTimetableFile = new File(context.getFilesDir(), TIMETABLE_FILENAME);
            if (cachedTimetableFile.exists()) {
                timetable = Timetable.readFromFile(cachedTimetableFile);
            } else {
                cachedTimetableFile = new File(context.getFilesDir(), TIMETABLE_FILENAME_XML);
                FileInputStream fis = null;
                ReturnValue rv;
                try {
                    fis = new FileInputStream(cachedTimetableFile);
                    rv = new TimetableParser().parseTimetable(fis);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    rv = new ReturnValue(Status.FILE_NOT_FOUND);
                } finally {
                    if (fis != null)
                        try {
                            fis.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                }

                if (Status.OK == rv.status && rv.objects.length > 0 && rv.objects[0] != null)
                    timetable = (Timetable) rv.objects[0];
            }
        } else if (isCachedTimetableAAvailable()) {
            TimetableParser timetableParser = new TimetableParser();
            ReturnValue rv = timetableParser.parseXML();

            if (Status.OK == rv.status && rv.objects.length > 0 && rv.objects[0] != null)
                timetable = (Timetable) rv.objects[0];
        }

        if (timetable == null || timetable.isEmpty())
            timetable = fillTimetable(timetable);
        else
            TimetableManager.this.timetable = timetable;

        return new ReturnValue(Status.OK, timetable);
    }

    private class TimetableParser {
        ReturnValue parseXML() {
            File file;
            Timetable timetable = new Timetable();
            ReturnValue rv = null;

            for (int i = 0; i < 2; i++) {
                if (i == 0)
                    file = new File(context.getFilesDir(), TIMETABLE_A_FILENAME);
                else
                    file = new File(context.getFilesDir(), TIMETABLE_B_FILENAME);

                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(file);

                    Document doc = Jsoup.parse(fis, "UTF-8", "www.franziskaneum.de",
                            Parser.xmlParser());

                    Element timetableElement = doc.getElementsByTag("timetable").first();
                    if (timetableElement != null) {
                        List<List<Timetable.TimetableData>> timetableWeek = new ArrayList<>();
                        timetable.add(timetableWeek);

                        for (Element dayElement : timetableElement.getElementsByTag("day")) {
                            List<Timetable.TimetableData> timetableDay = new ArrayList<>();
                            timetableWeek.add(timetableDay);

                            for (Element itemElement : dayElement.getElementsByTag("item")) {
                                Timetable.TimetableData subject = new Timetable.TimetableData();
                                timetableDay.add(subject);

                                Element hourElement = itemElement.getElementsByTag("hour").first();
                                if (hourElement != null)
                                    subject.setHour(Integer.parseInt(hourElement.text()));

                                Element subjectElement = itemElement.getElementsByTag("subject").
                                        first();
                                if (subjectElement != null)
                                    subject.setSubject(subjectElement.text());

                                Element roomElement = itemElement.getElementsByTag("room").first();
                                if (roomElement != null)
                                    subject.setRoom(roomElement.text());

                                Element teacherElement = itemElement.getElementsByTag("teacher").
                                        first();
                                if (teacherElement != null)
                                    subject.setTeacherOrSchoolClass(teacherElement.text());

                                Element doubleHourElement = itemElement.getElementsByTag("double").
                                        first();
                                if (doubleHourElement != null && doubleHourElement.text() != null)
                                    subject.setIsDoubleHour(doubleHourElement.text().equals("0"));
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    rv = new ReturnValue(Status.UNKNOWN_ERROR);
                } finally {
                    if (fis != null)
                        try {
                            fis.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                }
            }

            if (timetable.isEmpty())
                return new ReturnValue(Status.FILE_NOT_FOUND);
            else if (rv != null)
                return rv;
            else
                return new ReturnValue(Status.OK, timetable);
        }

        ReturnValue parseTimetable(@NonNull InputStream is) {
            ReturnValue rv;
            try {
                Document doc = Jsoup.parse(is, "UTF-8", "www.franziskaneum.de", Parser.xmlParser());

                Timetable timetable = null;
                Element timetableElement = doc.getElementsByTag("timetable").first();
                if (timetableElement != null) {
                    timetable = new Timetable();

                    for (Element weekElement : timetableElement.getElementsByTag("week")) {
                        List<List<Timetable.TimetableData>> timetableWeek = new ArrayList<>();
                        timetable.add(timetableWeek);

                        for (Element dayElement : weekElement.getElementsByTag("day")) {
                            List<Timetable.TimetableData> timetableDay = new ArrayList<>();
                            timetableWeek.add(timetableDay);

                            for (Element itemElement : dayElement.getElementsByTag("item")) {
                                Timetable.TimetableData subject = new Timetable.TimetableData();
                                timetableDay.add(subject);

                                Element hourElement = itemElement.getElementsByTag("hour").first();
                                if (hourElement != null)
                                    subject.setHour(Integer.parseInt(hourElement.text()));

                                Element subjectElement = itemElement.getElementsByTag("subject").
                                        first();
                                if (subjectElement != null)
                                    subject.setSubject(subjectElement.text());

                                Element roomElement = itemElement.getElementsByTag("room").first();
                                if (roomElement != null)
                                    subject.setRoom(roomElement.text());

                                Element teacherElement = itemElement.getElementsByTag("teacher").
                                        first();
                                if (teacherElement != null)
                                    subject.setTeacherOrSchoolClass(teacherElement.text());

                                Element doubleHourElement = itemElement.getElementsByTag("double").
                                        first();
                                if (doubleHourElement != null && doubleHourElement.text() != null)
                                    subject.setIsDoubleHour(doubleHourElement.text().equals("0"));
                            }
                        }
                    }
                }

                if (timetable == null || timetable.isEmpty())
                    rv = new ReturnValue(Status.UNKNOWN_ERROR);
                else
                    rv = new ReturnValue(Status.OK, timetable);
            } catch (IOException e) {
                e.printStackTrace();
                rv = new ReturnValue(Status.UNKNOWN_ERROR);
            }

            return rv;
        }

        void saveTimetableAsXML(@NonNull OutputStream os, @Nullable Timetable timetable) {
            try {
                if (timetable != null) {
                    os.write("<timetable>".getBytes());

                    for (List<List<Timetable.TimetableData>> timetableWeek : timetable) {
                        os.write("<week>".getBytes());

                        for (List<Timetable.TimetableData> timetableDay : timetableWeek) {
                            os.write("<day>".getBytes());

                            for (Timetable.TimetableData timetableData : timetableDay) {
                                os.write("<item>".getBytes());

                                os.write(("<hour>" + timetableData.getHour() + "</hour>").getBytes());
                                if (timetableData.getSubject() != null)
                                    os.write(("<subject>" + timetableData.getSubject() + "</subject>")
                                            .getBytes());
                                if (timetableData.getRoom() != null)
                                    os.write(("<room>" + timetableData.getRoom() + "</room>").getBytes());
                                if (timetableData.getTeacherOrSchoolClass() != null)
                                    os.write(("<teacher>" + timetableData.getTeacherOrSchoolClass() +
                                            "</teacher>").getBytes());
                                os.write(("<double>" + (timetableData.isDoubleHour() ? "0" : "1") +
                                        "</double>").getBytes());

                                os.write("</item>".getBytes());
                            }

                            os.write("</day>".getBytes());
                        }

                        os.write("</week>".getBytes());
                    }

                    os.write("</timetable>".getBytes());
                } else
                    os.write("".getBytes());

                os.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
