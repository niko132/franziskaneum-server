package de.franziskaneum.news;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.franziskaneum.FranzCallback;
import de.franziskaneum.R;
import de.franziskaneum.Status;
import de.franziskaneum.drawer.DrawerFragment;
import de.franziskaneum.views.SwipeRefreshLayout;

/**
 * Created by Niko on 26.02.2016.
 */
public class NewsFragment extends DrawerFragment implements android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener, View.OnClickListener {

    private NewsManager newsManager;
    @Nullable
    private News news;

    private boolean shouldRefresh;
    private NewsRecyclerAdapter recyclerAdapter;

    private SwipeRefreshLayout swipeRefreshLayout;
    private RelativeLayout errorContainer;
    private ImageView errorImage;
    private TextView errorDescription;

    private FranzCallback newsCallback = new FranzCallback() {
        @Override
        public void onCallback(int status, Object... objects) {
            if (errorContainer != null)
                errorContainer.setVisibility(View.GONE);

            if (recyclerAdapter != null)
                recyclerAdapter.cancelLoadingOlderPosts();

            if (Status.OK == status && objects.length > 0 && objects[0] != null) {
                News news = (News) objects[0];

                NewsFragment.this.news = news;
                if (recyclerAdapter != null)
                    recyclerAdapter.setNews(news);

                shouldRefresh = false;
                if (swipeRefreshLayout != null)
                    swipeRefreshLayout.setRefreshing(false);
            } else {
                switch (status) {
                    case Status.NO_CONNECTION:
                        shouldRefresh = false;
                        if (swipeRefreshLayout != null)
                            swipeRefreshLayout.setRefreshing(false);

                        if (NewsFragment.this.news == null) {
                            if (errorDescription != null)
                                errorDescription.setText(R.string.no_connection);
                            if (errorImage != null)
                                errorImage.setImageDrawable(ContextCompat.getDrawable(
                                        getContext(), R.drawable.ic_no_connection));

                            if (errorContainer != null)
                                errorContainer.setVisibility(View.VISIBLE);
                        } else if (swipeRefreshLayout != null)
                            Snackbar.make(swipeRefreshLayout, R.string.no_connection, Snackbar.LENGTH_LONG).
                                    show();
                        break;
                    default:
                        shouldRefresh = false;
                        if (swipeRefreshLayout != null) {
                            swipeRefreshLayout.setRefreshing(false);
                            Snackbar.make(swipeRefreshLayout, R.string.unknown_error, Snackbar.LENGTH_LONG).
                                    show();
                        }
                        break;
                }
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        newsManager = NewsManager.getInstance();

        if (news == null) {
            shouldRefresh = true;
            newsManager.getNewsAsync(NewsManager.NEWS_BASE_URL, false, newsCallback);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_news, container, false);

        Toolbar toolbar = (Toolbar) root.findViewById(R.id.toolbar);
        setToolbar(toolbar);

        swipeRefreshLayout = (SwipeRefreshLayout) root
                .findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(R.color.ColorAccent);
        swipeRefreshLayout.setRefreshing(shouldRefresh);

        RecyclerView recyclerView = (RecyclerView) root.findViewById(R.id.recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        if (recyclerAdapter == null)
            recyclerAdapter = new NewsRecyclerAdapter(this);
        recyclerAdapter.setNews(news);

        recyclerView.setAdapter(recyclerAdapter);

        errorContainer = (RelativeLayout) root
                .findViewById(R.id.error_container);
        errorImage = (ImageView) errorContainer.findViewById(R.id.error_image);
        errorDescription = (TextView) errorContainer
                .findViewById(R.id.error_description);

        errorContainer.setOnClickListener(this);

        return root;
    }

    @Override
    public void onRefresh() {
        shouldRefresh = true;
        newsManager.getNewsAsync(NewsManager.NEWS_BASE_URL, true, newsCallback);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.error_container:
                shouldRefresh = true;
                if (swipeRefreshLayout != null)
                    swipeRefreshLayout.setRefreshing(true);
                if (errorContainer != null)
                    errorContainer.setVisibility(View.GONE);
                onRefresh();
                break;
            default:
                Object itemPositionTag = view.getTag(R.string.recycler_item_position);

                if (itemPositionTag != null && news != null) {
                    int itemPosition = (int) itemPositionTag;

                    if (itemPosition == news.size()) {  // older posts button was pressed
                        newsManager.getNewsAsync(NewsManager.NEWS_OLDER_POSTS_URL, false,
                                newsCallback);
                    } else if (itemPosition >= 0 && itemPosition < news.size()) {
                        News.NewsData newsArticle = news.get(itemPosition);

                        Intent newsArticleIntent = new Intent(getActivity(), NewsArticleActivity.class);
                        newsArticleIntent.putExtra(NewsArticleActivity.EXTRA_NEWS_ARTICLE, newsArticle);

                        if (newsArticle.hasBaseImage() &&
                                Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            View startImage = view.findViewById(R.id.news_list_item_image);
                            if (startImage != null) {
                                View statusBar = getActivity().findViewById(
                                        android.R.id.statusBarBackground);
                                View navigationBar = getActivity().findViewById(
                                        android.R.id.navigationBarBackground);

                                List<Pair<View, String>> pairs = new ArrayList<>();
                                if (statusBar != null)
                                    pairs.add(Pair.create(statusBar,
                                            Window.STATUS_BAR_BACKGROUND_TRANSITION_NAME));
                                if (navigationBar != null)
                                    pairs.add(Pair.create(navigationBar,
                                            Window.NAVIGATION_BAR_BACKGROUND_TRANSITION_NAME));

                                pairs.add(Pair.create(startImage, startImage.getTransitionName()));

                                ActivityOptionsCompat activityOptions =
                                        ActivityOptionsCompat.makeSceneTransitionAnimation(
                                                getActivity(),
                                                pairs.toArray(new Pair[pairs.size()]));
                                ActivityCompat.startActivity(getActivity(), newsArticleIntent,
                                        activityOptions.toBundle());
                            } else
                                startActivity(newsArticleIntent);
                        } else
                            startActivity(newsArticleIntent);
                    }
                }
                break;
        }
    }

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.news);
    }
}
