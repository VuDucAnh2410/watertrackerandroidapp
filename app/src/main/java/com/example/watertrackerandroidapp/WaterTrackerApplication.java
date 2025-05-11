package com.example.watertrackerandroidapp;

import android.app.Application;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;

public class WaterTrackerApplication extends Application {
    private static final String TAG = "WaterTrackerApp";

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            // Khởi tạo Firebase
            FirebaseApp.initializeApp(this);

            // Bật tính năng lưu trữ offline cho Firebase
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            Log.d(TAG, "Firebase Persistence enabled");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Firebase", e);
        }
    }
}