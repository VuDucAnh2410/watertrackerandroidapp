package com.example.watertrackerandroidapp.SettingFunction;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.watertrackerandroidapp.HomeFunction.MainActivity;
import com.example.watertrackerandroidapp.R;
import com.example.watertrackerandroidapp.SettingFunction.Activity.ScheduleActivity;
import com.example.watertrackerandroidapp.SettingFunction.Activity.SoundSelectionActivity;

public class SettingActivity extends AppCompatActivity {

    private TextView tvWaterGoal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting);

        // Khởi tạo các view
        tvWaterGoal = findViewById(R.id.tvWaterGoal);
        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navStats = findViewById(R.id.navStats);
        LinearLayout navSettings = findViewById(R.id.navSettings);
        LinearLayout navProfile = findViewById(R.id.navProfile);

        // Lấy dữ liệu từ SharedPreferences và hiển thị mục tiêu nước
        SharedPreferences sharedPref = getSharedPreferences("WaterPrefs", MODE_PRIVATE);
        int waterGoal = sharedPref.getInt("waterGoal", 2000);
        if (tvWaterGoal != null) {
            tvWaterGoal.setText(waterGoal + " ml");
        }

        // Xử lý điều hướng bottom navigation
        if (navHome != null) {
            navHome.setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));
        }
        if (navStats != null) {
            navStats.setOnClickListener(v -> {
                // Chưa có màn hình thống kê, để trống
            });
        }
        if (navSettings != null) {
            navSettings.setOnClickListener(v -> {
                // Đã ở màn hình Settings, không cần làm gì
            });
        }
        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                // Chưa có màn hình hồ sơ, để trống
            });
        }
    }

    // Xử lý click vào "Lịch nhắc nhở"
    public void onReminderClick(View view) {
        startActivity(new Intent(this, ScheduleActivity.class));
    }

    // Xử lý click vào "Âm thanh nhắc nhở"
    public void onSoundClick(View view) {
        startActivity(new Intent(this, SoundSelectionActivity.class));
    }
}