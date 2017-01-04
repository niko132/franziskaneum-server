package de.franziskaneum;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.support.annotation.NonNull;
import android.text.Html;
import android.util.DisplayMetrics;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Entities;
import org.jsoup.safety.Whitelist;

/**
 * Created by Niko on 26.02.2016.
 */
public class Utils {

    public static CharSequence trim(CharSequence text) {
        int start = 0;
        int end = text.length() - 1;

        while (start < end && Character.isWhitespace(text.charAt(start))) {
            start++;
        }

        while (end > start && (Character.isWhitespace(text.charAt(end)) || text.charAt(end) == 160)) {
            end--;
        }

        return text.subSequence(start, end + 1);
    }

    @NonNull
    public static String escapeString(@NonNull String stringToEscape) {
        return stringToEscape.replaceAll("Ä", "Ae").replaceAll("Ö", "Oe").replaceAll("Ü", "Ue").replaceAll("ä", "ae").replaceAll("ö", "oe").replaceAll("ü", "ue").replaceAll("ß", "ss");
    }

    public static String htmlToText(String html) {
        if (html == null)
            return null;
        Document document = Jsoup.parse(html);
        document.outputSettings(new Document.OutputSettings().prettyPrint(false).escapeMode(Entities.EscapeMode.xhtml));  //makes html() preserve linebreaks and spacing
        document.select("br").append("\\n");
        document.select("p").prepend("\\n\\n");
        String s = document.html().replaceAll("\\\\n", "\n");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            return Html.fromHtml(Jsoup.clean(s, "", Whitelist.none(), new Document.OutputSettings().prettyPrint(false)), Html.FROM_HTML_MODE_LEGACY).toString();
        else
            return Html.fromHtml(Jsoup.clean(s, "", Whitelist.none(), new Document.OutputSettings().prettyPrint(false))).toString();
    }

    public static float dpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    public static float pixelsToDp(float px, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return px / ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }
}