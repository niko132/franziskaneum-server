package de.franziskaneum.news;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.List;

import de.franziskaneum.R;
import de.franziskaneum.utils.ClickableViewHolder;

/**
 * Created by Niko on 27.02.2016.
 */
public class NewsArticleImageRecyclerAdapter extends RecyclerView.Adapter<NewsArticleImageRecyclerAdapter.NewsArticleImageViewHolder> {

    public class NewsArticleImageViewHolder extends ClickableViewHolder {

        public ImageView image;

        public NewsArticleImageViewHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.news_article_image_list_item_image);
        }

        @Override
        public void onClick(int position, @Nullable Context context) {
            Intent newsArticleImageGalleryIntent = new Intent(context, NewsArticleImageGalleryActivity.class);
            newsArticleImageGalleryIntent.putParcelableArrayListExtra(
                    NewsArticleImageGalleryActivity.EXTRA_NEWS_ARTICLE_IMAGES,
                    (ArrayList<? extends Parcelable>) images);
            newsArticleImageGalleryIntent.putExtra(
                    NewsArticleImageGalleryActivity.EXTRA_START_IMAGE_INDEX, position);

            context.startActivity(newsArticleImageGalleryIntent);
        }
    }

    private List<News.NewsData.NewsArticleImage> images;

    public NewsArticleImageRecyclerAdapter() {
        super();
    }

    public void setImages(@NonNull List<News.NewsData.NewsArticleImage> images) {
        this.images = images;
        notifyDataSetChanged();
    }

    @Override
    public NewsArticleImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new NewsArticleImageViewHolder(inflater.inflate(
                R.layout.news_article_image_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(final NewsArticleImageViewHolder holder, final int position) {
        if (position < images.size()) {
            final News.NewsData.NewsArticleImage image = images.get(position);

            holder.image.setImageBitmap(null);

            Target target = new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    if (holder.getAdapterPosition() == position)
                        holder.image.setImageBitmap(bitmap);
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {
                }
            };

            holder.image.setTag(R.string.picasso_target, target);
            Picasso.with(holder.itemView.getContext()).load(image.getThumbnailUrl()).into(target);
        }
    }

    @Override
    public int getItemCount() {
        return images != null ? images.size() : 0;
    }
}
