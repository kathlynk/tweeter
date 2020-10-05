package com.abantaoj.tweeter;

import android.app.Application;
import android.content.Context;

import com.abantaoj.tweeter.services.TwitterClient;


public class TweeterApplication extends Application  {
    @Override
    public void onCreate() {
        super.onCreate();
    }

    public static TwitterClient getTwitterClient(Context context) {
        return (TwitterClient) TwitterClient.getInstance(TwitterClient.class, context);
    }
}
