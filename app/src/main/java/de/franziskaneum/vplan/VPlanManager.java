package de.franziskaneum.vplan;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Xml;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities;
import org.jsoup.parser.Parser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

import de.franziskaneum.FranzCallback;
import de.franziskaneum.ReturnValue;
import de.franziskaneum.Status;
import de.franziskaneum.Network;
import de.franziskaneum.settings.SettingsManager;

/**
 * Created by Niko on 11.08.2016.
 */
public class VPlanManager {
    private static final String VPLAN_URL = "http://www.franziskaneum.de/vplan/vplank.xml";
    private static final String CACHED_VPLAN_FILENAME = "vplan.xml";
    private static final String VPLAN_FILENAME = "vplan.json";

    private static VPlanManager instance;

    private Context context;
    private VPlan cachedVPlan;
    private SettingsManager settings;
    private long lastModified = 0;

    public enum Mode {
        DOWNLOAD, CACHE, IF_MODIFIED
    }

    private VPlanManager(Context context) {
        this.context = context;
        settings = SettingsManager.getInstance();
        this.lastModified = settings.getVPlanLastModified();
    }

    public static void initInstance(Context context) {
        instance = new VPlanManager(context);
    }

    public static VPlanManager getInstance() {
        if (instance == null)
            throw new NullPointerException("You have to init the instance");

        return instance;
    }

    public void getVPlanAsync(final Mode mode, @NonNull final FranzCallback callback) {
        final Handler handler = new Handler(Looper.getMainLooper());
        new Thread(new Runnable() {
            @Override
            public void run() {
                final ReturnValue rv = getVPlan(mode);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onCallback(rv.status, rv.objects);
                    }
                });
            }
        }).start();
    }

    ReturnValue getVPlan(final Mode mode) {
        if (Mode.CACHE == mode) {
            if (cachedVPlan != null)
                return new ReturnValue(Status.OK, mode, cachedVPlan);
            else if (isCachedVPlanAvailable())
                return cacheVPlan();
            else
                return downloadVPlan();
        } else if (Mode.IF_MODIFIED == mode) {
            ReturnValue rv = isNewVPlanAvailable();
            if (Status.OK == rv.status && rv.objects.length > 0 && rv.objects[0] != null) {
                boolean isNewVPlanAvailable = (boolean) rv.objects[0];

                if (isNewVPlanAvailable)
                    return downloadVPlan();
                else
                    return new ReturnValue(Status.CONTENT_NOT_MODIFIED, mode);
            } else
                return new ReturnValue(rv.status, mode);
        } else if (Mode.DOWNLOAD == mode) {
            return downloadVPlan();
        } else
            return new ReturnValue(Status.UNKNOWN_ERROR, mode);
    }

    private boolean isCachedVPlanAvailable() {
        File cachedVPlanFile = new File(context.getFilesDir(), VPLAN_FILENAME);
        if (cachedVPlanFile.exists() && cachedVPlanFile.length() > 0)
            return true;
        else {
            cachedVPlanFile = new File(context.getFilesDir(), CACHED_VPLAN_FILENAME);
            return cachedVPlanFile.exists() && cachedVPlanFile.length() > 0;
        }
    }

    ReturnValue isNewVPlanAvailable() {
        String base64Login = getBase64Login();
        if (base64Login != null) {
            if (Network.isConnected(context)) {
                HttpURLConnection vplanURLConnection = null;
                ReturnValue rv;

                try {
                    vplanURLConnection = (HttpURLConnection) new URL(VPLAN_URL).openConnection();
                    vplanURLConnection.addRequestProperty("Host", "www.franziskaneum.de");
                    vplanURLConnection.addRequestProperty("Authorization", "Basic " + base64Login);
                    vplanURLConnection.setRequestMethod("HEAD");    // use HEAD to only get the necessary
                    // information
                    // we don`t want the actual content
                    // we only want the last modified date
                    vplanURLConnection.connect();

                    if (vplanURLConnection.getResponseCode() == 401)
                        rv = new ReturnValue(Status.AUTHENTICATION_NEEDED);
                    else
                        rv = new ReturnValue(Status.OK, lastModified < vplanURLConnection.getLastModified());
                } catch (IOException e) {
                    e.printStackTrace();
                    rv = new ReturnValue(Status.UNKNOWN_ERROR);
                } finally {
                    if (vplanURLConnection != null)
                        vplanURLConnection.disconnect();
                }

                return rv;
            } else
                return new ReturnValue(Status.NO_CONNECTION);
        } else
            return new ReturnValue(Status.AUTHENTICATION_NEEDED);
    }

    private String getBase64Login() {
        String password = settings.getVPlanAuthenticationPassword();
        if (password == null || password.isEmpty())
            return null;
        else {
            String login = "FranzApp:" + password;

            try {
                return Base64.encodeToString(login.getBytes("UTF-8"), Base64.DEFAULT);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    void authenticateAsync(@NonNull final String password, @NonNull final FranzCallback callback) {
        final Handler handler = new Handler(Looper.getMainLooper());
        new Thread(new Runnable() {
            @Override
            public void run() {
                final ReturnValue rv = authenticate(password);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onCallback(rv.status, rv.objects);
                    }
                });
            }
        }).start();
    }

    ReturnValue authenticate(@NonNull String password) {
        settings.setVPlanAuthenticationPassword(password);

        final String base64Login = getBase64Login();
        if (base64Login != null) {
            if (Network.isConnected(context)) {
                HttpURLConnection vplanURLConnection = null;
                ReturnValue rv;

                try {
                    vplanURLConnection = (HttpURLConnection) new URL(VPLAN_URL).openConnection();
                    vplanURLConnection.addRequestProperty("Host", "www.franziskaneum.de");
                    vplanURLConnection.addRequestProperty("Authorization", "Basic " + base64Login);
                    vplanURLConnection.setRequestMethod("HEAD");
                    vplanURLConnection.connect();

                    if (vplanURLConnection.getResponseCode() == 401)
                        rv = new ReturnValue(Status.AUTHENTICATION_NEEDED);
                    else
                        rv = new ReturnValue(Status.OK);
                } catch (IOException e) {
                    e.printStackTrace();
                    rv = new ReturnValue(Status.UNKNOWN_ERROR);
                } finally {
                    if (vplanURLConnection != null)
                        vplanURLConnection.disconnect();
                }

                return rv;
            } else
                return new ReturnValue(Status.NO_CONNECTION);
        } else
            return new ReturnValue(Status.AUTHENTICATION_NEEDED);
    }

    private ReturnValue cacheVPlan() {
        if (isCachedVPlanAvailable()) {
            File cachedVPlanFile = new File(context.getFilesDir(), VPLAN_FILENAME);
            if (cachedVPlanFile.exists()) {
                VPlan vplan = VPlan.readFromFile(cachedVPlanFile);
                if (vplan != null) {
                    VPlanManager.this.cachedVPlan = vplan;
                    return new ReturnValue(Status.OK, Mode.CACHE, vplan);
                } else
                    return new ReturnValue(Status.UNKNOWN_ERROR, Mode.CACHE);
            } else {
                cachedVPlanFile = new File(context.getFilesDir(), CACHED_VPLAN_FILENAME);
                FileInputStream fis = null;
                ReturnValue rv;

                try {
                    fis = new FileInputStream(cachedVPlanFile);

                    rv = new VPlanParser().parseXML(fis);
                    if (Status.OK == rv.status && rv.objects.length > 1 && rv.objects[1] != null)
                        VPlanManager.this.cachedVPlan = (VPlan) rv.objects[1];
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    rv = new ReturnValue(Status.FILE_NOT_FOUND, Mode.CACHE);
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
            return new ReturnValue(Status.FILE_NOT_FOUND, Mode.CACHE);
    }

    private ReturnValue downloadVPlan() {
        String base64Login = getBase64Login();
        if (base64Login != null) {
            if (Network.isConnected(context)) {
                HttpURLConnection vplanURLConnection = null;
                ReturnValue rv;

                try {
                    vplanURLConnection = (HttpURLConnection) new URL(VPLAN_URL).openConnection();
                    vplanURLConnection.addRequestProperty("Host", "www.franziskaneum.de");
                    vplanURLConnection.addRequestProperty("Authorization", "Basic " + base64Login);
                    vplanURLConnection.setRequestMethod("GET");  // here we need the content
                    vplanURLConnection.connect();

                    if (vplanURLConnection.getResponseCode() == 401)
                        rv = new ReturnValue(Status.AUTHENTICATION_NEEDED, Mode.DOWNLOAD);
                    else {
                        final long lastModified = vplanURLConnection.getLastModified();

                        rv = new VPlanParser().parseHTML(vplanURLConnection.getInputStream());
                        if (Status.OK == rv.status && rv.objects.length > 1 && rv.objects[1] != null) {
                            final VPlan vplan = (VPlan) rv.objects[1];

                            VPlanManager.this.lastModified = lastModified;
                            settings.setVPlanLastModified(lastModified);
                            VPlanManager.this.cachedVPlan = vplan;

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    File cachedVPlanFile = new File(context.getFilesDir(), VPLAN_FILENAME);
                                    VPlan.writeToFile(vplan, cachedVPlanFile);
                                }
                            }).start();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    rv = new ReturnValue(Status.UNKNOWN_ERROR, Mode.DOWNLOAD);
                } finally {
                    if (vplanURLConnection != null)
                        vplanURLConnection.disconnect();
                }

                return rv;
            } else
                return new ReturnValue(Status.NO_CONNECTION, Mode.DOWNLOAD);
        } else
            return new ReturnValue(Status.AUTHENTICATION_NEEDED, Mode.DOWNLOAD);
    }

    private class VPlanParser {
        ReturnValue parseXML(@NonNull InputStream is) {
            XmlPullParser parser = Xml.newPullParser();
            ReturnValue rv;

            try {
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                parser.setInput(is, "UTF-8");
                int parserEvent;

                VPlan cachedVPlan = null;
                VPlan.VPlanDayData cachedVPlanDay = null;
                VPlan.VPlanDayData.VPlanTableData cachedVPlanDayTableRow = null;
                VPlan.VPlanDayData.VPlanExamData cachedVPlanDayExamRow = null;

                while ((parserEvent = parser.next()) != XmlPullParser.END_DOCUMENT) {
                    if (parserEvent == XmlPullParser.START_TAG) {
                        String startTag = parser.getName();
                        if (startTag.equalsIgnoreCase("vp")) {
                            cachedVPlan = new VPlan();
                        } else if (cachedVPlan != null) {
                            if (startTag.equalsIgnoreCase("title")) {
                                cachedVPlanDay = new VPlan.VPlanDayData();
                                cachedVPlan.add(cachedVPlanDay);
                                cachedVPlanDay.setTitle(parser.nextText());
                            } else if (cachedVPlanDay != null) {
                                if (cachedVPlanDayTableRow != null) {
                                    if (startTag.equalsIgnoreCase("class"))
                                        cachedVPlanDayTableRow.setSchoolClass(parser.nextText());
                                    else if (startTag.equalsIgnoreCase("hour"))
                                        cachedVPlanDayTableRow.setHour(parser.nextText());
                                    else if (startTag.equalsIgnoreCase("subject"))
                                        cachedVPlanDayTableRow.setSubject(parser.nextText());
                                    else if (startTag.equalsIgnoreCase("teacher"))
                                        cachedVPlanDayTableRow.setTeacher(parser.nextText());
                                    else if (startTag.equalsIgnoreCase("room"))
                                        cachedVPlanDayTableRow.setRoom(parser.nextText());
                                    else if (startTag.equalsIgnoreCase("info"))
                                        cachedVPlanDayTableRow.setInfo(parser.nextText());
                                }

                                if (cachedVPlanDayExamRow != null) {
                                    if (startTag.equalsIgnoreCase("classE"))
                                        cachedVPlanDayExamRow.setSchoolClass(parser.nextText());
                                    else if (startTag.equalsIgnoreCase("courseE"))
                                        cachedVPlanDayExamRow.setCourse(parser.nextText());
                                    else if (startTag.equalsIgnoreCase("hourE"))
                                        cachedVPlanDayExamRow.setHour(parser.nextText());
                                    else if (startTag.equalsIgnoreCase("teacherE"))
                                        cachedVPlanDayExamRow.setTeacher(parser.nextText());
                                    else if (startTag.equalsIgnoreCase("startTimeE"))
                                        cachedVPlanDayExamRow.setStartTime(parser.nextText());
                                    else if (startTag.equalsIgnoreCase("durationE"))
                                        cachedVPlanDayExamRow.setDuration(parser.nextText());
                                    else if (startTag.equalsIgnoreCase("infoE"))
                                        cachedVPlanDayExamRow.setInfo(parser.nextText());
                                }

                                if (startTag.equalsIgnoreCase("modified"))
                                    cachedVPlanDay.setModified(parser.nextText());
                                else if (startTag.equalsIgnoreCase("absentT"))
                                    cachedVPlanDay.setAbsentTeacher(parser.nextText());
                                else if (startTag.equalsIgnoreCase("absentC"))
                                    cachedVPlanDay.setAbsentClasses(parser.nextText());
                                else if (startTag.equalsIgnoreCase("notAvailableRooms"))
                                    cachedVPlanDay.setNotAvailableRooms(parser.nextText());
                                else if (startTag.equalsIgnoreCase("changesT"))
                                    cachedVPlanDay.setChangesTeacher(parser.nextText());
                                else if (startTag.equalsIgnoreCase("changesC"))
                                    cachedVPlanDay.setChangesClasses(parser.nextText());
                                else if (startTag.equalsIgnoreCase("changesS"))
                                    cachedVPlanDay.setChangesSupervision(parser.nextText());
                                else if (startTag.equalsIgnoreCase("additionalI"))
                                    cachedVPlanDay.setAdditionalInfo(parser.nextText());
                                else if (startTag.equalsIgnoreCase("row")) {
                                    cachedVPlanDayTableRow = new VPlan.VPlanDayData.VPlanTableData();
                                    cachedVPlanDay.addTableData(cachedVPlanDayTableRow);
                                } else if (startTag.equalsIgnoreCase("exam")) {
                                    cachedVPlanDayExamRow = new VPlan.VPlanDayData.VPlanExamData();
                                    cachedVPlanDay.addExamData(cachedVPlanDayExamRow);
                                }
                            }
                        }
                    }
                }

                if (cachedVPlan == null)
                    rv = new ReturnValue(Status.FILE_NOT_FOUND, Mode.CACHE);
                else
                    rv = new ReturnValue(Status.OK, Mode.CACHE, cachedVPlan);
            } catch (XmlPullParserException | IOException e) {
                e.printStackTrace();
                rv = new ReturnValue(Status.UNKNOWN_ERROR, Mode.CACHE);
            }

            return rv;
        }

        ReturnValue parseHTML(@NonNull InputStream is) {
            ReturnValue rv;

            try {
                Document doc = Jsoup.parse(is, "UTF-8", "www.franziskaneum.de", Parser.xmlParser());
                doc.outputSettings().escapeMode(Entities.EscapeMode.xhtml);

                VPlan downloadedVPlan = null;
                VPlan.VPlanDayData downloadedVPlanDay = null;
                VPlan.VPlanDayData.VPlanTableData downloadedVPlanDayTableRow = null;
                VPlan.VPlanDayData.VPlanExamData downloadedVPlanDayExamRow = null;

                Element vplanElement = doc.getElementsByTag("vp").first();
                if (vplanElement != null) {
                    downloadedVPlan = new VPlan();
                    for (Element element : doc.getAllElements()) {
                        if (element.tagName().equalsIgnoreCase("kopf")) {
                            downloadedVPlanDay = new VPlan.VPlanDayData();
                            downloadedVPlan.add(downloadedVPlanDay);
                        } else if (downloadedVPlanDay != null) {
                            if (element.tagName().equalsIgnoreCase("titel"))
                                downloadedVPlanDay.setTitle(element.text());
                            else if (element.tagName().equalsIgnoreCase("datum"))
                                downloadedVPlanDay.setModified(element.text());
                            else if (element.tagName().equalsIgnoreCase("abwesendl"))
                                downloadedVPlanDay.setAbsentTeacher(element.text());
                            else if (element.tagName().equalsIgnoreCase("abwesendk"))
                                downloadedVPlanDay.setAbsentClasses(element.text());
                            else if (element.tagName().equalsIgnoreCase("abwesendr"))
                                downloadedVPlanDay.setNotAvailableRooms(element.text());
                            else if (element.tagName().equalsIgnoreCase("aenderungl"))
                                downloadedVPlanDay.setChangesTeacher(element.text());
                            else if (element.tagName().equalsIgnoreCase("aenderungk"))
                                downloadedVPlanDay.setChangesClasses(element.text());
                            else if (element.tagName().equalsIgnoreCase("aufsichtinfo"))
                                downloadedVPlanDay.addChangesSupervision(element.text());
                            else if (element.tagName().equalsIgnoreCase("fussinfo"))
                                downloadedVPlanDay.addAdditionalInfo(element.text());
                            else if (element.tagName().equalsIgnoreCase("aktion")) {
                                downloadedVPlanDayTableRow = new VPlan.VPlanDayData.VPlanTableData();
                                downloadedVPlanDay.addTableData(downloadedVPlanDayTableRow);
                            } else if (element.tagName().equalsIgnoreCase("klausur")) {
                                downloadedVPlanDayExamRow = new VPlan.VPlanDayData.VPlanExamData();
                                downloadedVPlanDay.addExamData(downloadedVPlanDayExamRow);
                            }

                            if (downloadedVPlanDayTableRow != null) {
                                if (element.tagName().equalsIgnoreCase("klasse"))
                                    downloadedVPlanDayTableRow.setSchoolClass(element.text());
                                else if (element.tagName().equalsIgnoreCase("stunde"))
                                    downloadedVPlanDayTableRow.setHour(element.text());
                                else if (element.tagName().equalsIgnoreCase("fach"))
                                    downloadedVPlanDayTableRow.setSubject(element.text());
                                else if (element.tagName().equalsIgnoreCase("lehrer"))
                                    downloadedVPlanDayTableRow.setTeacher(element.text());
                                else if (element.tagName().equalsIgnoreCase("raum"))
                                    downloadedVPlanDayTableRow.setRoom(element.text());
                                else if (element.tagName().equalsIgnoreCase("info"))
                                    downloadedVPlanDayTableRow.setInfo(element.text());
                            }

                            if (downloadedVPlanDayExamRow != null) {
                                if (element.tagName().equalsIgnoreCase("jahrgang"))
                                    downloadedVPlanDayExamRow.setSchoolClass(element.text());
                                else if (element.tagName().equalsIgnoreCase("kurs"))
                                    downloadedVPlanDayExamRow.setCourse(element.text());
                                else if (element.tagName().equalsIgnoreCase("kursleiter"))
                                    downloadedVPlanDayExamRow.setTeacher(element.text());
                                else if (element.tagName().equalsIgnoreCase("stunde"))
                                    downloadedVPlanDayExamRow.setHour(element.text());
                                else if (element.tagName().equalsIgnoreCase("beginn"))
                                    downloadedVPlanDayExamRow.setStartTime(element.text());
                                else if (element.tagName().equalsIgnoreCase("dauer"))
                                    downloadedVPlanDayExamRow.setDuration(element.text());
                                else if (element.tagName().equalsIgnoreCase("kinfo"))
                                    downloadedVPlanDayExamRow.setInfo(element.text());
                            }
                        }
                    }
                }

                if (downloadedVPlan == null)
                    rv = new ReturnValue(Status.UNKNOWN_ERROR, Mode.DOWNLOAD);
                else
                    rv = new ReturnValue(Status.OK, Mode.DOWNLOAD, downloadedVPlan);
            } catch (IOException e) {
                e.printStackTrace();
                rv = new ReturnValue(Status.UNKNOWN_ERROR, Mode.DOWNLOAD);
            }

            return rv;
        }

        void saveVPlanAsXML(@NonNull OutputStream os, @Nullable VPlan vplan) {
            try {
                if (vplan != null) {
                    os.write("<vp>".getBytes());

                    for (VPlan.VPlanDayData vplanDay : vplan) {
                        if (vplanDay.getTitle() != null)
                            os.write(("<title>" + vplanDay.getTitle() + "</title>").getBytes());

                        if (vplanDay.getModified() != null)
                            os.write(("<modified>" + vplanDay.getModified() + "</modified>").getBytes());

                        if (vplanDay.getAbsentTeacher() != null)
                            os.write(("<absentT>" + vplanDay.getAbsentTeacher() + "</absentT>").getBytes());

                        if (vplanDay.getAbsentClasses() != null)
                            os.write(("<absentC>" + vplanDay.getAbsentClasses() + "</absentC>").getBytes());

                        if (vplanDay.getNotAvailableRooms() != null)
                            os.write(("<notAvailableRooms>" + vplanDay.getNotAvailableRooms() + "</notAvailableRooms>").getBytes());

                        if (vplanDay.getChangesTeacher() != null)
                            os.write(("<changesT>" + vplanDay.getChangesTeacher() + "</changesT>").getBytes());

                        if (vplanDay.getChangesClasses() != null)
                            os.write(("<changesC>" + vplanDay.getChangesClasses() + "</changesC>").getBytes());

                        if (vplanDay.getChangesSupervision() != null)
                            os.write(("<changesS>" + vplanDay.getChangesSupervision() + "</changesS>").getBytes());

                        if (vplanDay.getAdditionalInfo() != null)
                            os.write(("<additionalI>" + vplanDay.getAdditionalInfo() + "</additionalI>").getBytes());

                        if (vplanDay.getTableData() != null)
                            for (VPlan.VPlanDayData.VPlanTableData vplanDayTableRow : vplanDay.getTableData()) {
                                os.write("<row>".getBytes());

                                if (vplanDayTableRow.getSchoolClass() != null)
                                    os.write(("<class>" + vplanDayTableRow.getSchoolClass() + "</class>").getBytes());

                                if (vplanDayTableRow.getHour() != null)
                                    os.write(("<hour>" + vplanDayTableRow.getHour() + "</hour>").getBytes());

                                if (vplanDayTableRow.getSubject() != null)
                                    os.write(("<subject>" + vplanDayTableRow.getSubject() + "</subject>").getBytes());

                                if (vplanDayTableRow.getTeacher() != null)
                                    os.write(("<teacher>" + vplanDayTableRow.getTeacher() + "</teacher>").getBytes());

                                if (vplanDayTableRow.getRoom() != null)
                                    os.write(("<room>" + vplanDayTableRow.getRoom() + "</room>").getBytes());

                                if (vplanDayTableRow.getInfo() != null)
                                    os.write(("<info>" + vplanDayTableRow.getInfo() + "</info>").getBytes());

                                os.write("</row>".getBytes());
                                os.flush();
                            }

                        if (vplanDay.getExamData() != null)
                            for (VPlan.VPlanDayData.VPlanExamData vplanDayExamRow : vplanDay.getExamData()) {
                                os.write("<exam>".getBytes());

                                if (vplanDayExamRow.getSchoolClass() != null)
                                    os.write(("<classE>" + vplanDayExamRow.getSchoolClass() + "</classE>").getBytes());
                                if (vplanDayExamRow.getCourse() != null)
                                    os.write(("<courseE>" + vplanDayExamRow.getCourse() + "</courseE>").getBytes());
                                if (vplanDayExamRow.getHour() != null)
                                    os.write(("<hourE>" + vplanDayExamRow.getHour() + "</hourE>").getBytes());
                                if (vplanDayExamRow.getTeacher() != null)
                                    os.write(("<teacherE>" + vplanDayExamRow.getTeacher() + "</teacherE>").getBytes());
                                if (vplanDayExamRow.getStartTime() != null)
                                    os.write(("<startTimeE>" + vplanDayExamRow.getStartTime() + "</startTimeE>").getBytes());
                                if (vplanDayExamRow.getDuration() != null)
                                    os.write(("<durationE>" + vplanDayExamRow.getDuration() + "</durationE>").getBytes());
                                if (vplanDayExamRow.getInfo() != null)
                                    os.write(("<infoE>" + vplanDayExamRow.getInfo() + "</infoE>").getBytes());

                                os.write("</exam>".getBytes());
                                os.flush();
                            }

                        os.flush();
                    }

                    os.write("</vp>".getBytes());
                } else
                    os.write("".getBytes());

                os.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
