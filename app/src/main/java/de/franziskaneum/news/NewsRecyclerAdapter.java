package de.franziskaneum.news;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.List;

import de.franziskaneum.CircleImageView;
import de.franziskaneum.R;
import de.franziskaneum.utils.ClickableViewHolder;

/**
 * Created by Niko on 26.02.2016.
 */
public class NewsRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_ARTICLE_NORMAL = 0;
    private static final int VIEW_TYPE_ARTICLE_IMAGE = 1;
    private static final int VIEW_TYPE_OLDER_POSTS = 2;

    private News news;
    @Nullable
    private Activity activity; // needed for shared element transition
    private OlderPostsCallback olderPostsCallback;

    public interface OlderPostsCallback {
        void loadOlderPosts();
    }

    public class NewsArticleViewHolder extends ClickableViewHolder {

        public TextView title;
        public TextView content;

        public NewsArticleViewHolder(View itemView) {
            super(itemView);

            title = (TextView) itemView.findViewById(R.id.news_list_item_title);
            content = (TextView) itemView.findViewById(R.id.news_list_item_content);
        }

        @Override
        public void onClick(int position, @Nullable Context context) {

            News.NewsData newsArticle = news.get(position);

            Intent newsArticleIntent = new Intent(context, NewsArticleActivity.class);
            newsArticleIntent.putExtra(NewsArticleActivity.EXTRA_NEWS_ARTICLE, newsArticle);

            context.startActivity(newsArticleIntent);
        }
    }

    public class NewsArticleImageViewHolder extends ClickableViewHolder {

        public TextView title;
        public de.franziskaneum.views.FlowTextView content;
        public CircleImageView image;

        public NewsArticleImageViewHolder(View itemView) {
            super(itemView);

            title = (TextView) itemView.findViewById(R.id.news_list_item_title);
            content = (de.franziskaneum.views.FlowTextView) itemView.findViewById(R.id.news_list_item_content);
            image = (CircleImageView) itemView.findViewById(R.id.news_list_item_image);
            image.setCircularPercentage(1f);
        }

        @Override
        public void onClick(int position, @Nullable Context context) {
            if (context != null && news != null && news.size() > position) {
                News.NewsData newsArticle = news.get(position);

                Intent newsArticleIntent = new Intent(context, NewsArticleActivity.class);
                newsArticleIntent.putExtra(NewsArticleActivity.EXTRA_NEWS_ARTICLE, newsArticle);

                if (newsArticle.hasBaseImage() &&
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (activity != null) {
                        View startImage = image;
                        if (startImage != null) {
                            View decorView = activity.getWindow().getDecorView();

                            View statusBar = decorView.findViewById(
                                    android.R.id.statusBarBackground);
                            View navigationBar = decorView.findViewById(
                                    android.R.id.navigationBarBackground);

                            List<Pair<View, String>> pairs = new ArrayList<>();

                            pairs.add(Pair.create(startImage, startImage.getTransitionName()));

                            if (statusBar != null)
                                pairs.add(Pair.create(statusBar,
                                        Window.STATUS_BAR_BACKGROUND_TRANSITION_NAME));
                            if (navigationBar != null)
                                pairs.add(Pair.create(navigationBar,
                                        Window.NAVIGATION_BAR_BACKGROUND_TRANSITION_NAME));


                            ActivityOptionsCompat activityOptions =
                                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                                            activity,
                                            pairs.toArray(new Pair[pairs.size()]));
                            ActivityCompat.startActivity(context, newsArticleIntent,
                                    activityOptions.toBundle());
                        } else
                            context.startActivity(newsArticleIntent);
                    } else
                        context.startActivity(newsArticleIntent);
                } else
                    context.startActivity(newsArticleIntent);
            }
        }
    }

    public static class NewsOlderPostsViewHolder extends RecyclerView.ViewHolder {

        public ProgressBar olderPostsProgress;

        public NewsOlderPostsViewHolder(View itemView) {
            super(itemView);

            olderPostsProgress = (ProgressBar) itemView.findViewById(R.id.news_list_item_older_posts_progress);
        }
    }

    public NewsRecyclerAdapter(@Nullable OlderPostsCallback olderPostsCallback, Activity activity) {
        super();
        this.olderPostsCallback = olderPostsCallback;
        this.activity = activity;
    }

    public void setNews(News news) {
        this.news = news;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == news.size() && NewsManager.getInstance(activity).olderPostsAvailable())
            return VIEW_TYPE_OLDER_POSTS;
        else if (news.get(position).hasBaseImage())
            return VIEW_TYPE_ARTICLE_IMAGE;
        else
            return VIEW_TYPE_ARTICLE_NORMAL;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == VIEW_TYPE_ARTICLE_NORMAL) {
            return new NewsArticleViewHolder(inflater.inflate(R.layout.news_list_item, parent, false));
        } else if (viewType == VIEW_TYPE_ARTICLE_IMAGE) {
            return new NewsArticleImageViewHolder(inflater.inflate(R.layout.news_list_item_image, parent, false));
        } else if (viewType == VIEW_TYPE_OLDER_POSTS) {
            return new NewsOlderPostsViewHolder(inflater.inflate(R.layout.news_list_item_older_posts, parent, false));
        }

        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof NewsOlderPostsViewHolder) {
            if (olderPostsCallback != null)
                olderPostsCallback.loadOlderPosts();
        } else {
            if (position < news.size()) {
                final News.NewsData newsArticle = news.get(position);

                if (holder instanceof NewsArticleImageViewHolder) {
                    final NewsArticleImageViewHolder viewHolder = (NewsArticleImageViewHolder) holder;

                    viewHolder.title.setText(newsArticle.getTitle());
                    viewHolder.content.setText(newsArticle.getPreviewContent());

                    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) viewHolder.image.getLayoutParams();
                    viewHolder.content.setImageSize(params.width + params.leftMargin + params.rightMargin, params.height + params.topMargin + params.bottomMargin);

                    viewHolder.image.setImageBitmap(null);

                    Target target = new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            if (viewHolder.getAdapterPosition() == position)
                                viewHolder.image.setImageBitmap(bitmap);
                        }

                        @Override
                        public void onBitmapFailed(Drawable errorDrawable) {

                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {

                        }
                    };

                    viewHolder.image.setTag(R.string.picasso_target, target);
                    if (newsArticle.getBaseImage() != null)
                        Picasso.with(viewHolder.itemView.getContext()).
                                load(newsArticle.getBaseImage().getThumbnailUrl()).into(target);
                } else if (holder instanceof NewsArticleViewHolder) {
                    NewsArticleViewHolder viewHolder = (NewsArticleViewHolder) holder;

                    viewHolder.title.setText(newsArticle.getTitle());
                    viewHolder.content.setText(newsArticle.getPreviewContent());
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return news == null ? 0 :
                (NewsManager.getInstance(activity).olderPostsAvailable() ? news.size() + 1 : news.size());
    }

    public void freeMemory() {
        this.activity = null;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }
}
