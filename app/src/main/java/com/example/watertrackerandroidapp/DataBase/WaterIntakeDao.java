package com.example.watertrackerandroidapp.DataBase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.watertrackerandroidapp.Models.WaterIntake;
import com.example.watertrackerandroidapp.DataBase.WaterTrackerContract.WaterIntakeEntry;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WaterIntakeDao {
    private static final String TAG = "WaterIntakeDao";
    private final WaterTrackerDbHelper dbHelper;

    public WaterIntakeDao(Context context) {
        dbHelper = new WaterTrackerDbHelper(context);
    }

    /**
     * Thêm một lần uống nước mới
     * @param userId ID người dùng
     * @param amount Lượng nước (ml)
     * @param drinkType Loại đồ uống (mặc định là "Water")
     * @return ID của bản ghi nếu thành công, null nếu thất bại
     */
    public String addWaterIntake(String userId, int amount, String drinkType) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String intakeId = null;

        try {
            // Tạo IntakeID mới
            String newIntakeId = "INTAKE" + System.currentTimeMillis();

            // Lấy thời gian hiện tại
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String currentTime = timeFormat.format(new Date());
            String currentDate = dateFormat.format(new Date());

            ContentValues values = new ContentValues();
            values.put(WaterIntakeEntry.COLUMN_INTAKE_ID, newIntakeId);
            values.put(WaterIntakeEntry.COLUMN_USER_ID, userId);
            values.put(WaterIntakeEntry.COLUMN_AMOUNT, amount);
            values.put(WaterIntakeEntry.COLUMN_DRINK_TYPE, drinkType);
            values.put(WaterIntakeEntry.COLUMN_INTAKE_TIME, currentTime);
            values.put(WaterIntakeEntry.COLUMN_INTAKE_DATE, currentDate);

            long rowId = db.insert(WaterIntakeEntry.TABLE_NAME, null, values);

            if (rowId != -1) {
                intakeId = newIntakeId;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error adding water intake: " + e.getMessage());
        }

        return intakeId;
    }

    /**
     * Lấy tổng lượng nước đã uống trong ngày
     * @param userId ID người dùng
     * @param date Ngày cần kiểm tra (định dạng yyyy-MM-dd)
     * @return Tổng lượng nước (ml)
     */
    public int getTotalIntakeForDay(String userId, String date) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        int totalIntake = 0;

        try {
            String selection = WaterIntakeEntry.COLUMN_USER_ID + " = ? AND " +
                    WaterIntakeEntry.COLUMN_INTAKE_DATE + " = ?";
            String[] selectionArgs = {userId, date};

            Cursor cursor = db.query(
                    WaterIntakeEntry.TABLE_NAME,
                    new String[]{"SUM(" + WaterIntakeEntry.COLUMN_AMOUNT + ")"},
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            );

            if (cursor != null && cursor.moveToFirst()) {
                totalIntake = cursor.getInt(0);
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting total intake: " + e.getMessage());
        }

        return totalIntake;
    }

    /**
     * Lấy danh sách các lần uống nước trong ngày
     * @param userId ID người dùng
     * @param date Ngày cần kiểm tra (định dạng yyyy-MM-dd)
     * @return Danh sách các lần uống nước
     */
    public List<WaterIntake> getIntakeHistoryForDay(String userId, String date) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<WaterIntake> intakeList = new ArrayList<>();

        try {
            String selection = WaterIntakeEntry.COLUMN_USER_ID + " = ? AND " +
                    WaterIntakeEntry.COLUMN_INTAKE_DATE + " = ?";
            String[] selectionArgs = {userId, date};
            String orderBy = WaterIntakeEntry.COLUMN_INTAKE_TIME + " DESC";

            Cursor cursor = db.query(
                    WaterIntakeEntry.TABLE_NAME,
                    null,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    orderBy
            );

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    WaterIntake intake = new WaterIntake();
                    intake.setIntakeId(cursor.getString(cursor.getColumnIndexOrThrow(WaterIntakeEntry.COLUMN_INTAKE_ID)));
                    intake.setUserId(cursor.getString(cursor.getColumnIndexOrThrow(WaterIntakeEntry.COLUMN_USER_ID)));
                    intake.setAmount(cursor.getInt(cursor.getColumnIndexOrThrow(WaterIntakeEntry.COLUMN_AMOUNT)));
                    intake.setDrinkType(cursor.getString(cursor.getColumnIndexOrThrow(WaterIntakeEntry.COLUMN_DRINK_TYPE)));
                    intake.setIntakeTime(cursor.getString(cursor.getColumnIndexOrThrow(WaterIntakeEntry.COLUMN_INTAKE_TIME)));
                    intake.setIntakeDate(cursor.getString(cursor.getColumnIndexOrThrow(WaterIntakeEntry.COLUMN_INTAKE_DATE)));
                    intake.setScheduled(false);

                    intakeList.add(intake);
                } while (cursor.moveToNext());

                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting intake history: " + e.getMessage());
        }

        return intakeList;
    }

    /**
     * Đóng kết nối database
     */
    public void close() {
        dbHelper.close();
    }
}

