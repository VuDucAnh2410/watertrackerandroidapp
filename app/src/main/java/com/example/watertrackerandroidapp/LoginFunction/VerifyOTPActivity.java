package com.example.watertrackerandroidapp.LoginFunction;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.watertrackerandroidapp.R;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textfield.TextInputEditText;

public class VerifyOTPActivity extends AppCompatActivity {
    private ImageButton btnBack;
    private TextInputLayout inputLayoutOTP;
    private TextInputEditText etOTP;
    private Button btnConfirm;
    private TextView tvResendOTP;
    private TextView tvRegister;
    private CountDownTimer resendTimer;
    private String identifier;
    private boolean isPhone;

    private TextView tvOTPMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_otp);

        // Lấy dữ liệu từ Intent
        identifier = getIntent().getStringExtra("identifier");
        isPhone = getIntent().getBooleanExtra("isPhone", true);

        // Khởi tạo các view
        btnBack = findViewById(R.id.btnBack);
        inputLayoutOTP = findViewById(R.id.inputLayoutOTP);
        etOTP = findViewById(R.id.etOTP);
        btnConfirm = findViewById(R.id.btnConfirm);
        tvResendOTP = findViewById(R.id.tvResendOTP);
        tvRegister = findViewById(R.id.tvRegister);
        tvOTPMessage = findViewById(R.id.tvOTPMessage);

        tvOTPMessage.setText("Mã OTP đã được gửi qua " + (isPhone ? "SMS" : "Email"));

        // Thiết lập sự kiện cho nút quay lại
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Thiết lập sự kiện cho nút xác nhận
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateOTP()) {
                    // Chuyển đến màn hình đặt lại mật khẩu
                    Intent intent = new Intent(VerifyOTPActivity.this, ResetPasswordActivity.class);
                    intent.putExtra("identifier", identifier);
                    intent.putExtra("isPhone", isPhone);
                    startActivity(intent);
                }
            }
        });

        // Thiết lập sự kiện cho nút gửi lại mã
        tvResendOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tvResendOTP.isEnabled()) {
                    // Logic gửi lại mã OTP
                    startResendTimer();
                }
            }
        });

        // Thiết lập sự kiện cho nút đăng ký
        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(VerifyOTPActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        // Bắt đầu đếm ngược thời gian gửi lại mã
        startResendTimer();
    }

    // Bắt đầu đếm ngược thời gian gửi lại mã
    private void startResendTimer() {
        if (resendTimer != null) {
            resendTimer.cancel();
        }

        resendTimer = new CountDownTimer(25000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tvResendOTP.setText("Gửi lại mã (" + (millisUntilFinished / 1000) + " giây)");
                tvResendOTP.setEnabled(false);
            }

            @Override
            public void onFinish() {
                tvResendOTP.setText("Gửi lại mã");
                tvResendOTP.setEnabled(true);
            }
        }.start();
    }

    // Kiểm tra tính hợp lệ của mã OTP
    private boolean validateOTP() {
        String otp = etOTP.getText().toString().trim();
        if (otp.isEmpty()) {
            inputLayoutOTP.setError("Vui lòng nhập mã OTP");
            return false;
        }
        if (otp.length() != 6) {
            inputLayoutOTP.setError("Mã OTP không hợp lệ");
            return false;
        }
        inputLayoutOTP.setError(null);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (resendTimer != null) {
            resendTimer.cancel();
        }
    }
}