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
import com.example.watertrackerandroidapp.SettingFunction.Adapter.ScheduleAdapter;
import com.example.watertrackerandroidapp.SettingFunction.Item.ScheduleItem;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

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
        final int[] swipedPosition = {-1}; // Mảng để có thể thay đổi giá trị trong lambda

        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false; // Không hỗ trợ kéo thả
            }
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                swipedPosition[0] = position;

                // Xử lý logic sau khi vuốt, ví dụ: xóa item
                // adapter.removeItem(position);
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                View itemView = viewHolder.itemView;
                // Tính chiều rộng của nút thùng rác (7/10 chiều rộng item)
                float buttonWidth = itemView.getWidth() / 7f;
                // Giới hạn khoảng cách vuốt tối đa (dX là số âm khi vuốt sang trái)
                float limitedDX = Math.max(dX, -buttonWidth); // Không cho vuốt quá 7/10 chiều rộng

                super.onChildDraw(c, recyclerView, viewHolder, limitedDX, dY, actionState, isCurrentlyActive);

                // Tạo nền đỏ cho nút thùng rác
                ColorDrawable background = new ColorDrawable(Color.RED);
                if (limitedDX < 0) { // Vuốt sang trái
                    background.setBounds(itemView.getRight() + (int) limitedDX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                    swipedPosition[0] = viewHolder.getAdapterPosition(); // Lưu vị trí mục đang vuốt
                } else {
                    background.setBounds(0, 0, 0, 0);
                    swipedPosition[0] = -1; // Reset khi vuốt lại sang phải (ẩn nút thùng rác)
                }
                background.draw(c);

                // Thêm biểu tượng thùng rác
                Drawable icon = ContextCompat.getDrawable(ScheduleActivity.this, android.R.drawable.ic_menu_delete);
                if (icon != null) {
                    // Đặt màu trắng cho biểu tượng thùng rác
                    icon.setTint(Color.WHITE);

                    // Tính kích thước biểu tượng (giữ tỷ lệ hợp lý trong nút)
                    int intrinsicWidth = icon.getIntrinsicWidth();
                    int intrinsicHeight = icon.getIntrinsicHeight();
                    // Đảm bảo biểu tượng không vượt quá chiều cao của item
                    float scale = Math.min((float) itemView.getHeight() / intrinsicHeight, (float) buttonWidth / intrinsicWidth) * 0.8f; // Giảm 20% để có lề
                    intrinsicWidth = (int) (intrinsicWidth * scale);
                    intrinsicHeight = (int) (intrinsicHeight * scale);

                    // Căn giữa biểu tượng trong phần nút thùng rác
                    int iconMarginVertical = (itemView.getHeight() - intrinsicHeight) / 2;
                    int iconTop = itemView.getTop() + iconMarginVertical;
                    int iconBottom = iconTop + intrinsicHeight;

                    int iconMarginHorizontal = (int) (buttonWidth - intrinsicWidth) / 2;
                    int iconLeft = itemView.getRight() - (int) buttonWidth + iconMarginHorizontal;
                    int iconRight = iconLeft + intrinsicWidth;

                    icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                    deleteIconBounds = new Rect(iconLeft, iconTop, iconRight, iconBottom); // Cập nhật trực tiếp biến instance
                    icon.draw(c);
                }
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(rvReminder);

        // Xử lý sự kiện nhấn vào RecyclerView để phát hiện nhấn vào biểu tượng thùng rác
        rvReminder.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                if (e.getAction() == MotionEvent.ACTION_UP && swipedPosition[0] != -1 && deleteIconBounds != null) {
                    float x = e.getX();
                    float y = e.getY();
                    View swipedView = rv.findViewHolderForAdapterPosition(swipedPosition[0]).itemView;
                    float translatedX = x + swipedView.getLeft();
                    float translatedY = y + swipedView.getTop();

                    // Kiểm tra xem người dùng có nhấn vào biểu tượng thùng rác không
                    if (deleteIconBounds.contains((int) translatedX, (int) translatedY)) {
                        scheduleList.remove(swipedPosition[0]);
                        scheduleAdapter.notifyItemRemoved(swipedPosition[0]);
                        saveSchedules();
                        swipedPosition[0] = -1; // Reset sau khi xóa
                        return true;
                    }
                }
                return false;
            }

            @Override
            public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {}

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {}
        });
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
        for (ScheduleItem item : scheduleList) {
            schedules.add(item.getTime() + "|" + item.isEnabled());
        }
        sharedPref.edit().putStringSet("schedules", schedules).apply();
    }
}