package com.example.watertrackerandroidapp.HomeFunction;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.watertrackerandroidapp.Adapters.WaterHistoryAdapter;
import com.example.watertrackerandroidapp.CustomViews.WaterProgressView;
import com.example.watertrackerandroidapp.LoginFunction.LoginActivity;
import com.example.watertrackerandroidapp.Models.User;
import com.example.watertrackerandroidapp.Models.WaterIntake;
import com.example.watertrackerandroidapp.ProfileFunction.ProfileActivity;
import com.example.watertrackerandroidapp.R;
import com.example.watertrackerandroidapp.Repository.UserRepository;
import com.example.watertrackerandroidapp.Repository.WaterIntakeRepository;
import com.example.watertrackerandroidapp.SettingFunction.SettingActivity;
import com.example.watertrackerandroidapp.StatisticsFunction.StatisticsActivity;
import com.example.watertrackerandroidapp.firebase.FirebaseManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final long TIMEOUT_MS = 15000; // 15 giây timeout

    // UI Components
    private WaterProgressView waterProgressView;
    private TextView tvPercentage, tvTarget, tvConsumed, tvSelectedAmount;
    private CardView btnDrink;
    private LinearLayout waterAmountSelector;
    private RecyclerView rvHistory;
    private LinearLayout navHome, navStats, navSettings, navProfile;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private TextView tvNoData;
    private View mainContent;
    private ImageView ivTips;

    // Adapters
    private WaterHistoryAdapter historyAdapter;
    private List<WaterIntake> waterIntakeList = new ArrayList<>();

    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private UserRepository userRepository;
    private WaterIntakeRepository waterIntakeRepository;

    // Data
    private String userId;
    private int dailyTarget;
    private int currentIntake;
    private int selectedAmount = 250; // Default amount (ml)
    private String currentDate;
    private boolean isDataLoaded = false;
    private int loadingTasksCount = 0;

    // Handlers for timeout
    private Handler timeoutHandler;
    private Runnable userInfoTimeoutRunnable;
    private Runnable waterIntakeTimeoutRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Khởi tạo timeout handler
        timeoutHandler = new Handler(Looper.getMainLooper());

        // Khởi tạo Firebase
        mAuth = FirebaseManager.getInstance().getAuth();
        mDatabase = FirebaseManager.getInstance().getDatabase();
        userRepository = new UserRepository();
        waterIntakeRepository = new WaterIntakeRepository();

        // Lấy ngày hiện tại
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        currentDate = dateFormat.format(new Date());

        // Kiểm tra đăng nhập
        checkLoginStatus();

        // Khởi tạo views
        initViews();

        // Thiết lập sự kiện
        setupListeners();

        // Hiển thị loading
        showLoading(true);

        // Tải dữ liệu
        loadData();
    }

    private void checkLoginStatus() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
            Log.d(TAG, "User logged in with Firebase Auth: " + userId);
        } else {
            // Nếu không có người dùng hiện tại, kiểm tra trong SharedPreferences
            SharedPreferences sharedPreferences = getSharedPreferences("WaterReminderPrefs", MODE_PRIVATE);
            boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
            userId = sharedPreferences.getString("userId", null);

            Log.d(TAG, "Checking login from SharedPreferences: isLoggedIn=" + isLoggedIn + ", userId=" + userId);

            if (!isLoggedIn || userId == null) {
                // Chuyển đến màn hình đăng nhập
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                return;
            }
        }
    }

    private void initViews() {
        waterProgressView = findViewById(R.id.waterProgressView);
        tvPercentage = findViewById(R.id.tvPercentage);
        tvTarget = findViewById(R.id.tvTarget);
        tvConsumed = findViewById(R.id.tvConsumed);
        tvSelectedAmount = findViewById(R.id.tvSelectedAmount);
        btnDrink = findViewById(R.id.btnDrink);
        waterAmountSelector = findViewById(R.id.waterAmountSelector);
        rvHistory = findViewById(R.id.rvHistory);
        navHome = findViewById(R.id.navHome);
        navStats = findViewById(R.id.navStats);
        navSettings = findViewById(R.id.navSettings);
        navProfile = findViewById(R.id.navProfile);
        ivTips = findViewById(R.id.ivTips);

        // Thêm SwipeRefreshLayout
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        // Thêm ProgressBar và TextView cho trạng thái không có dữ liệu
        progressBar = findViewById(R.id.progressBar);
        tvNoData = findViewById(R.id.tvNoData);
        mainContent = findViewById(R.id.mainContent);

        // Thiết lập RecyclerView
        historyAdapter = new WaterHistoryAdapter(this, waterIntakeList);
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        rvHistory.setAdapter(historyAdapter);
    }

    private void setupListeners() {
        // Nút uống nước
        btnDrink.setOnClickListener(v -> {
            if (isNetworkAvailable()) {
                addWaterIntake(selectedAmount);
            } else {
                Toast.makeText(this, "Không có kết nối mạng. Vui lòng kiểm tra và thử lại.", Toast.LENGTH_SHORT).show();
            }
        });

        // Chọn lượng nước
        waterAmountSelector.setOnClickListener(v -> {
            showWaterAmountDialog();
        });

        // Hiển thị lời khuyên
        ivTips.setOnClickListener(v -> {
            showTipsDialog();
        });

        // Thanh điều hướng
        navHome.setOnClickListener(v -> {
            // Đã ở trang chủ, không cần làm gì
        });

        navStats.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, StatisticsActivity.class);
            startActivity(intent);
        });

        navSettings.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingActivity.class);
            startActivity(intent);
        });

        navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        // Thiết lập SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadData();
        });
    }

    private void loadData() {
        if (userId == null) {
            showLoading(false);
            showNoData(true);
            return;
        }

        // Reset trạng thái
        isDataLoaded = false;
        loadingTasksCount = 2; // Số lượng tác vụ tải dữ liệu (user info và water intake)

        // Tải thông tin người dùng
        loadUserInfo();

        // Tải lịch sử uống nước
        loadWaterIntakeHistory();
    }

    private void loadUserInfo() {
        // Hủy timeout trước đó nếu có
        if (userInfoTimeoutRunnable != null) {
            timeoutHandler.removeCallbacks(userInfoTimeoutRunnable);
        }

        // Thiết lập timeout mới
        userInfoTimeoutRunnable = () -> {
            Log.e(TAG, "Timeout: No response from Firebase for user info after " + TIMEOUT_MS + "ms");
            decreaseLoadingTasksCount();
        };
        timeoutHandler.postDelayed(userInfoTimeoutRunnable, TIMEOUT_MS);

        userRepository.getUserById(userId, new UserRepository.OnUserLoadedListener() {
            @Override
            public void onUserLoaded(User user) {
                // Hủy timeout
                timeoutHandler.removeCallbacks(userInfoTimeoutRunnable);

                if (user != null && user.getDailyTarget() > 0) {
                    dailyTarget = user.getDailyTarget();
                    Log.d(TAG, "User info loaded, dailyTarget: " + dailyTarget);
                } else {
                    Log.w(TAG, "User info loaded but dailyTarget is invalid, using default: 3000ml");
                    dailyTarget = 3000; // Giá trị mặc định nếu không có dữ liệu
                }

                updateUI();
                decreaseLoadingTasksCount();
            }

            @Override
            public void onError(String errorMessage) {
                // Hủy timeout
                timeoutHandler.removeCallbacks(userInfoTimeoutRunnable);

                Log.e(TAG, "Error loading user info: " + errorMessage);
                dailyTarget = 3000; // Giá trị mặc định nếu lỗi
                updateUI();
                decreaseLoadingTasksCount();
            }
        });
    }

    private void loadWaterIntakeHistory() {
        // Hủy timeout trước đó nếu có
        if (waterIntakeTimeoutRunnable != null) {
            timeoutHandler.removeCallbacks(waterIntakeTimeoutRunnable);
        }

        // Thiết lập timeout mới
        waterIntakeTimeoutRunnable = () -> {
            Log.e(TAG, "Timeout: No response from Firebase for water intake after " + TIMEOUT_MS + "ms");
            decreaseLoadingTasksCount();
        };
        timeoutHandler.postDelayed(waterIntakeTimeoutRunnable, TIMEOUT_MS);

        waterIntakeRepository.getTodayWaterIntake(userId, new WaterIntakeRepository.OnWaterIntakeLoadedListener() {
            @Override
            public void onWaterIntakeLoaded(List<WaterIntake> intakeList, int totalAmount) {
                // Hủy timeout
                timeoutHandler.removeCallbacks(waterIntakeTimeoutRunnable);

                waterIntakeList.clear();
                currentIntake = totalAmount;

                if (intakeList != null && !intakeList.isEmpty()) {
                    waterIntakeList.addAll(intakeList);
                    Log.d(TAG, "Water intake loaded, count: " + intakeList.size() + ", total: " + totalAmount);
                } else {
                    Log.d(TAG, "No water intake data found for today");
                }

                // Thêm lần uống nước tiếp theo (dự kiến)
                WaterIntake nextIntake = new WaterIntake();
                nextIntake.setAmount(selectedAmount);
                nextIntake.setScheduled(true);
                waterIntakeList.add(0, nextIntake);

                historyAdapter.notifyDataSetChanged();
                updateUI();
                decreaseLoadingTasksCount();
            }

            @Override
            public void onError(String errorMessage) {
                // Hủy timeout
                timeoutHandler.removeCallbacks(waterIntakeTimeoutRunnable);

                Log.e(TAG, "Error loading water intake: " + errorMessage);
                currentIntake = 0; // Giá trị mặc định nếu lỗi
                updateUI();
                decreaseLoadingTasksCount();
            }
        });
    }

    private void decreaseLoadingTasksCount() {
        loadingTasksCount--;
        if (loadingTasksCount <= 0) {
            isDataLoaded = true;
            showLoading(false);
            swipeRefreshLayout.setRefreshing(false);

            // Hiển thị thông báo không có dữ liệu nếu cần
            showNoData(waterIntakeList.size() <= 1); // Chỉ có mục "lần tới" thì coi như không có dữ liệu
        }
    }

    private void updateUI() {
        // Cập nhật mục tiêu
        tvTarget.setText(dailyTarget + "ml");

        // Cập nhật lượng nước đã uống
        tvConsumed.setText(currentIntake + "ml");

        // Cập nhật phần trăm
        float percentage = (float) currentIntake / dailyTarget * 100;
        if (percentage > 100) percentage = 100;

        tvPercentage.setText(Math.round(percentage) + "%");
        waterProgressView.setProgress(percentage);

        // Cập nhật lượng nước đã chọn
        tvSelectedAmount.setText(selectedAmount + " ml");

        // Cập nhật lượng nước cho lần uống tiếp theo trong danh sách
        if (waterIntakeList.size() > 0 && waterIntakeList.get(0).isScheduled()) {
            waterIntakeList.get(0).setAmount(selectedAmount);
            historyAdapter.notifyItemChanged(0);
        }
    }

    private void addWaterIntake(int amount) {
        if (userId == null) {
            Toast.makeText(this, "Phiên đăng nhập không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        // Hiển thị loading
        showLoading(true);

        waterIntakeRepository.addWaterIntake(
                userId,
                amount,
                "Water",
                aVoid -> {
                    showLoading(false);
                    Toast.makeText(MainActivity.this, "Đã thêm " + amount + "ml nước!", Toast.LENGTH_SHORT).show();

                    // Cập nhật UI
                    currentIntake += amount;
                    updateUI();

                    // Tải lại lịch sử uống nước
                    loadWaterIntakeHistory();
                },
                e -> {
                    showLoading(false);
                    Toast.makeText(MainActivity.this, "Không thể thêm lượng nước. Vui lòng thử lại!", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error adding water intake", e);
                }
        );
    }

    private void showWaterAmountDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.home_dialog_water_amount, null);
        builder.setView(dialogView);

        // Lấy các view trong dialog
        LinearLayout option250ml = dialogView.findViewById(R.id.option250ml);
        LinearLayout option500ml = dialogView.findViewById(R.id.option500ml);
        LinearLayout option750ml = dialogView.findViewById(R.id.option750ml);
        LinearLayout option1000ml = dialogView.findViewById(R.id.option1000ml);
        EditText etCustomAmount = dialogView.findViewById(R.id.etCustomAmount);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnConfirm = dialogView.findViewById(R.id.btnConfirm);

        // Tạo dialog
        AlertDialog dialog = builder.create();
        dialog.show();

        // Thiết lập sự kiện cho các tùy chọn
        option250ml.setOnClickListener(v -> {
            selectedAmount = 250;
            etCustomAmount.setText("250");
        });

        option500ml.setOnClickListener(v -> {
            selectedAmount = 500;
            etCustomAmount.setText("500");
        });

        option750ml.setOnClickListener(v -> {
            selectedAmount = 750;
            etCustomAmount.setText("750");
        });

        option1000ml.setOnClickListener(v -> {
            selectedAmount = 1000;
            etCustomAmount.setText("1000");
        });

        // Thiết lập sự kiện cho nút hủy
        btnCancel.setOnClickListener(v -> {
            dialog.dismiss();
        });

        // Thiết lập sự kiện cho nút xác nhận
        btnConfirm.setOnClickListener(v -> {
            String customAmountStr = etCustomAmount.getText().toString().trim();
            if (!TextUtils.isEmpty(customAmountStr)) {
                try {
                    int customAmount = Integer.parseInt(customAmountStr);
                    if (customAmount > 0 && customAmount <= 2000) {
                        selectedAmount = customAmount;
                        updateUI();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(MainActivity.this, "Vui lòng nhập lượng nước từ 1 đến 2000ml", Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(MainActivity.this, "Vui lòng nhập số hợp lệ", Toast.LENGTH_SHORT).show();
                }
            } else {
                dialog.dismiss();
            }
        });
    }

    private void showTipsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.home_dialog_water_tips, null);
        builder.setView(dialogView);

        // Lấy nút đóng trong dialog
        Button btnClose = dialogView.findViewById(R.id.btnClose);

        // Tạo dialog
        AlertDialog dialog = builder.create();
        dialog.show();

        // Thiết lập sự kiện cho nút đóng
        btnClose.setOnClickListener(v -> {
            dialog.dismiss();
        });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        mainContent.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showNoData(boolean show) {
        tvNoData.setVisibility(show ? View.VISIBLE : View.GONE);
        rvHistory.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isDataLoaded) {
            loadData();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Hủy tất cả các timeout
        if (userInfoTimeoutRunnable != null) {
            timeoutHandler.removeCallbacks(userInfoTimeoutRunnable);
        }
        if (waterIntakeTimeoutRunnable != null) {
            timeoutHandler.removeCallbacks(waterIntakeTimeoutRunnable);
        }
    }
}
