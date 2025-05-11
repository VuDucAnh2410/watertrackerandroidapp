package com.example.watertrackerandroidapp.LoginFunction.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesManager {

    private static final String PREF_NAME = "WaterTrackerPrefs";
    private static final String KEY_IS_FIRST_LOGIN = "isFirstLogin";

    // Ghi trạng thái login lần đầu
    public static void setFirstLogin(Context context, boolean isFirstLogin) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_IS_FIRST_LOGIN, isFirstLogin);
        editor.apply();
    }

    // Lấy trạng thái login lần đầu (mặc định là true)
    public static boolean isFirstLogin(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_IS_FIRST_LOGIN, true);
    }

    // Xóa dữ liệu nếu cần reset (tùy chọn)
    public static void clearAll(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }
}
