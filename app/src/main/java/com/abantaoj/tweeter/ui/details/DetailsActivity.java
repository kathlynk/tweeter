package com.abantaoj.tweeter.ui.details;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import androidx.databinding.DataBindingUtil;

import com.abantaoj.tweeter.R;
import com.abantaoj.tweeter.databinding.ActivityDetailsBinding;
import com.abantaoj.tweeter.models.Tweet;
import com.abantaoj.tweeter.ui.timeline.TimelineActivity;
import com.abantaoj.tweeter.ui.timeline.TimelineAdapter;

import org.parceler.Parcels;

public class DetailsActivity extends AppCompatActivity {
    private ActivityDetailsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_details);

        binding.detailsToolbar.setNavigationOnClickListener(v -> startActivity(new Intent(this, TimelineActivity.class)));

        Tweet tweet = Parcels.unwrap(getIntent().getParcelableExtra(TimelineAdapter.TWEET));

        binding.setTweet(tweet);
        binding.executePendingBindings();
    }
}