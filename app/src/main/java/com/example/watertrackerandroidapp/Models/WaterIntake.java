package com.example.watertrackerandroidapp.Models;

public class WaterIntake {
    private String intakeId;
    private String userId;
    private int amount;
    private String drinkType;
    private String intakeTime;
    private String intakeDate;
    private boolean isScheduled;

    public WaterIntake() {
    }

    public WaterIntake(String intakeId, String userId, int amount, String drinkType, String intakeTime, String intakeDate, boolean isScheduled) {
        this.intakeId = intakeId;
        this.userId = userId;
        this.amount = amount;
        this.drinkType = drinkType;
        this.intakeTime = intakeTime;
        this.intakeDate = intakeDate;
        this.isScheduled = isScheduled;
    }

    public String getIntakeId() {
        return intakeId;
    }

    public void setIntakeId(String intakeId) {
        this.intakeId = intakeId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getDrinkType() {
        return drinkType;
    }

    public void setDrinkType(String drinkType) {
        this.drinkType = drinkType;
    }

    public String getIntakeTime() {
        return intakeTime;
    }

    public void setIntakeTime(String intakeTime) {
        this.intakeTime = intakeTime;
    }

    public String getIntakeDate() {
        return intakeDate;
    }

    public void setIntakeDate(String intakeDate) {
        this.intakeDate = intakeDate;
    }

    public boolean isScheduled() {
        return isScheduled;
    }

    public void setScheduled(boolean scheduled) {
        isScheduled = scheduled;
    }
}