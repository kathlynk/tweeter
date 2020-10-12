package com.abantaoj.tweeter.ui.timeline;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.abantaoj.tweeter.R;
import com.abantaoj.tweeter.TweeterApplication;
import com.abantaoj.tweeter.databinding.ActivityTimelineBinding;
import com.abantaoj.tweeter.models.Tweet;
import com.abantaoj.tweeter.services.TwitterClient;
import com.abantaoj.tweeter.ui.compose.ComposeActivity;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;

public class TimelineActivity extends AppCompatActivity {
    private static final int REQUEST_CODE = 20;
    private final String TAG = this.getClass().getSimpleName();

    private ActivityTimelineBinding binding;
    private TwitterClient client;
    private List<Tweet> tweets;
    private TimelineAdapter adapter;
    private EndlessRecyclerViewScrollListener scrollListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_timeline);
        client = TweeterApplication.getTwitterClient(this);
        tweets = new ArrayList<>();
        adapter = new TimelineAdapter(this, tweets);

        setupRecyclerView();
        setupRefreshLayout();
        binding.timelineComposeButton.setOnClickListener(v -> {
            startActivityForResult(
                    new Intent(this, ComposeActivity.class),
                    REQUEST_CODE
            );
        });
        populateHomeTimeline();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            assert data != null;
            Tweet tweet = Parcels.unwrap(data.getParcelableExtra(ComposeActivity.class.getSimpleName()));
            tweets.add(0, tweet);
            adapter.notifyItemChanged(0);
            binding.timelineRecyclerView.scrollToPosition(0);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void setupRefreshLayout() {
        binding.timelineSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this, android.R.color.holo_blue_light));
        binding.timelineSwipeRefreshLayout.setOnRefreshListener(() -> {
            Log.d(TAG, "refreshing data");
            populateHomeTimeline();
        });
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                Log.i(TAG, "load more data" + page);
                loadMoreData();
            }
        };

        RecyclerView recyclerView = binding.timelineRecyclerView;
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(scrollListener);
    }

    private void populateHomeTimeline() {
        client.getHomeTimeline(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG, "success");
                JSONArray jsonArray = json.jsonArray;

                try {
                    adapter.clear();
                    adapter.addAll(Tweet.fromJsonArray(jsonArray));
                    binding.timelineSwipeRefreshLayout.setRefreshing(false);
                } catch (Exception e) {
                    Log.e(TAG, "populateHomeTimeline add fail", e);
                    binding.timelineSwipeRefreshLayout.setRefreshing(false);
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "error" + response, throwable);
                binding.timelineSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void loadMoreData() {
        if (tweets.isEmpty()) {
            return;
        }

        client.getAdditionalTweets(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG, "load more success");
                JSONArray jsonArray = json.jsonArray;

                try {
                    adapter.addAll(Tweet.fromJsonArray(jsonArray));
                } catch (Exception e) {
                    Log.e(TAG, "loadMoreData add fail", e);
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "error", throwable);
            }
        }, tweets.get(tweets.size() - 1).id - 1);
    }
}