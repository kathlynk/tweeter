package com.abantaoj.tweeter.ui.login;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.abantaoj.tweeter.R;
import com.abantaoj.tweeter.services.TwitterClient;
import com.codepath.oauth.OAuthLoginActionBarActivity;

public class LoginActivity extends OAuthLoginActionBarActivity<TwitterClient> {
    final String TAG = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        findViewById(R.id.login_button).setOnClickListener(v -> getClient().connect());
    }

    @Override
    public void onLoginSuccess() {
        Log.d(TAG, "login success");
    }

    @Override
    public void onLoginFailure(Exception e) {
        Log.e(TAG, "login failed", e);
        Toast.makeText(this, getString(R.string.login_failed), Toast.LENGTH_SHORT).show();
    }
}