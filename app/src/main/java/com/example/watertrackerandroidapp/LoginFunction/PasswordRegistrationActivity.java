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

import androidx.appcompat.app.AppCompatActivity;

import com.example.watertrackerandroidapp.LoginFunction.LoginActivity;
import com.example.watertrackerandroidapp.R;
import com.example.watertrackerandroidapp.DataBase.WaterTrackerDao;
import com.google.android.material.textfield.TextInputLayout;

public class PasswordRegistrationActivity extends AppCompatActivity {

    private static final String TAG = "PasswordRegistration";
    private EditText etPassword, etConfirmPassword;
    private Button btnRegister;
    private TextView tvLogin;
    private TextInputLayout inputLayoutPassword, inputLayoutConfirmPassword;
    private WaterTrackerDao waterTrackerDao;
    private String tempEmail, tempPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_password_registration);

            // Khởi tạo DAO
            waterTrackerDao = new WaterTrackerDao(this);

            // Lấy thông tin đăng ký tạm thời
            loadRegistrationInfo();

            initViews();
            setupListeners();
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void initViews() {
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);

        // Tìm TextInputLayout nếu có trong layout
        try {
            inputLayoutPassword = findViewById(R.id.inputLayoutPassword);
            inputLayoutConfirmPassword = findViewById(R.id.inputLayoutConfirmPassword);
        } catch (Exception e) {
            Log.w(TAG, "TextInputLayout not found in layout", e);
            // Không làm gì nếu không tìm thấy, sẽ sử dụng EditText.setError() thay thế
        }
    }

    private void setupListeners() {
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PasswordRegistrationActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Quay lại màn hình đăng nhập
                Intent intent = new Intent(PasswordRegistrationActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void loadRegistrationInfo() {
        SharedPreferences sharedPreferences = getSharedPreferences("WaterReminderPrefs", MODE_PRIVATE);
        tempEmail = sharedPreferences.getString("tempEmail", null);
        tempPhone = sharedPreferences.getString("tempPhone", null);
        boolean isRegistering = sharedPreferences.getBoolean("isRegistering", false);

        Log.d(TAG, "loadRegistrationInfo: tempEmail=" + tempEmail + ", tempPhone=" + tempPhone + ", isRegistering=" + isRegistering);

        // Kiểm tra xem có thông tin đăng ký không
        if ((tempEmail == null && tempPhone == null) || !isRegistering) {
            Log.d(TAG, "Không có thông tin đăng ký hoặc không trong quá trình đăng ký, quay về RegisterActivity");
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void registerUser() {
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Kiểm tra mật khẩu
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

        // Kiểm tra lại thông tin đăng ký
        if (tempEmail == null && tempPhone == null) {
            Toast.makeText(this, "Thông tin đăng ký không hợp lệ", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Thông tin đăng ký không hợp lệ: tempEmail=" + tempEmail + ", tempPhone=" + tempPhone);
            return;
        }

        // Kiểm tra tài khoản đã tồn tại chưa
        if (waterTrackerDao.isAccountExists(tempEmail, tempPhone)) {
            Toast.makeText(this, "Tài khoản đã tồn tại", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Tài khoản đã tồn tại: tempEmail=" + tempEmail + ", tempPhone=" + tempPhone);
            return;
        }

        try {
            Log.d(TAG, "Bắt đầu tạo tài khoản: tempEmail=" + tempEmail + ", tempPhone=" + tempPhone);

            // Tạo tài khoản mới trong database
            String accountId = waterTrackerDao.createAccount(tempEmail, tempPhone, password);

            Log.d(TAG, "Kết quả tạo tài khoản: accountId=" + accountId);

            if (accountId != null) {
                // Lấy userId từ accountId
                String userId = waterTrackerDao.getUserIdByAccountId(accountId);

                Log.d(TAG, "Lấy userId từ accountId: userId=" + userId);

                if (userId != null) {
                    // Lưu thông tin tài khoản vào SharedPreferences nhưng KHÔNG đặt isLoggedIn = true
                    saveAccountInfo(accountId, userId);

                    // Xóa thông tin đăng ký tạm thời
                    clearRegistrationInfo();

                    Toast.makeText(this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();

                    // Đặt cờ justRegistered để LoginActivity biết người dùng vừa đăng ký
                    SharedPreferences sharedPreferences = getSharedPreferences("WaterReminderPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("justRegistered", true);
                    editor.apply();

                    // Chuyển đến màn hình login
                    Intent intent = new Intent(PasswordRegistrationActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Log.e(TAG, "Không thể lấy userId từ accountId: " + accountId);
                    Toast.makeText(this, "Đăng ký thất bại: Không thể lấy thông tin người dùng", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e(TAG, "Tạo tài khoản thất bại");
                Toast.makeText(this, "Đăng ký thất bại. Vui lòng thử lại", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi trong quá trình đăng ký", e);
            Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // Helper method để đặt lỗi cho EditText hoặc TextInputLayout
    private void setError(EditText editText, TextInputLayout inputLayout, String errorMessage) {
        if (inputLayout != null) {
            inputLayout.setError(errorMessage);
        } else {
            editText.setError(errorMessage);
        }
    }

    // Helper method để xóa lỗi
    private void clearError(EditText editText, TextInputLayout inputLayout) {
        if (inputLayout != null) {
            inputLayout.setError(null);
        } else {
            editText.setError(null);
        }
    }

    // Lưu thông tin tài khoản nhưng KHÔNG đặt isLoggedIn = true
    private void saveAccountInfo(String accountId, String userId) {
        try {
            SharedPreferences sharedPreferences = getSharedPreferences("WaterReminderPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("accountId", accountId);
            editor.putString("userId", userId);
            // KHÔNG đặt isLoggedIn = true ở đây
            editor.apply();
            Log.d(TAG, "Đã lưu thông tin tài khoản: accountId=" + accountId + ", userId=" + userId);
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi lưu thông tin tài khoản", e);
        }
    }

    // Xóa thông tin đăng ký tạm thời
    private void clearRegistrationInfo() {
        try {
            SharedPreferences sharedPreferences = getSharedPreferences("WaterReminderPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove("tempEmail");
            editor.remove("tempPhone");
            editor.remove("isRegistering"); // Xóa flag isRegistering
            editor.apply();
            Log.d(TAG, "Đã xóa thông tin đăng ký tạm thời");
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi xóa thông tin đăng ký tạm thời", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (waterTrackerDao != null) {
            waterTrackerDao.close();
        }
    }
}
