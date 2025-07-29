package com.sirajul.profile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView gifImage = findViewById(R.id.splash);

        // Load animated GIF using Glide
        Glide.with(this)
                .asGif()
                .load(R.mipmap.splash) // Place your GIF in res/drawable/loading.gif
                .into(gifImage);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish();
            }
        }, 1500);
    }
}