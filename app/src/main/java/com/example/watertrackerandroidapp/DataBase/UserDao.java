package com.example.watertrackerandroidapp.DataBase;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.watertrackerandroidapp.DataBase.WaterTrackerContract.UserEntry;

public class UserDao {
    private static final String TAG = "UserDao";
    private final WaterTrackerDbHelper dbHelper;

    public UserDao(Context context) {
        dbHelper = new WaterTrackerDbHelper(context);
    }

    /**
     * Lấy mục tiêu uống nước hàng ngày của người dùng
     * @param userId ID người dùng
     * @return Mục tiêu uống nước (ml), hoặc 0 nếu không tìm thấy
     */
    public int getDailyTarget(String userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        int dailyTarget = 0;

        try {
            String selection = UserEntry.COLUMN_USER_ID + " = ?";
            String[] selectionArgs = {userId};

            Cursor cursor = db.query(
                    UserEntry.TABLE_NAME,
                    new String[]{UserEntry.COLUMN_DAILY_TARGET},
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            );

            if (cursor != null && cursor.moveToFirst()) {
                dailyTarget = cursor.getInt(cursor.getColumnIndexOrThrow(UserEntry.COLUMN_DAILY_TARGET));
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting daily target: " + e.getMessage());
        }

        return dailyTarget;
    }

    /**
     * Đóng kết nối database
     */
    public void close() {
        dbHelper.close();
    }
}

