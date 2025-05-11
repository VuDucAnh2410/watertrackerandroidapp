package com.example.watertrackerandroidapp.LoginFunction;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.watertrackerandroidapp.HomeFunction.MainActivity;
import com.example.watertrackerandroidapp.R;
import com.example.watertrackerandroidapp.Repository.AuthRepository;
import com.example.watertrackerandroidapp.Repository.UserRepository;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final long TIMEOUT_MS = 5000; // 5 giây timeout

    // Repositories
    private AuthRepository authRepository;
    private UserRepository userRepository;

    // UI Elements
    private TextInputEditText etAccount;
    private TextInputEditText etPassword;
    private Button btnLogin;
    private TabLayout tabLayout;
    private TextInputLayout inputLayoutAccount;
    private TextInputLayout inputLayoutPassword;
    private TextView tvRegister;
    private TextView tvForgotPassword;
    private TextView tvValidationMessage;
    private LinearLayout connectionErrorLayout;
    private TextView tvConnectionError;
    private Button btnRetryConnection;
    private ProgressBar progressBar;

    // Handler cho timeout
    private Handler timeoutHandler;
    private Runnable checkUserInfoTimeoutRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Khởi tạo timeout handler
        timeoutHandler = new Handler(Looper.getMainLooper());

        // Khởi tạo repositories
        authRepository = new AuthRepository();
        userRepository = new UserRepository();

        // Khởi tạo views và thiết lập sự kiện
        initViews();
        setupListeners();

        // Kiểm tra kết nối mạng
        if (!isNetworkAvailable()) {
            showNetworkConnectionError();
        }
    }

    private void initViews() {
        etAccount = findViewById(R.id.etAccount);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tabLayout = findViewById(R.id.tabLayout);
        inputLayoutAccount = findViewById(R.id.inputLayoutAccount);
        inputLayoutPassword = findViewById(R.id.inputLayoutPassword);
        tvRegister = findViewById(R.id.tvRegister);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvValidationMessage = findViewById(R.id.tvValidationMessage);
        connectionErrorLayout = findViewById(R.id.connectionErrorLayout);
        tvConnectionError = findViewById(R.id.tvConnectionError);
        btnRetryConnection = findViewById(R.id.btnRetryConnection);
        progressBar = findViewById(R.id.progressBar);

        // Cập nhật thông báo ban đầu
        tvValidationMessage.setText("* Vui lòng nhập số điện thoại và mật khẩu");
    }

    private void setupListeners() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    inputLayoutAccount.setHint(getString(R.string.hint_phone));
                    etAccount.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
                    inputLayoutPassword.setHint("Mật khẩu");
                    etPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    tvValidationMessage.setText("* Vui lòng nhập số điện thoại và mật khẩu");
                } else {
                    inputLayoutAccount.setHint(getString(R.string.hint_email));
                    etAccount.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                    inputLayoutPassword.setHint(getString(R.string.hint_password));
                    etPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    tvValidationMessage.setText("* Vui lòng nhập email và mật khẩu");
                }
                etAccount.setText("");
                etPassword.setText("");
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        btnLogin.setOnClickListener(v -> {
            String account = etAccount.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (account.isEmpty() || password.isEmpty()) {
                tvValidationMessage.setText("* Vui lòng nhập đầy đủ thông tin");
                return;
            }

            showLoading(true);

            if (tabLayout.getSelectedTabPosition() == 0) {
                if (!isValidPhoneNumber(account)) {
                    tvValidationMessage.setText("* Vui lòng nhập số điện thoại hợp lệ");
                    showLoading(false);
                    return;
                }
                loginWithPhone(account, password);
            } else {
                if (!isValidEmail(account)) {
                    tvValidationMessage.setText("* Vui lòng nhập email hợp lệ");
                    showLoading(false);
                    return;
                }
                loginWithEmail(account, password);
            }
        });

        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
            Toast.makeText(this, "Chuyển hướng đến màn hình quên mật khẩu", Toast.LENGTH_SHORT).show();
        });

        btnRetryConnection.setOnClickListener(v -> {
            if (isNetworkAvailable()) {
                connectionErrorLayout.setVisibility(View.GONE);
                recreate();
            } else {
                Toast.makeText(this, "Vẫn chưa có kết nối mạng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber.matches("\\d{9,15}");
    }

    private boolean isNetworkAvailable() {
        android.net.ConnectivityManager cm = (android.net.ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        android.net.NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    private void showNetworkConnectionError() {
        tvConnectionError.setText("Không có kết nối mạng. Vui lòng kiểm tra và thử lại.");
        connectionErrorLayout.setVisibility(View.VISIBLE);
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!show);
    }

    private void loginWithEmail(String email, String password) {
        authRepository.loginWithEmail(email, password, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                Log.d(TAG, "Email login successful: " + user.getEmail());
                checkUserInfoAndRedirect(user.getUid());
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Email login failed: " + errorMessage);
                showLoading(false);
                tvValidationMessage.setText("Đăng nhập thất bại: " + errorMessage);
            }
        });
    }

    private void loginWithPhone(String phone, String password) {
        authRepository.loginWithPhoneEmail(phone, password, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                Log.d(TAG, "Phone login successful: " + user.getEmail());
                checkUserInfoAndRedirect(user.getUid());
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Phone login failed: " + errorMessage);
                showLoading(false);
                tvValidationMessage.setText("Đăng nhập thất bại: " + errorMessage);
            }
        });
    }

    private void checkUserInfoAndRedirect(String userId) {
        showLoading(true);

        // Hủy timeout trước đó nếu có
        if (checkUserInfoTimeoutRunnable != null) {
            timeoutHandler.removeCallbacks(checkUserInfoTimeoutRunnable);
        }

        // Thiết lập timeout mới
        checkUserInfoTimeoutRunnable = () -> {
            Log.w(TAG, "Timeout: No response for user info after " + TIMEOUT_MS + "ms");
            Toast.makeText(LoginActivity.this, "Không thể kiểm tra thông tin người dùng. Đang chuyển hướng...", Toast.LENGTH_SHORT).show();
            navigateToMainActivity();
        };
        timeoutHandler.postDelayed(checkUserInfoTimeoutRunnable, TIMEOUT_MS);

        userRepository.getUserById(userId, new UserRepository.OnUserLoadedListener() {
            @Override
            public void onUserLoaded(com.example.watertrackerandroidapp.Models.User user) {
                // Hủy timeout
                timeoutHandler.removeCallbacks(checkUserInfoTimeoutRunnable);
                checkUserInfoTimeoutRunnable = null;

                showLoading(false);
                if (user != null && user.isHasUserInfo()) {
                    Log.d(TAG, "User has info, redirecting to MainActivity");
                    navigateToMainActivity();
                } else {
                    Log.d(TAG, "User has no info, redirecting to UserInfoActivity");
                    navigateToUserInfoActivity();
                }
            }

            @Override
            public void onError(String errorMessage) {
                // Hủy timeout
                timeoutHandler.removeCallbacks(checkUserInfoTimeoutRunnable);
                checkUserInfoTimeoutRunnable = null;

                Log.e(TAG, "Error checking user info: " + errorMessage);
                showLoading(false);
                tvValidationMessage.setText("Lỗi kiểm tra thông tin người dùng: " + errorMessage);
                timeoutHandler.postDelayed(() -> navigateToMainActivity(), 3000);
            }
        });
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        // Trì hoãn finish để tránh lỗi Input channel
        new Handler(Looper.getMainLooper()).postDelayed(this::finish, 100);
    }

    private void navigateToUserInfoActivity() {
        Intent intent = new Intent(LoginActivity.this, UserInfoActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        // Trì hoãn finish để tránh lỗi Input channel
        new Handler(Looper.getMainLooper()).postDelayed(this::finish, 100);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Hủy timeout
        if (checkUserInfoTimeoutRunnable != null) {
            timeoutHandler.removeCallbacks(checkUserInfoTimeoutRunnable);
            checkUserInfoTimeoutRunnable = null;
        }
    }
}