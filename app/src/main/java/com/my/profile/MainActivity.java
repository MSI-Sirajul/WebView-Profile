package com.my.profile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.my.profile.util.NetworkUtil;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "AppPrefs";
    private static final String KEY_IS_SETUP = "isSetup";
    private static final String KEY_APP_NAME = "appName";
    private static final String KEY_APP_ICON = "appIcon";
    private static final String KEY_WEB_URL = "webUrl"; // Also need to retrieve webUrl here

    private static final int SPLASH_DISPLAY_LENGTH = 2000; // 2 seconds
    private static final int LOADING_DISPLAY_LENGTH = 3000; // 3 seconds for initial loading

    private View splashScreenLayout;
    private View loadingAnimationLayout;
    private View noInternetLayout;
    private ImageView splashAppIcon;
    private TextView splashAppName;

    private String defaultWebUrl; // To store the URL loaded from SharedPreferences

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        splashScreenLayout = findViewById(R.id.splash_screen_layout);
        loadingAnimationLayout = findViewById(R.id.loading_animation_layout);
        noInternetLayout = findViewById(R.id.no_internet_layout);
        splashAppIcon = findViewById(R.id.splash_app_icon);
        splashAppName = findViewById(R.id.splash_app_name);

        // Hide all extra layouts initially, only splash is visible by default in XML
        loadingAnimationLayout.setVisibility(View.GONE);
        noInternetLayout.setVisibility(View.GONE);

        loadAppSettingsAndStart();
    }

    private void loadAppSettingsAndStart() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isSetup = prefs.getBoolean(KEY_IS_SETUP, false);

        if (!isSetup) {
            // If not set up, launch AppSetupActivity
            startActivity(new Intent(MainActivity.this, AppSetupActivity.class));
            finish();
            return;
        }

        // Load saved app name, icon, and URL
        String savedAppName = prefs.getString(KEY_APP_NAME, getString(R.string.app_name));
        String encodedAppIcon = prefs.getString(KEY_APP_ICON, null);
        defaultWebUrl = prefs.getString(KEY_WEB_URL, "https://www.google.com"); // Fallback URL

        // Update splash screen with saved data
        splashAppName.setText(savedAppName);
        if (encodedAppIcon != null) {
            try {
                byte[] decodedString = Base64.decode(encodedAppIcon, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                splashAppIcon.setImageBitmap(decodedByte);
            } catch (IllegalArgumentException e) {
                // Handle invalid Base64 string, fall back to default icon
                splashAppIcon.setImageResource(R.mipmap.ic_launcher);
                Toast.makeText(this, "Failed to load custom icon, using default.", Toast.LENGTH_SHORT).show();
            }
        } else {
            splashAppIcon.setImageResource(R.mipmap.ic_launcher);
        }

        // Show splash screen for a fixed duration
        new Handler(Looper.getMainLooper()).postDelayed(this::checkInternetAndProceed, SPLASH_DISPLAY_LENGTH);
    }

    private void checkInternetAndProceed() {
        // Hide splash screen and show loading animation
        splashScreenLayout.setVisibility(View.GONE);
        loadingAnimationLayout.setVisibility(View.VISIBLE);

        if (NetworkUtil.isInternetConnected(this)) {
            // Internet connected, proceed to WebViewActivity after loading animation
            new Handler(Looper.getMainLooper()).postDelayed(this::startWebViewActivity, LOADING_DISPLAY_LENGTH);
        } else {
            // No internet, show no internet layout
            loadingAnimationLayout.setVisibility(View.GONE);
            noInternetLayout.setVisibility(View.VISIBLE);

            // Set up retry button for no internet layout
            Button retryButton = noInternetLayout.findViewById(R.id.retry_button);
            if (retryButton != null) {
                retryButton.setOnClickListener(v -> {
                    if (NetworkUtil.isInternetConnected(MainActivity.this)) {
                        // Internet is back, restart the process
                        loadingAnimationLayout.setVisibility(View.VISIBLE);
                        noInternetLayout.setVisibility(View.GONE);
                        new Handler(Looper.getMainLooper()).postDelayed(MainActivity.this::startWebViewActivity, LOADING_DISPLAY_LENGTH);
                    } else {
                        Toast.makeText(MainActivity.this, R.string.no_internet_message, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    private void startWebViewActivity() {
        Intent intent = new Intent(MainActivity.this, WebViewActivity.class);
        intent.putExtra(KEY_WEB_URL, defaultWebUrl); // Pass the URL to WebViewActivity
        startActivity(intent);
        finish(); // Finish MainActivity so user can't go back to splash/loading
    }
}