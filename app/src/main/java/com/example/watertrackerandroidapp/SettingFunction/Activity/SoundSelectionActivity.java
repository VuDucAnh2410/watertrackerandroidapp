package com.example.watertrackerandroidapp.SettingFunction.Activity;


import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;


import com.example.watertrackerandroidapp.R;
import com.example.watertrackerandroidapp.SettingFunction.Item.SoundItem;
import com.example.watertrackerandroidapp.Repository.ReminderRepository;
import com.google.firebase.auth.FirebaseAuth;


import java.util.ArrayList;
import java.util.List;


public class SoundSelectionActivity extends AppCompatActivity {


    private ListView listViewSounds;
    private SoundAdapter soundAdapter;
    private ReminderRepository reminderRepository;
    private List<SoundItem> soundList = new ArrayList<>();
    private int selectedPosition = -1;
    private MediaPlayer mediaPlayer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_sound);
        // Khởi tạo ReminderRepository
        reminderRepository = new ReminderRepository();


        // Khởi tạo ListView
        listViewSounds = findViewById(R.id.listViewSounds);


        // Tạo danh sách âm thanh
        soundList.add(new SoundItem("Đóng nước", "water_pouring"));
        soundList.add(new SoundItem("Cổ điển", "classic"));
        soundList.add(new SoundItem("Giọt nước", "water_drop"));
        soundList.add(new SoundItem("Thiên nhiên", "nature"));


        // Load âm thanh đã chọn từ SharedPreferences
        SharedPreferences sharedPref = getSharedPreferences("WaterPrefs", MODE_PRIVATE);
        String selectedSound = sharedPref.getString("reminderSound", "water_pouring");
        for (int i = 0; i < soundList.size(); i++) {
            if (soundList.get(i).getSoundId().equals(selectedSound)) {
                selectedPosition = i;
                break;
            }
        }


        // Khởi tạo adapter
        soundAdapter = new SoundAdapter(soundList);
        listViewSounds.setAdapter(soundAdapter);
    }


    @Override
    protected void onDestroy() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        super.onDestroy();
    }


    // Xử lý sự kiện đóng màn hình
    public void onCloseClick(View view) {
        finish();
    }


    // Adapter cho ListView
    private class SoundAdapter extends BaseAdapter {
        private List<SoundItem> items;


        public SoundAdapter(List<SoundItem> items) {
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
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.setting_sound_item, parent, false);
            }


            SoundItem item = items.get(position);
            TextView tvSoundName = convertView.findViewById(R.id.tvSoundName);
            CheckBox checkBox = convertView.findViewById(R.id.checkBox);


            // Hiển thị tên âm thanh
            tvSoundName.setText(item.getSoundName());


            // Hiển thị/ẩn CheckBox dựa trên selectedPosition
            if (position == selectedPosition) {
                checkBox.setVisibility(View.VISIBLE); // Hiển thị CheckBox
                checkBox.setChecked(true); // Đặt dấu tích
            } else {
                checkBox.setVisibility(View.GONE); // Ẩn CheckBox
            }


            // Xử lý sự kiện nhấn vào mục
            convertView.setOnClickListener(v -> {
                if (selectedPosition != position) {
                    selectedPosition = position;
                    notifyDataSetChanged();


                    // Lưu âm thanh được chọn vào SharedPreferences
                    SharedPreferences sharedPref = getSharedPreferences("WaterPrefs", MODE_PRIVATE);
                    sharedPref.edit().putString("reminderSound", item.getSoundId()).apply();


                    // Cập nhật âm thanh trên Firebase
                    String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                            ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                            : null;
                    if (userId != null) {
                        reminderRepository.updateAllRemindersSound(userId, item.getSoundId(), task -> {
                            if (task.isSuccessful()) {
                                Log.d("SoundSelectionActivity", "Sound updated successfully on Firebase");
                            } else {
                                Log.e("SoundSelectionActivity", "Error updating sound on Firebase", task.getException());
                            }
                        });
                    } else {
                        Log.e("SoundSelectionActivity", "User not logged in");
                    }


                    // Dừng và giải phóng âm thanh hiện tại nếu đang phát
                    if (mediaPlayer != null) {
                        mediaPlayer.stop();
                        mediaPlayer.release();
                        mediaPlayer = null;
                    }


                    // Phát âm thanh mới
                    try {
                        int soundResId = getResources().getIdentifier(item.getSoundId(), "raw", getPackageName());
                        if (soundResId != 0) {
                            mediaPlayer = MediaPlayer.create(SoundSelectionActivity.this, soundResId);
                            mediaPlayer.start();
                        } else {
                            Log.e("SoundSelectionActivity", "Sound resource not found: " + item.getSoundId());
                        }
                    } catch (Exception e) {
                        Log.e("SoundSelectionActivity", "Error playing sound: " + item.getSoundId(), e);
                    }
                }
            });


            return convertView;
        }
    }
}
