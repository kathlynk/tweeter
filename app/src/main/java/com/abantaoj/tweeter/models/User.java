package com.abantaoj.tweeter.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.List;

@Parcel
@Entity
public class User {
    @ColumnInfo
    @PrimaryKey
    public long id;

    @ColumnInfo
    public String name;

    @ColumnInfo
    public String handle;

    @ColumnInfo
    public String profileImageUrl;

    public User() {}

    public User(String name, String handle, String profileImageUrl, long id) {
        this.name = name;
        this.handle = handle;
        this.profileImageUrl = profileImageUrl;
        this.id = id;
    }

    public static User fromJson(JSONObject jsonObject) throws JSONException {
        return new User(
                jsonObject.getString("name"),
                jsonObject.getString("screen_name"),
                jsonObject.getString("profile_image_url_https"),
                jsonObject.getLong("id")
        );
    }

    public static List<User> fromJsonTweetArray(List<Tweet> tweetsFromNetwork) throws JSONException {
        List<User> users = new ArrayList<>();

        for (int i = 0; i < tweetsFromNetwork.size(); i++) {
            users.add(tweetsFromNetwork.get(i).user);
        }

        return users;
    }
}
