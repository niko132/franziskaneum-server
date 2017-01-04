package de.franziskaneum.news;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Niko on 26.02.2016.
 */
public class News extends ArrayList<News.NewsData> {

    public static class NewsData implements Parcelable {

        @Nullable
        private String title;
        @Nullable
        private CharSequence previewContent;
        @Nullable
        private String articleUrl;
        @Nullable
        private CharSequence fullContent;
        @Nullable
        private NewsArticleImage baseImage;
        @Nullable
        private List<NewsArticleImage> images;

        public NewsData() {
        }

        public NewsData(Parcel in) {
            title = in.readString();
            previewContent = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
            articleUrl = in.readString();
            fullContent = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
            baseImage = in.readParcelable(NewsArticleImage.class.getClassLoader());
            images = in.createTypedArrayList(NewsArticleImage.CREATOR);
        }

        @Nullable
        public String getTitle() {
            return title;
        }

        public void setTitle(@Nullable String title) {
            this.title = title;
        }

        @Nullable
        public CharSequence getPreviewContent() {
            return previewContent;
        }

        public void setPreviewContent(@Nullable CharSequence previewContent) {
            this.previewContent = previewContent;
        }

        @Nullable
        public String getArticleUrl() {
            return articleUrl;
        }

        public void setArticleUrl(@Nullable String articleUrl) {
            this.articleUrl = articleUrl;
        }

        @Nullable
        public CharSequence getFullContent() {
            return fullContent;
        }

        public void setFullContent(@Nullable CharSequence fullContent) {
            this.fullContent = fullContent;
        }

        @Nullable
        public NewsArticleImage getBaseImage() {
            return baseImage;
        }

        public void setBaseImage(@Nullable NewsArticleImage baseImage) {
            this.baseImage = baseImage;
        }

        public boolean hasBaseImage() {
            return baseImage != null && baseImage.getThumbnailUrl() != null &&
                    !baseImage.getThumbnailUrl().isEmpty();
        }

        public void setBaseImageUrl(@Nullable String url) {
            if (baseImage == null)
                baseImage = new NewsArticleImage();

            baseImage.setThumbnailUrl(url);
        }

        @Nullable
        public List<NewsArticleImage> getImages() {
            return images;
        }

        public void setImages(@Nullable List<NewsArticleImage> images) {
            this.images = images;
        }

        public void addImage(@NonNull NewsArticleImage image) {
            if (this.images == null)
                this.images = new ArrayList<>();

            images.add(image);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeString(title);
            TextUtils.writeToParcel(previewContent, parcel, 0);
            parcel.writeString(articleUrl);
            TextUtils.writeToParcel(fullContent, parcel, 0);
            parcel.writeParcelable(baseImage, 0);
            parcel.writeTypedList(images);
        }

        public static final Parcelable.Creator<NewsData> CREATOR = new Parcelable.Creator<NewsData>() {

            @Override
            public NewsData createFromParcel(Parcel source) {
                return new NewsData(source);
            }

            @Override
            public NewsData[] newArray(int size) {
                return new NewsData[size];
            }

        };

        public static class NewsArticleImage implements Parcelable {

            @Nullable
            private String thumbnailUrl;
            @Nullable
            private String description;
            private boolean isLargeImageLoaded = false;

            public NewsArticleImage() {
            }

            public NewsArticleImage(Parcel in) {
                thumbnailUrl = in.readString();
                description = in.readString();
                isLargeImageLoaded = in.readInt() == 1;
            }

            @Nullable
            public String getThumbnailUrl() {
                return thumbnailUrl;
            }

            public void setThumbnailUrl(@Nullable String url) {
                this.thumbnailUrl = url;
            }

            @Nullable
            public String getLargeImageUrl() {
                if (getThumbnailUrl() != null)
                    return getThumbnailUrl().replace("-150x150", "");

                return null;
            }

            @Nullable
            public String getDescription() {
                return description ;
            }

            public void setDescription(@Nullable String description) {
                this.description = description;
            }

            public boolean isLargeImageLoaded() {
                return isLargeImageLoaded;
            }

            public void setIsLargeImageLoaded(boolean isLargeImageLoaded) {
                this.isLargeImageLoaded = isLargeImageLoaded;
            }

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel parcel, int flags) {
                parcel.writeString(thumbnailUrl);
                parcel.writeString(description);
                parcel.writeInt(isLargeImageLoaded ? 1 : 0);
            }

            public static final Parcelable.Creator<NewsArticleImage> CREATOR = new Parcelable.Creator<NewsArticleImage>() {

                @Override
                public NewsArticleImage createFromParcel(Parcel source) {
                    return new NewsArticleImage(source);
                }

                @Override
                public NewsArticleImage[] newArray(int size) {
                    return new NewsArticleImage[size];
                }

            };

        }

    }

}
