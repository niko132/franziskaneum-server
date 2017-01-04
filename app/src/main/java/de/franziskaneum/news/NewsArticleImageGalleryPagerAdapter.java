package de.franziskaneum.news;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.List;

import de.franziskaneum.R;
import uk.co.senab.photoview.PhotoView;

/**
 * Created by Niko on 27.02.2016.
 */
public class NewsArticleImageGalleryPagerAdapter extends PagerAdapter {

    @NonNull
    private List<News.NewsData.NewsArticleImage> images;
    private int screenWidth, screenHeight;

    public NewsArticleImageGalleryPagerAdapter(@NonNull List<News.NewsData.NewsArticleImage> images,
                                               int screenWidth, int screenHeight) {
        super();
        this.images = images;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

    @Override
    public int getCount() {
        return images.size();
    }

    @Override
    public Object instantiateItem(final ViewGroup container, final int position) {
        if (position < images.size()) {
            final News.NewsData.NewsArticleImage image = images.get(position);

            LayoutInflater inflater = LayoutInflater.from(container.getContext());
            View root = inflater.inflate(R.layout.news_article_image_gallery_list_item, container,
                    false);

            container.addView(root);

            final PhotoView photoView = (PhotoView) root.findViewById(
                    R.id.news_article_image_gallery_list_item_photo_view);
            TextView description = (TextView) root.findViewById(
                    R.id.news_article_image_gallery_list_item_description);
            description.setText(image.getDescription());

            Target smallImageTarget = new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    photoView.setImageBitmap(bitmap);

                    Target largeImageTarget = new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            photoView.setImageBitmap(bitmap);
                        }

                        @Override
                        public void onBitmapFailed(Drawable errorDrawable) {
                            Toast.makeText(container.getContext(),
                                    R.string.image_could_not_be_loaded, Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {
                        }
                    };

                    photoView.setTag(R.string.picasso_target, largeImageTarget);
                    Picasso.with(container.getContext()).load(images.get(position).getLargeImageUrl()).
                            resize(screenWidth, screenHeight).centerInside().skipMemoryCache().
                            into(largeImageTarget);
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {
                }
            };

            photoView.setTag(R.string.picasso_target, smallImageTarget);
            Picasso.with(container.getContext()).load(images.get(position).getThumbnailUrl()).
                    into(smallImageTarget);

            return root;
        } else
            return null;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
        System.gc();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }
}
