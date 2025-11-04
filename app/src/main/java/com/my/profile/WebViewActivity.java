package com.my.profile;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.my.profile.util.NetworkUtil; // For re-checking network if needed

public class WebViewActivity extends AppCompatActivity {

    private static final String KEY_WEB_URL = "webUrl";
    private static final int REFRESH_ANIMATION_DISPLAY_LENGTH = 3000; // 3 seconds for pull-to-refresh animation

    private WebView mainWebView;
    private ProgressBar webViewProgressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private String currentUrl; // To store the current URL, useful for refresh

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        mainWebView = findViewById(R.id.main_webview);
        webViewProgressBar = findViewById(R.id.web_view_progress_bar);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);

        // Get the URL from the intent
        if (getIntent().hasExtra(KEY_WEB_URL)) {
            currentUrl = getIntent().getStringExtra(KEY_WEB_URL);
        } else {
            // Fallback if no URL is passed (should not happen if MainActivity passes it correctly)
            currentUrl = "https://www.google.com";
            Toast.makeText(this, "No URL provided, loading default.", Toast.LENGTH_SHORT).show();
        }

        setupWebView();
        setupSwipeRefreshLayout();

        // Initial load
        loadUrl(currentUrl);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        WebSettings webSettings = mainWebView.getSettings();
        webSettings.setJavaScriptEnabled(true); // JavaScript support
        webSettings.setDomStorageEnabled(true); // DOM storage support
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT); // Cache support
        webSettings.setAllowFileAccess(true); // Allow file access
        webSettings.setAppCacheEnabled(true); // Enable app cache
        webSettings.setDatabaseEnabled(true); // Enable database storage
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setBuiltInZoomControls(true); // Enable zoom controls
        webSettings.setDisplayZoomControls(false); // Hide zoom controls on screen
        webSettings.setSupportZoom(true); // Allow zooming
        webSettings.setPluginState(WebSettings.PluginState.ON); // Enable plugins (if any)
        webSettings.setMediaPlaybackRequiresUserGesture(false); // Allow autoplay for media

        // Handle mixed content (HTTP and HTTPS) if necessary
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        // Custom WebViewClient to handle page navigation within the WebView
        mainWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // If the URL is mailto:, tel:, or other special schemes, open with external app
                if (url.startsWith("mailto:") || url.startsWith("tel:") || url.startsWith("sms:") || url.startsWith("geo:")) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                    return true;
                }
                // Otherwise, load the URL in the WebView itself
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                webViewProgressBar.setVisibility(View.VISIBLE);
                webViewProgressBar.setProgress(0);
                currentUrl = url; // Update current URL
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                webViewProgressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false); // Stop refresh animation
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                webViewProgressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                // Optionally show a custom error page or a Toast
                Toast.makeText(WebViewActivity.this, "Error loading page: " + description, Toast.LENGTH_LONG).show();

                if (!NetworkUtil.isInternetConnected(WebViewActivity.this)) {
                    // If network error, navigate to NoInternetActivity
                    Intent intent = new Intent(WebViewActivity.this, NoInternetActivity.class);
                    startActivity(intent);
                    finish(); // Close WebViewActivity
                }
            }
        });

        // Custom WebChromeClient to handle progress, title, and favicon
        mainWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                webViewProgressBar.setProgress(newProgress);
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                // Optionally update activity title, though we don't have an ActionBar
                // For a WebView-only app, you might not display the title.
            }
        });
    }

    private void setupSwipeRefreshLayout() {
        swipeRefreshLayout.setColorSchemeResources(
                R.color.colorPrimary, // Custom colors for refresh indicator
                R.color.colorSecondary
        );
        swipeRefreshLayout.setOnRefreshListener(() -> {
            // Show custom loading animation for 3 seconds while refreshing in background
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (mainWebView != null) {
                    mainWebView.reload(); // Reload the current URL
                }
            }, REFRESH_ANIMATION_DISPLAY_LENGTH);
            // The onPageFinished in WebViewClient will setRefreshing(false)
        });
    }

    private void loadUrl(String url) {
        if (NetworkUtil.isInternetConnected(this)) {
            mainWebView.loadUrl(url);
        } else {
            // Navigate to NoInternetActivity if no internet before loading
            Intent intent = new Intent(this, NoInternetActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Handle back button press for WebView navigation
        if (keyCode == KeyEvent.KEYCODE_BACK && mainWebView.canGoBack()) {
            mainWebView.goBack();
            return true;
        }
        // If WebView can't go back, let the system handle it (finish activity)
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mainWebView != null) {
            mainWebView.destroy(); // Important to release WebView resources
        }
    }
}