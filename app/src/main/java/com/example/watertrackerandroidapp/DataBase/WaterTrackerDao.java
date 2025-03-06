package com.example.watertrackerandroidapp.DataBase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.watertrackerandroidapp.DataBase.WaterTrackerContract.AccountEntry;
import com.example.watertrackerandroidapp.DataBase.WaterTrackerContract.UserEntry;
import com.example.watertrackerandroidapp.DataBase.WaterTrackerContract.WaterIntakeEntry;
import com.example.watertrackerandroidapp.DataBase.WaterTrackerContract.ReminderEntry;
import com.example.watertrackerandroidapp.model.Account;
import com.example.watertrackerandroidapp.model.User;
import com.example.watertrackerandroidapp.model.WaterIntake;
import com.example.watertrackerandroidapp.model.Reminder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class WaterTrackerDao {
    private static final String TAG = "WaterTrackerDao";

    private SQLiteDatabase database;
    private WaterTrackerDbHelper dbHelper;

    public WaterTrackerDao(Context context) {
        dbHelper = new WaterTrackerDbHelper(context);
    }

    // Mở kết nối đến database
    public void open() {
        database = dbHelper.getWritableDatabase();
        Log.d(TAG, "Database opened");
    }

    // Đóng kết nối đến database
    public void close() {
        dbHelper.close();
        Log.d(TAG, "Database closed");
    }

    // ===== ACCOUNT OPERATIONS =====

    // Thêm tài khoản mới
    public long insertAccount(Account account) {
        ContentValues values = new ContentValues();
        values.put(AccountEntry.COLUMN_USER_ID, account.getUserId());
        values.put(AccountEntry.COLUMN_PHONE_NUMBER, account.getPhoneNumber());
        values.put(AccountEntry.COLUMN_EMAIL, account.getEmail());
        values.put(AccountEntry.COLUMN_PASSWORD, account.getPassword());

        long newRowId = database.insert(AccountEntry.TABLE_NAME, null, values);
        Log.d(TAG, "Inserted account with ID: " + newRowId);
        return newRowId;
    }

    // Kiểm tra đăng nhập
    public Account checkLogin(String identifier, String password) {
        String selection = "(" + AccountEntry.COLUMN_USER_ID + " = ? OR " +
                AccountEntry.COLUMN_PHONE_NUMBER + " = ? OR " +
                AccountEntry.COLUMN_EMAIL + " = ?) AND " +
                AccountEntry.COLUMN_PASSWORD + " = ?";
        String[] selectionArgs = { identifier, identifier, identifier, password };

        Cursor cursor = database.query(
                AccountEntry.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        Account account = null;

        try {
            if (cursor.moveToFirst()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(AccountEntry._ID));
                String userId = cursor.getString(cursor.getColumnIndexOrThrow(AccountEntry.COLUMN_USER_ID));
                String phoneNumber = cursor.getString(cursor.getColumnIndexOrThrow(AccountEntry.COLUMN_PHONE_NUMBER));
                String email = cursor.getString(cursor.getColumnIndexOrThrow(AccountEntry.COLUMN_EMAIL));
                String pwd = cursor.getString(cursor.getColumnIndexOrThrow(AccountEntry.COLUMN_PASSWORD));
                long createAt = cursor.getLong(cursor.getColumnIndexOrThrow(AccountEntry.COLUMN_CREATE_AT));

                account = new Account(id, userId, phoneNumber, email, pwd, createAt);
            }
        } finally {
            cursor.close();
        }

        return account;
    }

    // Kiểm tra tài khoản tồn tại
    public boolean isAccountExists(String userId, String phoneNumber, String email) {
        String selection = AccountEntry.COLUMN_USER_ID + " = ? OR " +
                AccountEntry.COLUMN_PHONE_NUMBER + " = ? OR " +
                AccountEntry.COLUMN_EMAIL + " = ?";
        String[] selectionArgs = { userId, phoneNumber, email };

        Cursor cursor = database.query(
                AccountEntry.TABLE_NAME,
                new String[] { AccountEntry._ID },
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        boolean exists = cursor.getCount() > 0;
        cursor.close();

        return exists;
    }

    // ===== USER OPERATIONS =====

    // Thêm người dùng mới
    public long insertUser(User user) {
        ContentValues values = new ContentValues();
        values.put(UserEntry.COLUMN_USER_ID, user.getUserId());
        values.put(UserEntry.COLUMN_NAME, user.getName());
        values.put(UserEntry.COLUMN_SEX, user.getSex());
        values.put(UserEntry.COLUMN_WEIGHT, user.getWeight());
        values.put(UserEntry.COLUMN_AGE, user.getAge());
        values.put(UserEntry.COLUMN_SLEEP_TIME, user.getSleepTime());
        values.put(UserEntry.COLUMN_WAKE_TIME, user.getWakeTime());
        values.put(UserEntry.COLUMN_DAILY_GOAL, user.getDailyGoal());

        long newRowId = database.insert(UserEntry.TABLE_NAME, null, values);
        Log.d(TAG, "Inserted user with ID: " + user.getUserId());
        return newRowId;
    }

    // Lấy thông tin người dùng
    public User getUser(String userId) {
        String selection = UserEntry.COLUMN_USER_ID + " = ?";
        String[] selectionArgs = { userId };

        Cursor cursor = database.query(
                UserEntry.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        User user = null;

        try {
            if (cursor.moveToFirst()) {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(UserEntry.COLUMN_NAME));
                String sex = cursor.getString(cursor.getColumnIndexOrThrow(UserEntry.COLUMN_SEX));
                float weight = cursor.getFloat(cursor.getColumnIndexOrThrow(UserEntry.COLUMN_WEIGHT));
                int age = cursor.getInt(cursor.getColumnIndexOrThrow(UserEntry.COLUMN_AGE));
                String sleepTime = cursor.getString(cursor.getColumnIndexOrThrow(UserEntry.COLUMN_SLEEP_TIME));
                String wakeTime = cursor.getString(cursor.getColumnIndexOrThrow(UserEntry.COLUMN_WAKE_TIME));
                float dailyGoal = cursor.getFloat(cursor.getColumnIndexOrThrow(UserEntry.COLUMN_DAILY_GOAL));

                user = new User(userId, name, sex, weight, age, sleepTime, wakeTime, dailyGoal);
            }
        } finally {
            cursor.close();
        }

        return user;
    }

    // Cập nhật thông tin người dùng
    public int updateUser(User user) {
        ContentValues values = new ContentValues();
        values.put(UserEntry.COLUMN_NAME, user.getName());
        values.put(UserEntry.COLUMN_SEX, user.getSex());
        values.put(UserEntry.COLUMN_WEIGHT, user.getWeight());
        values.put(UserEntry.COLUMN_AGE, user.getAge());
        values.put(UserEntry.COLUMN_SLEEP_TIME, user.getSleepTime());
        values.put(UserEntry.COLUMN_WAKE_TIME, user.getWakeTime());
        values.put(UserEntry.COLUMN_DAILY_GOAL, user.getDailyGoal());

        String selection = UserEntry.COLUMN_USER_ID + " = ?";
        String[] selectionArgs = { user.getUserId() };

        int count = database.update(
                UserEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs
        );

        Log.d(TAG, "Updated user with ID: " + user.getUserId() + ", rows affected: " + count);
        return count;
    }

    // ===== WATER INTAKE OPERATIONS =====

    // Thêm bản ghi nước mới
    public long insertWaterIntake(WaterIntake waterIntake) {
        ContentValues values = new ContentValues();
        values.put(WaterIntakeEntry.COLUMN_USER_ID, waterIntake.getUserId());
        values.put(WaterIntakeEntry.COLUMN_AMOUNT, waterIntake.getAmount());
        values.put(WaterIntakeEntry.COLUMN_WATER_TIME, waterIntake.getWaterTime());

        long newRowId = database.insert(WaterIntakeEntry.TABLE_NAME, null, values);
        Log.d(TAG, "Inserted water intake with ID: " + newRowId);
        return newRowId;
    }

    // Lấy tất cả bản ghi nước của người dùng
    public List<WaterIntake> getAllWaterIntakes(String userId) {
        List<WaterIntake> waterIntakes = new ArrayList<>();

        String selection = WaterIntakeEntry.COLUMN_USER_ID + " = ?";
        String[] selectionArgs = { userId };
        String sortOrder = WaterIntakeEntry.COLUMN_WATER_TIME + " DESC";

        Cursor cursor = database.query(
                WaterIntakeEntry.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );

        try {
            int idColumnIndex = cursor.getColumnIndexOrThrow(WaterIntakeEntry._ID);
            int userIdColumnIndex = cursor.getColumnIndexOrThrow(WaterIntakeEntry.COLUMN_USER_ID);
            int amountColumnIndex = cursor.getColumnIndexOrThrow(WaterIntakeEntry.COLUMN_AMOUNT);
            int waterTimeColumnIndex = cursor.getColumnIndexOrThrow(WaterIntakeEntry.COLUMN_WATER_TIME);

            while (cursor.moveToNext()) {
                long id = cursor.getLong(idColumnIndex);
                String uid = cursor.getString(userIdColumnIndex);
                float amount = cursor.getFloat(amountColumnIndex);
                long waterTime = cursor.getLong(waterTimeColumnIndex);

                WaterIntake waterIntake = new WaterIntake(id, uid, amount, waterTime);
                waterIntakes.add(waterIntake);
            }
        } finally {
            cursor.close();
        }

        return waterIntakes;
    }

    // Lấy bản ghi nước trong một ngày cụ thể
    public List<WaterIntake> getWaterIntakesForDay(String userId, long dayTimestamp) {
        List<WaterIntake> waterIntakes = new ArrayList<>();

        // Tính thời gian bắt đầu và kết thúc của ngày
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(dayTimestamp);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startOfDay = calendar.getTimeInMillis();

        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND,  23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        long endOfDay = calendar.getTimeInMillis();

        String selection = WaterIntakeEntry.COLUMN_USER_ID + " = ? AND " +
                WaterIntakeEntry.COLUMN_WATER_TIME + " BETWEEN ? AND ?";
        String[] selectionArgs = { userId, String.valueOf(startOfDay), String.valueOf(endOfDay) };
        String sortOrder = WaterIntakeEntry.COLUMN_WATER_TIME + " DESC";

        Cursor cursor = database.query(
                WaterIntakeEntry.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );

        try {
            int idColumnIndex = cursor.getColumnIndexOrThrow(WaterIntakeEntry._ID);
            int userIdColumnIndex = cursor.getColumnIndexOrThrow(WaterIntakeEntry.COLUMN_USER_ID);
            int amountColumnIndex = cursor.getColumnIndexOrThrow(WaterIntakeEntry.COLUMN_AMOUNT);
            int waterTimeColumnIndex = cursor.getColumnIndexOrThrow(WaterIntakeEntry.COLUMN_WATER_TIME);

            while (cursor.moveToNext()) {
                long id = cursor.getLong(idColumnIndex);
                String uid = cursor.getString(userIdColumnIndex);
                float amount = cursor.getFloat(amountColumnIndex);
                long waterTime = cursor.getLong(waterTimeColumnIndex);

                WaterIntake waterIntake = new WaterIntake(id, uid, amount, waterTime);
                waterIntakes.add(waterIntake);
            }
        } finally {
            cursor.close();
        }

        return waterIntakes;
    }

    // Tính tổng lượng nước đã uống trong một ngày
    public float getTotalWaterForDay(String userId, long dayTimestamp) {
        // Tính thời gian bắt đầu và kết thúc của ngày
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(dayTimestamp);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startOfDay = calendar.getTimeInMillis();

        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        long endOfDay = calendar.getTimeInMillis();

        String[] projection = { "SUM(" + WaterIntakeEntry.COLUMN_AMOUNT + ")" };

        String selection = WaterIntakeEntry.COLUMN_USER_ID + " = ? AND " +
                WaterIntakeEntry.COLUMN_WATER_TIME + " BETWEEN ? AND ?";
        String[] selectionArgs = { userId, String.valueOf(startOfDay), String.valueOf(endOfDay) };

        Cursor cursor = database.query(
                WaterIntakeEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        float totalAmount = 0;

        try {
            if (cursor.moveToFirst()) {
                totalAmount = cursor.getFloat(0);
            }
        } finally {
            cursor.close();
        }

        return totalAmount;
    }

    // Xóa bản ghi nước
    public int deleteWaterIntake(long id) {
        String selection = WaterIntakeEntry._ID + " = ?";
        String[] selectionArgs = { String.valueOf(id) };

        int deletedRows = database.delete(
                WaterIntakeEntry.TABLE_NAME,
                selection,
                selectionArgs
        );

        Log.d(TAG, "Deleted water intake with ID: " + id + ", rows affected: " + deletedRows);
        return deletedRows;
    }

    // ===== REMINDER OPERATIONS =====

    // Thêm nhắc nhở mới
    public long insertReminder(Reminder reminder) {
        ContentValues values = new ContentValues();
        values.put(ReminderEntry.COLUMN_USER_ID, reminder.getUserId());
        values.put(ReminderEntry.COLUMN_REMINDER_TIME, reminder.getReminderTime());
        values.put(ReminderEntry.COLUMN_STATUS, reminder.isStatus() ? 1 : 0);
        values.put(ReminderEntry.COLUMN_ALARM_SOUND, reminder.getAlarmSound());

        long newRowId = database.insert(ReminderEntry.TABLE_NAME, null, values);
        Log.d(TAG, "Inserted reminder with ID: " + newRowId);
        return newRowId;
    }

    // Lấy tất cả nhắc nhở của người dùng
    public List<Reminder> getAllReminders(String userId) {
        List<Reminder> reminders = new ArrayList<>();

        String selection = ReminderEntry.COLUMN_USER_ID + " = ?";
        String[] selectionArgs = { userId };
        String sortOrder = ReminderEntry.COLUMN_REMINDER_TIME + " ASC";

        Cursor cursor = database.query(
                ReminderEntry.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );

        try {
            int idColumnIndex = cursor.getColumnIndexOrThrow(ReminderEntry._ID);
            int userIdColumnIndex = cursor.getColumnIndexOrThrow(ReminderEntry.COLUMN_USER_ID);
            int reminderTimeColumnIndex = cursor.getColumnIndexOrThrow(ReminderEntry.COLUMN_REMINDER_TIME);
            int statusColumnIndex = cursor.getColumnIndexOrThrow(ReminderEntry.COLUMN_STATUS);
            int alarmSoundColumnIndex = cursor.getColumnIndexOrThrow(ReminderEntry.COLUMN_ALARM_SOUND);

            while (cursor.moveToNext()) {
                long id = cursor.getLong(idColumnIndex);
                String uid = cursor.getString(userIdColumnIndex);
                String reminderTime = cursor.getString(reminderTimeColumnIndex);
                boolean status = cursor.getInt(statusColumnIndex) == 1;
                String alarmSound = cursor.getString(alarmSoundColumnIndex);

                Reminder reminder = new Reminder(id, uid, reminderTime, status, alarmSound);
                reminders.add(reminder);
            }
        } finally {
            cursor.close();
        }

        return reminders;
    }

    // Cập nhật nhắc nhở
    public int updateReminder(Reminder reminder) {
        ContentValues values = new ContentValues();
        values.put(ReminderEntry.COLUMN_REMINDER_TIME, reminder.getReminderTime());
        values.put(ReminderEntry.COLUMN_STATUS, reminder.isStatus() ? 1 : 0);
        values.put(ReminderEntry.COLUMN_ALARM_SOUND, reminder.getAlarmSound());

        String selection = ReminderEntry._ID + " = ?";
        String[] selectionArgs = { String.valueOf(reminder.getId()) };

        int count = database.update(
                ReminderEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs
        );

        Log.d(TAG, "Updated reminder with ID: " + reminder.getId() + ", rows affected: " + count);
        return count;
    }

    // Xóa nhắc nhở
    public int deleteReminder(long id) {
        String selection = ReminderEntry._ID + " = ?";
        String[] selectionArgs = { String.valueOf(id) };

        int deletedRows = database.delete(
                ReminderEntry.TABLE_NAME,
                selection,
                selectionArgs
        );

        Log.d(TAG, "Deleted reminder with ID: " + id + ", rows affected: " + deletedRows);
        return deletedRows;
    }
}