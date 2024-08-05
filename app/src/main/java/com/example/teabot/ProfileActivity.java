package com.example.teabot;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView profileImage;
    private EditText profileName;
    private EditText profileEmail;
    private Button saveProfileButton;
    private Button changeProfileImageButton;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private DocumentReference profileRef;

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

        setContentView(R.layout.activity_profile);

        // Initialize UI components
        profileImage = findViewById(R.id.profileImage);
        profileName = findViewById(R.id.profileName);
        profileEmail = findViewById(R.id.profileEmail);
        saveProfileButton = findViewById(R.id.saveProfileButton);
        changeProfileImageButton = findViewById(R.id.changeProfileImageButton);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Get current user
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            profileRef = db.collection("Users").document(userId);

            // Load profile information
            loadProfileInfo();

            // Set listeners
            changeProfileImageButton.setOnClickListener(v -> openImagePicker());

            saveProfileButton.setOnClickListener(v -> saveProfileInfo());
        } else {
            // Handle the case where the user is not logged in
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show();
            // Optionally, redirect to login activity
            finish();
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            profileImage.setImageURI(imageUri);
            // You might want to upload this image to your backend or save it locally.
        }
    }

    private void loadProfileInfo() {
        profileRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String name = documentSnapshot.getString("name");
                String email = documentSnapshot.getString("email");
                String profileImageUri = documentSnapshot.getString("profileImageUri");

                profileName.setText(name);
                profileEmail.setText(email);
                if (profileImageUri != null && !profileImageUri.isEmpty()) {
                    profileImage.setImageURI(Uri.parse(profileImageUri));
                }
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(ProfileActivity.this, "Failed to load profile info", Toast.LENGTH_SHORT).show();
        });
    }

    private void saveProfileInfo() {
        String name = profileName.getText().toString();
        String email = profileEmail.getText().toString();

        profileRef.update("name", name, "email", email)
                .addOnSuccessListener(aVoid -> Toast.makeText(ProfileActivity.this, "Profile information saved", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(ProfileActivity.this, "Failed to save profile info", Toast.LENGTH_SHORT).show());
    }
}
