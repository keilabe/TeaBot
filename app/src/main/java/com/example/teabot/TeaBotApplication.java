package com.example.teabot;
import android.app.Application;
import com.google.firebase.FirebaseApp;

public class TeaBotApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize Firebase
        FirebaseApp.initializeApp(this);
    }
}
