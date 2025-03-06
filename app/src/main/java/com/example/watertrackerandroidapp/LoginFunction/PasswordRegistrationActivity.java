package com.example.watertrackerandroidapp.LoginFunction;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.watertrackerandroidapp.R;

public class PasswordRegistrationActivity extends AppCompatActivity {

    private EditText etPassword;
    private EditText etConfirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_registration);

        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        Button btnRegister = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = etPassword.getText().toString();
                String confirmPassword = etConfirmPassword.getText().toString();

                // Kiểm tra mật khẩu
                if (password.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(PasswordRegistrationActivity.this, "Vui lòng nhập mật khẩu", Toast.LENGTH_SHORT).show();
                } else if (!password.equals(confirmPassword)) {
                    Toast.makeText(PasswordRegistrationActivity.this, "Mật khẩu không khớp", Toast.LENGTH_SHORT).show();

                } else if (password.length() < 6) {
                    Toast.makeText(PasswordRegistrationActivity.this, "Mật khẩu phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
                }
                else {
                    // Xử lý đăng ký (giả sử thành công)
                    Toast.makeText(PasswordRegistrationActivity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();

                    // Quay lại LoginActivity
                    Intent intent = new Intent(PasswordRegistrationActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish(); // Kết thúc activity hiện tại
                }
            }
        });
    }
}