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
            Log.e("PasswordRegistration", "Error in onCreate", e);
            Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void initViews() {
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);

        // Bỏ qua việc khởi tạo inputLayoutPassword và inputLayoutConfirmPassword
        // vì chúng không có trong XML
    }

    private void setupListeners() {
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
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

        // Kiểm tra xem có thông tin đăng ký không
        if (tempEmail == null && tempPhone == null) {
            Log.d("PasswordRegistrationActivity", "Không có thông tin đăng ký, quay về RegisterActivity");
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
            // Thay vì sử dụng inputLayoutPassword.setError(), hãy sử dụng etPassword.setError()
            etPassword.setError("Vui lòng nhập mật khẩu");
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Mật khẩu xác nhận không khớp");
            return;
        }

        try {
            // Tạo tài khoản mới trong database
            String accountId = waterTrackerDao.createAccount(tempEmail, tempPhone, password);

            if (accountId != null) {
                // Lấy userId từ accountId
                String userId = waterTrackerDao.getUserIdByAccountId(accountId);

                // Lưu thông tin đăng nhập vào SharedPreferences
                saveLoginInfo(accountId, userId);

                // Xóa thông tin đăng ký tạm thời
                clearRegistrationInfo();

                Toast.makeText(this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();

                // Chuyển đến màn hình chính
                Intent intent = new Intent(PasswordRegistrationActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Đăng ký thất bại. Vui lòng thử lại", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("PasswordRegistration", "Error in registerUser", e);
            Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // Lưu thông tin đăng nhập
    private void saveLoginInfo(String accountId, String userId) {
        SharedPreferences sharedPreferences = getSharedPreferences("WaterReminderPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("accountId", accountId);
        editor.putString("userId", userId);
        editor.putBoolean("isLoggedIn", true);
        editor.apply();
    }

    // Xóa thông tin đăng ký tạm thời
    private void clearRegistrationInfo() {
        SharedPreferences sharedPreferences = getSharedPreferences("WaterReminderPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("tempEmail");
        editor.remove("tempPhone");
        editor.remove("isRegistering"); // Xóa flag isRegistering
        editor.apply();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (waterTrackerDao != null) {
            waterTrackerDao.close();
        }
    }
}