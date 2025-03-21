package com.example.watertrackerandroidapp.LoginFunction;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.watertrackerandroidapp.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class VerifyOTPActivity extends AppCompatActivity {
    private static final String TAG = "VerifyOTPActivity";

    private TextInputLayout inputLayoutOTP;
    private TextInputEditText etOTP;
    private Button btnVerify;
    private TextView tvResendOTP, tvMessage;
    private CountDownTimer resendTimer;

    // Thêm biến để lưu trữ thông tin từ Intent
    private String identifier;
    private String correctOTP = "123456"; // Mã OTP mặc định
    private boolean isRegistrationFlow = false; // Mặc định là false

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_otp);

        // Khởi tạo các view
        inputLayoutOTP = findViewById(R.id.inputLayoutOTP);
        etOTP = findViewById(R.id.etOTP);
        btnVerify = findViewById(R.id.btnConfirm);
        tvResendOTP = findViewById(R.id.tvResendOTP);
        tvMessage = findViewById(R.id.tvOTPMessage);

        // Lấy thông tin từ Intent
        Intent intent = getIntent();
        if (intent != null) {
            // Kiểm tra xem có phải là luồng đăng ký không
            isRegistrationFlow = intent.getBooleanExtra("isRegistrationFlow", false);
            identifier = intent.getStringExtra("identifier");
            correctOTP = intent.getStringExtra("otp");

            if (correctOTP == null) {
                correctOTP = "123456"; // Mã OTP mặc định nếu không được truyền
            }
        }

        // Cập nhật thông báo
        if (tvMessage != null && identifier != null) {
            tvMessage.setText("Mã xác nhận đã được gửi đến " + identifier);
        }

        // Thiết lập sự kiện cho nút xác nhận
        btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String otp = etOTP.getText().toString().trim();

                if (otp.isEmpty()) {
                    inputLayoutOTP.setError("Vui lòng nhập mã xác nhận");
                    return;
                }

                if (otp.equals(correctOTP)) {
                    // Mã OTP đúng
                    if (isRegistrationFlow) {
                        // Luồng đăng ký - chuyển đến màn hình nhập mật khẩu
                        Intent passwordIntent = new Intent(VerifyOTPActivity.this, PasswordRegistrationActivity.class);
                        passwordIntent.putExtra("identifier", identifier);
                        startActivity(passwordIntent);
                        Toast.makeText(VerifyOTPActivity.this,
                                "Xác thực thành công, vui lòng tạo mật khẩu", Toast.LENGTH_SHORT).show();
                    } else {
                        // Luồng quên mật khẩu - chuyển đến màn hình đặt lại mật khẩu
                        Intent resetIntent = new Intent(VerifyOTPActivity.this, ResetPasswordActivity.class);
                        resetIntent.putExtra("identifier", identifier);
                        startActivity(resetIntent);
                        Toast.makeText(VerifyOTPActivity.this,
                                "Xác thực thành công, vui lòng đặt lại mật khẩu", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Mã OTP sai
                    inputLayoutOTP.setError("Mã xác nhận không đúng");
                    Toast.makeText(VerifyOTPActivity.this,
                            "Mã xác nhận không đúng", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Thiết lập sự kiện cho nút gửi lại mã
        tvResendOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tvResendOTP.isEnabled()) {
                    // Gửi lại mã OTP
                    Toast.makeText(VerifyOTPActivity.this,
                            "Đã gửi lại mã xác nhận", Toast.LENGTH_SHORT).show();
                    startResendTimer();
                }
            }
        });

        // Bắt đầu bộ đếm thời gian
        startResendTimer();
    }

    private void startResendTimer() {
        tvResendOTP.setEnabled(false);

        if (resendTimer != null) {
            resendTimer.cancel();
        }

        resendTimer = new CountDownTimer(30000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tvResendOTP.setText("Gửi lại mã (" + (millisUntilFinished / 1000) + "s)");
            }

            @Override
            public void onFinish() {
                tvResendOTP.setText("Gửi lại mã");
                tvResendOTP.setEnabled(true);
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (resendTimer != null) {
            resendTimer.cancel();
        }
    }
}