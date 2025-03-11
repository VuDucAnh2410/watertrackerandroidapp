package com.example.watertrackerandroidapp.LoginFunction;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.watertrackerandroidapp.HomeFunction.MainActivity;
import com.example.watertrackerandroidapp.R;
import com.example.watertrackerandroidapp.DataBase.WaterTrackerDao;
import com.example.watertrackerandroidapp.DataBase.WaterTrackerDbHelper;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private EditText etAccount; // Ô nhập tài khoản (số điện thoại hoặc email)
    private EditText etPassword; // Ô nhập mật khẩu
    private TabLayout tabLayout;
    private Button btnLogin;
    private TextView tvRegister, tvForgotPassword;
    private TextInputLayout inputLayoutAccount; // TextInputLayout cho ô nhập tài khoản
    private WaterTrackerDao waterTrackerDao; // Thêm DAO để tương tác với database

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        WaterTrackerDbHelper dbHelper = new WaterTrackerDbHelper(this);
        dbHelper.ensureDatabaseExists();
        dbHelper.close();

        // Khởi tạo DAO
        waterTrackerDao = new WaterTrackerDao(this);

        // Khởi tạo các view
        etAccount = findViewById(R.id.etAccount);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tabLayout = findViewById(R.id.tabLayout);
        tvRegister = findViewById(R.id.tvRegister);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        inputLayoutAccount = findViewById(R.id.inputLayoutAccount); // Khởi tạo TextInputLayout

        // Đặt sự kiện cho nút Đăng nhập
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String account = etAccount.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                if (account.isEmpty()) {
                    etAccount.setError("Vui lòng nhập tài khoản");
                    return;
                }
                if (password.isEmpty()) {
                    etPassword.setError("Vui lòng nhập mật khẩu");
                    return;
                }

                // Kiểm tra định dạng tài khoản dựa trên tab đang chọn
                int selectedTab = tabLayout.getSelectedTabPosition();
                if (selectedTab == 0) { // Tab Phone
                    if (!isValidPhoneNumber(account)) {
                        etAccount.setError("Số điện thoại không hợp lệ");
                        return;
                    }
                } else { // Tab Email
                    if (!isValidEmail(account)) {
                        etAccount.setError("Email không hợp lệ");
                        return;
                    }
                }

                login(account, password, selectedTab); // Truyền thêm selectedTab
            }
        });

        // Đặt sự kiện cho TabLayout
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                updateInputLabel(tab.getPosition()); // Cập nhật nhãn ô nhập
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Đặt sự kiện cho TextView Đăng ký
        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Chuyển đến RegisterActivity
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        // Đặt sự kiện cho TextView Quên mật khẩu
        tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Logic để xử lý quên mật khẩu
                Toast.makeText(LoginActivity.this, "Quên mật khẩu", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });

        // Cập nhật ô nhập khi khởi tạo
        updateInputLabel(tabLayout.getSelectedTabPosition());

        // Kiểm tra xem người dùng đã đăng nhập chưa
        checkLoggedInStatus();
    }

    // Kiểm tra trạng thái đăng nhập
    private void checkLoggedInStatus() {
        SharedPreferences sharedPreferences = getSharedPreferences("WaterReminderPrefs", MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
        String userId = sharedPreferences.getString("userId", null);

        if (isLoggedIn && userId != null) {
            // Người dùng đã đăng nhập, kiểm tra xem đã nhập thông tin cá nhân chưa
            if (waterTrackerDao.isFirstTimeLogin(userId)) {
                // Chưa nhập thông tin cá nhân, chuyển đến UserInfoActivity
                Intent intent = new Intent(this, UserInfoActivity.class);
                startActivity(intent);
                finish();
            } else {
                // Đã nhập thông tin cá nhân, chuyển đến MainActivity
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }

    // Cập nhật ô nhập dựa trên tab đã chọn
    private void updateInputLabel(int position) {
        if (position == 0) {
            inputLayoutAccount.setHint(getString(R.string.hint_phone)); // Cập nhật label thành "Nhập số điện thoại"
            etAccount.setHint(R.string.hint_phone); // Placeholder cho số điện thoại
        } else {
            inputLayoutAccount.setHint(getString(R.string.hint_email)); // Cập nhật label thành "Nhập email"
            etAccount.setHint(R.string.hint_email); // Placeholder cho email
        }
        etAccount.setText(""); // Xóa nội dung khi chuyển tab
    }

    // Kiểm tra định dạng email
    private boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    // Kiểm tra định dạng số điện thoại
    private boolean isValidPhoneNumber(String phone) {
        // Số điện thoại Việt Nam: bắt đầu bằng 0, có 10 số
        return phone.matches("^0\\d{9}$");
    }

    private void login(String account, String password, int selectedTab) {
        try {
            // Kiểm tra đăng nhập trong database dựa trên loại tài khoản
            String accountId;
            if (selectedTab == 0) { // Tab Phone
                accountId = waterTrackerDao.checkLoginByPhone(account, password);
            } else { // Tab Email
                accountId = waterTrackerDao.checkLoginByEmail(account, password);
            }

            if (accountId != null) {
                // Đăng nhập thành công
                // Lấy userId từ accountId
                String userId = waterTrackerDao.getUserIdByAccountId(accountId);

                if (userId != null) {
                    // Lưu thông tin đăng nhập vào SharedPreferences
                    saveLoginInfo(accountId, userId);

                    Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();

                    // Kiểm tra xem người dùng đã nhập thông tin cá nhân chưa
                    if (waterTrackerDao.isFirstTimeLogin(userId)) {
                        // Chưa nhập thông tin cá nhân, chuyển đến UserInfoActivity
                        Intent intent = new Intent(LoginActivity.this, UserInfoActivity.class);
                        startActivity(intent);
                    } else {
                        // Đã nhập thông tin cá nhân, chuyển đến MainActivity
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                    finish();
                } else {
                    Log.e(TAG, "Không thể lấy userId từ accountId: " + accountId);
                    Toast.makeText(this, "Lỗi: Không thể lấy thông tin người dùng", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Đăng nhập thất bại
                Toast.makeText(this, "Tài khoản hoặc mật khẩu không đúng", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi trong quá trình đăng nhập", e);
            Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // Lưu thông tin đăng nhập vào SharedPreferences
    private void saveLoginInfo(String accountId, String userId) {
        SharedPreferences sharedPreferences = getSharedPreferences("WaterReminderPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("accountId", accountId);
        editor.putString("userId", userId);
        editor.putBoolean("isLoggedIn", true);
        editor.apply();
        Log.d(TAG, "Đã lưu thông tin đăng nhập: accountId=" + accountId + ", userId=" + userId);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (waterTrackerDao != null) {
            waterTrackerDao.close();
        }
    }
}