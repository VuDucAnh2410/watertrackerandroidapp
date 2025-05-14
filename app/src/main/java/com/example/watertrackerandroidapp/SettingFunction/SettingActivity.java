package com.example.watertrackerandroidapp.SettingFunction;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.watertrackerandroidapp.HomeFunction.MainActivity;
import com.example.watertrackerandroidapp.Models.User;
import com.example.watertrackerandroidapp.R;
import com.example.watertrackerandroidapp.Repository.AuthRepository;
import com.example.watertrackerandroidapp.Repository.UserRepository;
import com.example.watertrackerandroidapp.SettingFunction.Activity.ScheduleActivity;
import com.example.watertrackerandroidapp.SettingFunction.Activity.SoundSelectionActivity;

public class SettingActivity extends AppCompatActivity {

    private TextView tvWaterGoal;
    private LinearLayout waterGoalLayout;
    private UserRepository userRepository;
    private AuthRepository authRepository;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting);

        // Khởi tạo repositories
        userRepository = new UserRepository();
        authRepository = new AuthRepository();

        // Khởi tạo các view
        tvWaterGoal = findViewById(R.id.tvWaterGoal);
        waterGoalLayout = (LinearLayout) findViewById(R.id.tvWaterGoal).getParent();
        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navStats = findViewById(R.id.navStats);
        LinearLayout navSettings = findViewById(R.id.navSettings);
        LinearLayout navProfile = findViewById(R.id.navProfile);

        // Kiểm tra trạng thái đăng nhập và lấy thông tin người dùng
        if (!authRepository.isUserLoggedIn()) {
            Toast.makeText(this, "Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, com.example.watertrackerandroidapp.LoginFunction.LoginActivity.class));
            finish();
            return;
        }

        String userId = authRepository.getCurrentUser().getUid();
        if (userId == null) {
            Toast.makeText(this, "Không thể lấy ID người dùng. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, com.example.watertrackerandroidapp.LoginFunction.LoginActivity.class));
            finish();
            return;
        }

        // Tải thông tin người dùng từ Firebase
        userRepository.getUserById(userId, new UserRepository.OnUserLoadedListener() {
            @Override
            public void onUserLoaded(User user) {
                currentUser = user;
                int waterGoal = user.getDailyTarget();
                // Lưu vào SharedPreferences để đồng bộ
                SharedPreferences sharedPref = getSharedPreferences("WaterPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt("waterGoal", waterGoal);
                editor.apply();

                // Hiển thị mục tiêu nước
                if (tvWaterGoal != null) {
                    tvWaterGoal.setText(waterGoal + " ml");
                }
            }
            @Override
            public void onError(String errorMessage) {
                Toast.makeText(SettingActivity.this, "Lỗi tải thông tin người dùng: " + errorMessage, Toast.LENGTH_LONG).show();
                // Sử dụng giá trị mặc định từ SharedPreferences nếu Firebase lỗi
                SharedPreferences sharedPref = getSharedPreferences("WaterPrefs", MODE_PRIVATE);
                int waterGoal = sharedPref.getInt("waterGoal", 2000);
                if (tvWaterGoal != null) {
                    tvWaterGoal.setText(waterGoal + " ml");
                }
            }
        });

        // Xử lý nhấn vào "Điều chỉnh mục tiêu"
        if (waterGoalLayout != null) {
            waterGoalLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showGoalDialog();
                }
            });
        }

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

    // Hiển thị dialog điều chỉnh mục tiêu
    private void showGoalDialog() {
        // Tạo dialog
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.setting_dialog_adjust_goal);

        // Khởi tạo SeekBar và các nút
        final TextView tvGoalValue = dialog.findViewById(R.id.tvGoalValue);
        final SeekBar seekBarGoal = dialog.findViewById(R.id.seekBarGoal);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);
        Button btnConfirm = dialog.findViewById(R.id.btnConfirm);

        // Đặt giá trị mặc định cho SeekBar từ SharedPreferences
        SharedPreferences sharedPref = getSharedPreferences("WaterPrefs", MODE_PRIVATE);
        int currentGoal = sharedPref.getInt("waterGoal", 2000);
        seekBarGoal.setProgress(currentGoal);
        tvGoalValue.setText(currentGoal + " ml");

        // Xử lý SeekBar
        seekBarGoal.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvGoalValue.setText(progress + " ml");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Xử lý nút Hủy bỏ
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        // Xử lý nút Đồng ý
        btnConfirm.setOnClickListener(v -> {
            // Lấy giá trị từ SeekBar
            int newGoal = seekBarGoal.getProgress();

            // Lưu vào SharedPreferences
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt("waterGoal", newGoal);
            editor.apply();

            // Cập nhật vào Firebase
            if (currentUser != null) {
                currentUser.setDailyTarget(newGoal);
                String userId = authRepository.getCurrentUser().getUid();
                userRepository.updateUserInfo(userId, currentUser, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(SettingActivity.this, "Cập nhật mục tiêu thành công", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(SettingActivity.this, "Lỗi cập nhật mục tiêu: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
            // Cập nhật TextView trên giao diện chính
            tvWaterGoal.setText(newGoal + " ml");

            // Đóng dialog
            dialog.dismiss();
        });

        // Hiển thị dialog
        dialog.show();
    }
}