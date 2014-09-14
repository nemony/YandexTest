package com.gmail.yurapapapa.yandextest;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

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

        final ProgressBar pb = (ProgressBar) v.findViewById(R.id.fragment_auth_pb);

        webView = (WebView) v.findViewById(R.id.webView);
        //webView.getSettings().setSaveFormData(false);

        android.webkit.CookieManager cookieManager = android.webkit.CookieManager.getInstance();
        cookieManager.removeAllCookie();
        //cookieManager.setAcceptCookie(false);


        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                pb.setProgress(newProgress);
            }

        });

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                pb.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                pb.setVisibility(View.INVISIBLE);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("yandextest")) {
                    Log.d(TAG, "starts with yandexTest");
                    getActivity().setIntent(new Intent().setData(Uri.parse(url)));
                    ((MyActivity) getActivity()).onLogin();
                    getActivity().getSupportFragmentManager().beginTransaction().
                            replace(android.R.id.content, new ListFragment(), MyActivity.FRAGMENT_LIST_TAG).commit();
                    return true;
                }
                return super.shouldOverrideUrlLoading(view, url);
            }

        });


        webView.loadUrl(MyActivity.AUTH_URL);
        webView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (webView.canGoBack()) {
                        webView.goBack();
                        return true;
                    } else if (!webView.canGoBack()) {
                        getActivity().finish();
                        return true;
                    }
                }
                return false;
            }
        });
        return v;
    }


}
