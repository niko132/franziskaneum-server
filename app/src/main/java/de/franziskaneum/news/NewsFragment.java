package de.franziskaneum.news;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import de.franziskaneum.FranzCallback;
import de.franziskaneum.R;
import de.franziskaneum.Status;
import de.franziskaneum.drawer.DrawerFragment;
import de.franziskaneum.views.SwipeRefreshLayout;

/**
 * Created by Niko on 26.02.2016.
 */
public class NewsFragment extends DrawerFragment implements android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener, View.OnClickListener, NewsRecyclerAdapter.OlderPostsCallback {

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
                            if (errorImage != null && getContext() != null)
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
        newsManager = NewsManager.getInstance(getActivity());

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
            recyclerAdapter = new NewsRecyclerAdapter(this, getActivity());
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
    public void onAttach(Context context) {
        super.onAttach(context);

        if (recyclerAdapter != null && context instanceof Activity)
            recyclerAdapter.setActivity((Activity) context);
    }

    @Override
    public void onDestroy() {
        if (recyclerAdapter != null)
            recyclerAdapter.freeMemory();

        super.onDestroy();
    }

    @Override
    public void onDetach() {
        if (recyclerAdapter != null)
            recyclerAdapter.freeMemory();

        super.onDetach();
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
        }
    }

    @Override
    public void loadOlderPosts() {
        newsManager.getNewsAsync(NewsManager.NEWS_OLDER_POSTS_URL, false,
                newsCallback);
    }

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.news);
    }
}
