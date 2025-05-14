package com.example.watertrackerandroidapp.SettingFunction.Activity;


import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TimePicker;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.watertrackerandroidapp.R;
import com.example.watertrackerandroidapp.Repository.ReminderRepository;
import com.example.watertrackerandroidapp.SettingFunction.Adapter.ScheduleAdapter;
import com.example.watertrackerandroidapp.SettingFunction.Item.ScheduleItem;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class ScheduleActivity extends AppCompatActivity {


    private RecyclerView rvReminder;
    private FloatingActionButton fabAdd;
    private List<ScheduleItem> scheduleList = new ArrayList<>();
    private ScheduleAdapter scheduleAdapter;
    private Rect deleteIconBounds;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_schedule);


        // Khởi tạo các view
        rvReminder = findViewById(R.id.rvReminder);
        fabAdd = findViewById(R.id.fabAdd);


        // Load lịch nhắc nhở từ SharedPreferences
        loadSchedules();


        // Khởi tạo adapter
        scheduleAdapter = new ScheduleAdapter(scheduleList, () -> saveSchedules());
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvReminder.setLayoutManager(layoutManager);
        rvReminder.setAdapter(scheduleAdapter);


        // Thêm phân cách giữa các mục
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, layoutManager.getOrientation());
        Drawable dividerDrawable = ContextCompat.getDrawable(this, android.R.color.darker_gray);
        if (dividerDrawable != null) {
            dividerItemDecoration.setDrawable(dividerDrawable);
            rvReminder.addItemDecoration(dividerItemDecoration);
        }


        // Thêm ItemTouchHelper để xử lý vuốt
        setupItemTouchHelper();


        // Xử lý sự kiện thêm thời gian mới
        fabAdd.setOnClickListener(v -> showTimePickerDialog());
    }


    // Thêm ItemTouchHelper để xử lý vuốt
    private void setupItemTouchHelper() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false; // Không hỗ trợ kéo thả
            }


            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                ScheduleItem item = scheduleList.get(position);
                String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                        ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                        : null;


                // Xóa nhắc nhở khỏi danh sách cục bộ
                scheduleList.remove(position);
                scheduleAdapter.notifyItemRemoved(position);
                saveSchedules(); // Lưu vào SharedPreferences và đồng bộ với Firebase


                // Xóa nhắc nhở khỏi Firebase
                if (userId != null) {
                    ReminderRepository reminderRepository = new ReminderRepository();
                    reminderRepository.deleteReminder(userId, item.getTime(), task -> {
                        if (task.isSuccessful()) {
                            Log.d("ScheduleActivity", "Reminder deleted from Firebase: " + item.getTime());
                        } else {
                            Log.e("ScheduleActivity", "Error deleting reminder from Firebase", task.getException());
                        }
                    });
                }
            }


            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                View itemView = viewHolder.itemView;
                float buttonWidth = itemView.getWidth() / 7f;
                float limitedDX = Math.max(dX, -buttonWidth);


                super.onChildDraw(c, recyclerView, viewHolder, limitedDX, dY, actionState, isCurrentlyActive);


                ColorDrawable background = new ColorDrawable(Color.RED);
                if (limitedDX < 0) {
                    background.setBounds(itemView.getRight() + (int) limitedDX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                } else {
                    background.setBounds(0, 0, 0, 0);
                }
                background.draw(c);


                Drawable icon = ContextCompat.getDrawable(ScheduleActivity.this, android.R.drawable.ic_menu_delete);
                if (icon != null) {
                    icon.setTint(Color.WHITE);
                    int intrinsicWidth = icon.getIntrinsicWidth();
                    int intrinsicHeight = icon.getIntrinsicHeight();
                    float scale = Math.min((float) itemView.getHeight() / intrinsicHeight, (float) buttonWidth / intrinsicWidth) * 0.8f;
                    intrinsicWidth = (int) (intrinsicWidth * scale);
                    intrinsicHeight = (int) (intrinsicHeight * scale);


                    int iconMarginVertical = (itemView.getHeight() - intrinsicHeight) / 2;
                    int iconTop = itemView.getTop() + iconMarginVertical;
                    int iconBottom = iconTop + intrinsicHeight;
                    int iconMarginHorizontal = (int) (buttonWidth - intrinsicWidth) / 2;
                    int iconLeft = itemView.getRight() - (int) buttonWidth + iconMarginHorizontal;
                    int iconRight = iconLeft + intrinsicWidth;


                    icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                    icon.draw(c);
                }
            }
        };


        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(rvReminder);
    }
    // Hiển thị dialog tùy chỉnh
    private void showTimePickerDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.setting_schedule_dialog_time_picker);


        TimePicker timePicker = dialog.findViewById(R.id.timePicker);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);
        Button btnConfirm = dialog.findViewById(R.id.btnConfirm);


        java.util.Calendar calendar = java.util.Calendar.getInstance();
        timePicker.setHour(calendar.get(java.util.Calendar.HOUR_OF_DAY));
        timePicker.setMinute(calendar.get(java.util.Calendar.MINUTE));


        btnCancel.setOnClickListener(v -> dialog.dismiss());


        btnConfirm.setOnClickListener(v -> {
            int hour = timePicker.getHour();
            int minute = timePicker.getMinute();
            String time = String.format("%02d:%02d", hour, minute);


            scheduleList.add(new ScheduleItem(time, true));
            scheduleList.sort((o1, o2) -> o1.getTime().compareTo(o2.getTime()));
            scheduleAdapter.notifyDataSetChanged();
            saveSchedules();
            dialog.dismiss();
        });


        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = (int) (displayMetrics.widthPixels * 0.9);
        int height = ViewGroup.LayoutParams.WRAP_CONTENT;


        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        dialog.getWindow().setDimAmount(0.5f);
        dialog.getWindow().setLayout(width, height);


        dialog.show();
    }


    public void onCloseClick(View view) {
        finish();
    }


    private void loadSchedules() {
        SharedPreferences sharedPref = getSharedPreferences("WaterPrefs", MODE_PRIVATE);
        Set<String> schedules = sharedPref.getStringSet("schedules", new HashSet<>());
        scheduleList.clear();
        for (String entry : schedules) {
            String[] parts = entry.split("\\|");
            if (parts.length == 2) {
                scheduleList.add(new ScheduleItem(parts[0], Boolean.parseBoolean(parts[1])));
            }
        }
        scheduleList.sort((o1, o2) -> o1.getTime().compareTo(o2.getTime()));
    }


    private void saveSchedules() {
        SharedPreferences sharedPref = getSharedPreferences("WaterPrefs", MODE_PRIVATE);
        Set<String> schedules = new HashSet<>();
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;


        if (userId != null) {
            ReminderRepository reminderRepository = new ReminderRepository();
            // Xóa tất cả nhắc nhở cũ trên Firebase
            reminderRepository.deleteAllReminders(userId, task -> {
                if (task.isSuccessful()) {
                    // Thêm lại các nhắc nhở mới
                    for (ScheduleItem item : scheduleList) {
                        schedules.add(item.getTime() + "|" + item.isEnabled());
                        reminderRepository.createReminder(
                                userId,
                                item.getTime(),
                                item.isEnabled(),
                                sharedPref.getString("reminderSound", "water_pouring"),
                                "Everyday",
                                task1 -> {
                                    if (task1.isSuccessful()) {
                                        Log.d("ScheduleActivity", "Reminder synced to Firebase: " + item.getTime());
                                    } else {
                                        Log.e("ScheduleActivity", "Error syncing to Firebase", task1.getException());
                                    }
                                });


                    }
                    sharedPref.edit().putStringSet("schedules", schedules).apply();
                } else {
                    Log.e("ScheduleActivity", "Error deleting old reminders", task.getException());
                }
            });
        } else {
            // Lưu cục bộ nếu không có userId
            for (ScheduleItem item : scheduleList) {
                schedules.add(item.getTime() + "|" + item.isEnabled());


            }
            sharedPref.edit().putStringSet("schedules", schedules).apply();
        }
    }
}
