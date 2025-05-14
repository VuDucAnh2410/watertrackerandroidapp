package com.example.watertrackerandroidapp.ProfileFunction;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.watertrackerandroidapp.HomeFunction.MainActivity;
import com.example.watertrackerandroidapp.LoginFunction.LoginActivity;
import com.example.watertrackerandroidapp.Models.User;
import com.example.watertrackerandroidapp.R;
import com.example.watertrackerandroidapp.Repository.UserRepository;
import com.example.watertrackerandroidapp.SettingFunction.SettingActivity;
import com.example.watertrackerandroidapp.StatisticsFunction.StatisticsActivity;
import com.example.watertrackerandroidapp.firebase.FirebaseManager;
import com.google.firebase.auth.FirebaseAuth;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";
    private static final int MAX_RETRIES = 5;
    private static final long RETRY_DELAY_MS = 1000;

    // UI Components
    private ImageView ivBack;
    private ImageView ivAvatar;
    private Button btnEdit;
    private Button btnLogout;
    private TextView tvName;
    private RadioGroup rgGender;
    private RadioButton rbMale, rbFemale, rbOther;
    private TextView tvWeight;
    private TextView tvAge;
    private TextView tvSleepTime;
    private TextView tvWakeTime;
    private View navHome, navStats, navSettings, navProfile;

    // Firebase
    private FirebaseAuth mAuth;
    private UserRepository userRepository;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_activity);

        try {
            // Khởi tạo Firebase
            mAuth = FirebaseManager.getInstance().getAuth();
            userRepository = new UserRepository();
            userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;

            if (userId == null) {
                Toast.makeText(this, "Phiên đăng nhập không hợp lệ", Toast.LENGTH_SHORT).show();
                redirectToLogin();
                return;
            }

            // Khởi tạo views
            initViews();

            // Thiết lập listeners
            setupListeners();

            // Tải thông tin người dùng
            loadUserInfo(0);
        } catch (Exception e) {
            Log.e(TAG, "Lỗi trong onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Có lỗi xảy ra: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            redirectToLogin();
        }
    }

    private void initViews() {
        try {
            ivBack = findViewById(R.id.ivBack);
            ivAvatar = findViewById(R.id.ivAvatar);
            btnEdit = findViewById(R.id.btnEdit);
            btnLogout = findViewById(R.id.btnLogout);
            tvName = findViewById(R.id.tvName);
            rgGender = findViewById(R.id.rgGender);
            rbMale = findViewById(R.id.rbMale);
            rbFemale = findViewById(R.id.rbFemale);
            rbOther = findViewById(R.id.rbOther);
            tvWeight = findViewById(R.id.tvWeight);
            tvAge = findViewById(R.id.tvAge);
            tvSleepTime = findViewById(R.id.tvSleepTime);
            tvWakeTime = findViewById(R.id.tvWakeTime);

            // Navigation
            navHome = findViewById(R.id.navHome);
            navStats = findViewById(R.id.navStats);
            navSettings = findViewById(R.id.navSettings);
            navProfile = findViewById(R.id.navProfile);

            // Kiểm tra views có null không
            if (tvName == null || rgGender == null || tvWeight == null || tvAge == null ||
                    tvSleepTime == null || tvWakeTime == null || btnLogout == null) {
                throw new IllegalStateException("Một hoặc nhiều view không được khởi tạo");
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi trong initViews: " + e.getMessage(), e);
            Toast.makeText(this, "Lỗi giao diện, vui lòng thử lại", Toast.LENGTH_SHORT).show();
            finish();
            throw e;
        }
    }

    private void setupListeners() {
        try {
            ivBack.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            });

            btnEdit.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
                startActivity(intent);
            });

            btnLogout.setOnClickListener(v -> {
                if (!isFinishing()) {
                    Log.d(TAG, "Đăng xuất người dùng: userId=" + userId);
                    mAuth.signOut();
                    Toast.makeText(ProfileActivity.this, "Đăng xuất thành công", Toast.LENGTH_SHORT).show();
                    redirectToLogin();
                }
            });

            // Navigation listeners
            navHome.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            });

            navStats.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileActivity.this, StatisticsActivity.class);
                startActivity(intent);
                finish();
            });

            navSettings.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileActivity.this, SettingActivity.class);
                startActivity(intent);
                finish();
            });

            // Profile đã active
            navProfile.setEnabled(false);
        } catch (Exception e) {
            Log.e(TAG, "Lỗi trong setupListeners: " + e.getMessage(), e);
            Toast.makeText(this, "Lỗi thiết lập giao diện: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadUserInfo(int retryCount) {
        if (isFinishing()) return;
        userRepository.getUserById(userId, new UserRepository.OnUserLoadedListener() {
            @Override
            public void onUserLoaded(User user) {
                if (user != null && !isFinishing()) {
                    Log.d(TAG, "Tải thông tin người dùng thành công: fullName=" + user.getFullName() +
                            ", weight=" + user.getWeight() + ", age=" + user.getAge() +
                            ", gender=" + user.getGender() + ", sleepTime=" + user.getSleepTime() +
                            ", wakeTime=" + user.getWakeTime());
                    updateUI(user);
                } else if (retryCount < MAX_RETRIES) {
                    Log.w(TAG, "Dữ liệu người dùng null, thử lại lần " + (retryCount + 1));
                    new Handler(Looper.getMainLooper()).postDelayed(
                            () -> loadUserInfo(retryCount + 1), RETRY_DELAY_MS);
                } else {
                    Toast.makeText(ProfileActivity.this, "Không thể tải thông tin người dùng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Lỗi tải thông tin người dùng: " + errorMessage);
                if (retryCount < MAX_RETRIES) {
                    Log.w(TAG, "Lỗi tải dữ liệu, thử lại lần " + (retryCount + 1));
                    new Handler(Looper.getMainLooper()).postDelayed(
                            () -> loadUserInfo(retryCount + 1), RETRY_DELAY_MS);
                } else if (!isFinishing()) {
                    Toast.makeText(ProfileActivity.this, "Lỗi: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateUI(User user) {
        if (isFinishing()) return;
        try {
            // Cập nhật giao diện với thông tin người dùng
            tvName.setText(user.getFullName() != null ? user.getFullName() : "Chưa cập nhật");

            // Cập nhật giới tính
            rgGender.clearCheck();
            String gender = user.getGender();
            if (gender != null) {
                if ("Nam".equals(gender)) {
                    rbMale.setChecked(true);
                } else if ("Nữ".equals(gender)) {
                    rbFemale.setChecked(true);
                } else {
                    rbOther.setChecked(true);
                }
            } else {
                rbOther.setChecked(true);
            }

            tvWeight.setText(user.getWeight() > 0 ? String.format("%.1f kg", user.getWeight()) : "Chưa cập nhật");
            tvAge.setText(user.getAge() > 0 ? user.getAge() + " tuổi" : "Chưa cập nhật");
            tvSleepTime.setText(user.getSleepTime() != null ? user.getSleepTime() : "Chưa cập nhật");
            tvWakeTime.setText(user.getWakeTime() != null ? user.getWakeTime() : "Chưa cập nhật");
        } catch (Exception e) {
            Log.e(TAG, "Lỗi trong updateUI: " + e.getMessage(), e);
            if (!isFinishing()) {
                Toast.makeText(this, "Lỗi khi cập nhật giao diện: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        Log.d(TAG, "Chuyển hướng đến LoginActivity");
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            // Tải lại thông tin người dùng nếu người dùng vẫn đăng nhập
            if (userId != null && mAuth.getCurrentUser() != null && !isFinishing()) {
                loadUserInfo(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi trong onResume: " + e.getMessage(), e);
            if (!isFinishing()) {
                Toast.makeText(this, "Lỗi khi tải lại thông tin: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Xóa tất cả tham chiếu và listener
        btnEdit.setOnClickListener(null);
        btnLogout.setOnClickListener(null);
        ivBack.setOnClickListener(null);
        navHome.setOnClickListener(null);
        navStats.setOnClickListener(null);
        navSettings.setOnClickListener(null);
        navProfile.setOnClickListener(null);
        rgGender.setOnCheckedChangeListener(null);
        tvName = null;
        tvWeight = null;
        tvAge = null;
        tvSleepTime = null;
        tvWakeTime = null;
        ivAvatar = null;
        rbMale = null;
        rbFemale = null;
        rbOther = null;
        rgGender = null;
        btnEdit = null;
        btnLogout = null;
        ivBack = null;
        navHome = null;
        navStats = null;
        navSettings = null;
        navProfile = null;
        Log.d(TAG, "ProfileActivity đã bị hủy");
    }
}