package com.gmail.yurapapapa.yandextest;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.HttpAuthHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by yurap_000 on 014 14 сен.
 */
public class AuthFragment extends Fragment {
    private static final String TAG = "AuthFragment";
    WebView webView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_auth, container, false);
        Log.d(TAG, "onCreateView");
        webView = (WebView) v.findViewById(R.id.webView);
        //webView.getSettings().setSaveFormData(false);

        android.webkit.CookieManager cookieManager = android.webkit.CookieManager.getInstance();
        cookieManager.removeAllCookie();
        //cookieManager.setAcceptCookie(false);


        final AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                getActivity().setProgress(newProgress * 1000);
            }
        });
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(failingUrl));
                startActivity(intent);
            }

            @Override
            public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
                super.onReceivedHttpAuthRequest(view, handler, host, realm);
                Log.d(TAG, "onReceivedHttpAuthRequest");
            }
        });


        webView.loadUrl("https://oauth.yandex.ru/authorize?response_type=token&client_id=" +
                MyActivity.CLIENT_ID + "&display=popup");
        webView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
                    webView.goBack();
                    return true;
                }
                return false;
            }
        });
        return v;
    }


}
