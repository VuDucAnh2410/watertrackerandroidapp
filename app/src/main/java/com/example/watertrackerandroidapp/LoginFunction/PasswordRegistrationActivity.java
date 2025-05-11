package com.example.watertrackerandroidapp.LoginFunction;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.watertrackerandroidapp.R;
import com.example.watertrackerandroidapp.Repository.AuthRepository;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseUser;

public class PasswordRegistrationActivity extends AppCompatActivity {

    private static final String TAG = "PasswordRegistration";
    private EditText etPassword, etConfirmPassword;
    private Button btnRegister;
    private TextView tvLogin;
    private TextInputLayout inputLayoutPassword, inputLayoutConfirmPassword;

    private AuthRepository authRepository;
    private String tempEmail, tempPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Bắt đầu khởi tạo PasswordRegistrationActivity");
        try {
            setContentView(R.layout.activity_password_registration);
            Log.d(TAG, "onCreate: setContentView thành công");

            authRepository = new AuthRepository();
            Log.d(TAG, "onCreate: Khởi tạo AuthRepository thành công");

            loadRegistrationInfo();
            Log.d(TAG, "onCreate: loadRegistrationInfo thành công");

            initViews();
            Log.d(TAG, "onCreate: initViews thành công");

            setupListeners();
            Log.d(TAG, "onCreate: setupListeners thành công");

            Log.d(TAG, "onCreate: PasswordRegistrationActivity khởi tạo thành công");
        } catch (Exception e) {
            Log.e(TAG, "Lỗi trong onCreate", e);
            Toast.makeText(this, "Lỗi khởi tạo: " + e.getMessage(), Toast.LENGTH_LONG).show();
            navigateToRegisterActivity();
        }
    }

    private void initViews() {
        try {
            etPassword = findViewById(R.id.etPassword);
            etConfirmPassword = findViewById(R.id.etConfirmPassword);
            btnRegister = findViewById(R.id.btnRegister);
            tvLogin = findViewById(R.id.tvLogin);
            inputLayoutPassword = findViewById(R.id.inputLayoutPassword);
            inputLayoutConfirmPassword = findViewById(R.id.inputLayoutConfirmPassword);

            if (etPassword == null) Log.e(TAG, "initViews: etPassword not found");
            if (etConfirmPassword == null) Log.e(TAG, "initViews: etConfirmPassword not found");
            if (btnRegister == null) Log.e(TAG, "initViews: btnRegister not found");
            if (tvLogin == null) Log.e(TAG, "initViews: tvLogin not found");
            if (inputLayoutPassword == null) Log.e(TAG, "initViews: inputLayoutPassword not found");
            if (inputLayoutConfirmPassword == null) Log.e(TAG, "initViews: inputLayoutConfirmPassword not found");

            if (etPassword == null || etConfirmPassword == null || btnRegister == null || tvLogin == null ||
                    inputLayoutPassword == null || inputLayoutConfirmPassword == null) {
                throw new IllegalStateException("Một hoặc nhiều view không được tìm thấy trong layout");
            }

            Log.d(TAG, "initViews: Khởi tạo views thành công");
        } catch (Exception e) {
            Log.e(TAG, "initViews: Lỗi khởi tạo views", e);
            Toast.makeText(this, "Lỗi khởi tạo giao diện: " + e.getMessage(), Toast.LENGTH_LONG).show();
            navigateToRegisterActivity();
        }
    }

    private void setupListeners() {
        try {
            if (btnRegister != null) {
                btnRegister.setOnClickListener(v -> registerUser());
            } else {
                Log.e(TAG, "setupListeners: btnRegister is null");
            }

            if (tvLogin != null) {
                tvLogin.setOnClickListener(v -> {
                    Intent intent = new Intent(PasswordRegistrationActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                });
            } else {
                Log.e(TAG, "setupListeners: tvLogin is null");
            }

            Log.d(TAG, "setupListeners: Thiết lập listeners thành công");
        } catch (Exception e) {
            Log.e(TAG, "setupListeners: Lỗi thiết lập listeners", e);
            Toast.makeText(this, "Lỗi thiết lập listeners: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void loadRegistrationInfo() {
        try {
            SharedPreferences sharedPreferences = getSharedPreferences("WaterReminderPrefs", MODE_PRIVATE);
            tempEmail = sharedPreferences.getString("tempEmail", null);
            tempPhone = sharedPreferences.getString("tempPhone", null);
            boolean isRegistering = sharedPreferences.getBoolean("isRegistering", false);

            Log.d(TAG, "loadRegistrationInfo: tempEmail=" + tempEmail + ", tempPhone=" + tempPhone + ", isRegistering=" + isRegistering);

            if ((tempEmail == null && tempPhone == null) || !isRegistering) {
                Log.e(TAG, "Không có thông tin đăng ký, quay về RegisterActivity");
                Toast.makeText(this, "Không có thông tin đăng ký", Toast.LENGTH_SHORT).show();
                navigateToRegisterActivity();
            }
        } catch (Exception e) {
            Log.e(TAG, "loadRegistrationInfo: Lỗi tải thông tin đăng ký", e);
            Toast.makeText(this, "Lỗi tải thông tin: " + e.getMessage(), Toast.LENGTH_LONG).show();
            navigateToRegisterActivity();
        }
    }

    private void registerUser() {
        try {
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            if (password.isEmpty()) {
                setError(etPassword, inputLayoutPassword, "Vui lòng nhập mật khẩu");
                return;
            } else {
                clearError(etPassword, inputLayoutPassword);
            }

            if (password.length() < 6) {
                setError(etPassword, inputLayoutPassword, "Mật khẩu phải có ít nhất 6 ký tự");
                return;
            } else {
                clearError(etPassword, inputLayoutPassword);
            }

            if (!password.equals(confirmPassword)) {
                setError(etConfirmPassword, inputLayoutConfirmPassword, "Mật khẩu xác nhận không khớp");
                return;
            } else {
                clearError(etConfirmPassword, inputLayoutConfirmPassword);
            }

            if (tempEmail == null && tempPhone == null) {
                Toast.makeText(this, "Thông tin đăng ký không hợp lệ", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Thông tin đăng ký không hợp lệ: tempEmail=" + tempEmail + ", tempPhone=" + tempPhone);
                return;
            }

            Log.d(TAG, "Bắt đầu tạo tài khoản: tempEmail=" + tempEmail + ", tempPhone=" + tempPhone);
            Toast.makeText(this, "Đang tạo tài khoản...", Toast.LENGTH_SHORT).show();

            final String email = tempEmail != null ? tempEmail : tempPhone + "@phone.com";
            authRepository.registerWithEmail(email, password, tempPhone, new AuthRepository.AuthCallback() {
                @Override
                public void onSuccess(FirebaseUser user) {
                    authRepository.signOut();
                    clearRegistrationInfo();

                    Toast.makeText(PasswordRegistrationActivity.this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();

                    SharedPreferences sharedPreferences = getSharedPreferences("WaterReminderPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("justRegistered", true);
                    editor.apply();

                    Intent intent = new Intent(PasswordRegistrationActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onError(String errorMessage) {
                    Log.e(TAG, "Tạo tài khoản thất bại: " + errorMessage);
                    Toast.makeText(PasswordRegistrationActivity.this, "Đăng ký thất bại: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Lỗi trong quá trình đăng ký", e);
            Toast.makeText(this, "Lỗi đăng ký: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void setError(EditText editText, TextInputLayout inputLayout, String errorMessage) {
        if (inputLayout != null) {
            inputLayout.setError(errorMessage);
        } else {
            editText.setError(errorMessage);
        }
    }

    private void clearError(EditText editText, TextInputLayout inputLayout) {
        if (inputLayout != null) {
            inputLayout.setError(null);
        } else {
            editText.setError(null);
        }
    }

    private void clearRegistrationInfo() {
        try {
            SharedPreferences sharedPreferences = getSharedPreferences("WaterReminderPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove("tempEmail");
            editor.remove("tempPhone");
            editor.remove("isRegistering");
            editor.apply();
            Log.d(TAG, "Đã xóa thông tin đăng ký tạm thời");
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi xóa thông tin đăng ký tạm thời", e);
        }
    }

    private void navigateToRegisterActivity() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
        finish();
    }
}