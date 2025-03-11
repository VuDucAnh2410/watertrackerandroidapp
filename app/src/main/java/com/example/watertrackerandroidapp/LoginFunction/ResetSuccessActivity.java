package com.example.watertrackerandroidapp.LoginFunction;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.watertrackerandroidapp.R;

public class ResetSuccessActivity extends AppCompatActivity {
    private Button btnLogin;
    private TextView tvMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_success);

        // Khởi tạo các view
        btnLogin = findViewById(R.id.btnLogin);

        // Hiển thị thông báo thành công
        tvMessage.setText("Mật khẩu của bạn đã được đặt lại thành công!");

        // Thiết lập sự kiện cho nút đăng nhập
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Quay lại màn hình đăng nhập và xóa tất cả các activity trước đó
                Intent intent = new Intent(ResetSuccessActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }
}
