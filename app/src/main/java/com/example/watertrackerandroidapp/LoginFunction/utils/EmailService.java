package com.example.watertrackerandroidapp.LoginFunction.utils;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class EmailService {
    private static final String TAG = "EmailService";
    private final FirebaseAuth mAuth;

    public EmailService() {
        mAuth = FirebaseAuth.getInstance();
    }

    public interface EmailCallback {
        void onSuccess();
        void onFailure(String errorMessage);
    }

    public void sendVerificationEmail(String recipientEmail, boolean isRegistrationFlow, EmailCallback callback) {
        try {
            if (isRegistrationFlow) {
                mAuth.createUserWithEmailAndPassword(recipientEmail, "temporaryPassword123")
                        .addOnCompleteListener(createTask -> {
                            if (createTask.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                if (user != null) {
                                    user.sendEmailVerification()
                                            .addOnCompleteListener(verifyTask -> {
                                                if (verifyTask.isSuccessful()) {
                                                    callback.onSuccess();
                                                    mAuth.signOut();
                                                } else {
                                                    callback.onFailure("Lỗi gửi email xác minh: " + verifyTask.getException().getMessage());
                                                    user.delete(); // Xóa tài khoản nếu thất bại
                                                }
                                            });
                                } else {
                                    callback.onFailure("Lỗi lấy thông tin người dùng");
                                }
                            } else {
                                callback.onFailure("Lỗi tạo tài khoản tạm thời: " + createTask.getException().getMessage());
                            }
                        });
            } else {
                mAuth.sendPasswordResetEmail(recipientEmail)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                callback.onSuccess();
                            } else {
                                callback.onFailure("Lỗi gửi email đặt lại mật khẩu: " + task.getException().getMessage());
                            }
                        });
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi gửi email", e);
            callback.onFailure("Lỗi: " + e.getMessage());
        }
    }
}