package com.example.watertrackerandroidapp.LoginFunction;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.watertrackerandroidapp.HomeFunction.MainActivity;
import com.example.watertrackerandroidapp.Models.User;
import com.example.watertrackerandroidapp.R;
import com.example.watertrackerandroidapp.Repository.ReminderRepository;
import com.example.watertrackerandroidapp.Repository.UserRepository;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class UserInfoActivity extends AppCompatActivity {
    private static final String TAG = "UserInfoActivity";
    private static final long TIMEOUT_MS = 30000; // Timeout 30 giây

    private TextInputEditText etFullName;
    private EditText etWeight, etHeight, etAge;
    private RadioGroup rgGender;
    private RadioButton rbMale, rbFemale, rbOther;
    private EditText etSleepHour, etSleepMinute, etWakeHour, etWakeMinute;
    private Button btnSave;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String userId;
    private UserRepository userRepository;
    private ReminderRepository reminderRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        // Khởi tạo Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        userRepository = new UserRepository();
        reminderRepository = new ReminderRepository();

        // Bật tính năng lưu trữ offline cho node users
        mDatabase.child("users").keepSynced(true);

        // Kiểm tra đăng nhập
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
            Log.d(TAG, "User logged in, userId: " + userId + ", auth.uid: " + (mAuth.getUid() != null ? mAuth.getUid() : "null"));
        } else {
            Log.e(TAG, "User ID is null");
            Toast.makeText(this, "Phiên đăng nhập không hợp lệ", Toast.LENGTH_SHORT).show();
            redirectToLogin();
            return;
        }

        // Khởi tạo views
        initViews();
        // Thiết lập listeners
        setupListeners();

        // Kiểm tra thông tin người dùng
        checkUserInfo();
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

    private void checkUserInfo() {
        userRepository.getUserById(userId, new UserRepository.OnUserLoadedListener() {
            @Override
            public void onUserLoaded(User user) {
                if (user != null && user.isHasUserInfo()) {
                    Log.d(TAG, "User already has info, redirecting to MainActivity");
                    redirectToMain();
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Error checking user info: " + errorMessage);
                Toast.makeText(UserInfoActivity.this, "Lỗi kiểm tra thông tin: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    private boolean validateInput() {
        if (TextUtils.isEmpty(etFullName.getText())) {
            etFullName.setError("Vui lòng nhập tên");
            Log.d(TAG, "Name is empty");
            return false;
        }

        if (TextUtils.isEmpty(etWeight.getText())) {
            etWeight.setError("Vui lòng nhập cân nặng");
            Log.d(TAG, "Weight is empty");
            return false;
        }

        if (TextUtils.isEmpty(etHeight.getText())) {
            etHeight.setError("Vui lòng nhập chiều cao");
            Log.d(TAG, "Height is empty");
            return false;
        }

        if (TextUtils.isEmpty(etAge.getText())) {
            etAge.setError("Vui lòng nhập tuổi");
            Log.d(TAG, "Age is empty");
            return false;
        }

        if (rgGender.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Vui lòng chọn giới tính", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Gender not selected");
            return false;
        }

        if (TextUtils.isEmpty(etSleepHour.getText()) || TextUtils.isEmpty(etSleepMinute.getText()) ||
                TextUtils.isEmpty(etWakeHour.getText()) || TextUtils.isEmpty(etWakeMinute.getText())) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thời gian", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Time fields incomplete");
            return false;
        }

        try {
            float weight = Float.parseFloat(etWeight.getText().toString());
            float height = Float.parseFloat(etHeight.getText().toString());
            int age = Integer.parseInt(etAge.getText().toString());
            int sleepHour = Integer.parseInt(etSleepHour.getText().toString());
            int sleepMinute = Integer.parseInt(etSleepMinute.getText().toString());
            int wakeHour = Integer.parseInt(etWakeHour.getText().toString());
            int wakeMinute = Integer.parseInt(etWakeMinute.getText().toString());

            if (weight <= 0 || weight > 300) {
                etWeight.setError("Cân nặng không hợp lệ (1-300 kg)");
                Log.d(TAG, "Invalid weight");
                return false;
            }

            if (height <= 0 || height > 3) {
                etHeight.setError("Chiều cao không hợp lệ (0-3 m)");
                Log.d(TAG, "Invalid height");
                return false;
            }

            if (age <= 0 || age > 150) {
                etAge.setError("Tuổi không hợp lệ (1-150)");
                Log.d(TAG, "Invalid age");
                return false;
            }

            if (sleepHour < 0 || sleepHour > 23 || sleepMinute < 0 || sleepMinute > 59 ||
                    wakeHour < 0 || wakeHour > 23 || wakeMinute < 0 || wakeMinute > 59) {
                Toast.makeText(this, "Thời gian không hợp lệ", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Invalid time");
                return false;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Vui lòng nhập số hợp lệ", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "NumberFormatException: " + e.getMessage());
            return false;
        }

        return true;
    }

    private void saveUserInfo() {
        if (!validateInput()) {
            btnSave.setEnabled(true);
            btnSave.setText("LƯU THÔNG TIN");
            return;
        }

        // Kiểm tra kết nối mạng
        if (!isNetworkAvailable()) {
            Toast.makeText(this, "Không có kết nối mạng. Vui lòng kiểm tra và thử lại.", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "No network connection");
            btnSave.setEnabled(true);
            btnSave.setText("LƯU THÔNG TIN");
            return;
        }

        // Vô hiệu hóa nút
        btnSave.setEnabled(false);
        btnSave.setText("Đang lưu...");

        try {
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

            String sleepTime = String.format("%02d:%02d",
                    Integer.parseInt(etSleepHour.getText().toString()),
                    Integer.parseInt(etSleepMinute.getText().toString()));

            String wakeTime = String.format("%02d:%02d",
                    Integer.parseInt(etWakeHour.getText().toString()),
                    Integer.parseInt(etWakeMinute.getText().toString()));

            int dailyTarget = userRepository.calculateDailyTarget(weight);

            User user = new User();
            user.setUserId(userId);
            user.setFullName(fullName);
            user.setGender(gender);
            user.setWeight(weight);
            user.setHeight(height);
            user.setAge(age);
            user.setDailyTarget(dailyTarget);
            user.setWakeTime(wakeTime);
            user.setSleepTime(sleepTime);
            user.setHasUserInfo(true);

            // Thêm log chi tiết trước khi lưu
            Log.d(TAG, "Saving user info: userId=" + userId + ", auth.uid=" + (mAuth.getUid() != null ? mAuth.getUid() : "null") +
                    ", fullName=" + fullName + ", weight=" + weight + ", height=" + height + ", age=" + age +
                    ", gender=" + gender + ", dailyTarget=" + dailyTarget + ", wakeTime=" + wakeTime + ", sleepTime=" + sleepTime);

            // Thêm timeout cho truy vấn
            Handler timeoutHandler = new Handler(Looper.getMainLooper());
            Runnable timeoutRunnable = () -> {
                Log.e(TAG, "Timeout: No response from Firebase after " + TIMEOUT_MS + "ms");
                Toast.makeText(UserInfoActivity.this, "Không thể lưu thông tin: Timeout.", Toast.LENGTH_SHORT).show();
                btnSave.setEnabled(true);
                btnSave.setText("LƯU THÔNG TIN");
            };
            timeoutHandler.postDelayed(timeoutRunnable, TIMEOUT_MS);

            userRepository.updateUserInfo(userId, user, task -> {
                // Hủy timeout khi nhận được phản hồi
                timeoutHandler.removeCallbacks(timeoutRunnable);

                // Kích hoạt lại nút
                btnSave.setEnabled(true);
                btnSave.setText("LƯU THÔNG TIN");

                if (task.isSuccessful()) {
                    Log.d(TAG, "User info saved successfully");
                    reminderRepository.createDefaultReminders(userId, wakeTime, sleepTime);
                    Toast.makeText(UserInfoActivity.this, "Lưu thông tin thành công!", Toast.LENGTH_SHORT).show();
                    redirectToMain();
                } else {
                    Log.e(TAG, "Error updating user info: " + task.getException().getMessage());
                    Toast.makeText(UserInfoActivity.this, "Lưu thông tin thất bại: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error saving user info: " + e.getMessage(), e);
            Toast.makeText(this, "Có lỗi xảy ra: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            btnSave.setEnabled(true);
            btnSave.setText("LƯU THÔNG TIN");
        }
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void redirectToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}