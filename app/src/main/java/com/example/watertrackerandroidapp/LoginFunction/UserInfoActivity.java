package com.example.watertrackerandroidapp.LoginFunction;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.watertrackerandroidapp.DataBase.WaterTrackerDao;
import com.example.watertrackerandroidapp.HomeFunction.MainActivity;
import com.example.watertrackerandroidapp.R;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class UserInfoActivity extends AppCompatActivity {
    private static final String TAG = "UserInfoActivity";

    private TextInputEditText etFullName;
    private EditText etWeight, etHeight, etAge;
    private RadioGroup rgGender;
    private RadioButton rbMale, rbFemale, rbOther;
    private EditText etSleepHour, etSleepMinute, etWakeHour, etWakeMinute;
    private Button btnSave;

    private WaterTrackerDao waterTrackerDao;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        // Khởi tạo DAO
        waterTrackerDao = new WaterTrackerDao(this);

        // Lấy userId từ SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("WaterReminderPrefs", MODE_PRIVATE);
        userId = sharedPreferences.getString("userId", null);

        if (userId == null) {
            // Nếu không có userId, quay lại màn hình đăng nhập
            Toast.makeText(this, "Phiên đăng nhập không hợp lệ", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Kiểm tra xem người dùng đã nhập thông tin chưa
        if (!waterTrackerDao.isFirstTimeLogin(userId)) {
            // Nếu đã nhập thông tin rồi, chuyển đến MainActivity
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        initViews();
        setupListeners();
    }

    private void initViews() {
        etFullName = findViewById(R.id.etFullName);
        etWeight = findViewById(R.id.etWeight);
        etHeight = findViewById(R.id.etHeight);
        etAge = findViewById(R.id.etAge);

        rgGender = findViewById(R.id.rgGender);
        rbMale = findViewById(R.id.rbMale);
        rbFemale = findViewById(R.id.rbFemale);
        rbOther = findViewById(R.id.rbOther);

        etSleepHour = findViewById(R.id.etSleepHour);
        etSleepMinute = findViewById(R.id.etSleepMinute);
        etWakeHour = findViewById(R.id.etWakeHour);
        etWakeMinute = findViewById(R.id.etWakeMinute);

        btnSave = findViewById(R.id.btnSave);
    }

    private void setupListeners() {
        btnSave.setOnClickListener(v -> saveUserInfo());
    }

    private void saveUserInfo() {
        // Validate input
        if (!validateInput()) {
            return;
        }

        try {
            // Lấy thông tin từ form
            String fullName = etFullName.getText().toString().trim();
            float weight = Float.parseFloat(etWeight.getText().toString().trim());
            float height = Float.parseFloat(etHeight.getText().toString().trim());
            int age = Integer.parseInt(etAge.getText().toString().trim());

            String gender;
            int checkedId = rgGender.getCheckedRadioButtonId();
            if (checkedId == R.id.rbMale) {
                gender = "Nam";
            } else if (checkedId == R.id.rbFemale) {
                gender = "Nữ";
            } else {
                gender = "Khác";
            }

            // Lấy thời gian ngủ và thức dậy
            String sleepTime = String.format("%02d:%02d",
                    Integer.parseInt(etSleepHour.getText().toString()),
                    Integer.parseInt(etSleepMinute.getText().toString()));

            String wakeTime = String.format("%02d:%02d",
                    Integer.parseInt(etWakeHour.getText().toString()),
                    Integer.parseInt(etWakeMinute.getText().toString()));

            // Tính toán lượng nước cần uống dựa trên cân nặng
            int dailyTarget = calculateDailyTarget(weight);

            // Cập nhật thông tin user
            boolean success = waterTrackerDao.updateUserInfo(
                    userId, fullName, gender, weight, height, age,
                    dailyTarget, wakeTime, sleepTime
            );

            if (success) {
                // Xóa tất cả reminder cũ (nếu có)
                waterTrackerDao.deleteAllReminders(userId);

                // Tạo các reminder mới
                createReminders(wakeTime, sleepTime);

                Toast.makeText(this, "Lưu thông tin thành công", Toast.LENGTH_SHORT).show();

                // Chuyển đến MainActivity
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Lưu thông tin thất bại", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error saving user info", e);
            Toast.makeText(this, "Có lỗi xảy ra: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateInput() {
        if (TextUtils.isEmpty(etFullName.getText())) {
            etFullName.setError("Vui lòng nhập tên");
            return false;
        }

        if (TextUtils.isEmpty(etWeight.getText())) {
            etWeight.setError("Vui lòng nhập cân nặng");
            return false;
        }

        if (TextUtils.isEmpty(etHeight.getText())) {
            etHeight.setError("Vui lòng nhập chiều cao");
            return false;
        }

        if (TextUtils.isEmpty(etAge.getText())) {
            etAge.setError("Vui lòng nhập tuổi");
            return false;
        }

        if (rgGender.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Vui lòng chọn giới tính", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validate thời gian
        try {
            if (TextUtils.isEmpty(etSleepHour.getText()) || TextUtils.isEmpty(etSleepMinute.getText()) ||
                    TextUtils.isEmpty(etWakeHour.getText()) || TextUtils.isEmpty(etWakeMinute.getText())) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thời gian", Toast.LENGTH_SHORT).show();
                return false;
            }

            int sleepHour = Integer.parseInt(etSleepHour.getText().toString());
            int sleepMinute = Integer.parseInt(etSleepMinute.getText().toString());
            int wakeHour = Integer.parseInt(etWakeHour.getText().toString());
            int wakeMinute = Integer.parseInt(etWakeMinute.getText().toString());

            if (sleepHour < 0 || sleepHour > 23 || sleepMinute < 0 || sleepMinute > 59 ||
                    wakeHour < 0 || wakeHour > 23 || wakeMinute < 0 || wakeMinute > 59) {
                Toast.makeText(this, "Thời gian không hợp lệ", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thời gian", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private int calculateDailyTarget(float weight) {
        // Công thức: 30ml * cân nặng (kg)
        return Math.round(weight * 30);
    }

    private void createReminders(String wakeTime, String sleepTime) {
        try {
            // Parse thời gian
            String[] wakeParts = wakeTime.split(":");
            String[] sleepParts = sleepTime.split(":");

            int wakeHour = Integer.parseInt(wakeParts[0]);
            int wakeMinute = Integer.parseInt(wakeParts[1]);
            int sleepHour = Integer.parseInt(sleepParts[0]);
            int sleepMinute = Integer.parseInt(sleepParts[1]);

            // Tạo danh sách các thời điểm nhắc nhở (mỗi giờ từ lúc thức dậy đến lúc đi ngủ)
            List<String> reminderTimes = new ArrayList<>();

            // Bắt đầu từ giờ thức dậy
            int currentHour = wakeHour;

            // Thêm reminder cho mỗi giờ từ lúc thức dậy đến lúc đi ngủ
            while (currentHour != sleepHour) {
                reminderTimes.add(String.format("%02d:00", currentHour));
                currentHour = (currentHour + 1) % 24;
            }

            // Lưu các reminder vào database
            for (String time : reminderTimes) {
                waterTrackerDao.createReminder(userId, time);
                Log.d(TAG, "Created reminder at " + time);
            }

            Log.d(TAG, "Created " + reminderTimes.size() + " reminders");
        } catch (Exception e) {
            Log.e(TAG, "Error creating reminders", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (waterTrackerDao != null) {
            waterTrackerDao.close();
        }
    }
}