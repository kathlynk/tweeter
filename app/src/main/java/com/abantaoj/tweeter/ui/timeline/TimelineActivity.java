package com.abantaoj.tweeter.ui.timeline;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.abantaoj.tweeter.R;
import com.abantaoj.tweeter.TweeterApplication;
import com.abantaoj.tweeter.databinding.ActivityTimelineBinding;
import com.abantaoj.tweeter.models.Tweet;
import com.abantaoj.tweeter.services.TwitterClient;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;

public class TimelineActivity extends AppCompatActivity {
    private final String TAG = this.getClass().getSimpleName();

    private ActivityTimelineBinding binding;
    private TwitterClient client;
    private List<Tweet> tweets;
    private TimelineAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_timeline);
        client = TweeterApplication.getTwitterClient(this);
        tweets = new ArrayList<>();
        adapter = new TimelineAdapter(this, tweets);

        setupRecyclerView();
        setupRefreshLayout();
        populateHomeTimeline();
    }

    private void setupRefreshLayout() {
        binding.timelineSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this, android.R.color.holo_blue_light));
        binding.timelineSwipeRefreshLayout.setOnRefreshListener(() -> {
            Log.d(TAG, "refreshing data");
            populateHomeTimeline();
        });
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = binding.timelineRecyclerView;
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
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
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "error" + response, throwable);
            }
        });
    }
}