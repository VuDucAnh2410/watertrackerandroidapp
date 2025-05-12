package com.example.watertrackerandroidapp.firebase;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseManager {
    private static final String TAG = "FirebaseManager";
    private static FirebaseManager instance;
    private final FirebaseAuth mAuth;
    private final DatabaseReference mDatabase;
    private final FirebaseDatabase firebaseDatabase;

    private FirebaseManager() {
        try {
            // Bật tính năng lưu trữ offline
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            Log.d(TAG, "Firebase Persistence enabled");
        } catch (Exception e) {
            Log.e(TAG, "Error enabling Firebase Persistence", e);
        }

        mAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        mDatabase = firebaseDatabase.getReference();

        try {
            // Bật tính năng lưu trữ offline cho các node thường xuyên sử dụng
            mDatabase.child("users").keepSynced(true);
            mDatabase.child("waterIntake").keepSynced(true);
            mDatabase.child("reminders").keepSynced(true);
            Log.d(TAG, "Firebase offline persistence enabled for common nodes");
        } catch (Exception e) {
            Log.e(TAG, "Error enabling Firebase offline persistence for nodes", e);
        }
    }

    public static synchronized FirebaseManager getInstance() {
        if (instance == null) {
            instance = new FirebaseManager();
        }
        return instance;
    }

    public FirebaseAuth getAuth() {
        return mAuth;
    }

    public DatabaseReference getDatabase() {
        return mDatabase;
    }

    public String getCurrentUserId() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        return currentUser != null ? currentUser.getUid() : null;
    }

    public boolean isUserLoggedIn() {
        return mAuth.getCurrentUser() != null;
    }

    public void signOut() {
        mAuth.signOut();
    }

    public void clearCache() {
        try {
            firebaseDatabase.purgeOutstandingWrites();
            Log.d(TAG, "Firebase cache cleared");
        } catch (Exception e) {
            Log.e(TAG, "Error clearing Firebase cache", e);
        }
    }
}