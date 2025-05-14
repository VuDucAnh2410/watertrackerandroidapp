package com.example.watertrackerandroidapp.HomeFunction;

import android.app.Application;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import com.example.watertrackerandroidapp.CustomViews.WaterProgressView;
import com.example.watertrackerandroidapp.LoginFunction.LoginActivity;
import com.example.watertrackerandroidapp.LoginFunction.UserInfoActivity;
import com.example.watertrackerandroidapp.Models.User;
import com.example.watertrackerandroidapp.Models.WaterIntake;
import com.example.watertrackerandroidapp.ProfileFunction.ProfileActivity;
import com.example.watertrackerandroidapp.R;
import com.example.watertrackerandroidapp.Repository.AuthRepository;
import com.example.watertrackerandroidapp.Repository.UserRepository;
import com.example.watertrackerandroidapp.Repository.WaterIntakeRepository;
import com.example.watertrackerandroidapp.SettingFunction.SettingActivity;
import com.example.watertrackerandroidapp.StatisticsFunction.StatisticsActivity;

import java.util.ArrayList;
import java.util.List;
import android.content.SharedPreferences;
import android.media.MediaPlayer;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    // UI components
    private SwipeRefreshLayout swipeRefreshLayout;
    private MediaPlayer mediaPlayer;
    private TextView tvTarget, tvConsumed, tvPercentage, tvSelectedAmount, tvNoData;
    private RecyclerView rvHistory; // Thay tvHistory bằng RecyclerView
    private ProgressBar progressBar;
    private CardView btnDrink;
    private LinearLayout waterAmountSelector,navSettings, navHome, navProfile, navStats;
    private ImageView ivTips;
    private WaterProgressView waterProgressView;

    // Repositories
    private AuthRepository authRepository;
    private UserRepository userRepository;
    private WaterIntakeRepository waterIntakeRepository;

    // Data
    private User currentUser;
    private List<WaterIntake> waterIntakeList = new ArrayList<>();
    private int selectedAmount = 250; // Mặc định 250ml
    private boolean isActivityRunning = false;
    private WaterIntakeAdapter waterIntakeAdapter; // Adapter cho RecyclerView

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Khởi tạo repositories
        authRepository = new AuthRepository();
        userRepository = new UserRepository();
        waterIntakeRepository = new WaterIntakeRepository();

        // Khởi tạo views
        initViews();

        // Thiết lập listeners
        setupListeners();

        // Kiểm tra trạng thái đăng nhập và thông tin người dùng
        checkUserStatus();
    }

    @Override
    protected void onStart() {
        super.onStart();
        isActivityRunning = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        isActivityRunning = false;
        // Giải phóng tài nguyên MediaPlayer
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void initViews() {
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        tvTarget = findViewById(R.id.tvTarget);
        tvConsumed = findViewById(R.id.tvConsumed);
        tvPercentage = findViewById(R.id.tvPercentage);
        tvSelectedAmount = findViewById(R.id.tvSelectedAmount);
        tvNoData = findViewById(R.id.tvNoData);
        rvHistory = findViewById(R.id.rvHistory); // Thay tvHistory bằng rvHistory
        progressBar = findViewById(R.id.progressBar);
        waterAmountSelector = findViewById(R.id.waterAmountSelector);
        ivTips = findViewById(R.id.ivTips);
        btnDrink = findViewById(R.id.btnDrink);
        waterProgressView = findViewById(R.id.waterProgressView);
        navSettings = findViewById(R.id.navSettings);
        navHome = findViewById(R.id.navHome);
        navStats = findViewById(R.id.navStats);
        navProfile = findViewById(R.id.navProfile);

        // Thiết lập RecyclerView
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        waterIntakeAdapter = new WaterIntakeAdapter(waterIntakeList);
        rvHistory.setAdapter(waterIntakeAdapter);
    }

    private void setupListeners() {
        // Làm mới dữ liệu
        swipeRefreshLayout.setOnRefreshListener(this::loadData);

        // Chọn lượng nước
        waterAmountSelector.setOnClickListener(v -> showWaterAmountDialog());

        // Hiển thị lời khuyên
        ivTips.setOnClickListener(v -> showWaterTipsDialog());

        // Ghi nhận uống nước
        btnDrink.setOnClickListener(v -> recordWaterIntake());

        navSettings.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingActivity.class);
            startActivity(intent);
        });
        navStats.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, StatisticsActivity.class);
            startActivity(intent);
        });
        navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        });
        navHome.setOnClickListener(v -> {
        });
    }

    private void checkUserStatus() {
        if (!authRepository.isUserLoggedIn()) {
            Toast.makeText(this, "Phiên đăng nhập hết hạn hoặc không hợp lệ. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            redirectToLogin();
            return;
        }

        String userId = authRepository.getCurrentUser().getUid();
        if (userId == null) {
            Toast.makeText(this, "Không thể lấy ID người dùng. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            redirectToLogin();
            return;
        }

        // Kiểm tra thông tin người dùng
        userRepository.getUserById(userId, new UserRepository.OnUserLoadedListener() {
            @Override
            public void onUserLoaded(User user) {
                if (user == null || !user.isHasUserInfo()) {
                    Toast.makeText(MainActivity.this, "Vui lòng nhập thông tin cá nhân.", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(MainActivity.this, UserInfoActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    currentUser = user;
                    loadData();
                }
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(MainActivity.this, "Lỗi tải thông tin người dùng: " + errorMessage, Toast.LENGTH_LONG).show();
                redirectToLogin();
            }
        });
    }

    private void loadData() {
        if (!isActivityRunning) return;
        swipeRefreshLayout.setRefreshing(true);
        progressBar.setVisibility(View.VISIBLE);

        if (!authRepository.isUserLoggedIn()) {
            Toast.makeText(this, "Phiên đăng nhập hết hạn hoặc không hợp lệ. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            redirectToLogin();
            return;
        }

        String userId = authRepository.getCurrentUser().getUid();
        if (userId == null) {
            Toast.makeText(this, "Không thể lấy ID người dùng. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            redirectToLogin();
            return;
        }

        // Tải thông tin người dùng
        userRepository.getUserById(userId, new UserRepository.OnUserLoadedListener() {
            @Override
            public void onUserLoaded(User user) {
                if (user != null) {
                    currentUser = user;
                    tvTarget.setText(user.getDailyTarget() + "ml");

                    // Tải lịch sử uống nước
                    waterIntakeRepository.getTodayWaterIntake(userId, new WaterIntakeRepository.OnWaterIntakeLoadedListener() {
                        @Override
                        public void onWaterIntakeLoaded(List<WaterIntake> intakeList, int totalAmount) {
                            if (!isActivityRunning) return;
                            waterIntakeList.clear();
                            waterIntakeList.addAll(intakeList);

                            // Cập nhật lịch sử uống nước
                            updateHistoryTextView();

                            // Cập nhật lượng nước đã uống
                            tvConsumed.setText(totalAmount + "ml");

                            // Cập nhật phần trăm và WaterProgressView
                            int percentage = user.getDailyTarget() > 0 ? (totalAmount * 100 / user.getDailyTarget()) : 0;
                            percentage = Math.min(percentage, 100); // Giới hạn phần trăm tối đa 100%
                            tvPercentage.setText(percentage + "%");
                            waterProgressView.setProgress(percentage);

                            // Hiển thị/ẩn thông báo không có dữ liệu
                            tvNoData.setVisibility(intakeList.isEmpty() ? View.VISIBLE : View.GONE);

                            swipeRefreshLayout.setRefreshing(false);
                            progressBar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError(String errorMessage) {
                            if (!isActivityRunning) return;
                            Toast.makeText(MainActivity.this, "Không thể tải lịch sử uống nước: " + errorMessage, Toast.LENGTH_LONG).show();
                            swipeRefreshLayout.setRefreshing(false);
                            progressBar.setVisibility(View.GONE);
                        }
                    });
                } else {
                    Toast.makeText(MainActivity.this, "Không thể tải thông tin người dùng.", Toast.LENGTH_LONG).show();
                    redirectToLogin();
                }
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(MainActivity.this, "Không thể tải thông tin người dùng: " + errorMessage, Toast.LENGTH_LONG).show();
                redirectToLogin();
            }
        });
    }

    private void updateHistoryTextView() {
        waterIntakeAdapter.notifyDataSetChanged(); // Cập nhật RecyclerView
    }

    private void showWaterAmountDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.home_dialog_water_amount);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        LinearLayout option250ml = dialog.findViewById(R.id.option250ml);
        LinearLayout option500ml = dialog.findViewById(R.id.option500ml);
        LinearLayout option750ml = dialog.findViewById(R.id.option750ml);
        LinearLayout option1000ml = dialog.findViewById(R.id.option1000ml);
        EditText etCustomAmount = dialog.findViewById(R.id.etCustomAmount);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);
        Button btnConfirm = dialog.findViewById(R.id.btnConfirm);

        option250ml.setOnClickListener(v -> {
            selectedAmount = 250;
            tvSelectedAmount.setText("250 ml");
            dialog.dismiss();
        });

        option500ml.setOnClickListener(v -> {
            selectedAmount = 500;
            tvSelectedAmount.setText("500 ml");
            dialog.dismiss();
        });

        option750ml.setOnClickListener(v -> {
            selectedAmount = 750;
            tvSelectedAmount.setText("750 ml");
            dialog.dismiss();
        });

        option1000ml.setOnClickListener(v -> {
            selectedAmount = 1000;
            tvSelectedAmount.setText("1000 ml");
            dialog.dismiss();
        });

        btnConfirm.setOnClickListener(v -> {
            String customAmountStr = etCustomAmount.getText().toString().trim();
            if (!TextUtils.isEmpty(customAmountStr)) {
                try {
                    selectedAmount = Integer.parseInt(customAmountStr);
                    if (selectedAmount > 0) {
                        tvSelectedAmount.setText(selectedAmount + " ml");
                        dialog.dismiss();
                    } else {
                        etCustomAmount.setError("Vui lòng nhập số dương");
                    }
                } catch (NumberFormatException e) {
                    etCustomAmount.setError("Vui lòng nhập số hợp lệ");
                }
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void showWaterTipsDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.home_dialog_water_tips);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        Button btnClose = dialog.findViewById(R.id.btnClose);
        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void recordWaterIntake() {
        if (!authRepository.isUserLoggedIn()) {
            Toast.makeText(this, "Phiên đăng nhập hết hạn hoặc không hợp lệ. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            redirectToLogin();
            return;
        }

        String userId = authRepository.getCurrentUser().getUid();
        if (userId == null) {
            Toast.makeText(this, "Không thể lấy ID người dùng. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            redirectToLogin();
            return;
        }

        // Phát âm thanh đã chọn
        SharedPreferences sharedPref = getSharedPreferences("WaterPrefs", MODE_PRIVATE);
        String selectedSound = sharedPref.getString("reminderSound", "water_pouring");
        try {
            int soundResId = getResources().getIdentifier(selectedSound, "raw", getPackageName());
            if (soundResId != 0) {
                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                }
                mediaPlayer = MediaPlayer.create(this, soundResId);
                mediaPlayer.start();
            } else {
                android.util.Log.e(TAG, "Không tìm thấy tài nguyên âm thanh: " + selectedSound);
            }
        } catch (Exception e) {
            android.util.Log.e(TAG, "Lỗi khi phát âm thanh: " + selectedSound, e);
        }

        waterIntakeRepository.addWaterIntake(
                userId,
                selectedAmount,
                "Nước lọc",
                aVoid -> {
                    Toast.makeText(this, "Đã ghi nhận " + selectedAmount + "ml nước.", Toast.LENGTH_SHORT).show();
                    loadData();
                },
                e -> Toast.makeText(this, "Không thể ghi nhận lượng nước: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // Adapter cho RecyclerView
    private static class WaterIntakeAdapter extends RecyclerView.Adapter<WaterIntakeAdapter.ViewHolder> {
        private final List<WaterIntake> waterIntakeList;

        public WaterIntakeAdapter(List<WaterIntake> waterIntakeList) {
            this.waterIntakeList = waterIntakeList;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.home_item_water_history, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            WaterIntake intake = waterIntakeList.get(position);
            holder.tvTime.setText(intake.getIntakeTime() != null ? intake.getIntakeTime() : "N/A");
            holder.tvAmount.setText(intake.getAmount() + " ml");
            // Icon đã được thiết lập trong XML, không cần thay đổi
        }

        @Override
        public int getItemCount() {
            return waterIntakeList.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTime, tvAmount;
            ImageView ivIcon;

            ViewHolder(View itemView) {
                super(itemView);
                tvTime = itemView.findViewById(R.id.tvTime);
                tvAmount = itemView.findViewById(R.id.tvAmount);
                ivIcon = itemView.findViewById(R.id.ivIcon);
            }
        }
    }

    public class WaterTrackerApp extends Application {
        @Override
        public void onCreate() {
            super.onCreate();
            createNotificationChannel();
        }

        private void createNotificationChannel() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(
                        "water_reminder_channel",
                        "Water Reminder",
                        NotificationManager.IMPORTANCE_DEFAULT
                );
                NotificationManager manager = getSystemService(NotificationManager.class);
                manager.createNotificationChannel(channel);
            }
        }
    }
}