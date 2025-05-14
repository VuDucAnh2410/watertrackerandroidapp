package com.example.watertrackerandroidapp.Repository;


import android.util.Log;


import androidx.annotation.NonNull;


import com.example.watertrackerandroidapp.Models.Reminder;
import com.example.watertrackerandroidapp.firebase.FirebaseManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ReminderRepository {
    private static final String TAG = "ReminderRepository";
    private final DatabaseReference mDatabase;


    public ReminderRepository() {
        mDatabase = FirebaseManager.getInstance().getDatabase();
    }


    public interface OnRemindersLoadedListener {
        void onRemindersLoaded(List<Reminder> reminderList);
        void onError(String errorMessage);
    }


    public void createReminder(String userId, String time, boolean active, String sound, String days, OnCompleteListener<Void> listener) {
        String reminderId = "REMINDER" + System.currentTimeMillis();


        Map<String, Object> reminderValues = new HashMap<>();
        reminderValues.put("reminderId", reminderId);
        reminderValues.put("time", time);
        reminderValues.put("active", active);
        reminderValues.put("sound", sound);
        reminderValues.put("days", days);


        String reminderKey = mDatabase.child("reminders").child(userId).push().getKey();


        mDatabase.child("reminders").child(userId).child(reminderKey)
                .setValue(reminderValues)
                .addOnCompleteListener(listener);
    }


    public void getReminders(String userId, OnRemindersLoadedListener listener) {
        mDatabase.child("reminders").child(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        List<Reminder> reminderList = new ArrayList<>();


                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Reminder reminder = new Reminder();
                            reminder.setReminderId(snapshot.child("reminderId").getValue(String.class));
                            reminder.setUserId(userId);
                            reminder.setTime(snapshot.child("time").getValue(String.class));
                            reminder.setActive(snapshot.child("active").getValue(Boolean.class));
                            reminder.setSound(snapshot.child("sound").getValue(String.class));
                            reminder.setDays(snapshot.child("days").getValue(String.class));


                            reminderList.add(reminder);
                        }


                        listener.onRemindersLoaded(reminderList);
                    }


                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        listener.onError(databaseError.getMessage());
                    }
                });
    }


    public void updateReminderStatus(String userId, String reminderId, boolean active, OnCompleteListener<Void> listener) {
        mDatabase.child("reminders").child(userId)
                .orderByChild("reminderId").equalTo(reminderId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            snapshot.getRef().child("active").setValue(active)
                                    .addOnCompleteListener(listener);
                            break;
                        }
                    }


                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e(TAG, "Error finding reminder", databaseError.toException());
                    }
                });
    }


    public void deleteReminder(String userId, String reminderId, OnCompleteListener<Void> listener) {
        mDatabase.child("reminders").child(userId)
                .orderByChild("reminderId").equalTo(reminderId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            snapshot.getRef().removeValue()
                                    .addOnCompleteListener(listener);
                            break;
                        }
                    }


                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e(TAG, "Error finding reminder", databaseError.toException());
                    }
                });
    }
    public void deleteAllReminders(String userId, OnCompleteListener<Void> listener) {
        mDatabase.child("reminders").child(userId).removeValue().addOnCompleteListener(listener);
    }


    public void updateAllRemindersSound(String userId, String sound, OnCompleteListener<Void> listener) {
        mDatabase.child("reminders").child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Map<String, Object> updates = new HashMap<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String key = snapshot.getKey();
                            updates.put(key + "/sound", sound);
                        }
                        mDatabase.child("reminders").child(userId).updateChildren(updates)
                                .addOnCompleteListener(listener);
                    }


                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e(TAG, "Error updating reminders sound", databaseError.toException());
                    }
                });
    }


    public void createDefaultReminders(String userId, String wakeTime, String sleepTime) {
        try {
            String[] wakeParts = wakeTime.split(":");
            String[] sleepParts = sleepTime.split(":");


            int wakeHour = Integer.parseInt(wakeParts[0]);
            int sleepHour = Integer.parseInt(sleepParts[0]);


            Map<String, Object> reminders = new HashMap<>();
            int currentHour = wakeHour;
            int count = 0;


            while (currentHour != sleepHour) {
                String time = String.format("%02d:00", currentHour);
                Map<String, Object> reminder = new HashMap<>();
                reminder.put("reminderId", "REMINDER" + System.currentTimeMillis() + count);
                reminder.put("time", time);
                reminder.put("active", true);
                reminder.put("sound", "water_pouring");
                reminder.put("days", "Everyday");


                reminders.put("reminder_" + count, reminder);
                count++;


                currentHour = (currentHour + 1) % 24;
            }


            mDatabase.child("reminders").child(userId).setValue(reminders)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Default reminders created successfully");
                        } else {
                            Log.e(TAG, "Error creating default reminders", task.getException());
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error creating default reminders", e);
        }
    }
}
