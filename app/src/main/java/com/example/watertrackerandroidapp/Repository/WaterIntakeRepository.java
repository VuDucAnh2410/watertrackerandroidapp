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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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

    public interface OnWaterIntakeRangeLoadedListener {
        void onWaterIntakeRangeLoaded(Map<String, List<WaterIntake>> intakeMap, Map<String, Integer> dailyTotalMap);
        void onError(String errorMessage);
    }

    public interface OnMonthlyIntakeLoadedListener {
        void onMonthlyIntakeLoaded(List<WaterIntake> intakeList, Map<Integer, Integer> dailyTotalMap, int totalAmount);
        void onError(String errorMessage);
    }

    public interface OnYearlyIntakeLoadedListener {
        void onYearlyIntakeLoaded(Map<Integer, Integer> monthlyData, int totalAmount);
        void onError(String errorMessage);
    }

    public interface OnYearlyIntakeDetailsLoadedListener {
        void onYearlyIntakeDetailsLoaded(List<WaterIntake> intakeList);
        void onError(String errorMessage);
    }

    public void addWaterIntake(String userId, int amount, String drinkType, OnSuccessListener<Void> successListener, OnFailureListener failureListener) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());
        String currentTime = timeFormat.format(new Date());

        String intakeId = "INTAKE" + System.currentTimeMillis();

        Map<String, Object> intakeValues = new HashMap<>();
        intakeValues.put("intakeId", intakeId);
        intakeValues.put("userId", userId);
        intakeValues.put("amount", amount);
        intakeValues.put("drinkType", drinkType);
        intakeValues.put("intakeTime", currentTime);
        intakeValues.put("intakeDate", currentDate);
        intakeValues.put("scheduled", false);

        Log.d(TAG, "Saving water intake: userId=" + userId + ", date=" + currentDate + ", id=" + intakeId + ", amount=" + amount + "ml");

        // Lưu vào database
        mDatabase.child("waterIntake").child(userId).child(currentDate).child(intakeId)
                .setValue(intakeValues)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Water intake saved successfully: " + intakeId);
                    successListener.onSuccess(aVoid);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving water intake: " + e.getMessage());
                    failureListener.onFailure(e);
                });
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

                Log.d(TAG, "Loading water intake for userId=" + userId + ", date=" + date + ", entries=" + dataSnapshot.getChildrenCount());

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    try {
                        WaterIntake intake = new WaterIntake();

                        intake.setIntakeId(snapshot.getKey());

                        if (snapshot.hasChild("amount")) {
                            Integer amount = snapshot.child("amount").getValue(Integer.class);
                            if (amount != null) {
                                intake.setAmount(amount);
                                totalAmount += amount;
                                Log.d(TAG, "Found intake: id=" + intake.getIntakeId() + ", amount=" + amount + "ml");
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

                intakeList.sort((o1, o2) -> {
                    if (o1.getIntakeTime() == null || o2.getIntakeTime() == null) {
                        return 0;
                    }
                    return o2.getIntakeTime().compareTo(o1.getIntakeTime());
                });

                Log.d(TAG, "Total water intake for today: " + totalAmount + "ml from " + intakeList.size() + " entries");
                listener.onWaterIntakeLoaded(intakeList, totalAmount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error loading water intake: " + databaseError.getMessage());
                listener.onError(databaseError.getMessage());
            }
        });
    }

    public void getWaterIntakeForDateRange(String userId, String startDate, String endDate, OnWaterIntakeRangeLoadedListener listener) {
        DatabaseReference userRef = mDatabase.child("waterIntake").child(userId);
        userRef.keepSynced(true);

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map<String, List<WaterIntake>> intakeMap = new HashMap<>();
                Map<String, Integer> dailyTotalMap = new HashMap<>();

                Log.d(TAG, "Loading water intake for userId=" + userId + " from " + startDate + " to " + endDate);

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                try {
                    Date start = dateFormat.parse(startDate);
                    Date end = dateFormat.parse(endDate);

                    for (DataSnapshot dateSnapshot : dataSnapshot.getChildren()) {
                        String dateKey = dateSnapshot.getKey();

                        try {
                            Date currentDate = dateFormat.parse(dateKey);

                            if (currentDate != null && !currentDate.before(start) && !currentDate.after(end)) {
                                List<WaterIntake> dailyIntakes = new ArrayList<>();
                                int dailyTotal = 0;

                                for (DataSnapshot intakeSnapshot : dateSnapshot.getChildren()) {
                                    try {
                                        WaterIntake intake = new WaterIntake();
                                        intake.setIntakeId(intakeSnapshot.getKey());
                                        intake.setIntakeDate(dateKey);
                                        intake.setUserId(userId);

                                        if (intakeSnapshot.hasChild("amount")) {
                                            Integer amount = intakeSnapshot.child("amount").getValue(Integer.class);
                                            if (amount != null) {
                                                intake.setAmount(amount);
                                                dailyTotal += amount;
                                            } else {
                                                intake.setAmount(0);
                                            }
                                        }

                                        if (intakeSnapshot.hasChild("drinkType")) {
                                            intake.setDrinkType(intakeSnapshot.child("drinkType").getValue(String.class));
                                        } else {
                                            intake.setDrinkType("Water");
                                        }

                                        if (intakeSnapshot.hasChild("intakeTime")) {
                                            intake.setIntakeTime(intakeSnapshot.child("intakeTime").getValue(String.class));
                                        } else {
                                            intake.setIntakeTime("00:00");
                                        }

                                        dailyIntakes.add(intake);
                                    } catch (Exception e) {
                                        Log.e(TAG, "Error parsing water intake: " + e.getMessage());
                                    }
                                }

                                intakeMap.put(dateKey, dailyIntakes);
                                dailyTotalMap.put(dateKey, dailyTotal);
                            }
                        } catch (ParseException e) {
                            Log.e(TAG, "Error parsing date: " + e.getMessage());
                        }
                    }

                    listener.onWaterIntakeRangeLoaded(intakeMap, dailyTotalMap);
                } catch (ParseException e) {
                    Log.e(TAG, "Error parsing date range: " + e.getMessage());
                    listener.onError("Lỗi định dạng ngày: " + e.getMessage());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error loading water intake range: " + databaseError.getMessage());
                listener.onError(databaseError.getMessage());
            }
        });
    }

    public void getMonthlyWaterIntake(String userId, String monthYear, OnMonthlyIntakeLoadedListener listener) {
        try {
            SimpleDateFormat monthYearFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
            Date date = monthYearFormat.parse(monthYear);

            if (date == null) {
                listener.onError("Định dạng tháng/năm không hợp lệ");
                return;
            }

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);

            calendar.set(year, month, 1);
            String startDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());

            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
            String endDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());

            getWaterIntakeForDateRange(userId, startDate, endDate, new OnWaterIntakeRangeLoadedListener() {
                @Override
                public void onWaterIntakeRangeLoaded(Map<String, List<WaterIntake>> intakeMap, Map<String, Integer> dailyTotalMap) {
                    List<WaterIntake> allIntakes = new ArrayList<>();
                    Map<Integer, Integer> dailyData = new HashMap<>();
                    int totalAmount = 0;

                    for (Map.Entry<String, List<WaterIntake>> entry : intakeMap.entrySet()) {
                        String dateStr = entry.getKey();
                        List<WaterIntake> intakes = entry.getValue();

                        allIntakes.addAll(intakes);

                        try {
                            int day = Integer.parseInt(dateStr.substring(8));
                            int amount = dailyTotalMap.getOrDefault(dateStr, 0);

                            dailyData.put(day, amount);
                            totalAmount += amount;
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing day from date: " + e.getMessage());
                        }
                    }

                    listener.onMonthlyIntakeLoaded(allIntakes, dailyData, totalAmount);
                }

                @Override
                public void onError(String errorMessage) {
                    listener.onError(errorMessage);
                }
            });

        } catch (ParseException e) {
            Log.e(TAG, "Error parsing month/year: " + e.getMessage());
            listener.onError("Lỗi định dạng tháng/năm: " + e.getMessage());
        }
    }

    public void getYearlyWaterIntake(String userId, String year, OnYearlyIntakeLoadedListener listener) {
        try {
            int yearInt = Integer.parseInt(year);
            Map<Integer, Integer> monthlyData = new HashMap<>();
            final int[] monthsProcessed = {0};
            final int[] totalAmount = {0};

            for (int month = 1; month <= 12; month++) {
                final int currentMonth = month;
                String monthYear = String.format(Locale.getDefault(), "%04d-%02d", yearInt, month);

                getMonthlyWaterIntake(userId, monthYear, new OnMonthlyIntakeLoadedListener() {
                    @Override
                    public void onMonthlyIntakeLoaded(List<WaterIntake> intakeList, Map<Integer, Integer> dailyTotalMap, int monthTotal) {
                        monthlyData.put(currentMonth, monthTotal);
                        totalAmount[0] += monthTotal;

                        monthsProcessed[0]++;

                        if (monthsProcessed[0] == 12) {
                            listener.onYearlyIntakeLoaded(monthlyData, totalAmount[0]);
                        }
                    }

                    @Override
                    public void onError(String errorMessage) {
                        monthsProcessed[0]++;

                        Log.e(TAG, "Error loading data for month " + currentMonth + ": " + errorMessage);

                        if (monthsProcessed[0] == 12) {
                            listener.onYearlyIntakeLoaded(monthlyData, totalAmount[0]);
                        }
                    }
                });
            }
        } catch (NumberFormatException e) {
            Log.e(TAG, "Error parsing year: " + e.getMessage());
            listener.onError("Năm không hợp lệ: " + e.getMessage());
        }
    }

    public void getYearlyWaterIntakeDetails(String userId, String year, OnYearlyIntakeDetailsLoadedListener listener) {
        try {
            String startDate = year + "-01-01";
            String endDate = year + "-12-31";

            getWaterIntakeForDateRange(userId, startDate, endDate, new OnWaterIntakeRangeLoadedListener() {
                @Override
                public void onWaterIntakeRangeLoaded(Map<String, List<WaterIntake>> intakeMap, Map<String, Integer> dailyTotalMap) {
                    List<WaterIntake> allIntakes = new ArrayList<>();
                    for (List<WaterIntake> dailyIntakes : intakeMap.values()) {
                        allIntakes.addAll(dailyIntakes);
                    }

                    allIntakes.sort((o1, o2) -> {
                        if (o1.getIntakeTime() == null || o2.getIntakeTime() == null) {
                            return 0;
                        }
                        return o2.getIntakeTime().compareTo(o1.getIntakeTime());
                    });

                    Log.d(TAG, "Loaded " + allIntakes.size() + " water intake entries for year " + year);
                    listener.onYearlyIntakeDetailsLoaded(allIntakes);
                }

                @Override
                public void onError(String errorMessage) {
                    Log.e(TAG, "Error loading yearly water intake details: " + errorMessage);
                    listener.onError(errorMessage);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in getYearlyWaterIntakeDetails: " + e.getMessage());
            listener.onError("Lỗi khi lấy dữ liệu năm: " + e.getMessage());
        }
    }

    public void deleteWaterIntake(String userId, String date, String intakeId, OnSuccessListener<Void> successListener, OnFailureListener failureListener) {
        Log.d(TAG, "Deleting water intake: userId=" + userId + ", date=" + date + ", id=" + intakeId);

        mDatabase.child("waterIntake").child(userId).child(date).child(intakeId)
                .removeValue()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Water intake deleted successfully: " + intakeId);
                    successListener.onSuccess(aVoid);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting water intake: " + e.getMessage());
                    failureListener.onFailure(e);
                });
    }
}