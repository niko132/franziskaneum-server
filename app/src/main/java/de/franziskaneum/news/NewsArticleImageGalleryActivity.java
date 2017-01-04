package de.franziskaneum.news;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;

import java.util.List;

import de.franziskaneum.R;
import de.franziskaneum.views.ViewPager;

/**
 * Created by Niko on 27.02.2016.
 */
public class NewsArticleImageGalleryActivity extends AppCompatActivity {

    public static final String EXTRA_NEWS_ARTICLE_IMAGES =
            "de.franziskaneum.news.NewsArticleImagesGalleryActivity.extra.NEWS_ARTICLE_IMAGES";
    public static final String EXTRA_START_IMAGE_INDEX =
            "de.franziskaneum.news.NewsArticleImagesGalleryActivity.extra.START_IMAGE_INDEX";

    private int currentImageIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        List<News.NewsData.NewsArticleImage> images;

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            images = extras.getParcelableArrayList(EXTRA_NEWS_ARTICLE_IMAGES);
            currentImageIndex = extras.getInt(EXTRA_START_IMAGE_INDEX, currentImageIndex);
            if (images == null) {
                finish();
                return;
            }
        } else {
            finish();
            return;
        }

        setContentView(R.layout.activity_news_article_image_gallery);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }

        DisplayMetrics displayMetrics = new DisplayMetrics(); // get the display metrics to generate the max pic size
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        ViewPager viewPager = (ViewPager) findViewById(R.id.news_article_image_gallery_view_pager);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            viewPager.setPageTransformer(true, new DepthPageTransformer());

        viewPager.setAdapter(new NewsArticleImageGalleryPagerAdapter(images,
                displayMetrics.widthPixels, displayMetrics.heightPixels));
        viewPager.setCurrentItem(currentImageIndex);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.gc();
    }

    public class DepthPageTransformer implements ViewPager.PageTransformer {
        private static final float MIN_SCALE = 0.75f;

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        public void transformPage(View view, float position) {
            int pageWidth = view.getWidth();

            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.setAlpha(0);

            } else if (position <= 0) { // [-1,0]
                // Use the default slide transition when moving to the left page
                view.setAlpha(1);
                view.setTranslationX(0);
                view.setScaleX(1);
                view.setScaleY(1);

            } else if (position <= 1) { // (0,1]
                // Fade the page out.
                view.setAlpha(1 - position);

                // Counteract the default slide transition
                view.setTranslationX(pageWidth * -position);

                // Scale the page down (between MIN_SCALE and 1)
                float scaleFactor = MIN_SCALE
                        + (1 - MIN_SCALE) * (1 - Math.abs(position));
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);

            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.setAlpha(0);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
