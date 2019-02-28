package de.franziskaneum.news;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.transition.Fade;
import android.transition.Slide;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import com.github.ksoichiro.android.observablescrollview.ObservableScrollView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.github.ksoichiro.android.observablescrollview.ScrollUtils;
import com.nineoldandroids.view.ViewHelper;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.List;

import de.franziskaneum.ChangeImageTransition;
import de.franziskaneum.CircleImageView;
import de.franziskaneum.CircularRevealTransition;
import de.franziskaneum.FranzCallback;
import de.franziskaneum.R;
import de.franziskaneum.Status;

/**
 * Created by Niko on 27.02.2016.
 */
public class NewsArticleActivity extends AppCompatActivity implements ObservableScrollViewCallbacks,
        View.OnClickListener {

    public static final String EXTRA_NEWS_ARTICLE =
            "de.franziskaneum.news.NewsArticleActivity.extra.NEWS_ARTICLE";
    public static final String KEY_NEWS_ARTICLE = EXTRA_NEWS_ARTICLE;
    public static final String KEY_NEWS_ARTICLE_SMALL_IMAGE_SIZE =
            "de.franziskaneum.news.NewsArticleActivity.extra.SMALL_IMAGE_SIZE";

    private News.NewsData newsArticle;
    private boolean hasBaseImage = false;
    private int parallaxImageHeight = 0;
    private int smallImageSize = 0;
    private int toolbarColor = 0;
    private boolean backPressed = false;
    private int lastScrollY = 0;

    private CircleImageView parallaxImage;
    private ObservableScrollView observableScrollView;
    private Toolbar toolbar;
    private TextView articleContent;

    private NewsArticleImageRecyclerAdapter recyclerAdapter;

    private FranzCallback newsArticleCallback = new FranzCallback() {
        @Override
        public void onCallback(int status, Object... objects) {
            if (Status.OK == status && objects.length > 1 && (objects[0] != null || objects[1] != null)) {
                CharSequence articleContent = (CharSequence) objects[0];
                List<News.NewsData.NewsArticleImage> articleImages = (List<News.NewsData.NewsArticleImage>) objects[1];

                newsArticle.setFullContent(articleContent);
                newsArticle.setImages(articleImages);

                NewsArticleActivity.this.articleContent.setText(articleContent);

                if (articleImages != null) {
                    recyclerAdapter.setImages(articleImages);
                    View galleryContainer = findViewById(R.id.news_article_gallery_container);
                    if (galleryContainer != null)
                        galleryContainer.setVisibility(View.VISIBLE);
                }

                loadBaseImage();
            } else
                finish();
        }
    };

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            newsArticle = savedInstanceState.getParcelable(KEY_NEWS_ARTICLE);
            smallImageSize = savedInstanceState.getInt(KEY_NEWS_ARTICLE_SMALL_IMAGE_SIZE);
        } else
            newsArticle = getIntent().getParcelableExtra(EXTRA_NEWS_ARTICLE);

        if (newsArticle == null) {
            finish();
            return;
        }

        if (hasBaseImage = newsArticle.hasBaseImage()) {
            setContentView(R.layout.activity_news_article_image);

            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            parallaxImageHeight = displayMetrics.widthPixels;

            parallaxImage = (CircleImageView) findViewById(R.id.news_article_image);
            parallaxImage.setTag(R.string.news_image_description, newsArticle.getBaseImage());
            parallaxImage.setOnClickListener(this);

            if (savedInstanceState != null) {
                ViewGroup.LayoutParams imageParams = parallaxImage.getLayoutParams();
                imageParams.height = imageParams.width = parallaxImageHeight;
                parallaxImage.setLayoutParams(imageParams);
                parallaxImage.setDisableCircularTransformation(true);
            }

            View anchor = findViewById(R.id.news_article_anchor);
            final ViewGroup.LayoutParams params = anchor.getLayoutParams();
            params.height = parallaxImageHeight;
            anchor.setLayoutParams(params);

            observableScrollView = (ObservableScrollView) findViewById(R.id.news_article_observable_scroll_view);
            observableScrollView.setScrollViewCallbacks(this);

            Target target = new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    parallaxImage.setImageBitmap(bitmap);
                    generateToolbarColorFromBitmap(bitmap);
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {
                }
            };

            parallaxImage.setTag(R.string.picasso_target, target);
            Picasso.with(this).load(newsArticle.getBaseImage().getThumbnailUrl()).into(target);

            ViewGroup.LayoutParams imageParams = parallaxImage.getLayoutParams();
            imageParams.width = parallaxImageHeight;
            imageParams.height = parallaxImageHeight;
            parallaxImage.setLayoutParams(imageParams);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Slide slide = new Slide(Gravity.BOTTOM);
                slide.excludeTarget(android.R.id.statusBarBackground, true);
                slide.excludeTarget(R.id.toolbar, true);
                slide.setInterpolator(new LinearOutSlowInInterpolator());
                slide.setDuration(500);

                Fade fade = new Fade();
                fade.setInterpolator(new DecelerateInterpolator());
                fade.setStartDelay(100);
                fade.setDuration(400);

                TransitionSet transition = new TransitionSet();
                transition.addTransition(slide);
                transition.addTransition(fade);

                getWindow().setEnterTransition(transition);
                getWindow().setReturnTransition(transition);

                getWindow().setSharedElementEnterTransition(new CircularRevealTransition().setDuration1(500).setInterpolator(new FastOutSlowInInterpolator()));

                postponeEnterTransition();
            } else {
                parallaxImage.setDisableCircularTransformation(true);
            }

            observableScrollView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        onScrollChanged(lastScrollY, false, false);
                        observableScrollView.scrollTo(0, (int) (parallaxImageHeight / 1.5f));
                    } else {
                        onScrollChanged(lastScrollY, false, false);
                        observableScrollView.scrollTo(0, 0);
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        startPostponedEnterTransition();
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        observableScrollView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    } else {
                        observableScrollView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    }
                }
            });
        } else
            setContentView(R.layout.activity_news_article);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        if (recyclerAdapter == null)
            recyclerAdapter = new NewsArticleImageRecyclerAdapter();

        recyclerView.setAdapter(recyclerAdapter);

        TextView articleTitle = (TextView) findViewById(R.id.news_article_title);
        articleTitle.setText(newsArticle.getTitle());

        articleContent = (TextView) findViewById(R.id.news_article_content);
        articleContent.setMovementMethod(LinkMovementMethod.getInstance());

        if (newsArticle.getFullContent() == null ||
                newsArticle.getFullContent().toString().isEmpty()) {
            if (newsArticle.getArticleUrl() != null && !newsArticle.getArticleUrl().isEmpty())
                NewsManager.getInstance(this).getArticleAsync(newsArticle.getArticleUrl(),
                        newsArticleCallback);
            else {
                articleContent.setText(newsArticle.getPreviewContent());
                loadBaseImage();
            }
        } else {
            articleContent.setText(newsArticle.getFullContent());

            if (newsArticle.getImages() != null) {
                recyclerAdapter.setImages(newsArticle.getImages());
                findViewById(R.id.news_article_gallery_container).setVisibility(View.VISIBLE);
            }

            loadBaseImage();
        }
    }

    private void generateToolbarColorFromBitmap(@Nullable Bitmap bitmap) {
        if (bitmap != null) {
            Palette.Builder builder = new Palette.Builder(bitmap);
            builder.resizeBitmapArea(150);
            builder.generate(new Palette.PaletteAsyncListener() {

                @Override
                public void onGenerated(Palette palette) {
                    Palette.Swatch swatch = palette.getVibrantSwatch();

                    if (swatch == null) {
                        swatch = palette.getLightVibrantSwatch();
                        if (swatch == null) {
                            swatch = palette.getDarkVibrantSwatch();
                            if (swatch == null) {
                                swatch = palette.getMutedSwatch();
                                if (swatch == null) {
                                    swatch = palette.getLightMutedSwatch();
                                    if (swatch == null)
                                        swatch = palette.getDarkMutedSwatch();
                                }
                            }
                        }
                    }

                    float[] color;

                    if (swatch != null) {
                        color = swatch.getHsl();
                        color[1] *= 0.8f;
                    } else {
                        color = new float[3];
                        Color.colorToHSV(ContextCompat.getColor(NewsArticleActivity.this,
                                R.color.ColorPrimary), color);
                    }

                    toolbarColor = Color.HSVToColor(color);

                    // change the toolbarColor after the image is loaded
                     onScrollChanged(lastScrollY, false, false);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        Window window = getWindow();
                        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

                        color[2] = color[2] * 0.8f;

                        window.setStatusBarColor(Color.HSVToColor(color));
                    }
                }

            });
        } else {
            float[] color = new float[3];
            Color.colorToHSV(ContextCompat.getColor(NewsArticleActivity.this,
                    R.color.ColorPrimary), color);

            toolbarColor = Color.HSVToColor(color);

            // change the toolbarColor after the image is loaded
            onScrollChanged(lastScrollY, false, false);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

                color[2] = color[2] * 0.8f;

                window.setStatusBarColor(Color.HSVToColor(color));
            }
        }
    }

    private void loadBaseImage() {
        if (newsArticle.hasBaseImage()) {
            Target target = new Target() {

                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom flags) {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                        ChangeImageTransition changeImageTransition = new ChangeImageTransition();
                        TransitionManager.beginDelayedTransition((ViewGroup) parallaxImage.getParent(), changeImageTransition.setDuration(250));
                    }

                    parallaxImage.setImageBitmap(bitmap);
                    generateToolbarColorFromBitmap(bitmap);
                }

                @Override
                public void onBitmapFailed(Drawable arg0) {
                }

                @Override
                public void onPrepareLoad(Drawable arg0) {
                }

            };

            parallaxImage.setTag(R.string.picasso_target, target);

            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int size = metrics.widthPixels;

            if (newsArticle.getBaseImage() != null &&
                    newsArticle.getBaseImage().getLargeImageUrl() != null)
                Picasso.with(this).load(newsArticle.getBaseImage().getLargeImageUrl())
                        .resize(size, size).centerCrop().skipMemoryCache().into(target);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_NEWS_ARTICLE, newsArticle);
        outState.putInt(KEY_NEWS_ARTICLE_SMALL_IMAGE_SIZE, smallImageSize);
    }

    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
        lastScrollY = scrollY;

        if (newsArticle.hasBaseImage()) {
            if (scrollY < 0)
                scrollY = 0;

            int toolbarHeight = toolbar != null ? toolbar.getHeight() : 0;

            float a = ((float) parallaxImageHeight * 0.8f) - toolbarHeight;

            float alpha = 0;

            if (scrollY >= a)
                alpha = Math.min(1, (scrollY - a)
                        / (parallaxImageHeight - a - toolbarHeight));

            if (toolbar != null)
                toolbar.setBackgroundColor(ScrollUtils.getColorWithAlpha(alpha, toolbarColor));

            ViewHelper.setTranslationY((ViewGroup) parallaxImage.getParent(), scrollY / 2);
        }
    }

    @Override
    public void onDownMotionEvent() {

    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {

    }

    @Override
    public void onClick(View view) {
        Object descriptionTag = view.getTag(R.string.news_image_description);

        if (descriptionTag != null && descriptionTag instanceof News.NewsData.NewsArticleImage) {
            News.NewsData.NewsArticleImage image = (News.NewsData.NewsArticleImage) descriptionTag;
            List<News.NewsData.NewsArticleImage> images = new ArrayList<>();
            images.add(image);

            Intent newsArticleImageGalleryIntent = new Intent(this,
                    NewsArticleImageGalleryActivity.class);
            newsArticleImageGalleryIntent.putExtra(
                    NewsArticleImageGalleryActivity.EXTRA_NEWS_ARTICLE_IMAGES,
                    (ArrayList<? extends Parcelable>) images);
            startActivity(newsArticleImageGalleryIntent);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                supportFinishAfterTransition();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
