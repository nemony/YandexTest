package com.gmail.yurapapapa.yandextest;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MyActivity extends FragmentActivity {
    public static final String CLIENT_ID = "f0ef70586d6f48f488157456fea17322";
    public static final String AUTH_URL =
            "https://oauth.yandex.ru/authorize?response_type=token&client_id=" + CLIENT_ID + "&display=popup";
    private static final String TAG = "MyActivity";
    private static final int GET_ACCOUNT_CREDS_INTENT = 100;
    public static String FRAGMENT_TAG = "ListFragment";
    public static String USERNAME = "username";
    public static String TOKEN = "token";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        if (getIntent() != null && getIntent().getData() != null) {
            onLogin();
        }
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String token = preferences.getString(TOKEN, null);
        if(token == null) {
            getSupportFragmentManager().beginTransaction().
                    replace(android.R.id.content, new AuthFragment(), "authFrag").commit();
            return;
        }
        if(savedInstanceState == null) {
            Log.d(TAG, "starting Fragment");
            startFragment();
        }

    }




    private void startFragment() {
        getSupportFragmentManager().beginTransaction().
                replace(android.R.id.content, new ListFragment(), FRAGMENT_TAG).commit();
    }
    private void onLogin() {
        Log.d(TAG, "onLogin");
        Uri data = getIntent().getData();
        setIntent(null);
        Pattern pattern = Pattern.compile("access_token=(.*?)(&|$)");
        Matcher matcher = pattern.matcher(data.toString());
        if (matcher.find()) {
            final String token = matcher.group(1);
            if(!TextUtils.isEmpty(token)) {
                Log.d(TAG, "onLogin: token = " + token);
                Log.d(TAG, "data " + data.toString());
                saveToken(token);
            } else {
                Log.d(TAG, "onRegistrationSuccess: emptyToken");
            }
        } else {
            Log.d(TAG, "onRegistrationSuccess: token not found in returned url");
        }
    }
    private void saveToken(String token) {
        Log.d(TAG, "saveToken");
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putString(USERNAME, "");
        editor.putString(TOKEN, token);
        editor.apply();
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed");
        Fragment fragment = getSupportFragmentManager().findFragmentById(android.R.id.content);

        if (fragment.getTag().equals(FRAGMENT_TAG)) {
            ((ListFragment)fragment).onBackPressed();
        } else {
            super.onBackPressed();
        }
    }
}