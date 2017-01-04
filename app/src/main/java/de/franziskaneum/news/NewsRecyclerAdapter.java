package de.franziskaneum.news;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import de.franziskaneum.R;
import de.hdodenhof.circleimageview.CircleImageView;
import uk.co.deanwild.flowtextview.FlowTextView;

/**
 * Created by Niko on 26.02.2016.
 */
public class NewsRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_ARTICLE_NORMAL = 0;
    private static final int VIEW_TYPE_ARTICLE_IMAGE = 1;
    private static final int VIEW_TYPE_OLDER_POSTS = 2;

    private View.OnClickListener onClickListener;
    private News news;
    private boolean isLoading = false;

    public static class NewsArticleViewHolder extends RecyclerView.ViewHolder {

        public TextView title;
        public TextView content;

        public NewsArticleViewHolder(View itemView) {
            super(itemView);

            title = (TextView) itemView.findViewById(R.id.news_list_item_title);
            content = (TextView) itemView.findViewById(R.id.news_list_item_content);
        }
    }

    public static class NewsArticleImageViewHolder extends RecyclerView.ViewHolder {

        public TextView title;
        public FlowTextView content;
        public CircleImageView image;

        public NewsArticleImageViewHolder(View itemView) {
            super(itemView);

            title = (TextView) itemView.findViewById(R.id.news_list_item_title);
            content = (FlowTextView) itemView.findViewById(R.id.news_list_item_content);
            content.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.DarkGray));
            image = (CircleImageView) itemView.findViewById(R.id.news_list_item_image);
        }
    }

    public static class NewsOlderPostsViewHolder extends RecyclerView.ViewHolder {

        public Button loadOlderPosts;
        public ProgressBar olderPostsProgress;

        public NewsOlderPostsViewHolder(View itemView) {
            super(itemView);

            loadOlderPosts = (Button) itemView.findViewById(R.id.news_list_item_older_posts);
            olderPostsProgress = (ProgressBar) itemView.findViewById(R.id.news_list_item_older_posts_progress);
        }
    }

    public NewsRecyclerAdapter(@Nullable View.OnClickListener onClickListener) {
        super();
        this.onClickListener = onClickListener;
    }

    public void cancelLoadingOlderPosts() {
        isLoading = false;
        int itemCount = getItemCount();
        if (NewsManager.getInstance().olderPostsAvailable() && itemCount > 0)
            notifyItemChanged(itemCount - 1);
    }

    public void setNews(News news) {
        this.news = news;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == news.size() && NewsManager.getInstance().olderPostsAvailable())
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
            final NewsOlderPostsViewHolder viewHolder = (NewsOlderPostsViewHolder) holder;

            if (isLoading) {
                viewHolder.loadOlderPosts.setVisibility(View.INVISIBLE);
                viewHolder.olderPostsProgress.setVisibility(View.VISIBLE);
            } else {
                viewHolder.loadOlderPosts.setVisibility(View.VISIBLE);
                viewHolder.olderPostsProgress.setVisibility(View.INVISIBLE);
            }

            viewHolder.loadOlderPosts.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    isLoading = !isLoading;
                    int itemCount = getItemCount();
                    if (itemCount > 0)
                        notifyItemChanged(itemCount - 1);

                    if (onClickListener != null)
                        onClickListener.onClick(view);
                }
            });
            viewHolder.loadOlderPosts.setTag(R.string.recycler_item_position, position);
        } else {
            if (position < news.size()) {
                final News.NewsData newsArticle = news.get(position);

                if (holder instanceof NewsArticleImageViewHolder) {
                    final NewsArticleImageViewHolder viewHolder = (NewsArticleImageViewHolder) holder;

                    viewHolder.title.setText(newsArticle.getTitle());
                    viewHolder.content.setText(newsArticle.getPreviewContent());

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

                    if (onClickListener != null) {
                        viewHolder.itemView.setOnClickListener(onClickListener);
                        viewHolder.itemView.setTag(R.string.recycler_item_position, position);
                    }
                } else if (holder instanceof NewsArticleViewHolder) {
                    NewsArticleViewHolder viewHolder = (NewsArticleViewHolder) holder;

                    viewHolder.title.setText(newsArticle.getTitle());
                    viewHolder.content.setText(newsArticle.getPreviewContent());

                    if (onClickListener != null) {
                        viewHolder.itemView.setOnClickListener(onClickListener);
                        viewHolder.itemView.setTag(R.string.recycler_item_position, position);
                    }
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return news == null ? 0 :
                (NewsManager.getInstance().olderPostsAvailable() ? news.size() + 1 : news.size());
    }
}
