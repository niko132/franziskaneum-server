package de.franziskaneum.news;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.text.Html;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import de.franziskaneum.FranzCallback;
import de.franziskaneum.ReturnValue;
import de.franziskaneum.Status;
import de.franziskaneum.utils.HtmlTagHandler;
import de.franziskaneum.utils.Network;
import de.franziskaneum.utils.Utils;

/**
 * Created by Niko on 13.08.2016.
 */
public class NewsManager {
    public static final String NEWS_BASE_URL =
            "https://www.franziskaneum.de/wordpress/category/aktuelles/";
    public static String NEWS_OLDER_POSTS_URL = NEWS_BASE_URL;

    private static NewsManager instance;

    private Context context;
    private News news;

    private NewsManager(Context context) {
        this.context = context;
    }

    public static void initInstance(Context context) {
        instance = new NewsManager(context.getApplicationContext());
    }

    public static NewsManager getInstance(Context context) {
        if (instance == null) {
//            throw new NullPointerException("You have to init the instance");

            initInstance(context);
        }

        return instance;
    }

    public void getNewsAsync(@NonNull final String newsURL, final boolean refresh, @NonNull final FranzCallback callback) {
        final Handler handler = new Handler(Looper.getMainLooper());
        new Thread(new Runnable() {
            @Override
            public void run() {
                final ReturnValue rv = getNews(newsURL, refresh);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onCallback(rv.status, rv.objects);
                    }
                });
            }
        }).start();
    }

    public ReturnValue getNews(@NonNull String newsURL, final boolean refresh) {
        if (NEWS_BASE_URL.equals(newsURL)) {
            if (!refresh && news != null)
                return new ReturnValue(Status.OK, news);
            else {
                ReturnValue rv = downloadNewsFromURL(newsURL);
                if (Status.OK == rv.status && rv.objects.length > 0 && rv.objects[0] != null)
                    NewsManager.this.news = (News) rv.objects[0];

                return rv;
            }
        } else {
            if (olderPostsAvailable()) {
                ReturnValue rv = downloadNewsFromURL(newsURL);
                if (Status.OK == rv.status && rv.objects.length > 0 && rv.objects[0] != null) {
                    if (NewsManager.this.news == null)
                        NewsManager.this.news = (News) rv.objects[0];
                    else
                        NewsManager.this.news.addAll((News) rv.objects[0]);
                }

                return new ReturnValue(rv.status, NewsManager.this.news);
            } else
                return new ReturnValue(Status.NO_MORE_CONTENT_AVAILABLE);
        }
    }

    public void getArticleAsync(@NonNull final String newsArticleURL, @NonNull final FranzCallback callback) {

        final Handler handler = new Handler(Looper.getMainLooper());
        new Thread(new Runnable() {
            @Override
            public void run() {
                final ReturnValue rv = getArticle(newsArticleURL);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onCallback(rv.status, rv.objects);
                    }
                });
            }
        }).start();
    }

    public ReturnValue getArticle(@NonNull String newsArticleURL) {
        return downloadArticle(newsArticleURL);
    }

    public boolean olderPostsAvailable() {
        return NEWS_OLDER_POSTS_URL != null && !NEWS_OLDER_POSTS_URL.isEmpty();
    }

    private ReturnValue downloadNewsFromURL(@NonNull String newsURL) {
        ReturnValue rv = null;

        if (Network.isConnected(context)) {
            HttpURLConnection newsURLConnection = null;
            try {
                newsURLConnection = (HttpURLConnection) new URL(newsURL).openConnection();
                newsURLConnection.connect();

                rv = new NewsParser().parseHTML(newsURLConnection.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
                rv = new ReturnValue(Status.UNKNOWN_ERROR);
            } finally {
                if (newsURLConnection != null)
                    newsURLConnection.disconnect();
            }
        } else
            rv = new ReturnValue(Status.NO_CONNECTION);

        return rv;
    }

    private ReturnValue downloadArticle(@NonNull String newsArticleURL) {
        ReturnValue rv;

        if (Network.isConnected(context)) {
            HttpURLConnection newsArticleURLConnection = null;
            try {
                newsArticleURLConnection = (HttpURLConnection) new URL(newsArticleURL).openConnection();
                newsArticleURLConnection.connect();

                rv = new NewsArticleParser().parseHTML(newsArticleURLConnection.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
                rv = new ReturnValue(Status.UNKNOWN_ERROR);
            } finally {
                if (newsArticleURLConnection != null)
                    newsArticleURLConnection.disconnect();
            }
        } else
            rv = new ReturnValue(Status.NO_CONNECTION);

        return rv;
    }

    private class NewsParser {
        ReturnValue parseHTML(@NonNull InputStream is) {
            ReturnValue rv = null;

            try {
                Document doc = Jsoup.parse(is, "UTF-8", "www.franziskaneum.de");

                NewsManager.NEWS_OLDER_POSTS_URL = null;
                Element olderPostsElement = doc.getElementsByClass("nav-previous").first();
                if (olderPostsElement != null) {
                    Element olderPostLink = olderPostsElement.getElementsByTag("a").first();
                    if (olderPostLink != null)
                        NewsManager.NEWS_OLDER_POSTS_URL = olderPostLink.attr("href");
                }

                News news = null;

                for (Element articleElement : doc.getElementsByTag("article")) {
                    News.NewsData newsArticle = new News.NewsData();
                    if (news == null)
                        news = new News();
                    news.add(newsArticle);

                    Element titleElement = articleElement.getElementsByClass("entry-title").first();
                    if (titleElement != null)
                        newsArticle.setTitle(titleElement.text());

                    Element contentElement = articleElement.getElementsByClass("entry-content").
                            first();
                    if (contentElement != null) {
                        Element imageElement = contentElement.getElementsByTag("img").first();
                        if (imageElement != null)
                            newsArticle.setBaseImageUrl(imageElement.attr("src"));

                        Element moreLink = contentElement.getElementsByClass("more-link").first();
                        if (moreLink != null)
                            newsArticle.setArticleUrl(moreLink.attr("href"));

                        // remove the more link
                        contentElement.getElementsByClass("more-link").remove();
                        // remove the images
                        contentElement.getElementsByTag("img").remove();

                        CharSequence previewContent;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                            previewContent = Utils.trim(Html.fromHtml(contentElement.html(),
                                    Html.FROM_HTML_MODE_LEGACY, null, new HtmlTagHandler()));
                        else
                            previewContent = Utils.trim(Html.fromHtml(contentElement.html(), null,
                                    new HtmlTagHandler()));

                        newsArticle.setPreviewContent(previewContent);
                    }
                }

                if (news != null)
                    rv = new ReturnValue(Status.OK, news);
                else
                    rv = new ReturnValue(Status.UNKNOWN_ERROR);
            } catch (IOException e) {
                e.printStackTrace();
                rv = new ReturnValue(Status.UNKNOWN_ERROR);
            }

            return rv;
        }
    }

    private class NewsArticleParser {
        ReturnValue parseHTML(@NonNull InputStream is) {
            ReturnValue rv;
            try {
                Document doc = Jsoup.parse(is, "UTF-8", "www.franziskaneum.de");

                CharSequence articleContent = null;
                List<News.NewsData.NewsArticleImage> articleImages = null;

                Element contentElement = doc.getElementsByClass("entry-content").first();
                if (contentElement != null) {
                    if (contentElement.getElementsByClass("gallery").first() != null) {
                        articleImages = new ArrayList<>();

                        for (Element galleryItemElement :
                                contentElement.getElementsByClass("gallery-item")) {
                            News.NewsData.NewsArticleImage articleImage =
                                    new News.NewsData.NewsArticleImage();

                            Element imageElement = galleryItemElement.getElementsByTag("img").
                                    first();
                            if (imageElement != null)
                                articleImage.setThumbnailUrl(imageElement.attr("src"));

                            Element descriptionElement = galleryItemElement.
                                    getElementsByClass("gallery-caption").first();
                            if (descriptionElement != null)
                                articleImage.setDescription(descriptionElement.text());

                            articleImages.add(articleImage);
                        }
                    }

                    contentElement.getElementsByClass("gallery").remove();
                    contentElement.getElementsByTag("img").remove();

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                        articleContent = Utils.trim(Html.fromHtml(contentElement.html(),
                                Html.FROM_HTML_MODE_LEGACY, null, new HtmlTagHandler()));
                    else
                        articleContent = Utils.trim(Html.fromHtml(contentElement.html(), null,
                                new HtmlTagHandler()));
                }

                if (articleContent != null || articleImages != null)
                    rv = new ReturnValue(Status.OK, articleContent, articleImages);
                else
                    rv = new ReturnValue(Status.UNKNOWN_ERROR);
            } catch (IOException e) {
                e.printStackTrace();
                rv = new ReturnValue(Status.UNKNOWN_ERROR);
            }

            return rv;
        }
    }
}
