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
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class ResetPasswordActivity extends AppCompatActivity {
    private TextInputLayout inputLayoutNewPassword, inputLayoutConfirmPassword;
    private TextInputEditText etNewPassword, etConfirmPassword;
    private Button btnResetPassword;
    private TextView tvMessage;

    private WaterTrackerDao waterTrackerDao;
    private String identifier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        // Khởi tạo DAO
        waterTrackerDao = new WaterTrackerDao(this);

        // Lấy thông tin từ SharedPreferences
        loadResetInfo();

        // Khởi tạo các view
        inputLayoutNewPassword = findViewById(R.id.inputLayoutNewPassword);
        inputLayoutConfirmPassword = findViewById(R.id.inputLayoutConfirmPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnResetPassword = findViewById(R.id.btnResetPassword);

        // Thiết lập sự kiện cho nút đặt lại mật khẩu
        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInput()) {
                    String newPassword = etNewPassword.getText().toString().trim();

                    // Cập nhật mật khẩu trong database
                    boolean success = waterTrackerDao.updatePassword(identifier, newPassword);

                    if (success) {
                        // Cập nhật thành công
                        Toast.makeText(ResetPasswordActivity.this,
                                "Đặt lại mật khẩu thành công", Toast.LENGTH_SHORT).show();

                        // Xóa thông tin đặt lại mật khẩu
                        clearResetInfo();

                        // Chuyển về màn hình đăng nhập
                        Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        // Cập nhật thất bại
                        Toast.makeText(ResetPasswordActivity.this,
                                "Đặt lại mật khẩu thất bại. Vui lòng thử lại", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void loadResetInfo() {
        SharedPreferences sharedPreferences = getSharedPreferences("WaterReminderPrefs", MODE_PRIVATE);
        identifier = sharedPreferences.getString("resetIdentifier", "");

        // Kiểm tra xem có đang trong quá trình đặt lại mật khẩu không
        boolean isResetting = sharedPreferences.getBoolean("isResetting", false);
        if (!isResetting || identifier.isEmpty()) {
            // Nếu không phải đang đặt lại mật khẩu, quay lại màn hình quên mật khẩu
            Intent intent = new Intent(this, ForgotPasswordActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private boolean validateInput() {
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (newPassword.isEmpty()) {
            inputLayoutNewPassword.setError("Vui lòng nhập mật khẩu mới");
            return false;
        }

        if (newPassword.length() < 6) {
            inputLayoutNewPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            return false;
        }

        if (confirmPassword.isEmpty()) {
            inputLayoutConfirmPassword.setError("Vui lòng xác nhận mật khẩu");
            return false;
        }

        if (!newPassword.equals(confirmPassword)) {
            inputLayoutConfirmPassword.setError("Mật khẩu xác nhận không khớp");
            return false;
        }

        inputLayoutNewPassword.setError(null);
        inputLayoutConfirmPassword.setError(null);
        return true;
    }

    // Xóa thông tin đặt lại mật khẩu
    private void clearResetInfo() {
        SharedPreferences sharedPreferences = getSharedPreferences("WaterReminderPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("resetIdentifier");
        editor.remove("resetIsPhone");
        editor.remove("resetOTP");
        editor.remove("isResetting");
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

