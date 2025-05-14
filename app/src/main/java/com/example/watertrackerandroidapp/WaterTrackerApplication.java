package com.example.watertrackerandroidapp;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;

public class WaterTrackerApplication extends Application {
    private static final String TAG = "WaterTrackerApp";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();

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
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "water_reminder_channel",
                    "Water Reminder",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
}