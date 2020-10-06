package com.abantaoj.tweeter.ui.timeline;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
    private RecyclerView.Adapter<TimelineAdapter.ViewHolder> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_timeline);
        client = TweeterApplication.getTwitterClient(this);
        tweets = new ArrayList<>();
        adapter = new TimelineAdapter(this, tweets);

        setupRecyclerView();
        populateHomeTimeline();
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
                    tweets.addAll(Tweet.fromJsonArray(jsonArray));
                    adapter.notifyDataSetChanged();
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