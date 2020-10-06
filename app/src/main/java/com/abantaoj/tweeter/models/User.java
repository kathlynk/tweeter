package com.abantaoj.tweeter.models;

import org.json.JSONException;
import org.json.JSONObject;

public class User {
    public String name;
    public String handle;
    public String profileImageUrl;

    public User(String name, String handle, String profileImageUrl) {
        this.name = name;
        this.handle = handle;
        this.profileImageUrl = profileImageUrl;
    }

    public static User fromJson(JSONObject jsonObject) throws JSONException {
        return new User(
                jsonObject.getString("name"),
                jsonObject.getString("screen_name"),
                jsonObject.getString("profile_image_url_https")
        );
    }
}
