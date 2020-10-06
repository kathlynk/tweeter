package com.abantaoj.tweeter.utils;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.databinding.BindingAdapter;

import com.bumptech.glide.Glide;

public class BindingAdapterUtils {
    @BindingAdapter({"profileImageUrl"})
    public static void loadProfileImage(ImageView view, String url) {
        Glide.with(view.getContext())
                .load(url)
                .circleCrop()
                .placeholder(new ColorDrawable(Color.GRAY))
                .error(new ColorDrawable(Color.GRAY))
                .into(view);
    }

    @BindingAdapter({"createdAt"})
    public static void getTimestamp(TextView view, String createdAt) {
        view.setText(TimeFormatterUtils.getTimeDifference(createdAt));
    }
}
