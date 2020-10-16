package com.abantaoj.tweeter.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.List;

@Parcel
@Entity(foreignKeys = @ForeignKey(entity = User.class, parentColumns = "id", childColumns = "userId"))
public class Tweet {
    @PrimaryKey
    @ColumnInfo
    public long id;

    @ColumnInfo
    public String body;

    @ColumnInfo
    public String createdAt;

    @ColumnInfo
    public long userId;

    @Ignore
    public User user;

    public Tweet() {}

    public Tweet(long id, String body, String createdAt, User user) {
        this.id = id;
        this.body = body;
        this.createdAt = createdAt;
        this.user = user;
        this.userId = user.id;
    }

    public static Tweet fromJson(JSONObject jsonObject) throws JSONException {
        User user = User.fromJson(jsonObject.getJSONObject("user"));

        return new Tweet(
                jsonObject.getLong("id"),
                jsonObject.getString("full_text"),
                jsonObject.getString("created_at"),
                User.fromJson(jsonObject.getJSONObject("user"))
        );
    }

    public static List<Tweet> fromJsonArray(JSONArray jsonArray) throws JSONException {
        List<Tweet> tweets = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
           tweets.add(Tweet.fromJson(jsonArray.getJSONObject(i)));
        }

        return tweets;
    }
}
