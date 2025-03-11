package com.example.watertrackerandroidapp.DataBase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.example.watertrackerandroidapp.DataBase.WaterTrackerContract.AccountEntry;
import com.example.watertrackerandroidapp.DataBase.WaterTrackerContract.UserEntry;
import com.example.watertrackerandroidapp.DataBase.WaterTrackerContract.ReminderEntry;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class WaterTrackerDao {

    private static final String TAG = "WaterTrackerDao";
    private final WaterTrackerDbHelper dbHelper;

    public WaterTrackerDao(Context context) {
        dbHelper = new WaterTrackerDbHelper(context);
    }

    /**
     * Kiểm tra đăng nhập
     * @param identifier Email hoặc số điện thoại
     * @param password Mật khẩu
     * @return AccountID nếu đăng nhập thành công, null nếu thất bại
     */
    public String checkLogin(String identifier, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String accountId = null;

        try {
            // Kiểm tra xem identifier là email hay số điện thoại
            String selection = "(" + AccountEntry.COLUMN_EMAIL + " = ? OR " +
                    AccountEntry.COLUMN_PHONE + " = ?) AND " +
                    AccountEntry.COLUMN_PASSWORD + " = ? AND " +
                    AccountEntry.COLUMN_ACTIVE + " = 1";
            String[] selectionArgs = {identifier, identifier, password};

            Cursor cursor = db.query(
                    AccountEntry.TABLE_NAME,
                    new String[]{AccountEntry.COLUMN_ACCOUNT_ID},
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            );

            if (cursor != null && cursor.moveToFirst()) {
                accountId = cursor.getString(cursor.getColumnIndexOrThrow(AccountEntry.COLUMN_ACCOUNT_ID));
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking login: " + e.getMessage());
        }

        return accountId;
    }

    /**
     * Kiểm tra đăng nhập bằng email
     * @param email Email
     * @param password Mật khẩu
     * @return AccountID nếu đăng nhập thành công, null nếu thất bại
     */
    public String checkLoginByEmail(String email, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String accountId = null;

        try {
            String selection = AccountEntry.COLUMN_EMAIL + " = ? AND " +
                    AccountEntry.COLUMN_PASSWORD + " = ? AND " +
                    AccountEntry.COLUMN_ACTIVE + " = 1";
            String[] selectionArgs = {email, password};

            Cursor cursor = db.query(
                    AccountEntry.TABLE_NAME,
                    new String[]{AccountEntry.COLUMN_ACCOUNT_ID},
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            );

            if (cursor != null && cursor.moveToFirst()) {
                accountId = cursor.getString(cursor.getColumnIndexOrThrow(AccountEntry.COLUMN_ACCOUNT_ID));
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking login by email: " + e.getMessage());
        }

        return accountId;
    }

    /**
     * Kiểm tra đăng nhập bằng số điện thoại
     * @param phone Số điện thoại
     * @param password Mật khẩu
     * @return AccountID nếu đăng nhập thành công, null nếu thất bại
     */
    public String checkLoginByPhone(String phone, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String accountId = null;

        try {
            String selection = AccountEntry.COLUMN_PHONE + " = ? AND " +
                    AccountEntry.COLUMN_PASSWORD + " = ? AND " +
                    AccountEntry.COLUMN_ACTIVE + " = 1";
            String[] selectionArgs = {phone, password};

            Cursor cursor = db.query(
                    AccountEntry.TABLE_NAME,
                    new String[]{AccountEntry.COLUMN_ACCOUNT_ID},
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            );

            if (cursor != null && cursor.moveToFirst()) {
                accountId = cursor.getString(cursor.getColumnIndexOrThrow(AccountEntry.COLUMN_ACCOUNT_ID));
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking login by phone: " + e.getMessage());
        }

        return accountId;
    }

    /**
     * Kiểm tra xem email hoặc số điện thoại đã tồn tại chưa
     * @param email Email cần kiểm tra
     * @param phone Số điện thoại cần kiểm tra
     * @return true nếu đã tồn tại, false nếu chưa
     */
    public boolean isAccountExists(String email, String phone) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        boolean exists = false;

        try {
            String selection = AccountEntry.COLUMN_EMAIL + " = ? OR " + AccountEntry.COLUMN_PHONE + " = ?";
            String[] selectionArgs = {email, phone};

            Cursor cursor = db.query(
                    AccountEntry.TABLE_NAME,
                    new String[]{AccountEntry.COLUMN_ACCOUNT_ID},
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            );

            if (cursor != null) {
                exists = cursor.getCount() > 0;
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking account existence: " + e.getMessage());
        }

        return exists;
    }

    /**
     * Lấy ID tài khoản dựa trên email hoặc số điện thoại
     * @param identifier Email hoặc số điện thoại
     * @return AccountID nếu tìm thấy, null nếu không
     */
    public String getAccountIdByIdentifier(String identifier) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String accountId = null;

        try {
            String selection = AccountEntry.COLUMN_EMAIL + " = ? OR " + AccountEntry.COLUMN_PHONE + " = ?";
            String[] selectionArgs = {identifier, identifier};

            Cursor cursor = db.query(
                    AccountEntry.TABLE_NAME,
                    new String[]{AccountEntry.COLUMN_ACCOUNT_ID},
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            );

            if (cursor != null && cursor.moveToFirst()) {
                accountId = cursor.getString(cursor.getColumnIndexOrThrow(AccountEntry.COLUMN_ACCOUNT_ID));
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting account ID: " + e.getMessage());
        }

        return accountId;
    }

    /**
     * Cập nhật mật khẩu
     * @param identifier Email hoặc số điện thoại
     * @param newPassword Mật khẩu mới
     * @return true nếu cập nhật thành công, false nếu thất bại
     */
    public boolean updatePassword(String identifier, String newPassword) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        boolean success = false;

        try {
            ContentValues values = new ContentValues();
            values.put(AccountEntry.COLUMN_PASSWORD, newPassword);

            String selection = AccountEntry.COLUMN_EMAIL + " = ? OR " + AccountEntry.COLUMN_PHONE + " = ?";
            String[] selectionArgs = {identifier, identifier};

            int count = db.update(
                    AccountEntry.TABLE_NAME,
                    values,
                    selection,
                    selectionArgs
            );

            success = count > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error updating password: " + e.getMessage());
        }

        return success;
    }

    /**
     * Tạo tài khoản mới
     * @param email Email (có thể null nếu đăng ký bằng số điện thoại)
     * @param phone Số điện thoại (có thể null nếu đăng ký bằng email)
     * @param password Mật khẩu
     * @return AccountID nếu tạo thành công, null nếu thất bại
     */
    public String createAccount(String email, String phone, String password) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String accountId = null;

        try {
            // Kiểm tra xem email hoặc phone có giá trị không
            if ((email == null || email.isEmpty()) && (phone == null || phone.isEmpty())) {
                Log.e(TAG, "Both email and phone are empty");
                return null;
            }

            // Kiểm tra tài khoản đã tồn tại chưa
            if (isAccountExists(email, phone)) {
                Log.e(TAG, "Account already exists with email: " + email + ", phone: " + phone);
                return null;
            }

            // Tạo AccountID mới
            String newAccountId = generateNewAccountId();

            // Lấy ngày hiện tại
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String currentDate = dateFormat.format(new Date());

            // Xác định loại tài khoản (email hoặc phone)
            String accountType = (email != null && !email.isEmpty()) ? "email" : "phone";

            ContentValues accountValues = new ContentValues();
            accountValues.put(AccountEntry.COLUMN_ACCOUNT_ID, newAccountId);
            accountValues.put(AccountEntry.COLUMN_EMAIL, email);
            accountValues.put(AccountEntry.COLUMN_PHONE, phone);
            accountValues.put(AccountEntry.COLUMN_PASSWORD, password);
            accountValues.put(AccountEntry.COLUMN_TYPE, accountType);
            accountValues.put(AccountEntry.COLUMN_ACTIVE, 1);
            accountValues.put(AccountEntry.COLUMN_CREATED_DATE, currentDate);

            long accountRowId = db.insert(AccountEntry.TABLE_NAME, null, accountValues);

            if (accountRowId != -1) {
                // Tạo UserID mới
                String newUserId = generateNewUserId();

                ContentValues userValues = new ContentValues();
                userValues.put(UserEntry.COLUMN_USER_ID, newUserId);
                userValues.put(UserEntry.COLUMN_ACCOUNT_ID, newAccountId);

                long userRowId = db.insert(UserEntry.TABLE_NAME, null, userValues);

                if (userRowId != -1) {
                    accountId = newAccountId;
                    Log.d(TAG, "Account created successfully: " + accountId + ", userId: " + newUserId);
                } else {
                    Log.e(TAG, "Failed to create user record");
                    // Xóa tài khoản đã tạo vì không thể tạo user
                    db.delete(AccountEntry.TABLE_NAME, AccountEntry.COLUMN_ACCOUNT_ID + " = ?", new String[]{newAccountId});
                }
            } else {
                Log.e(TAG, "Failed to create account record");
            }
        } catch (SQLiteException e) {
            Log.e(TAG, "Error creating account: " + e.getMessage());
        }

        return accountId;
    }

    /**
     * Tạo AccountID mới
     * @return AccountID mới
     */
    private String generateNewAccountId() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        int maxId = 0;

        try {
            Cursor cursor = db.rawQuery("SELECT MAX(" + AccountEntry._ID + ") FROM " + AccountEntry.TABLE_NAME, null);

            if (cursor != null && cursor.moveToFirst()) {
                maxId = cursor.getInt(0);
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error generating account ID: " + e.getMessage());
        }

        return "ACC" + (maxId + 1);
    }

    /**
     * Tạo UserID mới
     * @return UserID mới với định dạng USER1, USER2, ...
     */
    private String generateNewUserId() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        int maxId = 0;

        try {
            Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + UserEntry.TABLE_NAME, null);

            if (cursor != null && cursor.moveToFirst()) {
                maxId = cursor.getInt(0);
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error generating user ID: " + e.getMessage());
        }

        return "USER" + (maxId + 1);
    }

    /**
     * Lấy thông tin người dùng dựa trên AccountID
     * @param accountId AccountID
     * @return UserID nếu tìm thấy, null nếu không
     */
    public String getUserIdByAccountId(String accountId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String userId = null;

        try {
            String selection = UserEntry.COLUMN_ACCOUNT_ID + " = ?";
            String[] selectionArgs = {accountId};

            Cursor cursor = db.query(
                    UserEntry.TABLE_NAME,
                    new String[]{UserEntry.COLUMN_USER_ID},
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            );

            if (cursor != null && cursor.moveToFirst()) {
                userId = cursor.getString(cursor.getColumnIndexOrThrow(UserEntry.COLUMN_USER_ID));
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting user ID: " + e.getMessage());
        }

        return userId;
    }

    /**
     * Kiểm tra xem người dùng đã nhập thông tin cá nhân chưa
     * @param userId ID người dùng
     * @return true nếu là lần đầu đăng nhập (chưa có thông tin), false nếu đã có thông tin
     */
    public boolean isFirstTimeLogin(String userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        boolean isFirstTime = true;

        try {
            String selection = UserEntry.COLUMN_USER_ID + " = ? AND " +
                    UserEntry.COLUMN_FULL_NAME + " IS NOT NULL";
            String[] selectionArgs = {userId};

            Cursor cursor = db.query(
                    UserEntry.TABLE_NAME,
                    null,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            );

            if (cursor != null) {
                isFirstTime = cursor.getCount() == 0;
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking first time login: " + e.getMessage());
        }

        return isFirstTime;
    }

    /**
     * Cập nhật thông tin người dùng
     * @param userId ID người dùng
     * @param fullName Họ tên đầy đủ
     * @param gender Giới tính
     * @param weight Cân nặng
     * @param height Chiều cao
     * @param age Tuổi
     * @param dailyTarget Mục tiêu uống nước hàng ngày (ml)
     * @param wakeTime Thời gian thức dậy
     * @param sleepTime Thời gian đi ngủ
     * @return true nếu cập nhật thành công, false nếu thất bại
     */
    public boolean updateUserInfo(String userId, String fullName, String gender,
                                  float weight, float height, int age,
                                  int dailyTarget, String wakeTime, String sleepTime) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        boolean success = false;

        try {
            ContentValues values = new ContentValues();
            values.put(UserEntry.COLUMN_FULL_NAME, fullName);
            values.put(UserEntry.COLUMN_GENDER, gender);
            values.put(UserEntry.COLUMN_WEIGHT, weight);
            values.put(UserEntry.COLUMN_AGE, age);
            values.put(UserEntry.COLUMN_DAILY_TARGET, dailyTarget);
            values.put(UserEntry.COLUMN_WAKE_TIME, wakeTime);
            values.put(UserEntry.COLUMN_SLEEP_TIME, sleepTime);

            String selection = UserEntry.COLUMN_USER_ID + " = ?";
            String[] selectionArgs = {userId};

            int count = db.update(
                    UserEntry.TABLE_NAME,
                    values,
                    selection,
                    selectionArgs
            );

            success = count > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error updating user info: " + e.getMessage());
        }

        return success;
    }

    /**
     * Tạo lịch nhắc nhở uống nước
     * @param userId ID người dùng
     * @param reminderTime Thời gian nhắc nhở
     * @return true nếu tạo thành công, false nếu thất bại
     */
    public boolean createReminder(String userId, String reminderTime) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        boolean success = false;

        try {
            // Kiểm tra xem reminder đã tồn tại chưa
            String selection = ReminderEntry.COLUMN_USER_ID + " = ? AND " +
                    ReminderEntry.COLUMN_REMINDER_TIME + " = ?";
            String[] selectionArgs = {userId, reminderTime};

            Cursor cursor = db.query(
                    ReminderEntry.TABLE_NAME,
                    null,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            );

            if (cursor != null && cursor.getCount() > 0) {
                // Reminder đã tồn tại
                cursor.close();
                return true;
            }

            if (cursor != null) {
                cursor.close();
            }

            // Tạo ReminderID mới
            String reminderId = "REM" + System.currentTimeMillis();

            ContentValues values = new ContentValues();
            values.put(ReminderEntry.COLUMN_REMINDER_ID, reminderId);
            values.put(ReminderEntry.COLUMN_USER_ID, userId);
            values.put(ReminderEntry.COLUMN_REMINDER_TIME, reminderTime);
            values.put(ReminderEntry.COLUMN_SOUND, "default");
            values.put(ReminderEntry.COLUMN_ACTIVE, 1);
            values.put(ReminderEntry.COLUMN_DAYS, "all");

            long rowId = db.insert(ReminderEntry.TABLE_NAME, null, values);
            success = rowId != -1;
        } catch (Exception e) {
            Log.e(TAG, "Error creating reminder: " + e.getMessage());
        }

        return success;
    }

    /**
     * Xóa tất cả lịch nhắc nhở của người dùng
     * @param userId ID người dùng
     * @return true nếu xóa thành công, false nếu thất bại
     */
    public boolean deleteAllReminders(String userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        boolean success = false;

        try {
            String selection = ReminderEntry.COLUMN_USER_ID + " = ?";
            String[] selectionArgs = {userId};

            int count = db.delete(
                    ReminderEntry.TABLE_NAME,
                    selection,
                    selectionArgs
            );

            success = count >= 0; // Thành công ngay cả khi không có reminder nào bị xóa
        } catch (Exception e) {
            Log.e(TAG, "Error deleting reminders: " + e.getMessage());
        }

        return success;
    }

    /**
     * Đóng kết nối database
     */
    public void close() {
        dbHelper.close();
    }
}