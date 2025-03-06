package com.example.watertrackerandroidapp.LoginFunction;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.watertrackerandroidapp.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textfield.TextInputEditText;

public class ForgotPasswordActivity extends AppCompatActivity {
    // Khai báo các view
    private TabLayout tabLayout;
    private TextInputLayout inputLayoutAccount;
    private TextInputEditText etAccount;
    private Button btnResetPassword;
    private TextView tvLogin;
    private TextView tvError;
    private TextView tvRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Khởi tạo các view
        tabLayout = findViewById(R.id.tabLayout);
        inputLayoutAccount = findViewById(R.id.inputLayoutAccount);
        etAccount = findViewById(R.id.etAccount);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        tvLogin = findViewById(R.id.tvLogin);
        tvError = findViewById(R.id.tvError);

        // Thiết lập sự kiện cho TabLayout
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    // Tab Số điện thoại
                    etAccount.setHint("Nhập số điện thoại của bạn");
                    etAccount.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
                    updateInputLabel(tab.getPosition());
                    tvError.setText("* Vui lòng nhập số điện thoại để lấy mã xác nhận");
                } else {
                    // Tab Email
                    etAccount.setHint("Nhập email của bạn");
                    etAccount.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                    updateInputLabel(tab.getPosition());
                    tvError.setText("* Vui lòng nhập email để lấy mã xác nhận");
                }
                etAccount.setText("");
                inputLayoutAccount.setError(null);

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Thiết lập sự kiện cho nút lấy lại mật khẩu
        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInput()) {
                    // Chuyển đến màn hình xác thực OTP
                    Intent intent = new Intent(ForgotPasswordActivity.this, VerifyOTPActivity.class);
                    intent.putExtra("identifier", etAccount.getText().toString().trim());
                    intent.putExtra("isPhone", tabLayout.getSelectedTabPosition() == 0);
                    startActivity(intent);
                }
            }
        });

        // Thiết lập sự kiện cho nút quay lại đăng nhập
        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

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
    private boolean validateInput() {
        String input = etAccount.getText().toString().trim();
        boolean isPhone = tabLayout.getSelectedTabPosition() == 0;

        if (input.isEmpty()) {
            inputLayoutAccount.setError("Vui lòng nhập " + (isPhone ? "số điện thoại" : "email"));
            return false;
        }

        if (isPhone) {
            if (!input.matches("^0\\d{9}$")) {
                inputLayoutAccount.setError("Số điện thoại không hợp lệ");
                return false;
            }
        } else {
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(input).matches()) {
                inputLayoutAccount.setError("Email không hợp lệ");
                return false;
            }
        }

        inputLayoutAccount.setError(null);
        return true;
    }
}