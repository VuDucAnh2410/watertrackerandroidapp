package com.example.watertrackerandroidapp.LoginFunction;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.watertrackerandroidapp.R;
import com.example.watertrackerandroidapp.Repository.AuthRepository;
import com.example.watertrackerandroidapp.LoginFunction.utils.NetworkUtils;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";

    // UI components
    private TabLayout tabLayout;
    private TextInputLayout inputLayoutPhone, inputLayoutPassword, inputLayoutConfirmPassword;
    private TextInputEditText etInput, etPassword, etConfirmPassword;
    private Button btnRegister;
    private TextView tvValidationMessage, tvLogin;

    // Repository
    private AuthRepository authRepository;

    // State variables
    private boolean isPhoneTab = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        try {
            // Khởi tạo repository
            authRepository = new AuthRepository();

            // Khởi tạo views
            initViews();

            // Thiết lập listeners
            setupListeners();

            Log.d(TAG, "onCreate: RegisterActivity đã khởi tạo thành công");
        } catch (Exception e) {
            Log.e(TAG, "onCreate: Lỗi khi khởi tạo RegisterActivity", e);
            Toast.makeText(this, "Lỗi khởi tạo: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void initViews() {
        try {
            Log.d(TAG, "initViews: Bắt đầu khởi tạo views");

            tabLayout = findViewById(R.id.tabLayout);
            inputLayoutPhone = findViewById(R.id.inputLayoutPhone);
            inputLayoutPassword = findViewById(R.id.inputLayoutPassword);
            inputLayoutConfirmPassword = findViewById(R.id.inputLayoutConfirmPassword);
            etInput = findViewById(R.id.etInput);
            etPassword = findViewById(R.id.etPassword);
            etConfirmPassword = findViewById(R.id.etConfirmPassword);
            btnRegister = findViewById(R.id.btnRegister);
            tvValidationMessage = findViewById(R.id.tvValidationMessage);
            tvLogin = findViewById(R.id.tvLogin);

            // Thiết lập thông báo ban đầu
            tvValidationMessage.setText("* Vui lòng nhập số điện thoại để tạo tài khoản");

            Log.d(TAG, "initViews: Đã khởi tạo views thành công");
        } catch (Exception e) {
            Log.e(TAG, "initViews: Lỗi khi khởi tạo views", e);
            Toast.makeText(this, "Lỗi khởi tạo views: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void setupListeners() {
        try {
            Log.d(TAG, "setupListeners: Bắt đầu thiết lập listeners");

            // Listener cho TabLayout
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

            // Listener cho nút đăng ký
            btnRegister.setOnClickListener(v -> {
                try {
                    if (!NetworkUtils.isNetworkAvailable(RegisterActivity.this)) {
                        tvValidationMessage.setText("* Không có kết nối mạng");
                        return;
                    }

                    registerUser();
                } catch (Exception e) {
                    Log.e(TAG, "onClick btnRegister: Lỗi", e);
                    Toast.makeText(RegisterActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });

            // Listener cho TextView đăng nhập
            tvLogin.setOnClickListener(v -> {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            });

            Log.d(TAG, "setupListeners: Đã thiết lập listeners thành công");
        } catch (Exception e) {
            Log.e(TAG, "setupListeners: Lỗi khi thiết lập listeners", e);
            Toast.makeText(this, "Lỗi thiết lập listeners: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void updateTabUI() {
        try {
            Log.d(TAG, "updateTabUI: Cập nhật UI theo tab, isPhoneTab = " + isPhoneTab);

            if (isPhoneTab) {
                tvValidationMessage.setText("* Vui lòng nhập số điện thoại để tạo tài khoản");
                etInput.setHint("Nhập số điện thoại của bạn");
                inputLayoutPhone.setHint("Nhập số điện thoại");
                etInput.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
            } else {
                tvValidationMessage.setText("* Vui lòng nhập email để tạo tài khoản");
                etInput.setHint("Nhập email của bạn");
                inputLayoutPhone.setHint("Nhập email");
                etInput.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
            }

            etInput.setText("");
            etPassword.setText("");
            etConfirmPassword.setText("");
            inputLayoutPhone.setError(null);
            inputLayoutPassword.setError(null);
            inputLayoutConfirmPassword.setError(null);

            Log.d(TAG, "updateTabUI: Đã cập nhật UI thành công");
        } catch (Exception e) {
            Log.e(TAG, "updateTabUI: Lỗi khi cập nhật UI", e);
            Toast.makeText(this, "Lỗi cập nhật UI: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void registerUser() {
        String input = etInput.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Kiểm tra input
        if (TextUtils.isEmpty(input)) {
            inputLayoutPhone.setError(isPhoneTab ? "Vui lòng nhập số điện thoại" : "Vui lòng nhập email");
            return;
        }

        if (isPhoneTab && !input.matches("\\d{10,11}")) {
            inputLayoutPhone.setError("Số điện thoại không hợp lệ");
            return;
        }

        if (!isPhoneTab && !Patterns.EMAIL_ADDRESS.matcher(input).matches()) {
            inputLayoutPhone.setError("Email không hợp lệ");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            inputLayoutPassword.setError("Vui lòng nhập mật khẩu");
            return;
        }

        if (password.length() < 6) {
            inputLayoutPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            return;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            inputLayoutConfirmPassword.setError("Vui lòng xác nhận mật khẩu");
            return;
        }

        if (!password.equals(confirmPassword)) {
            inputLayoutConfirmPassword.setError("Mật khẩu xác nhận không khớp");
            return;
        }

        // Tạo email cho đăng ký
        final String email = isPhoneTab ? input + "@phone.com" : input;
        final String phone = isPhoneTab ? input : "";

        // Đăng ký người dùng
        authRepository.registerWithEmail(email, password, phone, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                // Gửi email xác minh
                user.sendEmailVerification()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(RegisterActivity.this,
                                        "Đăng ký thành công! Vui lòng kiểm tra email để xác minh.",
                                        Toast.LENGTH_LONG).show();
                            } else {
                                Log.e(TAG, "sendEmailVerification: failed", task.getException());
                                Toast.makeText(RegisterActivity.this,
                                        "Không thể gửi email xác minh: " + task.getException().getMessage(),
                                        Toast.LENGTH_LONG).show();
                            }
                        });

                // Chuyển hướng về LoginActivity
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(RegisterActivity.this,
                        "Đăng ký thất bại: " + errorMessage,
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}