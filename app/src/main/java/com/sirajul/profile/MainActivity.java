package com.sirajul.profile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class MainActivity extends Activity {

    private WebView webView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RelativeLayout noInternetLayout;
    private Button reloadButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Remove title bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        // Show status bar and set custom color
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setStatusBarColor(Color.parseColor("#FFFFFFFF"));

        setContentView(R.layout.activity_main);

        // Initialize views
        webView = findViewById(R.id.webView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        noInternetLayout = findViewById(R.id.noInternetLayout);
        reloadButton = findViewById(R.id.reloadButton);

        // Pull to Refresh লিসেনার
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (isConnected()) {
                    webView.reload();
                } else {
                    swipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(MainActivity.this, "No internet connection!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        reloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkAndLoadWeb();
            }
        });

        checkAndLoadWeb();
    }

    private void checkAndLoadWeb() {
        if (isConnected()) {
            noInternetLayout.setVisibility(View.GONE);
            webView.setVisibility(View.VISIBLE);
            initWebView();
            webView.loadUrl("https://md-sirajul-islam.vercel.app/");
        } else {
            webView.setVisibility(View.GONE);
            noInternetLayout.setVisibility(View.VISIBLE);
        }
    }

    private boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            return netInfo != null && netInfo.isConnected();
        }
        return false;
    }

    private void initWebView() {
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                String url = uri.toString();

                if (!url.contains("md-sirajul-islam.vercel.app")) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                    return true;
                }
                return false;
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (!url.contains("md-sirajul-islam.vercel.app")) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                    return true;
                }
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // পেজ লোড শেষ হলে রিফ্রেশ আইকন বন্ধ করুন
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
		webSettings.setDatabaseEnabled(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setAllowFileAccess(true);
		webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
		webSettings.setAppCacheEnabled(true);
		webSettings.setAppCachePath(getCacheDir().getPath());
    }

    @Override
    public void onBackPressed() {
        if (webView.getVisibility() == View.VISIBLE && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroy();
    }
}