package com.example.watertrackerandroidapp.Repository;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.watertrackerandroidapp.Models.User;
import com.example.watertrackerandroidapp.firebase.FirebaseManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class UserRepository {
    private static final String TAG = "UserRepository";
    private final DatabaseReference mDatabase;

    public UserRepository() {
        mDatabase = FirebaseManager.getInstance().getDatabase();
        // Bật tính năng lưu trữ offline
        mDatabase.child("users").keepSynced(true);
    }

    public interface OnUserLoadedListener {
        void onUserLoaded(User user);
        void onError(String errorMessage);
    }

    public void getUserById(String userId, OnUserLoadedListener listener) {
        DatabaseReference userRef = mDatabase.child("users").child(userId);
        userRef.keepSynced(true);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    if (dataSnapshot.exists()) {
                        User user = new User();
                        user.setUserId(userId);

                        // Lấy thông tin từ dataSnapshot
                        if (dataSnapshot.hasChild("fullName")) {
                            user.setFullName(dataSnapshot.child("fullName").getValue(String.class));
                        }

                        if (dataSnapshot.hasChild("email")) {
                            user.setEmail(dataSnapshot.child("email").getValue(String.class));
                        }

                        if (dataSnapshot.hasChild("phone")) {
                            user.setPhone(dataSnapshot.child("phone").getValue(String.class));
                        }

                        if (dataSnapshot.hasChild("gender")) {
                            user.setGender(dataSnapshot.child("gender").getValue(String.class));
                        }

                        if (dataSnapshot.hasChild("weight")) {
                            Object weightObj = dataSnapshot.child("weight").getValue();
                            if (weightObj instanceof Long) {
                                user.setWeight(((Long) weightObj).floatValue());
                            } else if (weightObj instanceof Double) {
                                user.setWeight(((Double) weightObj).floatValue());
                            } else if (weightObj instanceof Float) {
                                user.setWeight((Float) weightObj);
                            }
                        }

                        if (dataSnapshot.hasChild("height")) {
                            Object heightObj = dataSnapshot.child("height").getValue();
                            if (heightObj instanceof Long) {
                                user.setHeight(((Long) heightObj).floatValue());
                            } else if (heightObj instanceof Double) {
                                user.setHeight(((Double) heightObj).floatValue());
                            } else if (heightObj instanceof Float) {
                                user.setHeight((Float) heightObj);
                            }
                        }

                        if (dataSnapshot.hasChild("age")) {
                            Object ageObj = dataSnapshot.child("age").getValue();
                            if (ageObj instanceof Long) {
                                user.setAge(((Long) ageObj).intValue());
                            } else if (ageObj instanceof Integer) {
                                user.setAge((Integer) ageObj);
                            }
                        }

                        if (dataSnapshot.hasChild("dailyTarget")) {
                            Object targetObj = dataSnapshot.child("dailyTarget").getValue();
                            if (targetObj instanceof Long) {
                                user.setDailyTarget(((Long) targetObj).intValue());
                            } else if (targetObj instanceof Integer) {
                                user.setDailyTarget((Integer) targetObj);
                            }
                        }

                        if (dataSnapshot.hasChild("wakeTime")) {
                            user.setWakeTime(dataSnapshot.child("wakeTime").getValue(String.class));
                        }

                        if (dataSnapshot.hasChild("sleepTime")) {
                            user.setSleepTime(dataSnapshot.child("sleepTime").getValue(String.class));
                        }

                        if (dataSnapshot.hasChild("hasUserInfo")) {
                            Object hasInfoObj = dataSnapshot.child("hasUserInfo").getValue();
                            if (hasInfoObj instanceof Boolean) {
                                user.setHasUserInfo((Boolean) hasInfoObj);
                            }
                        }

                        listener.onUserLoaded(user);
                    } else {
                        listener.onError("User not found");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing user data", e);
                    listener.onError("Error parsing user data: " + e.getMessage());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                listener.onError(databaseError.getMessage());
            }
        });
    }

    public void updateUserInfo(String userId, User user, OnCompleteListener<Void> listener) {
        Map<String, Object> userUpdates = new HashMap<>();

        if (user.getFullName() != null) userUpdates.put("fullName", user.getFullName());
        if (user.getGender() != null) userUpdates.put("gender", user.getGender());
        if (user.getWeight() > 0) userUpdates.put("weight", user.getWeight());
        if (user.getHeight() > 0) userUpdates.put("height", user.getHeight());
        if (user.getAge() > 0) userUpdates.put("age", user.getAge());
        if (user.getDailyTarget() > 0) userUpdates.put("dailyTarget", user.getDailyTarget());
        if (user.getWakeTime() != null) userUpdates.put("wakeTime", user.getWakeTime());
        if (user.getSleepTime() != null) userUpdates.put("sleepTime", user.getSleepTime());
        userUpdates.put("hasUserInfo", true);

        mDatabase.child("users").child(userId).updateChildren(userUpdates)
                .addOnCompleteListener(listener);
    }

    public int calculateDailyTarget(float weight) {
        // Công thức: 30ml * cân nặng (kg)
        return Math.round(weight * 30);
    }

    public void createUser(String userId, String email, String phone) {
        DatabaseReference userRef = mDatabase.child("users").child(userId);

        Map<String, Object> userData = new HashMap<>();
        if (email != null) userData.put("email", email);
        if (phone != null) userData.put("phone", phone);
        userData.put("createdAt", System.currentTimeMillis());
        userData.put("hasUserInfo", false);

        userRef.setValue(userData);
    }
}