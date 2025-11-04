package com.my.profile;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AppSetupActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "AppPrefs";
    private static final String KEY_IS_SETUP = "isSetup";
    private static final String KEY_APP_NAME = "appName";
    private static final String KEY_WEB_URL = "webUrl";
    private static final String KEY_APP_ICON = "appIcon"; // Base64 encoded string of the icon

    private ImageView appIconPicker;
    private EditText inputAppName;
    private EditText inputWebUrl;
    private Button buttonSave;
    private ImageButton infoButton;
    private TextView packageNameText;

    private Uri selectedImageUri;

    // ActivityResultLauncher for picking image from gallery
    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    appIconPicker.setImageURI(uri);
                } else {
                    Toast.makeText(this, R.string.image_select_error, Toast.LENGTH_SHORT).show();
                }
            }
    );

    // ActivityResultLauncher for requesting permissions
    private final ActivityResultLauncher<String[]> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            permissions -> {
                boolean allGranted = true;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
                    if (permissions.containsKey(Manifest.permission.READ_MEDIA_IMAGES) && !permissions.get(Manifest.permission.READ_MEDIA_IMAGES)) {
                        allGranted = false;
                    }
                } else { // Older Android versions
                    if (permissions.containsKey(Manifest.permission.READ_EXTERNAL_STORAGE) && !permissions.get(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        allGranted = false;
                    }
                }

                if (allGranted) {
                    pickImageLauncher.launch("image/*");
                } else {
                    Toast.makeText(this, R.string.permission_denied_message, Toast.LENGTH_LONG).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if the app is already set up
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isSetup = prefs.getBoolean(KEY_IS_SETUP, false);

        if (isSetup) {
            // If already set up, go directly to MainActivity
            startActivity(new Intent(AppSetupActivity.this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_app_setup);

        // Initialize UI elements
        appIconPicker = findViewById(R.id.app_icon_picker);
        inputAppName = findViewById(R.id.input_app_name);
        inputWebUrl = findViewById(R.id.input_web_url);
        buttonSave = findViewById(R.id.button_save);
        infoButton = findViewById(R.id.info_button);
        packageNameText = findViewById(R.id.package_name_text);

        packageNameText.setText(getPackageName()); // Set package name dynamically

        // Set listeners
        appIconPicker.setOnClickListener(v -> requestImagePickPermission());
        buttonSave.setOnClickListener(v -> saveAppSettings());
        infoButton.setOnClickListener(v -> startActivity(new Intent(AppSetupActivity.this, InfoActivity.class)));

        // Load default icon if no custom icon selected yet (initial state)
        appIconPicker.setImageResource(R.mipmap.ic_launcher);
    }

    private void requestImagePickPermission() {
        String[] permissions;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            permissions = new String[]{Manifest.permission.READ_MEDIA_IMAGES};
        } else { // Older Android versions
            permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
        }

        if (ContextCompat.checkSelfPermission(this, permissions[0]) == PackageManager.PERMISSION_GRANTED) {
            // Permission already granted, proceed to pick image
            pickImageLauncher.launch("image/*");
        } else {
            // Request permission
            requestPermissionLauncher.launch(permissions);
        }
    }

    private void saveAppSettings() {
        String appName = inputAppName.getText().toString().trim();
        String webUrl = inputWebUrl.getText().toString().trim();

        if (TextUtils.isEmpty(appName)) {
            inputAppName.setError(getString(R.string.app_name_validation_error));
            return;
        }

        if (TextUtils.isEmpty(webUrl) || !isValidUrl(webUrl)) {
            inputWebUrl.setError(getString(R.string.url_validation_error));
            return;
        }

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString(KEY_APP_NAME, appName);
        editor.putString(KEY_WEB_URL, webUrl);

        // Convert selected image to Base64 string and save
        if (selectedImageUri != null) {
            try {
                InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                if (bitmap != null) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos); // Use PNG for transparency
                    byte[] b = baos.toByteArray();
                    String encodedImage = android.util.Base64.encodeToString(b, android.util.Base64.DEFAULT);
                    editor.putString(KEY_APP_ICON, encodedImage);
                }
            } catch (Exception e) {
                Log.e("AppSetupActivity", "Error encoding image: " + e.getMessage());
                Toast.makeText(this, R.string.image_select_error, Toast.LENGTH_SHORT).show();
            }
        } else {
            // If no custom image is selected, save the default launcher icon as Base64
            Bitmap defaultIconBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            defaultIconBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] b = baos.toByteArray();
            String encodedImage = android.util.Base64.encodeToString(b, android.util.Base64.DEFAULT);
            editor.putString(KEY_APP_ICON, encodedImage);
        }


        editor.putBoolean(KEY_IS_SETUP, true);
        editor.apply();

        // Dynamically change app name and icon (this typically requires reinstall or system restart,
        // but we'll simulate the change for subsequent app launches)
        // For actual runtime icon change, it's more complex and involves PackageManager,
        // often requiring a manifest alias and system refresh.
        // For this app, we'll ensure these are loaded correctly on subsequent starts.

        Toast.makeText(this, "Settings saved!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(AppSetupActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private boolean isValidUrl(String url) {
        // Simple regex for URL validation (can be made more robust)
        String urlRegex = "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
        Pattern pattern = Pattern.compile(urlRegex);
        Matcher matcher = pattern.matcher(url);
        return matcher.matches();
    }
}