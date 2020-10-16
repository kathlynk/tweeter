package com.abantaoj.tweeter;

import android.app.Application;
import android.content.Context;

import androidx.room.Room;

import com.abantaoj.tweeter.database.TweeterDatabase;
import com.abantaoj.tweeter.services.TwitterClient;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class TweeterApplication extends Application  {
    ExecutorService executorService;
    TweeterDatabase tweeterDatabase;

    @Override
    public void onCreate() {
        super.onCreate();
        executorService = Executors.newFixedThreadPool(4);
        tweeterDatabase = Room.databaseBuilder(this, TweeterDatabase.class, TweeterDatabase.NAME)
                .fallbackToDestructiveMigration()
                .build();
    }

    public static TwitterClient getTwitterClient(Context context) {
        return (TwitterClient) TwitterClient.getInstance(TwitterClient.class, context);
    }

    public TweeterDatabase getTweeterDatabase() {
        return tweeterDatabase;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }
}
