package com.example.watertrackerandroidapp.LoginFunction;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.watertrackerandroidapp.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class RegisterActivity extends AppCompatActivity {
    private TextInputEditText etInput;
    private TextInputEditText etOTP;
    private TextInputLayout inputLayoutPhone;
    private Button btnSendOTP, btnConfirm;
    private TextView tvLogin, tvResendOTP, tvValidationMessage;
    private TabLayout tabLayout;
    private boolean isPhoneTab = true;
    private CountDownTimer resendTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initViews();
        setupListeners();
        updateTabUI();
    }

    private void initViews() {
        etInput = findViewById(R.id.etInput);
        etOTP = findViewById(R.id.etOTP);
        inputLayoutPhone = findViewById(R.id.inputLayoutPhone);
        btnSendOTP = findViewById(R.id.btnSendOTP);
        btnConfirm = findViewById(R.id.btnConfirm);
        tvLogin = findViewById(R.id.tvLogin);
        tvResendOTP = findViewById(R.id.tvResendOTP);
        tvValidationMessage = findViewById(R.id.tvValidationMessage);
        tabLayout = findViewById(R.id.tabLayout);

        tvResendOTP.setVisibility(View.GONE);
    }

    private void setupListeners() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                isPhoneTab = tab.getPosition() == 0;
                updateTabUI();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        btnSendOTP.setOnClickListener(v -> {
            String input = etInput.getText().toString().trim();
            if (input.isEmpty()) {
                inputLayoutPhone.setError("Vui lòng nhập thông tin");
                return;
            } else {
                inputLayoutPhone.setError(null);
            }

            if (isPhoneTab) {
                if (!isValidPhoneNumber(input)) {
                    inputLayoutPhone.setError("Số điện thoại không hợp lệ");
                    return;
                }
                sendOTP(input);
            } else {
                if (!isValidEmail(input)) {
                    inputLayoutPhone.setError("Email không hợp lệ");
                    return;
                }
                sendEmailVerification(input);
            }

            startResendTimer();
        });

        btnConfirm.setOnClickListener(v -> {
            String otp = etOTP.getText().toString().trim();
            if (otp.isEmpty()) {
                etOTP.setError("Vui lòng nhập mã OTP");
                return;
            }

            if (isPhoneTab) {
                confirmOTP(otp);
            } else {
                confirmEmailVerification(otp);
            }

            // Chuyển đến PasswordRegistrationActivity
            Intent intent = new Intent(RegisterActivity.this, PasswordRegistrationActivity.class);
            startActivity(intent);
            finish();
        });

        tvLogin.setOnClickListener(v -> finish());

        tvResendOTP.setOnClickListener(v -> {
            if (tvResendOTP.isEnabled()) {
                String input = etInput.getText().toString().trim();
                if (!input.isEmpty()) {
                    if (isPhoneTab) {
                        sendOTP(input);
                    } else {
                        sendEmailVerification(input);
                    }
                    startResendTimer();
                }
            }
        });
    }

    private void updateTabUI() {
        // Update validation message and input hint
        if (isPhoneTab) {
            tvValidationMessage.setText("* Vui lòng nhập số điện thoại để tạo tài khoản");
            etInput.setHint("Nhập số điện thoại của bạn");
            etInput.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
        } else {
            tvValidationMessage.setText("* Vui lòng nhập email để tạo tài khoản");
            etInput.setHint("Nhập email của bạn");
            etInput.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        }

        etInput.setText("");
        etOTP.setText("");
        inputLayoutPhone.setError(null);

        // Reset OTP timer
        if (resendTimer != null) {
            resendTimer.cancel();
        }
        tvResendOTP.setVisibility(View.GONE);
    }

    private void startResendTimer() {
        tvResendOTP.setVisibility(View.VISIBLE);
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

    private boolean isValidPhoneNumber(String phoneNumber) {
        // Kiểm tra số điện thoại Việt Nam (10 số, bắt đầu bằng 0)
        return phoneNumber.matches("^0\\d{9}$");
    }

    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void sendOTP(String phoneNumber) {
        Toast.makeText(this, "Mã OTP đã được gửi đến " + phoneNumber, Toast.LENGTH_SHORT).show();
    }

    private void sendEmailVerification(String email) {
        Toast.makeText(this, "Mã xác nhận đã được gửi đến " + email, Toast.LENGTH_SHORT).show();
    }

    private void confirmOTP(String otp) {
        Toast.makeText(this, "Đã xác nhận mã OTP: " + otp, Toast.LENGTH_SHORT).show();
    }

    private void confirmEmailVerification(String emailVerificationCode) {
        Toast.makeText(this, "Đã xác nhận mã email: " + emailVerificationCode, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (resendTimer != null) {
            resendTimer.cancel();
        }
    }
}