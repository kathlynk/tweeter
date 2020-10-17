package com.abantaoj.tweeter.ui.compose;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;

import com.abantaoj.tweeter.R;
import com.abantaoj.tweeter.TweeterApplication;
import com.abantaoj.tweeter.databinding.FragmentComposeBinding;
import com.abantaoj.tweeter.models.Tweet;
import com.abantaoj.tweeter.services.TwitterClient;
import com.abantaoj.tweeter.ui.timeline.TimelineActivity;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONException;

import java.util.StringJoiner;

import okhttp3.Headers;

public class ComposeFragment extends DialogFragment {

    private int MAX_TWEET_LENGTH = 280;
    private FragmentComposeBinding binding;
    private String TAG = this.getClass().getSimpleName();
    private String COMPOSE_TWEET = "COMPOSE_TWEET";
    private SharedPreferences pref;

    public interface ComposeDialogListener {
        void onFinishComposeDialog(Tweet tweet);
    }

    public ComposeFragment() {

    }

    public static ComposeFragment newInstance() {
        return new ComposeFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_compose, container, false);
        pref = PreferenceManager.getDefaultSharedPreferences(getContext());
        String existingText = pref.getString(COMPOSE_TWEET, "");

        if (getArguments() != null) {
            String titleOfIntent = getArguments().getString(TimelineActivity.SEND_ACTION_TITLE);
            String urlOfIntent = getArguments().getString(TimelineActivity.SEND_ACTION_URL);
            StringBuilder builder = new StringBuilder();

            if (titleOfIntent != null) {
                builder.append(titleOfIntent);
                builder.append("\n");
            }

            if (urlOfIntent != null) {
                builder.append(urlOfIntent);
            }

            String body = builder.toString();
            binding.composeTweetMultilineTextView.setText(body);
            updateCharactersRemainingText(body.length());
        } else if (existingText != null) {
            binding.composeTweetMultilineTextView.setText(existingText);
            SharedPreferences.Editor edit = pref.edit();
            edit.clear();
            edit.apply();

            updateCharactersRemainingText(existingText.length());
        } else {
            updateCharactersRemainingText(0);
        }

        binding.composeToolbar.setNavigationOnClickListener(v -> dismiss());

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

        binding.composeTweetMultilineTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                updateCharactersRemainingText(s.length());
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        WindowManager.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        getDialog().getWindow().setAttributes(params);
        super.onResume();
    }

    @Override
    public void dismiss() {
        String tweetText = binding.composeTweetMultilineTextView.getText().toString();

        if (!tweetText.isEmpty()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage(R.string.dialog_save_draft)
                    .setCancelable(false)
                    .setPositiveButton(R.string.yes, (dialog, id) -> {
                        SharedPreferences pref =
                                PreferenceManager.getDefaultSharedPreferences(getContext());
                        SharedPreferences.Editor edit = pref.edit();
                        edit.putString(COMPOSE_TWEET, tweetText);
                        edit.apply();

                        super.dismiss();
                    })
                    .setNegativeButton(R.string.no, (dialog, id) -> {
                        super.dismiss();
                    });
            AlertDialog alert = builder.create();
            alert.show();
        } else {
            super.dismiss();
        }
    }

    private void showToastError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void postTweet(String tweetContent) {
        TwitterClient client = TweeterApplication.getTwitterClient(getContext());

        client.postTweet(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                try {
                    Tweet tweet = Tweet.fromJson(json.jsonObject);
                    ComposeDialogListener listener = (ComposeDialogListener) getActivity();
                    assert listener != null;
                    listener.onFinishComposeDialog(tweet);
                    forceDismiss();
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

    private void forceDismiss() {
        super.dismiss();
    }

    private void updateCharactersRemainingText(int currentChars) {
        Resources res = getResources();
        int charsRemain = Math.max(MAX_TWEET_LENGTH - currentChars, 0);

        binding.composeTweetCharsRemainTextView.setText(res.getQuantityString(R.plurals.compose_tweet_chars_remain, charsRemain, charsRemain));
    }
}