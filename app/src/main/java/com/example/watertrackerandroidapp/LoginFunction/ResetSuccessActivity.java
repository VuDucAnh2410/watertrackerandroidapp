package com.example.watertrackerandroidapp.LoginFunction;

import android.annotation.SuppressLint;
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

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_success);

        btnLogin = findViewById(R.id.btnLogin);
        tvMessage = findViewById(R.id.tvMessage);
        tvMessage.setText("Mật khẩu của bạn đã được đặt lại thành công!");

        btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(ResetSuccessActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}