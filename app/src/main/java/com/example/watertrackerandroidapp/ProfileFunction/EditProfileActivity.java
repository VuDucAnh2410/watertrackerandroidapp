package com.example.watertrackerandroidapp.ProfileFunction;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.watertrackerandroidapp.LoginFunction.LoginActivity;
import com.example.watertrackerandroidapp.Models.User;
import com.example.watertrackerandroidapp.R;
import com.example.watertrackerandroidapp.Repository.UserRepository;
import com.example.watertrackerandroidapp.firebase.FirebaseManager;
import com.google.firebase.auth.FirebaseAuth;

public class EditProfileActivity extends AppCompatActivity {
    private static final String TAG = "EditProfileActivity";
    private boolean isSaving = false;

    // UI Components
    private ImageView ivBack;
    private ImageView ivAvatar;
    private EditText etName;
    private RadioGroup rgGender;
    private RadioButton rbMale, rbFemale, rbOther;
    private EditText etWeight;
    private EditText etAge;
    private EditText etSleepHour, etSleepMinute;
    private EditText etWakeHour, etWakeMinute;
    private Button btnSave;

    // Firebase
    private FirebaseAuth mAuth;
    private UserRepository userRepository;
    private String userId;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_edit_activity);

        // Khởi tạo Firebase
        mAuth = FirebaseManager.getInstance().getAuth();
        userRepository = new UserRepository();
        userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;

        if (userId == null) {
            Toast.makeText(this, "Phiên đăng nhập không hợp lệ", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Khởi tạo views
        initViews();

        // Thiết lập listeners
        setupListeners();

        // Tải thông tin người dùng
        loadUserInfo();
    }

    private void initViews() {
        try {
            ivBack = findViewById(R.id.ivBack);
            ivAvatar = findViewById(R.id.ivAvatar);
            etName = findViewById(R.id.etName);
            rgGender = findViewById(R.id.rgGender);
            rbMale = findViewById(R.id.rbMale);
            rbFemale = findViewById(R.id.rbFemale);
            rbOther = findViewById(R.id.rbOther);
            etWeight = findViewById(R.id.etWeight);
            etAge = findViewById(R.id.etAge);
            etSleepHour = findViewById(R.id.etSleepHour);
            etSleepMinute = findViewById(R.id.etSleepMinute);
            etWakeHour = findViewById(R.id.etWakeHour);
            etWakeMinute = findViewById(R.id.etWakeMinute);
            btnSave = findViewById(R.id.btnSave);

            // Kiểm tra views có null không
            if (etName == null || rgGender == null || etWeight == null || etAge == null ||
                    etSleepHour == null || etSleepMinute == null || etWakeHour == null || etWakeMinute == null ||
                    btnSave == null) {
                throw new IllegalStateException("Một hoặc nhiều view không được khởi tạo");
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi trong initViews: " + e.getMessage(), e);
            Toast.makeText(this, "Lỗi giao diện, vui lòng thử lại", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());

        btnSave.setOnClickListener(v -> {
            if (!isSaving && validateInput()) {
                saveUserInfo();
            }
        });
    }

    private void loadUserInfo() {
        userRepository.getUserById(userId, new UserRepository.OnUserLoadedListener() {
            @Override
            public void onUserLoaded(User user) {
                if (user != null && !isFinishing()) {
                    currentUser = user;
                    updateUI(user);
                } else {
                    Toast.makeText(EditProfileActivity.this, "Không thể tải thông tin người dùng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Lỗi tải thông tin người dùng: " + errorMessage);
                if (!isFinishing()) {
                    Toast.makeText(EditProfileActivity.this, "Lỗi: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateUI(User user) {
        if (isFinishing()) return;
        try {
            etName.setText(user.getFullName() != null ? user.getFullName() : "");

            // Thiết lập giới tính
            rgGender.clearCheck();
            if ("Nam".equals(user.getGender())) {
                rbMale.setChecked(true);
            } else if ("Nữ".equals(user.getGender())) {
                rbFemale.setChecked(true);
            } else {
                rbOther.setChecked(true);
            }

            // Thiết lập cân nặng và tuổi
            if (user.getWeight() > 0) {
                etWeight.setText(String.valueOf(user.getWeight()));
            } else {
                etWeight.setText("");
            }

            if (user.getAge() > 0) {
                etAge.setText(String.valueOf(user.getAge()));
            } else {
                etAge.setText("");
            }

            // Thiết lập thời gian ngủ
            if (user.getSleepTime() != null) {
                String[] sleepTimeParts = user.getSleepTime().split(":");
                if (sleepTimeParts.length == 2) {
                    etSleepHour.setText(sleepTimeParts[0]);
                    etSleepMinute.setText(sleepTimeParts[1]);
                }
            }

            // Thiết lập thời gian thức
            if (user.getWakeTime() != null) {
                String[] wakeTimeParts = user.getWakeTime().split(":");
                if (wakeTimeParts.length == 2) {
                    etWakeHour.setText(wakeTimeParts[0]);
                    etWakeMinute.setText(wakeTimeParts[1]);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi trong updateUI: " + e.getMessage(), e);
            Toast.makeText(this, "Lỗi cập nhật giao diện: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateInput() {
        if (TextUtils.isEmpty(etName.getText())) {
            etName.setError("Vui lòng nhập tên");
            return false;
        }

        if (TextUtils.isEmpty(etWeight.getText())) {
            etWeight.setError("Vui lòng nhập cân nặng");
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

        if (TextUtils.isEmpty(etSleepHour.getText()) || TextUtils.isEmpty(etSleepMinute.getText()) ||
                TextUtils.isEmpty(etWakeHour.getText()) || TextUtils.isEmpty(etWakeMinute.getText())) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thời gian", Toast.LENGTH_SHORT).show();
            return false;
        }

        try {
            float weight = Float.parseFloat(etWeight.getText().toString());
            int age = Integer.parseInt(etAge.getText().toString());
            int sleepHour = Integer.parseInt(etSleepHour.getText().toString());
            int sleepMinute = Integer.parseInt(etSleepMinute.getText().toString());
            int wakeHour = Integer.parseInt(etWakeHour.getText().toString());
            int wakeMinute = Integer.parseInt(etWakeMinute.getText().toString());

            if (weight <= 0 || weight > 300) {
                etWeight.setError("Cân nặng không hợp lệ (1-300 kg)");
                return false;
            }

            if (age <= 0 || age > 150) {
                etAge.setError("Tuổi không hợp lệ (1-150)");
                return false;
            }

            if (sleepHour < 0 || sleepHour > 23 || sleepMinute < 0 || sleepMinute > 59 ||
                    wakeHour < 0 || wakeHour > 23 || wakeMinute < 0 || wakeMinute > 59) {
                Toast.makeText(this, "Thời gian không hợp lệ", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Vui lòng nhập số hợp lệ", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void saveUserInfo() {
        if (isSaving || isFinishing()) return;
        isSaving = true;

        // Vô hiệu hóa toàn bộ giao diện
        btnSave.setEnabled(false);
        btnSave.setText("Đang lưu thông tin...");
        ivBack.setEnabled(false);
        etName.setEnabled(false);
        rgGender.setEnabled(false);
        rbMale.setEnabled(false);
        rbFemale.setEnabled(false);
        rbOther.setEnabled(false);
        etWeight.setEnabled(false);
        etAge.setEnabled(false);
        etSleepHour.setEnabled(false);
        etSleepMinute.setEnabled(false);
        etWakeHour.setEnabled(false);
        etWakeMinute.setEnabled(false);

        try {
            // Tạo đối tượng User
            User user = new User();
            user.setUserId(userId);
            user.setFullName(etName.getText().toString().trim());
            user.setWeight(Float.parseFloat(etWeight.getText().toString().trim()));
            user.setAge(Integer.parseInt(etAge.getText().toString().trim()));

            String gender;
            int checkedId = rgGender.getCheckedRadioButtonId();
            if (checkedId == R.id.rbMale) {
                gender = "Nam";
            } else if (checkedId == R.id.rbFemale) {
                gender = "Nữ";
            } else {
                gender = "Khác";
            }
            user.setGender(gender);

            String sleepTime = String.format("%02d:%02d",
                    Integer.parseInt(etSleepHour.getText().toString()),
                    Integer.parseInt(etSleepMinute.getText().toString()));
            user.setSleepTime(sleepTime);

            String wakeTime = String.format("%02d:%02d",
                    Integer.parseInt(etWakeHour.getText().toString()),
                    Integer.parseInt(etWakeMinute.getText().toString()));
            user.setWakeTime(wakeTime);

            // Sử dụng chiều cao từ currentUser nếu có, nếu không mặc định 1.70m
            float height = currentUser != null && currentUser.getHeight() > 0 ? currentUser.getHeight() : 1.70f;
            user.setHeight(height);

            // Tính dailyTarget
            user.setDailyTarget(userRepository.calculateDailyTarget(user.getWeight()));

            // Ghi log chi tiết
            Log.d(TAG, "Lưu thông tin người dùng: userId=" + userId +
                    ", fullName=" + user.getFullName() + ", weight=" + user.getWeight() +
                    ", height=" + user.getHeight() + ", age=" + user.getAge() +
                    ", gender=" + user.getGender() + ", wakeTime=" + user.getWakeTime() +
                    ", sleepTime=" + user.getSleepTime() + ", dailyTarget=" + user.getDailyTarget());

            // Cập nhật thông tin người dùng qua UserRepository
            userRepository.updateUserInfo(userId, user, task -> {
                if (task.isSuccessful() && !isFinishing()) {
                    Log.d(TAG, "Lưu thông tin thành công");
                    Toast.makeText(EditProfileActivity.this, "Lưu thông tin thành công", Toast.LENGTH_SHORT).show();
                    // Chuyển hướng đến LoginActivity
                    Intent intent = new Intent(EditProfileActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                    Log.d(TAG, "Chuyển hướng đến LoginActivity");
                } else if (!isFinishing()) {
                    String errorMessage = task.getException() != null ? task.getException().getMessage() : "Lỗi không xác định";
                    Log.e(TAG, "Lỗi khi cập nhật thông tin: " + errorMessage);
                    Toast.makeText(EditProfileActivity.this, "Lưu thông tin thất bại: " + errorMessage, Toast.LENGTH_SHORT).show();
                    // Kích hoạt lại giao diện
                    btnSave.setEnabled(true);
                    btnSave.setText("LƯU THÔNG TIN");
                    ivBack.setEnabled(true);
                    etName.setEnabled(true);
                    rgGender.setEnabled(true);
                    rbMale.setEnabled(true);
                    rbFemale.setEnabled(true);
                    rbOther.setEnabled(true);
                    etWeight.setEnabled(true);
                    etAge.setEnabled(true);
                    etSleepHour.setEnabled(true);
                    etSleepMinute.setEnabled(true);
                    etWakeHour.setEnabled(true);
                    etWakeMinute.setEnabled(true);
                    isSaving = false;
                }
            });
        } catch (Exception e) {
            if (!isFinishing()) {
                Log.e(TAG, "Lỗi khi lưu thông tin: " + e.getMessage(), e);
                Toast.makeText(this, "Có lỗi xảy ra: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                // Kích hoạt lại giao diện
                btnSave.setEnabled(true);
                btnSave.setText("LƯU THÔNG TIN");
                ivBack.setEnabled(true);
                etName.setEnabled(true);
                rgGender.setEnabled(true);
                rbMale.setEnabled(true);
                rbFemale.setEnabled(true);
                rbOther.setEnabled(true);
                etWeight.setEnabled(true);
                etAge.setEnabled(true);
                etSleepHour.setEnabled(true);
                etSleepMinute.setEnabled(true);
                etWakeHour.setEnabled(true);
                etWakeMinute.setEnabled(true);
                isSaving = false;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Xóa tất cả tham chiếu và listener
        btnSave.setOnClickListener(null);
        ivBack.setOnClickListener(null);
        rgGender.setOnCheckedChangeListener(null);
        etName = null;
        etWeight = null;
        etAge = null;
        etSleepHour = null;
        etSleepMinute = null;
        etWakeHour = null;
        etWakeMinute = null;
        ivAvatar = null;
        rbMale = null;
        rbFemale = null;
        rbOther = null;
        rgGender = null;
        btnSave = null;
        ivBack = null;
        isSaving = false;
        Log.d(TAG, "EditProfileActivity đã bị hủy");
    }
}