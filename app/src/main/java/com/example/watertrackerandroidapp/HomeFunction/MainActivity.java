package com.example.watertrackerandroidapp.HomeFunction;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.watertrackerandroidapp.Adapters.WaterHistoryAdapter;
import com.example.watertrackerandroidapp.CustomViews.WaterProgressView;
import com.example.watertrackerandroidapp.Models.WaterIntake;
import com.example.watertrackerandroidapp.ProfileFunction.ProfileActivity;
import com.example.watertrackerandroidapp.R;
import com.example.watertrackerandroidapp.SettingFunction.SettingFragment;
import com.example.watertrackerandroidapp.StatisticsFunction.StatisticsActivity;
import com.example.watertrackerandroidapp.DataBase.UserDao;
import com.example.watertrackerandroidapp.DataBase.WaterIntakeDao;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private WaterProgressView waterProgressView;
    private TextView tvPercentage, tvTarget, tvConsumed, tvSelectedAmount;
    private CardView btnDrink;
    private LinearLayout waterAmountSelector;
    private RecyclerView rvHistory;
    private LinearLayout navHome, navStats, navSettings, navProfile;
    private ImageView ivTips;

    private WaterHistoryAdapter historyAdapter;
    private List<WaterIntake> waterIntakeList;
    private WaterIntakeDao waterIntakeDao;
    private UserDao userDao;

    private String userId;
    private int dailyTarget = 3000; // Default target (ml)
    private int currentIntake = 0;
    private int selectedAmount = 250; // Default amount (ml)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Kiểm tra đăng nhập
        checkLoginStatus();

        // Khởi tạo DAO
        waterIntakeDao = new WaterIntakeDao(this);
        userDao = new UserDao(this);

        // Khởi tạo views
        initViews();

        // Thiết lập sự kiện
        setupListeners();

        // Lấy thông tin người dùng
        loadUserInfo();

        // Lấy lịch sử uống nước
        loadWaterIntakeHistory();

        // Cập nhật UI
        updateUI();
    }

    private void checkLoginStatus() {
        SharedPreferences sharedPreferences = getSharedPreferences("WaterReminderPrefs", MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

        if (!isLoggedIn) {
            // Chuyển đến màn hình đăng nhập
            Intent intent = new Intent(MainActivity.this, com.example.watertrackerandroidapp.LoginFunction.LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Lấy userId
        userId = sharedPreferences.getString("userId", null);
        if (userId == null) {
            // Nếu không có userId, chuyển đến màn hình đăng nhập
            Intent intent = new Intent(MainActivity.this, com.example.watertrackerandroidapp.LoginFunction.LoginActivity.class);
            startActivity(intent);
            finish();
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

        // Thiết lập RecyclerView
        waterIntakeList = new ArrayList<>();
        historyAdapter = new WaterHistoryAdapter(this, waterIntakeList);
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        rvHistory.setAdapter(historyAdapter);
    }

    private void setupListeners() {
        // Nút uống nước
        btnDrink.setOnClickListener(v -> {
            addWaterIntake(selectedAmount);
        });

        // Chọn lượng nước
        waterAmountSelector.setOnClickListener(v -> {
            showWaterAmountDialog();
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
            Intent intent = new Intent(MainActivity.this, SettingFragment.class);
            startActivity(intent);
        });

        navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        // Tips
        ivTips.setOnClickListener(v -> {
            showTipsDialog();
        });
    }

    private void loadUserInfo() {
        if (userId != null) {
            // Lấy mục tiêu uống nước từ database
            int target = userDao.getDailyTarget(userId);
            if (target > 0) {
                dailyTarget = target;
            }
        }
    }

    private void loadWaterIntakeHistory() {
        if (userId != null) {
            // Lấy ngày hiện tại
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String currentDate = dateFormat.format(new Date());

            // Lấy tổng lượng nước đã uống trong ngày
            currentIntake = waterIntakeDao.getTotalIntakeForDay(userId, currentDate);

            // Lấy lịch sử uống nước
            List<WaterIntake> history = waterIntakeDao.getIntakeHistoryForDay(userId, currentDate);

            // Thêm lần uống nước tiếp theo (dự kiến)
            if (!history.isEmpty()) {
                WaterIntake nextIntake = new WaterIntake();
                nextIntake.setAmount(selectedAmount);
                nextIntake.setScheduled(true);

                // Thêm vào đầu danh sách
                history.add(0, nextIntake);
            }

            waterIntakeList.clear();
            waterIntakeList.addAll(history);
            historyAdapter.notifyDataSetChanged();
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
    }

    private void addWaterIntake(int amount) {
        if (userId != null) {
            // Thêm vào database
            String intakeId = waterIntakeDao.addWaterIntake(userId, amount, "Water");

            if (intakeId != null) {
                // Cập nhật lượng nước đã uống
                currentIntake += amount;

                // Cập nhật UI
                updateUI();

                // Cập nhật lịch sử
                loadWaterIntakeHistory();

                Toast.makeText(this, "Đã thêm " + amount + "ml nước!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Không thể thêm lượng nước. Vui lòng thử lại!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showWaterAmountDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.home_dialog_water_amount);

        // Thiết lập kích thước của dialog
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            // Thiết lập chiều rộng của dialog là 85% chiều rộng màn hình
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(window.getAttributes());
            layoutParams.width = (int)(getResources().getDisplayMetrics().widthPixels * 0.85);
            window.setAttributes(layoutParams);
        }

        LinearLayout option250ml = dialog.findViewById(R.id.option250ml);
        LinearLayout option500ml = dialog.findViewById(R.id.option500ml);
        LinearLayout option750ml = dialog.findViewById(R.id.option750ml);
        LinearLayout option1000ml = dialog.findViewById(R.id.option1000ml);
        EditText etCustomAmount = dialog.findViewById(R.id.etCustomAmount);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);
        Button btnConfirm = dialog.findViewById(R.id.btnConfirm);

        // Thiết lập sự kiện cho các tùy chọn
        option250ml.setOnClickListener(v -> {
            selectedAmount = 250;
            etCustomAmount.setText("");
        });

        option500ml.setOnClickListener(v -> {
            selectedAmount = 500;
            etCustomAmount.setText("");
        });

        option750ml.setOnClickListener(v -> {
            selectedAmount = 750;
            etCustomAmount.setText("");
        });

        option1000ml.setOnClickListener(v -> {
            selectedAmount = 1000;
            etCustomAmount.setText("");
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            // Kiểm tra nếu có lượng nước tùy chỉnh
            String customAmountStr = etCustomAmount.getText().toString().trim();
            if (!customAmountStr.isEmpty()) {
                try {
                    int customAmount = Integer.parseInt(customAmountStr);
                    if (customAmount > 0 && customAmount <= 2000) {
                        selectedAmount = customAmount;
                    } else {
                        Toast.makeText(MainActivity.this, "Lượng nước phải từ 1 đến 2000ml", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(MainActivity.this, "Lượng nước không hợp lệ", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            // Cập nhật UI
            tvSelectedAmount.setText(selectedAmount + " ml");
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showTipsDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.home_dialog_water_tips);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        Button btnClose = dialog.findViewById(R.id.btnClose);
        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Cập nhật dữ liệu khi quay lại màn hình
        loadUserInfo();
        loadWaterIntakeHistory();
        updateUI();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (waterIntakeDao != null) {
            waterIntakeDao.close();
        }
        if (userDao != null) {
            userDao.close();
        }
    }
}

