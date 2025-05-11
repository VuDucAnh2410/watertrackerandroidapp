package com.example.watertrackerandroidapp.Repository;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.watertrackerandroidapp.Models.WaterIntake;
import com.example.watertrackerandroidapp.firebase.FirebaseManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class WaterIntakeRepository {
    private static final String TAG = "WaterIntakeRepository";
    private final DatabaseReference mDatabase;

    public WaterIntakeRepository() {
        mDatabase = FirebaseManager.getInstance().getDatabase();
        // Bật tính năng lưu trữ offline
        mDatabase.child("waterIntake").keepSynced(true);
    }

    public interface OnWaterIntakeLoadedListener {
        void onWaterIntakeLoaded(List<WaterIntake> intakeList, int totalAmount);
        void onError(String errorMessage);
    }

    public void addWaterIntake(String userId, int amount, String drinkType, OnSuccessListener<Void> successListener, OnFailureListener failureListener) {
        // Lấy ngày và giờ hiện tại
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());
        String currentTime = timeFormat.format(new Date());

        // Tạo ID cho lần uống nước mới
        String intakeId = "INTAKE" + System.currentTimeMillis();

        // Tạo đối tượng lần uống nước
        Map<String, Object> intakeValues = new HashMap<>();
        intakeValues.put("intakeId", intakeId);
        intakeValues.put("userId", userId);
        intakeValues.put("amount", amount);
        intakeValues.put("drinkType", drinkType);
        intakeValues.put("intakeTime", currentTime);
        intakeValues.put("intakeDate", currentDate);
        intakeValues.put("scheduled", false);

        // Lưu vào database
        mDatabase.child("waterIntake").child(userId).child(currentDate).child(intakeId)
                .setValue(intakeValues)
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);
    }

    public void getTodayWaterIntake(String userId, OnWaterIntakeLoadedListener listener) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());

        getWaterIntakeForDay(userId, currentDate, listener);
    }

    public void getWaterIntakeForDay(String userId, String date, OnWaterIntakeLoadedListener listener) {
        DatabaseReference dateRef = mDatabase.child("waterIntake").child(userId).child(date);
        dateRef.keepSynced(true);

        dateRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<WaterIntake> intakeList = new ArrayList<>();
                int totalAmount = 0;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    try {
                        WaterIntake intake = new WaterIntake();

                        // Lấy ID từ key của snapshot
                        intake.setIntakeId(snapshot.getKey());

                        // Lấy các giá trị khác
                        if (snapshot.hasChild("amount")) {
                            Integer amount = snapshot.child("amount").getValue(Integer.class);
                            if (amount != null) {
                                intake.setAmount(amount);
                                totalAmount += amount;
                            } else {
                                intake.setAmount(0);
                            }
                        }

                        if (snapshot.hasChild("drinkType")) {
                            intake.setDrinkType(snapshot.child("drinkType").getValue(String.class));
                        } else {
                            intake.setDrinkType("Water");
                        }

                        if (snapshot.hasChild("intakeTime")) {
                            intake.setIntakeTime(snapshot.child("intakeTime").getValue(String.class));
                        } else {
                            intake.setIntakeTime("00:00");
                        }

                        intake.setIntakeDate(date);
                        intake.setUserId(userId);
                        intake.setScheduled(false);

                        intakeList.add(intake);
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing water intake: " + e.getMessage());
                    }
                }

                // Sắp xếp theo thời gian (mới nhất lên đầu)
                intakeList.sort((o1, o2) -> {
                    if (o1.getIntakeTime() == null || o2.getIntakeTime() == null) {
                        return 0;
                    }
                    return o2.getIntakeTime().compareTo(o1.getIntakeTime());
                });

                listener.onWaterIntakeLoaded(intakeList, totalAmount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                listener.onError(databaseError.getMessage());
            }
        });
    }

    public void deleteWaterIntake(String userId, String date, String intakeId, OnSuccessListener<Void> successListener, OnFailureListener failureListener) {
        mDatabase.child("waterIntake").child(userId).child(date).child(intakeId)
                .removeValue()
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);
    }
}