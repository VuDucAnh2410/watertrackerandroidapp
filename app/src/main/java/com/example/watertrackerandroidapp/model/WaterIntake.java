package com.example.watertrackerandroidapp.model;

public class WaterIntake {
    private long id;
    private String userId;
    private float amount;
    private long waterTime;

    // Constructor
    public WaterIntake(long id, String userId, float amount, long waterTime) {
        this.id = id;
        this.userId = userId;
        this.amount = amount;
        this.waterTime = waterTime;
    }

    // Constructor không có ID (cho bản ghi mới)
    public WaterIntake(String userId, float amount) {
        this(-1, userId, amount, System.currentTimeMillis());
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

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public long getWaterTime() {
        return waterTime;
    }

    public void setWaterTime(long waterTime) {
        this.waterTime = waterTime;
    }
}