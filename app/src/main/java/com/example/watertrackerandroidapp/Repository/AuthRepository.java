package com.example.watertrackerandroidapp.Repository;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.watertrackerandroidapp.firebase.FirebaseManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthRepository {
    private static final String TAG = "AuthRepository";
    private final FirebaseAuth mAuth;
    private final UserRepository userRepository;

    public AuthRepository() {
        mAuth = FirebaseManager.getInstance().getAuth();
        userRepository = new UserRepository();
    }

    public interface AuthCallback {
        void onSuccess(FirebaseUser user);
        void onError(String errorMessage);
    }

    public void registerWithEmail(String email, String password, String phone, AuthCallback callback) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            userRepository.createUser(user.getUid(), email, phone);
                            callback.onSuccess(user);
                        } else {
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            callback.onError(task.getException().getMessage());
                        }
                    }
                });
    }

    public void loginWithEmail(String email, String password, AuthCallback callback) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            callback.onSuccess(user);
                        } else {
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            callback.onError(task.getException().getMessage());
                        }
                    }
                });
    }

    public void loginWithPhoneEmail(String phone, String password, AuthCallback callback) {
        String email = phone + "@phone.com";
        loginWithEmail(email, password, callback);
    }

    public void resetPassword(String email, OnCompleteListener<Void> listener) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(listener);
    }

    public void signOut() {
        mAuth.signOut();
    }

    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    public boolean isUserLoggedIn() {
        return mAuth.getCurrentUser() != null;
    }
}