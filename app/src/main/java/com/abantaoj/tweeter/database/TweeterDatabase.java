package com.abantaoj.tweeter.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.abantaoj.tweeter.models.TweetDao;
import com.abantaoj.tweeter.models.Tweet;
import com.abantaoj.tweeter.models.User;

@Database(entities={Tweet.class, User.class}, version=1)
public abstract class TweeterDatabase extends RoomDatabase {
    public abstract TweetDao tweetDao();

    public static final String NAME = "TweeterDatabase";
}
