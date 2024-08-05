package com.example.teabot;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.content.SharedPreferences;

import android.content.Intent;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    EditText userName, wel;
    ImageView userprofile;


    FirebaseAuth mAuth;
    FirebaseFirestore db;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_THEME = "app_theme";
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        SharedPreferences sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE);
        boolean isDarkMode = sharedPreferences.getBoolean("isDarkMode", false);

        if (isDarkMode) {
            setTheme(R.style.DarkTheme);
        } else {
            setTheme(R.style.AppTheme); // Default theme
        }

        setContentView(R.layout.activity_main);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        FloatingActionButton fab_message = findViewById(R.id.fab_message);
        ImageView menuButton = findViewById(R.id.main_menu);
        ImageView userprofile = findViewById(R.id.profileImageButton);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_settings) {
                    startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                } else if (id == R.id.nav_start_chat) {
                    startActivity(new Intent(MainActivity.this, ChatWindowActivity.class));
                } else if (id == R.id.nav_theme) {
                    toggleTheme();
                } else if (id == R.id.nav_logout) {
                    showLogoutConfirmationDialog();
                }

                drawerLayout.closeDrawers();
                return true;
            }

        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.chat), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        fab_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ImagePickerActivity.class);
                startActivity(intent);
            }
        });


        userprofile.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        menuButton.setOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });


        userName = findViewById(R.id.userName);
        wel = findViewById(R.id.wel);


        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("message");

        myRef.setValue("Hello, World!");

        loadUserProfile();
        loadRecentSearches();

    }
    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            loadUserProfile();
            loadRecentSearches();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop any background tasks or UI updates
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Further clean up resources, if necessary
    }


    private void toggleTheme() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int currentTheme = prefs.getInt(KEY_THEME, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

        int newTheme;
        if (currentTheme == AppCompatDelegate.MODE_NIGHT_YES) {
            newTheme = AppCompatDelegate.MODE_NIGHT_NO;
        } else {
            newTheme = AppCompatDelegate.MODE_NIGHT_YES;

        }
        AppCompatDelegate.setDefaultNightMode(newTheme);
        prefs.edit().putInt(KEY_THEME, newTheme).apply();

        recreate();

    }

    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Logout user
                    mAuth.signOut();
                    // Redirect to login screen
                    Intent intent = new Intent(this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void storeUserProfileInFirestore(FirebaseUser user) {
        String name = user.getDisplayName();
        String email = user.getEmail();
        Uri photoUrl = user.getPhotoUrl();

        if (photoUrl != null) {
            Map<String, Object> userProfile = new HashMap<>();
            userProfile.put("name", name);
            userProfile.put("email", email);
            userProfile.put("profileImageUrl", photoUrl.toString());

            db.collection("Users").document(user.getUid())
                    .set(userProfile)
                    .addOnSuccessListener(aVoid -> Log.d("Firestore", "User profile stored successfully!"))
                    .addOnFailureListener(e -> Log.w("Firestore", "Error storing user profile", e));
        }
    }

    private void loadUserProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            db.collection("Users").document
              (userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if(documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            String profileImageUrl = documentSnapshot.getString("profileImageUrl");

                            wel.setText("Welcome,");
                            userName.setText(name);

                            if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                                Picasso.get().load(profileImageUrl).into(userprofile);
                            } else {
                                // Set a default image or handle no image case
                                userprofile.setImageResource(R.drawable.default_profile_image);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Handle any errors that occurred during the query
                    });
        }
    }

    private void loadRecentSearches() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            db.collection("Users").document(userId)
                .collection("recentSearches")
                .orderBy("timestamp") //Each search has a timestamp attached
                .limit(5)//the results are limited to 5 of the recent searches
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int cardIndex = 1;//starts with the first card view
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String diseaseName = document.getString("diseaseName");
                        int cardId = getResources().getIdentifier("cardname" + cardIndex, "id", getPackageName());
                        EditText cardNameView = findViewById(cardId);
                        if (cardNameView != null) {
                            cardNameView.setText(diseaseName);
                        }
                        cardIndex++;
                    }
                })
                .addOnFailureListener(e -> Log.w("Firestore", "Error getting recent searches", e) );

        }
    }
}
