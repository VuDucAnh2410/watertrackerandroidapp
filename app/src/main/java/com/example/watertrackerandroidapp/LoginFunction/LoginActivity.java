package com.example.watertrackerandroidapp.LoginFunction;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.watertrackerandroidapp.HomeFunction.MainActivity;
import com.example.watertrackerandroidapp.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {
    private EditText etAccount; // Ô nhập tài khoản (số điện thoại hoặc email)
    private EditText etPassword; // Ô nhập mật khẩu
    private TabLayout tabLayout;
    private Button btnLogin;
    private TextView tvRegister, tvForgotPassword;
    private TextInputLayout inputLayoutAccount; // TextInputLayout cho ô nhập tài khoản

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Khởi tạo các view
        etAccount = findViewById(R.id.etAccount);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tabLayout = findViewById(R.id.tabLayout);
        tvRegister = findViewById(R.id.tvRegister);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        inputLayoutAccount = findViewById(R.id.inputLayoutAccount); // Khởi tạo TextInputLayout

        // Đặt sự kiện cho nút Đăng nhập
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String account = etAccount.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                if (account.isEmpty()) {
                    etAccount.setError("Vui lòng nhập tài khoản");
                    return;
                }
                if (password.isEmpty()) {
                    etPassword.setError("Vui lòng nhập mật khẩu");
                    return;
                }

                login(account, password); // Chỉ gọi hàm đăng nhập nếu không có lỗi
            }
        });

        // Đặt sự kiện cho TabLayout
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                updateInputLabel(tab.getPosition()); // Cập nhật nhãn ô nhập
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Đặt sự kiện cho TextView Đăng ký
        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Chuyển đến RegisterActivity
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        // Đặt sự kiện cho TextView Quên mật khẩu
        tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Logic để xử lý quên mật khẩu
                Toast.makeText(LoginActivity.this, "Quên mật khẩu", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });

        // Cập nhật ô nhập khi khởi tạo
        updateInputLabel(tabLayout.getSelectedTabPosition());
    }

    // Cập nhật ô nhập dựa trên tab đã chọn
    private void updateInputLabel(int position) {
        if (position == 0) {
            inputLayoutAccount.setHint(getString(R.string.hint_phone)); // Cập nhật label thành "Nhập số điện thoại"
            etAccount.setHint(R.string.hint_phone); // Placeholder cho số điện thoại
        } else {
            inputLayoutAccount.setHint(getString(R.string.hint_email)); // Cập nhật label thành "Nhập email"
            etAccount.setHint(R.string.hint_email); // Placeholder cho email
        }
        etAccount.setText(""); // Xóa nội dung khi chuyển tab
    }

    private void login(String account, String password) {
        // Logic để xử lý đăng nhập
        Toast.makeText(this, "Đăng nhập với tài khoản: " + account, Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}