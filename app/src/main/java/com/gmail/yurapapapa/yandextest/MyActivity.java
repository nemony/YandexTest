package com.gmail.yurapapapa.yandextest;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorDescription;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MyActivity extends FragmentActivity {
    private static final String TAG = "MyActivity";

    private static final int GET_ACCOUNT_CREDS_INTENT = 100;

    public static String FRAGMENT_TAG = "ListFragment";

    public static final String CLIENT_ID = "f0ef70586d6f48f488157456fea17322";
    public static final String CLIENT_SECRET = "9eb9ac0053714f9e86719339a65f416c";

    public static final String ACCOUNT_TYPE = "com.yandex";
    public static final String AUTH_URL = "https://oauth.yandex.ru/authorize?response_type=token&client_id="+CLIENT_ID;

    private static final String ACTION_ADD_ACCOUNT = "com.yandex.intent.ADD_ACCOUNT";
    private static final String KEY_CLIENT_SECRET = "clientSecret";

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
            getToken();
            return;
        }
        if(savedInstanceState == null) {
            startFragment();
        }

    }
    private void getToken() {
        Log.d(TAG, "getToken");
        AccountManager accountManager = AccountManager.get(getApplicationContext());
        Account[] accounts = accountManager.getAccountsByType(ACCOUNT_TYPE);
        Log.d(TAG, "accounts: " + (accounts != null ? accounts.length : null));
        if (accounts != null && accounts.length > 0) {
            // get the first account, for example (you must show the list and allow user to choose)
            Account account = accounts[0];
            Log.d(TAG, "account: " + account);
            getAuthToken(account);
            return;
        }
        Log.d(TAG, "No such accounts: " + ACCOUNT_TYPE);
        for (AuthenticatorDescription authDesc : accountManager.getAuthenticatorTypes()) {
            if (ACCOUNT_TYPE.equals(authDesc.type)) {
                Log.d(TAG, "Starting " + ACTION_ADD_ACCOUNT);
                Intent intent = new Intent(ACTION_ADD_ACCOUNT);
                startActivityForResult(intent, GET_ACCOUNT_CREDS_INTENT);
                return;
            }
        }
        // no account found for com.yandex
        new AuthDialogFragment().show(getSupportFragmentManager(), "auth");
    }
    private void getAuthToken(Account account) {
        Log.d(TAG, "getAuthToken");
        AccountManager systemAccountManager = AccountManager.get(getApplicationContext());
        Bundle options = new Bundle();
        options.putString(KEY_CLIENT_SECRET, CLIENT_SECRET);
        systemAccountManager.getAuthToken(account, CLIENT_ID, options, this, new GetAuthTokenCallbacks(), null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult resultCode = " + resultCode);
    }

    private class GetAuthTokenCallbacks implements AccountManagerCallback<Bundle> {

        @Override
        public void run(AccountManagerFuture<Bundle> future) {
            Log.d(TAG, "GetAuthTokenCallbacks");
            try {
                Bundle bundle = future.getResult();
                Log.d(TAG, "bundle = " + bundle);

                String message = (String) bundle.get(AccountManager.KEY_ERROR_MESSAGE);
                if (message != null) {
                    Toast.makeText(MyActivity.this, message, Toast.LENGTH_LONG).show();
                }
                Intent intent = (Intent) bundle.get(AccountManager.KEY_INTENT);
                Log.d(TAG, "intent = " + intent);
                if (intent != null) {
                    // User input required
                    startActivityForResult(intent, GET_ACCOUNT_CREDS_INTENT);
                } else {
                    String token = bundle.getString(AccountManager.KEY_AUTHTOKEN);
                    Log.d(TAG, "GetAuthTokenCallback token = " + token);
                    saveToken(token);
                    startFragment();
                }
            } catch (Exception e) {
                Log.d(TAG, "GetAuthTokenCallback" + e.toString());
            }
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