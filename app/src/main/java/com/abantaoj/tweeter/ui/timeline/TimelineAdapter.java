package com.abantaoj.tweeter.ui.timeline;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.abantaoj.tweeter.R;
import com.abantaoj.tweeter.databinding.ItemTimelineBinding;
import com.abantaoj.tweeter.models.Tweet;

import java.util.List;

public class TimelineAdapter extends RecyclerView.Adapter<TimelineAdapter.ViewHolder> {
    private Context context;
    private List<Tweet> tweets;

    static class ViewHolder extends RecyclerView.ViewHolder {
        ItemTimelineBinding binding;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            binding = ItemTimelineBinding.bind(itemView);
        }
    }

    public TimelineAdapter(Context context, List<Tweet> tweets) {
        this.context = context;
        this.tweets = tweets;
    }

    public void clear() {
        tweets.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<Tweet> tweetList) {
        tweets.addAll(tweetList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TimelineAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_timeline, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TimelineAdapter.ViewHolder holder, int position) {
        Tweet tweet = tweets.get(position);

        holder.binding.setTweet(tweet);
        holder.binding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return tweets.size();
    }
}
