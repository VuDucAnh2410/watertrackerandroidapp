package com.example.watertrackerandroidapp.LoginFunction;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.watertrackerandroidapp.R;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textfield.TextInputEditText;

public class ResetPasswordActivity extends AppCompatActivity {
    private TextInputLayout inputLayoutNewPassword, inputLayoutConfirmPassword;
    private TextInputEditText etNewPassword, etConfirmPassword;
    private Button btnResetPassword;
    private TextView tvRegister;
    private String identifier;
    private boolean isPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        // Lấy dữ liệu từ Intent
        identifier = getIntent().getStringExtra("identifier");
        isPhone = getIntent().getBooleanExtra("isPhone", true);

        // Khởi tạo các view
        inputLayoutNewPassword = findViewById(R.id.inputLayoutNewPassword);
        inputLayoutConfirmPassword = findViewById(R.id.inputLayoutConfirmPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        tvRegister = findViewById(R.id.tvRegister);

        // Thiết lập sự kiện cho nút đặt lại mật khẩu
        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validatePasswords()) {
                    // Chuyển đến màn hình thành công
                    Intent intent = new Intent(ResetPasswordActivity.this, ResetSuccessActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });

        // Thiết lập sự kiện cho nút đăng ký
        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    // Kiểm tra tính hợp lệ của mật khẩu
    private boolean validatePasswords() {
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (newPassword.isEmpty()) {
            inputLayoutNewPassword.setError("Vui lòng nhập mật khẩu mới");
            return false;
        }

        if (confirmPassword.isEmpty()) {
            inputLayoutConfirmPassword.setError("Vui lòng nhập lại mật khẩu");
            return false;
        }

        if (!newPassword.equals(confirmPassword)) {
            inputLayoutConfirmPassword.setError("Mật khẩu không khớp");
            return false;
        }

        if (newPassword.length() < 6) {
            inputLayoutNewPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            return false;
        }

        inputLayoutNewPassword.setError(null);
        inputLayoutConfirmPassword.setError(null);
        return true;
    }
}