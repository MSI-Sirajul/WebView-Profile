package com.my.profile;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class InfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        // Initialize UI elements
        TextView developerNameText = findViewById(R.id.developer_name_text);
        TextView developerEmailText = findViewById(R.id.developer_email_text);
        TextView appVersionText = findViewById(R.id.app_version_text);
        TextView privacyPolicyUrlText = findViewById(R.id.privacy_policy_url_text);

        // Set text from string resources
        developerNameText.setText(getString(R.string.developer_name));
        developerEmailText.setText(getString(R.string.developer_email));
        appVersionText.setText(getString(R.string.app_version_code));
        privacyPolicyUrlText.setText(getString(R.string.privacy_policy_url));

        // Note: autoLink="web" and autoLink="email" in XML will make these clickable
        // without needing explicit click listeners here.
    }
}