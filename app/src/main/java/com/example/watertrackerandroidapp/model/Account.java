package com.example.watertrackerandroidapp.model;

public class Account {
    private long id;
    private String userId;
    private String phoneNumber;
    private String email;
    private String password;
    private long createAt;

    // Constructor
    public Account(long id, String userId, String phoneNumber, String email, String password, long createAt) {
        this.id = id;
        this.userId = userId;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.password = password;
        this.createAt = createAt;
    }

    // Constructor không có ID (cho bản ghi mới)
    public Account(String userId, String phoneNumber, String email, String password) {
        this(-1, userId, phoneNumber, email, password, System.currentTimeMillis());
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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public long getCreateAt() {
        return createAt;
    }

    public void setCreateAt(long createAt) {
        this.createAt = createAt;
    }
}