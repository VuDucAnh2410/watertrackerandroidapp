package com.example.watertrackerandroidapp.SettingFunction.Activity;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.app.AppCompatActivity;

import com.example.watertrackerandroidapp.R;
import com.example.watertrackerandroidapp.SettingFunction.Item.ScheduleItem;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ScheduleActivity extends AppCompatActivity {

    private ListView listViewReminder;
    private FloatingActionButton fabAdd;
    private List<ScheduleItem> scheduleList = new ArrayList<>();
    private ScheduleAdapter scheduleAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_schedule);

        // Khởi tạo các view
        listViewReminder = findViewById(R.id.listViewReminder);
        fabAdd = findViewById(R.id.fabAdd);

        // Load lịch nhắc nhở từ SharedPreferences
        loadSchedules();

        // Khởi tạo adapter
        scheduleAdapter = new ScheduleAdapter(scheduleList);
        listViewReminder.setAdapter(scheduleAdapter);

        // Xử lý sự kiện thêm thời gian mới
        fabAdd.setOnClickListener(v -> showTimePickerDialog());
    }

    // Hiển thị dialog tùy chỉnh
    private void showTimePickerDialog() {
        // Tạo dialog
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.setting_schedule_dialog_time_picker);

        // Khởi tạo TimePicker và các nút
        TimePicker timePicker = dialog.findViewById(R.id.timePicker);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);
        Button btnConfirm = dialog.findViewById(R.id.btnConfirm);

        // Đặt giờ mặc định cho TimePicker
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        timePicker.setHour(calendar.get(java.util.Calendar.HOUR_OF_DAY));
        timePicker.setMinute(calendar.get(java.util.Calendar.MINUTE));

        // Xử lý nút Hủy bỏ
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        // Xử lý nút Đồng ý
        btnConfirm.setOnClickListener(v -> {
            // Lấy giờ và phút từ TimePicker
            int hour = timePicker.getHour();
            int minute = timePicker.getMinute();

            // Định dạng thời gian
            String time = String.format("%02d:%02d", hour, minute);

            // Thêm vào danh sách
            scheduleList.add(new ScheduleItem(time, true));
            scheduleList.sort((o1, o2) -> o1.getTime().compareTo(o2.getTime()));
            scheduleAdapter.notifyDataSetChanged();

            // Lưu vào SharedPreferences
            saveSchedules();

            // Đóng dialog
            dialog.dismiss();
        });

        // Hiển thị dialog
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = (int) (displayMetrics.widthPixels * 0.9); // 90% chiều rộng màn hình
        int height = ViewGroup.LayoutParams.WRAP_CONTENT;

        // Đặt nền trắng cho dialog
        dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.WHITE));

        // Thêm hiệu ứng làm tối giao diện phía dưới
        dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        dialog.getWindow().setDimAmount(0.5f); // Độ tối từ 0.0f (không tối) đến 1.0f (hoàn toàn tối)

        // Đặt kích thước dialog
        dialog.getWindow().setLayout(width, height);

        dialog.show();


    }

    // Xử lý sự kiện đóng màn hình
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

    // Adapter cho ListView
    private class ScheduleAdapter extends BaseAdapter {
        private List<ScheduleItem> items;

        public ScheduleAdapter(List<ScheduleItem> items) {
            this.items = items;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.setting_schedule_item, parent, false);
            }

            ScheduleItem item = items.get(position);
            TextView tvTime = convertView.findViewById(R.id.tvTime);
            SwitchCompat switchReminder = convertView.findViewById(R.id.switchReminder);

            tvTime.setText(item.getTime());
            switchReminder.setChecked(item.isEnabled());

            switchReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
                item.setEnabled(isChecked);
                saveSchedules();
            });

            return convertView;
        }
    }
}