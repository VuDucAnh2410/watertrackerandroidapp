package com.example.watertrackerandroidapp.LoginFunction;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.watertrackerandroidapp.R;
import com.example.watertrackerandroidapp.firebase.FirebaseManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ResetPasswordActivity extends AppCompatActivity {
    private static final String TAG = "ResetPasswordActivity";
    private TextInputLayout inputLayoutNewPassword, inputLayoutConfirmPassword;
    private TextInputEditText etNewPassword, etConfirmPassword;
    private Button btnResetPassword;
    private TextView tvMessage;
    private FirebaseAuth mAuth;
    private String identifier;
    private boolean isPhoneIdentifier = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        mAuth = FirebaseManager.getInstance().getAuth();
        loadResetInfo();

        inputLayoutNewPassword = findViewById(R.id.inputLayoutNewPassword);
        inputLayoutConfirmPassword = findViewById(R.id.inputLayoutConfirmPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnResetPassword = findViewById(R.id.btnResetPassword);

        btnResetPassword.setOnClickListener(v -> {
            if (validateInput()) {
                resetPassword();
            }
        });
    }

    private void loadResetInfo() {
        SharedPreferences sharedPreferences = getSharedPreferences("WaterReminderPrefs", MODE_PRIVATE);
        identifier = sharedPreferences.getString("resetIdentifier", "");
        isPhoneIdentifier = sharedPreferences.getBoolean("resetIsPhone", false);
        boolean isResetting = sharedPreferences.getBoolean("isResetting", false);

        if (!isResetting || identifier.isEmpty()) {
            Intent intent = new Intent(this, ForgotPasswordActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private boolean validateInput() {
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (newPassword.isEmpty()) {
            inputLayoutNewPassword.setError("Vui lòng nhập mật khẩu mới");
            return false;
        }
        if (newPassword.length() < 6) {
            inputLayoutNewPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            return false;
        }
        if (confirmPassword.isEmpty()) {
            inputLayoutConfirmPassword.setError("Vui lòng xác nhận mật khẩu");
            return false;
        }
        if (!newPassword.equals(confirmPassword)) {
            inputLayoutConfirmPassword.setError("Mật khẩu xác nhận không khớp");
            return false;
        }

        inputLayoutNewPassword.setError(null);
        inputLayoutConfirmPassword.setError(null);
        return true;
    }

    private void resetPassword() {
        final String newPassword = etNewPassword.getText().toString().trim();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            currentUser.updatePassword(newPassword)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(ResetPasswordActivity.this, "Đặt lại mật khẩu thành công", Toast.LENGTH_SHORT).show();
                                clearResetInfo();
                                mAuth.signOut();
                                Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            } else {
                                Log.e(TAG, "Error updating password", task.getException());
                                Toast.makeText(ResetPasswordActivity.this, "Đặt lại mật khẩu thất bại: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            Toast.makeText(this, "Phiên đăng nhập đã hết hạn. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void clearResetInfo() {
        SharedPreferences sharedPreferences = getSharedPreferences("WaterReminderPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("resetIdentifier");
        editor.remove("resetIsPhone");
        editor.remove("isResetting");
        editor.apply();
    }
}