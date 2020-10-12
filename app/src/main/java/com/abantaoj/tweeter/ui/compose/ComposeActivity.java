package com.abantaoj.tweeter.ui.compose;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.abantaoj.tweeter.R;
import com.abantaoj.tweeter.TweeterApplication;
import com.abantaoj.tweeter.databinding.ActivityComposeBinding;
import com.abantaoj.tweeter.models.Tweet;
import com.abantaoj.tweeter.services.TwitterClient;
import com.abantaoj.tweeter.ui.timeline.TimelineActivity;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONException;
import org.parceler.Parcels;

import okhttp3.Headers;

public class ComposeActivity extends AppCompatActivity {

    private int MAX_TWEET_LENGTH = 280;
    private ActivityComposeBinding binding;
    private String TAG = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_compose);

        binding.composeToolbar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(this, TimelineActivity.class);
            setResult(RESULT_CANCELED, intent);
            finish();
        });

        binding.composeTweetButton.setOnClickListener(v -> {
            String tweetContent = binding.composeTweetMultilineTextView.getText().toString();

            if (tweetContent.isEmpty()) {
                showToastError(getString(R.string.compose_tweet_empty_error));
            } else if (tweetContent.length() > MAX_TWEET_LENGTH) {
                showToastError(getString(R.string.compose_tweet_too_long_error));
            } else {
                postTweet(tweetContent);
            }
        });
    }

    private void showToastError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void postTweet(String tweetContent) {
        TwitterClient client = TweeterApplication.getTwitterClient(this);

        client.postTweet(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                try {
                    Tweet tweet = Tweet.fromJson(json.jsonObject);
                    Intent intent = new Intent();
                    intent.putExtra(TAG, Parcels.wrap(tweet));
                    setResult(RESULT_OK, intent);
                    finish();
                } catch (JSONException e) {
                    Log.e(TAG, "onSuccess fail", e);
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "post error", throwable);
            }
        }, tweetContent);

    }
}