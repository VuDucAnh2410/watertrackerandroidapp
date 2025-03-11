package com.example.watertrackerandroidapp.LoginFunction;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.watertrackerandroidapp.R;
import com.example.watertrackerandroidapp.DataBase.WaterTrackerDao;
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

    // Khai báo DAO
    private WaterTrackerDao waterTrackerDao;
    private String verificationCode = "123456"; // Mã OTP mặc định (trong thực tế nên tạo ngẫu nhiên)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Khởi tạo DAO
        waterTrackerDao = new WaterTrackerDao(this);

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
                    String identifier = etAccount.getText().toString().trim();
                    boolean isPhone = tabLayout.getSelectedTabPosition() == 0;

                    // Kiểm tra tài khoản tồn tại
                    String accountId = waterTrackerDao.getAccountIdByIdentifier(identifier);

                    if (accountId != null) {
                        // Tài khoản tồn tại, lưu thông tin và chuyển đến màn hình xác thực OTP
                        saveResetInfo(identifier, isPhone);

                        // Trong thực tế, bạn sẽ gửi mã OTP đến email hoặc số điện thoại
                        // Ở đây, chúng ta giả định mã OTP là "123456"

                        // Chuyển đến màn hình xác thực OTP
                        Intent intent = new Intent(ForgotPasswordActivity.this, VerifyOTPActivity.class);
                        startActivity(intent);
                    } else {
                        // Tài khoản không tồn tại
                        inputLayoutAccount.setError("Tài khoản không tồn tại");
                        Toast.makeText(ForgotPasswordActivity.this,
                                "Tài khoản không tồn tại", Toast.LENGTH_SHORT).show();
                    }
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

    // Lưu thông tin đặt lại mật khẩu
    // Lưu thông tin đặt lại mật khẩu
    private void saveResetInfo(String identifier, boolean isPhone) {
        SharedPreferences sharedPreferences = getSharedPreferences("WaterReminderPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("resetIdentifier", identifier);
        editor.putBoolean("resetIsPhone", isPhone);
        editor.putString("resetOTP", verificationCode); // Trong thực tế, mã OTP nên được tạo ngẫu nhiên
        editor.putBoolean("isResetting", true);
        editor.putBoolean("isRegistering", false); // Đảm bảo flag đăng ký bị tắt
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
