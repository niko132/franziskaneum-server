package de.franziskaneum.teacher;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Html;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import de.franziskaneum.FranzCallback;
import de.franziskaneum.ReturnValue;
import de.franziskaneum.Status;
import de.franziskaneum.utils.Network;

/**
 * Created by Niko on 13.08.2016.
 */
public class TeacherManager {
    public static final String CACHED_TEACHER_LIST_FILENAME = "teacher.xml";
    private static final String TEACHER_LIST_FILENAME = "teacher.json";

    /*
    private static final String TEACHER_LIST_URL =
            "https://www.franziskaneum.de/wordpress/wer-wir-sind/lehrerliste/";
    */
    private static final String TEACHER_LIST_URL =
            "https://www.franziskaneum.de/wordpress/wer-wir-sind/lehrer-im-schuljahr-201718/";

    private static TeacherManager instance;

    private Context context;
    private TeacherList teacherList;

    private TeacherManager(Context context) {
        this.context = context;
    }

    public static void initInstance(Context context) {
        instance = new TeacherManager(context);
    }

    public static TeacherManager getInstance() {
        if (instance == null)
            throw new NullPointerException("You have to init the instance");

        return instance;
    }

    public void getTeacherListAsync(final boolean refresh, @NonNull final FranzCallback callback) {
        final Handler handler = new Handler(Looper.getMainLooper());
        new Thread(new Runnable() {
            @Override
            public void run() {
                final ReturnValue rv = getTeacherList(refresh);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onCallback(rv.status, rv.objects);
                    }
                });
            }
        }).start();
    }

    private ReturnValue getTeacherList(boolean refresh) {
        if (refresh)
            return downloadTeacherList();
        else {
            if (teacherList != null)
                return new ReturnValue(Status.OK, teacherList);
            else if (isCachedTeacherListAvailable())
                return cacheTeacherList();
            else
                return downloadTeacherList();
        }
    }

    private boolean isCachedTeacherListAvailable() {
        File cachedTeacherListFile = new File(context.getFilesDir(), TEACHER_LIST_FILENAME);
        if (cachedTeacherListFile.exists() && cachedTeacherListFile.length() > 0)
            return true;
        else {
            cachedTeacherListFile = new File(context.getFilesDir(), CACHED_TEACHER_LIST_FILENAME);
            return cachedTeacherListFile.exists() && cachedTeacherListFile.length() > 0;
        }
    }

    private ReturnValue cacheTeacherList() {
        if (isCachedTeacherListAvailable()) {
            File cachedTeacherListFile = new File(context.getFilesDir(), TEACHER_LIST_FILENAME);
            if (cachedTeacherListFile.exists()) {
                TeacherList teacherList = TeacherList.readFromFile(cachedTeacherListFile);
                if (teacherList != null) {
                    TeacherManager.this.teacherList = teacherList;
                    return new ReturnValue(Status.OK, teacherList);
                } else
                    return new ReturnValue(Status.UNKNOWN_ERROR);
            } else {
                cachedTeacherListFile =
                        new File(context.getFilesDir(), CACHED_TEACHER_LIST_FILENAME);
                FileInputStream fis = null;
                ReturnValue rv;
                try {
                    fis = new FileInputStream(cachedTeacherListFile);

                    rv = new TeacherListParser().parseXML(fis);
                    if (Status.OK == rv.status && rv.objects.length > 0 && rv.objects[0] != null)
                        TeacherManager.this.teacherList = (TeacherList) rv.objects[0];
                } catch (FileNotFoundException e) {
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

                return rv;
            }
        } else
            return new ReturnValue(Status.FILE_NOT_FOUND);
    }

    private ReturnValue downloadTeacherList() {
        if (Network.isConnected(context)) {
            HttpURLConnection teacherListURLConnection = null;
            ReturnValue rv;
            try {
                teacherListURLConnection =
                        (HttpURLConnection) new URL(TEACHER_LIST_URL).openConnection();
                teacherListURLConnection.connect();

                rv = new TeacherListParser().parseHTML(teacherListURLConnection.getInputStream());
                if (Status.OK == rv.status && rv.objects.length > 0 && rv.objects[0] != null) {
                    TeacherManager.this.teacherList = (TeacherList) rv.objects[0];
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            File cachedTeacherListFile = new File(context.getFilesDir(), TEACHER_LIST_FILENAME);
                            TeacherList.writeToFile(teacherList, cachedTeacherListFile);
                        }
                    }).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
                rv = new ReturnValue(Status.UNKNOWN_ERROR);
            } finally {
                if (teacherListURLConnection != null)
                    teacherListURLConnection.disconnect();
            }

            return rv;
        } else
            return new ReturnValue(Status.NO_CONNECTION);
    }

    private class TeacherListParser {
        public ReturnValue parseXML(@NonNull InputStream is) {
            ReturnValue rv;

            try {
                Document doc = Jsoup.parse(is, "UTF-8", "www.franziskaneum.de", Parser.xmlParser());

                TeacherList teacherList = null;

                for (Element teacherElement : doc.getElementsByTag("teacher")) {
                    TeacherList.TeacherData teacherData = new TeacherList.TeacherData();
                    if (teacherList == null)
                        teacherList = new TeacherList();
                    teacherList.add(teacherData);

                    teacherData.setName(teacherElement.getElementsByTag("name").first().text());

                    Element vorenameElement = teacherElement.getElementsByTag("vorename").first();
                    if (vorenameElement != null)
                        teacherData.setForename(vorenameElement.text()); // fail xD

                    Element forenameElement = teacherElement.getElementsByTag("forename").first();
                    if (forenameElement != null)
                        teacherData.setForename(forenameElement.text());

                    teacherData.setShortcut(teacherElement.getElementsByTag("shortcut").first().
                            text());
                    teacherData.setSubjects(teacherElement.getElementsByTag("subjects").first().
                            text().split(", "));
                    teacherData.setSpecificTasks(teacherElement.getElementsByTag("specificTasks").
                            first().html().split(", "));
                }

                if (teacherList == null || teacherList.isEmpty())
                    rv = new ReturnValue(Status.UNKNOWN_ERROR);
                else
                    rv = new ReturnValue(Status.OK, teacherList);
            } catch (IOException e) {
                e.printStackTrace();
                rv = new ReturnValue(Status.UNKNOWN_ERROR);
            }

            return rv;
        }

        public ReturnValue parseHTML(@NonNull InputStream is) {
            ReturnValue rv;

            try {
                Document doc = Jsoup.parse(is, "UTF-8", "www.franziskaneum.de");

                TeacherList teacherList = null;

                Element tableElement = doc.getElementsByTag("tbody").first();
                if (tableElement != null) {
                    teacherList = new TeacherList();
                    Elements teacherElements = tableElement.getElementsByTag("tr");
                    if (teacherElements.size() >= 2) {
                        TeacherList.TeacherData teacherData;

                        for (int i = 1; i < teacherElements.size(); i++) { // begin at index 1 to ignore the table header
                            Element teacherElement = teacherElements.get(i);
                            Elements tdElements = teacherElement.getElementsByTag("td");

                            if (tdElements.size() >= 1) {
                                teacherData = new TeacherList.TeacherData();
                                teacherList.add(teacherData);

                                teacherData.setShortcut(tdElements.get(0).text().trim());

                                if (tdElements.size() >= 2) {
                                    String names = tdElements.get(1).text().trim();

                                    if (names.contains(",")) {
                                        String[] splittedNames = names.split(",");
                                        if (splittedNames.length >= 2) {
                                            teacherData.setName(splittedNames[0].trim());
                                            teacherData.setForename(splittedNames[1].trim());
                                        } else
                                            teacherData.setName(names);
                                    } else if (names.contains(";")) {
                                        String[] splittedNames = names.split(";");
                                        if (splittedNames.length >= 2) {
                                            teacherData.setName(splittedNames[0].trim());
                                            teacherData.setForename(splittedNames[1].trim());
                                        } else
                                            teacherData.setName(names);
                                    } else if (names.contains(" ")) {
                                        String[] splittedNames = names.split(" ");
                                        if (splittedNames.length >= 2) {
                                            teacherData.setName(splittedNames[0].trim());
                                            teacherData.setForename(splittedNames[1].trim());
                                        } else
                                            teacherData.setName(names);
                                    } else
                                        teacherData.setName(names);

                                    if (tdElements.size() >= 3) {
                                        teacherData.setSubjects(tdElements.get(2).text().
                                                split(", "));

                                        if (tdElements.size() >= 4)
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                                teacherData.setSpecificTasks(Html.fromHtml(
                                                        tdElements.get(3).html(),
                                                        Html.FROM_HTML_MODE_LEGACY).toString().
                                                        split("\n"));
                                            } else {
                                                teacherData.setSpecificTasks(Html.fromHtml(
                                                        tdElements.get(3).html()).toString().
                                                        split("\n"));
                                            }
                                    }
                                }
                            }
                        }
                    }
                }

                if (teacherList == null || teacherList.isEmpty())
                    rv = new ReturnValue(Status.UNKNOWN_ERROR);
                else
                    rv = new ReturnValue(Status.OK, teacherList);
            } catch (IOException e) {
                e.printStackTrace();
                rv = new ReturnValue(Status.UNKNOWN_ERROR);
            }

            return rv;
        }

        public void saveTeacherListAsXML(@NonNull OutputStream os, @Nullable TeacherList teacherList) {
            try {
                if (teacherList != null) {
                    os.write("<teacherList>".getBytes());

                    for (TeacherList.TeacherData teacherData : teacherList) {
                        os.write("<teacher>".getBytes());

                        if (teacherData.getName() != null)
                            os.write(("<name>" + teacherData.getName() + "</name>").getBytes());
                        if (teacherData.getForename() != null)
                            os.write(("<forename>" + teacherData.getForename() + "</forename>").
                                    getBytes());
                        if (teacherData.getShortcut() != null)
                            os.write(("<shortcut>" + teacherData.getShortcut() + "</shortcut>").
                                    getBytes());

                        String subjects = null;
                        if (teacherData.getSubjects() != null)
                            for (String subject : teacherData.getSubjects()) {
                                if (subjects == null)
                                    subjects = subject;
                                else
                                    subjects += ", " + subject;
                            }
                        if (subjects != null)
                            os.write(("<subjects>" + subjects + "</subjects>").getBytes());

                        String specificTasks = null;
                        if (teacherData.getSpecificTasks() != null)
                            for (String specificTask : teacherData.getSpecificTasks()) {
                                if (specificTasks == null)
                                    specificTasks = specificTask;
                                else
                                    specificTasks += ", " + specificTask;
                            }
                        if (specificTasks != null)
                            os.write(("<specificTasks>" + specificTasks + "</specificTasks>")
                                    .getBytes());

                        os.write("</teacher>".getBytes());
                    }

                    os.write("</teacherList>".getBytes());
                } else
                    os.write("".getBytes());

                os.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
