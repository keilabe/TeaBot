package com.example.teabot;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private Switch notificationsSwitch;
    private Spinner languageSpinner;
    private Button manageProfileButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE);
        boolean isDarkMode = sharedPreferences.getBoolean("isDarkMode", false);

        if (isDarkMode) {
            setTheme(R.style.DarkTheme);
        } else {
            setTheme(R.style.AppTheme); // Default theme
        }

        setContentView(R.layout.activity_settings);

        // Initialize UI components
        notificationsSwitch = findViewById(R.id.notificationsSwitch);
        languageSpinner = findViewById(R.id.languageSpinner);
        manageProfileButton = findViewById(R.id.manageProfileButton);

        // Set up the language spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.languages_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        languageSpinner.setAdapter(adapter);

        // Load and save preferences (for demonstration purposes, assume SharedPreferences is used)
        loadPreferences();

        // Set listeners
        notificationsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Handle enabling/disabling notifications
            savePreference("notifications_enabled", isChecked);
        });

        languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Handle language change
                String selectedLanguage = (String) parentView.getItemAtPosition(position);
                savePreference("selected_language", selectedLanguage);
                Toast.makeText(SettingsActivity.this, "Language changed to " + selectedLanguage, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing
            }
        });

        manageProfileButton.setOnClickListener(v -> {
            // Handle managing profile
            Intent intent = new Intent(SettingsActivity.this, ProfileActivity.class);
            startActivity(intent);
        });
    }

    private void saveThemePreference(boolean isDarkMode) {
        SharedPreferences sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isDarkMode", isDarkMode);
        editor.apply();
    }


    private void loadPreferences() {
        // Load preferences (for demonstration purposes, assume SharedPreferences is used)
        // SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        // boolean notificationsEnabled = prefs.getBoolean("notifications_enabled", true);
        // String selectedLanguage = prefs.getString("selected_language", "English");

        // notificationsSwitch.setChecked(notificationsEnabled);
        // int spinnerPosition = ((ArrayAdapter) languageSpinner.getAdapter()).getPosition(selectedLanguage);
        // languageSpinner.setSelection(spinnerPosition);
    }

    private void savePreference(String key, Object value) {
        // Save preferences (for demonstration purposes, assume SharedPreferences is used)
        // SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        // SharedPreferences.Editor editor = prefs.edit();
        // if (value instanceof Boolean) {
        //     editor.putBoolean(key, (Boolean) value);
        // } else if (value instanceof String) {
        //     editor.putString(key, (String) value);
        // }
        // editor.apply();
    }
}
