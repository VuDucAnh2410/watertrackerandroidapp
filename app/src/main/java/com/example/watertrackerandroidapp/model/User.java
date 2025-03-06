package com.example.watertrackerandroidapp.model;

public class User {
    private String userId;
    private String name;
    private String sex;
    private float weight;
    private int age;
    private String sleepTime;
    private String wakeTime;
    private float dailyGoal;

    // Constructor
    public User(String userId, String name, String sex, float weight, int age,
                String sleepTime, String wakeTime, float dailyGoal) {
        this.userId = userId;
        this.name = name;
        this.sex = sex;
        this.weight = weight;
        this.age = age;
        this.sleepTime = sleepTime;
        this.wakeTime = wakeTime;
        this.dailyGoal = dailyGoal;
    }

    // Constructor với giá trị mặc định
    public User(String userId, String name) {
        this(userId, name, null, 0, 0, null, null, 2000);
    }

    // Getters and setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getSleepTime() {
        return sleepTime;
    }

    public void setSleepTime(String sleepTime) {
        this.sleepTime = sleepTime;
    }

    public String getWakeTime() {
        return wakeTime;
    }

    public void setWakeTime(String wakeTime) {
        this.wakeTime = wakeTime;
    }

    public float getDailyGoal() {
        return dailyGoal;
    }

    public void setDailyGoal(float dailyGoal) {
        this.dailyGoal = dailyGoal;
    }
}