package com.my.profile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.my.profile.util.NetworkUtil; // Import NetworkUtil

public class NoInternetActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_no_internet); // Using the included layout directly

        Button retryButton = findViewById(R.id.retry_button);
        retryButton.setOnClickListener(v -> {
            if (NetworkUtil.isInternetConnected(this)) {
                // If internet is back, go back to MainActivity
                Intent intent = new Intent(NoInternetActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, R.string.no_internet_message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        // Optionally, prevent going back from NoInternetActivity or handle it differently
        // For now, let's just finish the activity if user presses back
        super.onBackPressed();
    }
}