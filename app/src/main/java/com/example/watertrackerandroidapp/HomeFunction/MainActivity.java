package com.example.watertrackerandroidapp.HomeFunction; // Thay đổi theo tên package của bạn

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import com.example.watertrackerandroidapp.R;
import com.example.watertrackerandroidapp.LoginFunction.LoginActivity; // Import LoginActivity

public class MainActivity extends AppCompatActivity {
    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Tìm nút đăng xuất
        btnLogout = findViewById(R.id.btnLogout);

        // Bắt sự kiện click
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });
    }

    private void logout() {
        SharedPreferences sharedPreferences = getSharedPreferences("WaterReminderPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("accountId");
        editor.remove("userId");
        editor.putBoolean("isLoggedIn", false);
        editor.apply();

        // Chuyển về màn hình đăng nhập
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish(); // Đóng MainActivity sau khi đăng xuất
    }
}
