package com.abantaoj.tweeter.ui.timeline;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.abantaoj.tweeter.R;
import com.abantaoj.tweeter.TweeterApplication;
import com.abantaoj.tweeter.databinding.ActivityTimelineBinding;
import com.abantaoj.tweeter.models.Tweet;
import com.abantaoj.tweeter.models.TweetDao;
import com.abantaoj.tweeter.models.TweetWithUser;
import com.abantaoj.tweeter.models.User;
import com.abantaoj.tweeter.services.TwitterClient;
import com.abantaoj.tweeter.ui.compose.ComposeFragment;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import okhttp3.Headers;

public class TimelineActivity extends AppCompatActivity implements ComposeFragment.ComposeDialogListener {
    public static String SEND_ACTION_TITLE = "SEND_ACTION_TITLE";
    public static String SEND_ACTION_URL = "SEND_ACTION_URL";

    private final String TAG = this.getClass().getSimpleName();

    private ActivityTimelineBinding binding;
    private TwitterClient client;
    private List<Tweet> tweets;
    private TimelineAdapter adapter;
    private FragmentManager fragmentManager;
    private TweetDao tweetDao;
    private ExecutorService executorService;
    private Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_timeline);
        client = TweeterApplication.getTwitterClient(this);
        tweets = new ArrayList<>();
        adapter = new TimelineAdapter(this, tweets);
        fragmentManager = getSupportFragmentManager();
        tweetDao = ((TweeterApplication) getApplicationContext()).getTweeterDatabase().tweetDao();
        executorService = ((TweeterApplication) getApplicationContext()).getExecutorService();
        activity = this;

        setupRecyclerView();
        setupRefreshLayout();
        binding.timelineComposeButton.setOnClickListener(v -> {
            ComposeFragment fragment = ComposeFragment.newInstance();
            fragment.show(fragmentManager, "fragment_compose");
        });

        populateHomeTimeline();
        checkForSendAction();
    }

    private void checkForSendAction() {
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (action != null) {
            Log.d(TAG, action);
        }

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                ComposeFragment fragment = ComposeFragment.newInstance();
                Bundle bundle = new Bundle();

                String titleOfPage = intent.getStringExtra(Intent.EXTRA_SUBJECT);
                String urlOfPage = intent.getStringExtra(Intent.EXTRA_TEXT);

                if (titleOfPage != null) {
                    bundle.putString(TimelineActivity.SEND_ACTION_TITLE, titleOfPage);
                }

                if (urlOfPage != null) {
                    bundle.putString(TimelineActivity.SEND_ACTION_URL, urlOfPage);
                }

                fragment.setArguments(bundle);
                fragment.show(fragmentManager, "fragment_compose");
            }
        }
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

        EndlessRecyclerViewScrollListener scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
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
        binding.timelineSwipeRefreshLayout.setRefreshing(true);

        client.getHomeTimeline(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG, "success");
                JSONArray jsonArray = json.jsonArray;

                try {
                    adapter.clear();
                    final List<Tweet> tweets = Tweet.fromJsonArray(jsonArray);
                    final List<User> users = User.fromJsonTweetArray(tweets);
                    adapter.addAll(tweets);

                    saveLatestTweetsToDatabase(tweets, users);

                    binding.timelineSwipeRefreshLayout.setRefreshing(false);
                } catch (Exception e) {
                    Log.e(TAG, "populateHomeTimeline add fail", e);
                    binding.timelineSwipeRefreshLayout.setRefreshing(false);
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "error" + response, throwable);
                populateFromDatabase();
                binding.timelineSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void saveLatestTweetsToDatabase(List<Tweet> tweets, List<User> users) {
        executorService
                .execute(() -> ((TweeterApplication) getApplicationContext()).getTweeterDatabase()
                        .runInTransaction(() -> {
                            tweetDao.insertModel(users.toArray(new User[0]));
                            tweetDao.insertModel(tweets.toArray(new Tweet[0]));

                            Log.d(TAG, "Tweets inserted");
                        }));
    }

    private void populateFromDatabase() {
        activity.runOnUiThread(() -> Toast.makeText(activity, R.string.timeline_no_connectivity_get_tweets, Toast.LENGTH_SHORT).show());

        executorService.execute(() -> {
            List<TweetWithUser> tweetsFromDatabase = tweetDao.recentItems();
            activity.runOnUiThread(() -> adapter.clear());
            List<Tweet> tweetList = TweetWithUser.getTweetList(tweetsFromDatabase);
            activity.runOnUiThread(() -> adapter.addAll(tweetList));
        });
    }

    private void loadMoreData() {
        if (tweets.isEmpty()) {
            return;
        }
        binding.timelineSwipeRefreshLayout.setRefreshing(true);

        client.getAdditionalTweets(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG, "load more success");
                JSONArray jsonArray = json.jsonArray;

                try {
                    adapter.addAll(Tweet.fromJsonArray(jsonArray));
                    binding.timelineSwipeRefreshLayout.setRefreshing(false);
                } catch (Exception e) {
                    Log.e(TAG, "loadMoreData add fail", e);
                    binding.timelineSwipeRefreshLayout.setRefreshing(false);
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "error", throwable);
                binding.timelineSwipeRefreshLayout.setRefreshing(false);
            }
        }, tweets.get(tweets.size() - 1).id - 1);
    }

    @Override
    public void onFinishComposeDialog(Tweet tweet) {
        tweets.add(0, tweet);
        adapter.notifyItemChanged(0);
        binding.timelineRecyclerView.scrollToPosition(0);
    }
}