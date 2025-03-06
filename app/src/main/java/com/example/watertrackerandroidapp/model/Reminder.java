package com.example.watertrackerandroidapp.model;

public class Reminder {
    private long id;
    private String userId;
    private String reminderTime;
    private boolean status;
    private String alarmSound;

    // Constructor
    public Reminder(long id, String userId, String reminderTime, boolean status, String alarmSound) {
        this.id = id;
        this.userId = userId;
        this.reminderTime = reminderTime;
        this.status = status;
        this.alarmSound = alarmSound;
    }

    // Constructor không có ID (cho bản ghi mới)
    public Reminder(String userId, String reminderTime) {
        this(-1, userId, reminderTime, true, "Dòng nước");
    }

    // Getters and setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(String reminderTime) {
        this.reminderTime = reminderTime;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getAlarmSound() {
        return alarmSound;
    }

    public void setAlarmSound(String alarmSound) {
        this.alarmSound = alarmSound;
    }
}