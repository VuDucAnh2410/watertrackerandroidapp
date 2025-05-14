package com.example.watertrackerandroidapp.StatisticsFunction;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.ViewFlipper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.watertrackerandroidapp.Adapters.WaterHistoryAdapter;
import com.example.watertrackerandroidapp.HomeFunction.MainActivity;
import com.example.watertrackerandroidapp.Models.User;
import com.example.watertrackerandroidapp.Models.WaterIntake;
import com.example.watertrackerandroidapp.ProfileFunction.ProfileActivity;
import com.example.watertrackerandroidapp.R;
import com.example.watertrackerandroidapp.Repository.UserRepository;
import com.example.watertrackerandroidapp.Repository.WaterIntakeRepository;
import com.example.watertrackerandroidapp.SettingFunction.SettingActivity;
import com.example.watertrackerandroidapp.firebase.FirebaseManager;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StatisticsActivity extends AppCompatActivity {
    private static final String TAG = "StatisticsActivity";

    private TextView tvMonthTab, tvYearTab, tvDateSelector;
    private ViewFlipper viewFlipper;
    private RecyclerView rvCalendarMonth;
    private BarChart barChart;
    private TextView tvDailyGoal, tvDailyGoalYear;
    private TextView tvWeeklyAverage, tvWeeklyAverageYear;
    private TextView tvMonthlyAverage, tvMonthlyAverageYear;
    private TextView tvCompletionRate, tvCompletionRateYear;
    private TextView tvDrinkingFrequency, tvDrinkingFrequencyYear;
    private View navHome, navStats, navSettings, navProfile;

    private CalendarAdapter calendarAdapter;

    private FirebaseAuth mAuth;
    private UserRepository userRepository;
    private WaterIntakeRepository waterIntakeRepository;
    private String userId;

    // Data
    private Calendar currentDate;
    private int dailyTarget = 2000;
    private List<WaterIntake> monthlyIntakeList = new ArrayList<>();
    private List<WaterIntake> yearlyIntakeList = new ArrayList<>(); // Danh sách mới cho tab "Năm"
    private Map<String, Integer> dailyIntakeMap = new HashMap<>();
    private Map<Integer, Integer> monthlyIntakeMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stats_activity_statistics);

        mAuth = FirebaseManager.getInstance().getAuth();
        userRepository = new UserRepository();
        waterIntakeRepository = new WaterIntakeRepository();
        userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;

        if (userId == null) {
            finish();
            return;
        }

        currentDate = Calendar.getInstance();
        initViews();
        setupListeners();
        loadUserInfo();
        loadStatisticsData();
    }

    private void initViews() {
        tvMonthTab = findViewById(R.id.tvMonthTab);
        tvYearTab = findViewById(R.id.tvYearTab);
        tvDateSelector = findViewById(R.id.tvDateSelector);
        viewFlipper = findViewById(R.id.viewFlipper);
        rvCalendarMonth = findViewById(R.id.rvCalendarMonth);
        barChart = findViewById(R.id.barChart);

        tvDailyGoal = findViewById(R.id.tvDailyGoal);
        tvWeeklyAverage = findViewById(R.id.tvWeeklyAverage);
        tvMonthlyAverage = findViewById(R.id.tvMonthlyAverage);
        tvCompletionRate = findViewById(R.id.tvCompletionRate);
        tvDrinkingFrequency = findViewById(R.id.tvDrinkingFrequency);

        tvDailyGoalYear = findViewById(R.id.tvDailyGoalYear);
        tvWeeklyAverageYear = findViewById(R.id.tvWeeklyAverageYear);
        tvMonthlyAverageYear = findViewById(R.id.tvMonthlyAverageYear);
        tvCompletionRateYear = findViewById(R.id.tvCompletionRateYear);
        tvDrinkingFrequencyYear = findViewById(R.id.tvDrinkingFrequencyYear);

        navHome = findViewById(R.id.navHome);
        navStats = findViewById(R.id.navStats);
        navSettings = findViewById(R.id.navSettings);
        navProfile = findViewById(R.id.navProfile);

        calendarAdapter = new CalendarAdapter(this, generateCalendarDays(), dailyIntakeMap, dailyTarget);
        calendarAdapter.setOnDayClickListener(this::showDayDetailsDialog);
        rvCalendarMonth.setLayoutManager(new GridLayoutManager(this, 7));
        rvCalendarMonth.setAdapter(calendarAdapter);

        setupBarChart();
        updateDateDisplay();
    }

    private void setupListeners() {
        tvMonthTab.setOnClickListener(v -> {
            tvMonthTab.setTextColor(getResources().getColor(R.color.lightBlue));
            tvYearTab.setTextColor(getResources().getColor(R.color.black));
            viewFlipper.setDisplayedChild(0);
            updateDateDisplay();
        });

        tvYearTab.setOnClickListener(v -> {
            tvMonthTab.setTextColor(getResources().getColor(R.color.black));
            tvYearTab.setTextColor(getResources().getColor(R.color.lightBlue));
            viewFlipper.setDisplayedChild(1);
            updateDateDisplay();
        });

        tvDateSelector.setOnClickListener(v -> {
            if (viewFlipper.getDisplayedChild() == 0) {
                showMonthPicker();
            } else {
                showYearPicker();
            }
        });

        navHome.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

        navStats.setOnClickListener(v -> {});

        navSettings.setOnClickListener(v -> {
            startActivity(new Intent(this, SettingActivity.class));
            finish();
        });

        navProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
            finish();
        });
    }

    private void loadUserInfo() {
        userRepository.getUserById(userId, new UserRepository.OnUserLoadedListener() {
            @Override
            public void onUserLoaded(User user) {
                if (user != null) {
                    dailyTarget = user.getDailyTarget();
                    updateDailyGoalDisplay();
                    calendarAdapter.setDailyTarget(dailyTarget);
                    calendarAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Lỗi khi tải thông tin người dùng: " + errorMessage);
            }
        });
    }

    private void loadStatisticsData() {
        int year = currentDate.get(Calendar.YEAR);
        int month = currentDate.get(Calendar.MONTH) + 1;

        String monthStr = String.format(Locale.getDefault(), "%04d-%02d", year, month);
        String yearStr = String.format(Locale.getDefault(), "%04d", year);

        loadMonthlyData(monthStr);
        loadYearlyData(yearStr);
    }

    private void loadMonthlyData(String monthStr) {
        waterIntakeRepository.getMonthlyWaterIntake(userId, monthStr, new WaterIntakeRepository.OnMonthlyIntakeLoadedListener() {
            @Override
            public void onMonthlyIntakeLoaded(List<WaterIntake> intakeList, Map<Integer, Integer> dailyData, int totalAmount) {
                monthlyIntakeList.clear();
                dailyIntakeMap.clear();
                monthlyIntakeList.addAll(intakeList);

                if (dailyData != null && !dailyData.isEmpty()) {
                    for (Map.Entry<Integer, Integer> entry : dailyData.entrySet()) {
                        String date = String.format(Locale.getDefault(), "%04d-%02d-%02d",
                                currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH) + 1, entry.getKey());
                        dailyIntakeMap.put(date, entry.getValue());
                        Log.d(TAG, "Daily intake: date=" + date + ", amount=" + entry.getValue());
                    }

                    calendarAdapter.updateDailyIntake(dailyIntakeMap);
                    calendarAdapter.notifyDataSetChanged();
                    updateMonthlyStatistics();
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Lỗi khi tải dữ liệu lượng nước uống hàng tháng: " + errorMessage);
            }
        });
    }

    private void loadYearlyData(String yearStr) {
        waterIntakeRepository.getYearlyWaterIntake(userId, yearStr, new WaterIntakeRepository.OnYearlyIntakeLoadedListener() {
            @Override
            public void onYearlyIntakeLoaded(Map<Integer, Integer> monthlyData, int totalAmount) {
                monthlyIntakeMap.clear();
                monthlyIntakeMap.putAll(monthlyData);
                updateBarChart();
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Lỗi khi tải dữ liệu lượng nước uống hàng năm: " + errorMessage);
            }
        });

        waterIntakeRepository.getYearlyWaterIntakeDetails(userId, yearStr, new WaterIntakeRepository.OnYearlyIntakeDetailsLoadedListener() {
            @Override
            public void onYearlyIntakeDetailsLoaded(List<WaterIntake> intakeList) {
                yearlyIntakeList.clear();
                yearlyIntakeList.addAll(intakeList);
                updateYearlyStatistics();
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Lỗi khi tải dữ liệu chi tiết lượng nước uống hàng năm: " + errorMessage);
            }
        });
    }

    private void updateMonthlyStatistics() {
        Map<String, Integer> dailyIntakeTotals = new HashMap<>();
        String currentMonthStr = String.format(Locale.getDefault(), "%04d-%02d",
                currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH) + 1);

        for (WaterIntake intake : monthlyIntakeList) {
            String date = intake.getIntakeDate();
            if (date.startsWith(currentMonthStr)) {
                dailyIntakeTotals.put(date, dailyIntakeTotals.getOrDefault(date, 0) + intake.getAmount());
            }
        }

        int daysWithIntake = dailyIntakeTotals.size();
        int totalIntakes = monthlyIntakeList.size();

        int weeklyTotal = 0;
        int weeklyDaysWithData = 0;
        int monthlyTotal = 0;
        int monthlyDaysWithData = dailyIntakeTotals.size();
        int completedDays = 0;

        for (Map.Entry<String, Integer> entry : dailyIntakeTotals.entrySet()) {
            String date = entry.getKey();
            int amount = entry.getValue();

            monthlyTotal += amount;

            if (isDateInCurrentWeek(date)) {
                weeklyTotal += amount;
                weeklyDaysWithData++;
            }

            if (amount >= dailyTarget) {
                completedDays++;
            }
        }

        int weeklyAvg = weeklyDaysWithData > 0 ? weeklyTotal / weeklyDaysWithData : 0;
        int monthlyAvg = monthlyDaysWithData > 0 ? monthlyTotal / monthlyDaysWithData : 0;
        int completionRate = monthlyDaysWithData > 0 ? (completedDays * 100) / monthlyDaysWithData : 0;
        float frequency = daysWithIntake > 0 ? (float) totalIntakes / daysWithIntake : 0;

        tvWeeklyAverage.setText(weeklyAvg + " ml");
        tvMonthlyAverage.setText(monthlyAvg + " ml");
        tvCompletionRate.setText(completionRate + "%");
        tvDrinkingFrequency.setText(String.format(Locale.getDefault(), "%.1f lần / Ngày", frequency));
    }

    private void updateYearlyStatistics() {
        Map<String, Integer> dailyIntakeTotals = new HashMap<>();
        String currentYearStr = String.format(Locale.getDefault(), "%04d", currentDate.get(Calendar.YEAR));

        for (WaterIntake intake : yearlyIntakeList) {
            String date = intake.getIntakeDate();
            if (date.startsWith(currentYearStr)) {
                dailyIntakeTotals.put(date, dailyIntakeTotals.getOrDefault(date, 0) + intake.getAmount());
            }
        }

        int daysWithIntake = dailyIntakeTotals.size();
        int totalIntakes = yearlyIntakeList.size();

        int weeklyTotal = 0;
        int weeklyDaysWithData = 0;
        for (Map.Entry<String, Integer> entry : dailyIntakeTotals.entrySet()) {
            String date = entry.getKey();
            int amount = entry.getValue();

            if (isDateInCurrentWeek(date)) {
                weeklyTotal += amount;
                weeklyDaysWithData++;
            }
        }

        int yearlyTotal = 0;
        int yearlyDaysWithData = dailyIntakeTotals.size();
        int completedDays = 0;

        for (Map.Entry<String, Integer> entry : dailyIntakeTotals.entrySet()) {
            int amount = entry.getValue();
            yearlyTotal += amount;

            if (amount >= dailyTarget) {
                completedDays++;
            }
        }

        Map<Integer, Integer> monthsWithData = new HashMap<>();
        for (String date : dailyIntakeTotals.keySet()) {
            String[] parts = date.split("-");
            int month = Integer.parseInt(parts[1]);
            monthsWithData.put(month, 1);
        }

        int weeklyAvg = weeklyDaysWithData > 0 ? weeklyTotal / weeklyDaysWithData : 0;
        int yearlyAvg = yearlyDaysWithData > 0 ? yearlyTotal / yearlyDaysWithData : 0;
        int completionRate = yearlyDaysWithData > 0 ? (completedDays * 100) / yearlyDaysWithData : 0;
        float frequency = daysWithIntake > 0 ? (float) totalIntakes / daysWithIntake : 0;

        tvWeeklyAverageYear.setText(weeklyAvg + " ml");
        tvMonthlyAverageYear.setText(yearlyAvg + " ml");
        tvCompletionRateYear.setText(completionRate + "%");
        tvDrinkingFrequencyYear.setText(String.format(Locale.getDefault(), "%.1f lần / Ngày", frequency));
    }

    private boolean isDateInCurrentWeek(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = sdf.parse(dateStr);

            Calendar cal1 = Calendar.getInstance();
            cal1.setTime(date);

            Calendar cal2 = Calendar.getInstance();

            return cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR)
                    && cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);
        } catch (Exception e) {
            return false;
        }
    }

    private int getDaysInMonth(int month, int year) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month - 1);
        return cal.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    private void updateDateDisplay() {
        SimpleDateFormat monthFormat = new SimpleDateFormat("MM/yyyy", Locale.getDefault());
        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", Locale.getDefault());

        if (viewFlipper.getDisplayedChild() == 0) {
            tvDateSelector.setText("Th" + (currentDate.get(Calendar.MONTH) + 1) + ", " + currentDate.get(Calendar.YEAR));
        } else {
            tvDateSelector.setText(yearFormat.format(currentDate.getTime()));
        }

        loadStatisticsData();
    }

    private void updateDailyGoalDisplay() {
        tvDailyGoal.setText("Mục tiêu: " + dailyTarget + "ml / ngày");
        tvDailyGoalYear.setText("Mục tiêu: " + dailyTarget + "ml / ngày");
    }

    private List<CalendarDay> generateCalendarDays() {
        List<CalendarDay> days = new ArrayList<>();

        Calendar cal = (Calendar) currentDate.clone();
        cal.set(Calendar.DAY_OF_MONTH, 1);

        int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int offset = firstDayOfWeek - Calendar.MONDAY;
        if (offset < 0) offset += 7;

        for (int i = 0; i < offset; i++) {
            days.add(new CalendarDay(0, false));
        }

        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (int i = 1; i <= daysInMonth; i++) {
            days.add(new CalendarDay(i, true));
        }

        int remainingDays = 7 - (days.size() % 7);
        if (remainingDays < 7) {
            for (int i = 0; i < remainingDays; i++) {
                days.add(new CalendarDay(0, false));
            }
        }

        return days;
    }

    private void setupBarChart() {
        barChart.getDescription().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.setDrawBarShadow(false);
        barChart.setDrawValueAboveBar(true);
        barChart.setPinchZoom(false);
        barChart.setScaleEnabled(false);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(12, true);
        xAxis.setTextSize(10f);
        xAxis.setLabelRotationAngle(0);
        xAxis.setGranularityEnabled(true);
        xAxis.setTextColor(ContextCompat.getColor(this, R.color.black));

        barChart.getAxisLeft().setDrawGridLines(false);
        barChart.getAxisRight().setEnabled(false);
        barChart.getLegend().setEnabled(false);

        updateBarChart();
    }

    private void updateBarChart() {
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        for (int i = 1; i <= 12; i++) {
            int amount = monthlyIntakeMap.getOrDefault(i, 0);
            entries.add(new BarEntry(i - 1, amount));
            labels.add(String.valueOf(i));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Lượng nước");
        dataSet.setColor(getResources().getColor(R.color.lightBlue));
        dataSet.setValueTextColor(getResources().getColor(R.color.black));
        dataSet.setValueTextSize(10f);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.6f);

        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        barChart.getXAxis().setLabelCount(labels.size(), false);
        barChart.setData(data);
        barChart.invalidate();
    }

    private void showMonthPicker() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.stats_dialog_month_picker, null);

        RecyclerView rvMonths = view.findViewById(R.id.rvMonths);
        ImageButton btnBack = view.findViewById(R.id.btnBack);
        Button btnSelect = view.findViewById(R.id.btnSelect);

        List<String> months = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            months.add("Tháng " + i);
        }

        MonthAdapter adapter = new MonthAdapter(this, months, currentDate.get(Calendar.MONTH));
        final int[] selectedMonth = {currentDate.get(Calendar.MONTH)};

        adapter.setOnMonthClickListener(position -> {
            selectedMonth[0] = position;
            adapter.setSelectedPosition(position);
            Log.d(TAG, "Month selected: " + (position + 1));
        });

        rvMonths.setLayoutManager(new LinearLayoutManager(this));
        rvMonths.setAdapter(adapter);

        AlertDialog dialog = builder.setView(view).create();

        btnBack.setOnClickListener(v -> {
            Log.d(TAG, "Back button clicked in month picker");
            dialog.dismiss();
        });

        btnSelect.setOnClickListener(v -> {
            Log.d(TAG, "Select button clicked, selected month: " + (selectedMonth[0] + 1));
            currentDate.set(Calendar.MONTH, selectedMonth[0]);
            updateDateDisplay();
            calendarAdapter.setCalendarDays(generateCalendarDays());
            calendarAdapter.notifyDataSetChanged();
            dialog.dismiss();
        });

        Log.d(TAG, "Showing month picker dialog");
        dialog.show();
    }

    private void showYearPicker() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.stats_dialog_year_picker, null);

        NumberPicker yearPicker = view.findViewById(R.id.yearPicker);
        Button btnCancel = view.findViewById(R.id.btnCancel);
        Button btnConfirm = view.findViewById(R.id.btnConfirm);

        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        yearPicker.setMinValue(currentYear - 10);
        yearPicker.setMaxValue(currentYear + 10);
        yearPicker.setValue(currentDate.get(Calendar.YEAR));

        AlertDialog dialog = builder.setView(view).create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            currentDate.set(Calendar.YEAR, yearPicker.getValue());
            updateDateDisplay();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showDayDetailsDialog(CalendarDay day, String dateStr) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.stats_dialog_day_details, null);

        TextView tvDate = view.findViewById(R.id.tvDate);
        TextView tvWaterIntake = view.findViewById(R.id.tvWaterIntake);
        TextView tvCompletionRate = view.findViewById(R.id.tvCompletionRate);
        TextView tvFrequency = view.findViewById(R.id.tvFrequency);
        RecyclerView rvIntakeHistory = view.findViewById(R.id.rvIntakeHistory);
        ImageButton btnBack = view.findViewById(R.id.btnBack);

        Log.d(TAG, "Initializing day details dialog for date: " + dateStr);
        if (btnBack == null) {
            Log.e(TAG, "btnBack is null, check R.id.btnBack in stats_dialog_day_details.xml");
        } else {
            Log.d(TAG, "btnBack initialized successfully");
        }

        tvDate.setText(dateStr);

        int waterIntake = dailyIntakeMap.containsKey(dateStr) ? dailyIntakeMap.get(dateStr) : 0;
        tvWaterIntake.setText(waterIntake + " ml");

        int completionRate = dailyTarget > 0 ? (waterIntake * 100) / dailyTarget : 0;
        tvCompletionRate.setText(completionRate + "%");

        List<WaterIntake> dailyIntakes = new ArrayList<>();
        int drinkCount = 0;
        for (WaterIntake intake : monthlyIntakeList) {
            if (intake.getIntakeDate().equals(dateStr)) {
                drinkCount++;
                dailyIntakes.add(intake);
            }
        }
        tvFrequency.setText(drinkCount + " lần");
        Log.d(TAG, "Daily intakes for " + dateStr + ": " + dailyIntakes.size());

        WaterHistoryAdapter historyAdapter = new WaterHistoryAdapter(this, dailyIntakes);
        rvIntakeHistory.setLayoutManager(new LinearLayoutManager(this));
        rvIntakeHistory.setAdapter(historyAdapter);

        AlertDialog dialog = builder.setView(view).create();
        dialog.setCancelable(true);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                Log.d(TAG, "Back button clicked for day details dialog");
                dialog.dismiss();
            });
        }

        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().getDecorView().post(() -> {
            Log.d(TAG, "Dialog height: " + dialog.getWindow().getDecorView().getHeight());
        });
        dialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserInfo();
        loadStatisticsData();
    }

    public static class CalendarDay {
        private int day;
        private boolean isCurrentMonth;

        public CalendarDay(int day, boolean isCurrentMonth) {
            this.day = day;
            this.isCurrentMonth = isCurrentMonth;
        }

        public int getDay() {
            return day;
        }

        public boolean isCurrentMonth() {
            return isCurrentMonth;
        }
    }
}