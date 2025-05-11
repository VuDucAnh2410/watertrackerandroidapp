package com.example.watertrackerandroidapp.LoginFunction;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.watertrackerandroidapp.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {
    private TabLayout tabLayout;
    private TextInputLayout inputLayoutPhone;
    private TextInputEditText etInput;
    private Button btnResetPassword;
    private TextView tvLogin, tvValidationMessage;
    private FirebaseAuth mAuth;
    private boolean isPhoneSelected = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Khởi tạo Firebase
        mAuth = FirebaseAuth.getInstance();

        // Khởi tạo views
        tabLayout = findViewById(R.id.tabLayout);
        inputLayoutPhone = findViewById(R.id.inputLayoutPhone);
        etInput = findViewById(R.id.etInput);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        tvLogin = findViewById(R.id.tvLogin);
        tvValidationMessage = findViewById(R.id.tvValidationMessage);

        // Thiết lập TabLayout
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                isPhoneSelected = tab.getPosition() == 0;
                etInput.setText("");
                etInput.setInputType(isPhoneSelected ? android.text.InputType.TYPE_CLASS_PHONE : android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                inputLayoutPhone.setHint(isPhoneSelected ? getString(R.string.hint_phone) : getString(R.string.hint_email));
                tvValidationMessage.setText(isPhoneSelected ? "* Vui lòng nhập số điện thoại để nhận email đặt lại mật khẩu" : "* Vui lòng nhập email để nhận email đặt lại mật khẩu");
                inputLayoutPhone.setError(null);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Xử lý nút gửi email đặt lại mật khẩu
        btnResetPassword.setOnClickListener(v -> {
            String input = etInput.getText().toString().trim();

            if (TextUtils.isEmpty(input)) {
                tvValidationMessage.setText(isPhoneSelected ? "* Vui lòng nhập số điện thoại" : "* Vui lòng nhập email");
                inputLayoutPhone.setError(isPhoneSelected ? "Vui lòng nhập số điện thoại" : "Vui lòng nhập email");
                return;
            }

            String email;
            if (isPhoneSelected) {
                if (!input.matches("\\d+")) {
                    tvValidationMessage.setText("* Số điện thoại không hợp lệ");
                    inputLayoutPhone.setError("Số điện thoại không hợp lệ");
                    return;
                }
                email = input + "@phone.com";
            } else {
                if (!Patterns.EMAIL_ADDRESS.matcher(input).matches()) {
                    tvValidationMessage.setText("* Email không hợp lệ");
                    inputLayoutPhone.setError("Email không hợp lệ");
                    return;
                }
                email = input;
            }

            // Gửi email đặt lại mật khẩu
            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(ForgotPasswordActivity.this, "Email đặt lại mật khẩu đã được gửi. Vui lòng kiểm tra email.", Toast.LENGTH_LONG).show();
                            // Chuyển về LoginActivity
                            Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            tvValidationMessage.setText("* Lỗi: " + task.getException().getMessage());
                            inputLayoutPhone.setError("Lỗi gửi email");
                        }
                    });
        });

        // Xử lý liên kết quay lại đăng nhập
        tvLogin.setOnClickListener(v -> {
            Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}