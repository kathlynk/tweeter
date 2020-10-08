package com.abantaoj.tweeter.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Tweet {
    public long id;
    public String body;
    public String createdAt;
    public User user;

    public Tweet(long id, String body, String createdAt, User user) {
        this.id = id;
        this.body = body;
        this.createdAt = createdAt;
        this.user = user;
    }

    public static Tweet fromJson(JSONObject jsonObject) throws JSONException {
        return new Tweet(
                jsonObject.getLong("id"),
                jsonObject.getString("text"),
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
