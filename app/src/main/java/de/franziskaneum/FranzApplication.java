package de.franziskaneum;

import android.app.Application;

import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import de.franziskaneum.news.NewsManager;
import de.franziskaneum.settings.SettingsManager;
import de.franziskaneum.teacher.TeacherManager;
import de.franziskaneum.timetable.TimetableManager;
import de.franziskaneum.vplan.VPlanManager;
import de.franziskaneum.vplan.VPlanNotificationManager;

/**
 * Created by Niko on 20.12.2015.
 */
public class FranzApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        initSingletons();
        trustFranzCertificates();
    }

    private void initSingletons() {
        SettingsManager.initInstance(this);
        NewsManager.initInstance(this);
        VPlanManager.initInstance(this);
        VPlanNotificationManager.initInstance(this);
        TimetableManager.initInstance(this);
        TeacherManager.initInstance(this);
    }

    private void trustFranzCertificates() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }

                        public void checkServerTrusted(X509Certificate[] certs, String authType) throws CertificateException {
                            return;
                        }

                        public void checkClientTrusted(X509Certificate[] certs, String authType) throws CertificateException {
                            return;
                        }
                    }
            };

            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new SecureRandom());
            SSLSocketFactory tlsSocketFactory = new MyTLSSocketFactory(sc);
            HttpsURLConnection.setDefaultSSLSocketFactory(tlsSocketFactory);
            HostnameVerifier hv = new HostnameVerifier() {
                public boolean verify(String urlHostName, SSLSession session) {
                    if (!urlHostName.equalsIgnoreCase(session.getPeerHost())) {
                        System.out.println("Warning: URL host '" + urlHostName + "' is different to SSLSession host '" + session.getPeerHost() + "'.");
                        return false;
                    }
                    return true;
                }
            };
            HttpsURLConnection.setDefaultHostnameVerifier(hv);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
