package com.example.watertrackerandroidapp.DataBase;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.watertrackerandroidapp.DataBase.WaterTrackerContract.AccountEntry;
import com.example.watertrackerandroidapp.DataBase.WaterTrackerContract.UserEntry;
import com.example.watertrackerandroidapp.DataBase.WaterTrackerContract.WaterIntakeEntry;
import com.example.watertrackerandroidapp.DataBase.WaterTrackerContract.ReminderEntry;

public class WaterTrackerDbHelper extends SQLiteOpenHelper {
    private static final String TAG = "WaterTrackerDbHelper";

    // Nếu bạn thay đổi schema, bạn phải tăng phiên bản database
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "WaterTracker.db";

    // SQL tạo bảng ACCOUNT
    private static final String SQL_CREATE_ACCOUNT =
            "CREATE TABLE " + AccountEntry.TABLE_NAME + " (" +
                    AccountEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    AccountEntry.COLUMN_USER_ID + " TEXT UNIQUE," +
                    AccountEntry.COLUMN_PHONE_NUMBER + " TEXT UNIQUE," +
                    AccountEntry.COLUMN_EMAIL + " TEXT UNIQUE," +
                    AccountEntry.COLUMN_PASSWORD + " TEXT NOT NULL," +
                    AccountEntry.COLUMN_CREATE_AT + " INTEGER DEFAULT (strftime('%s','now') * 1000)," +
                    "CHECK (" + AccountEntry.COLUMN_PHONE_NUMBER + " LIKE '0%' AND length(" +
                    AccountEntry.COLUMN_PHONE_NUMBER + ") = 10 OR " +
                    AccountEntry.COLUMN_PHONE_NUMBER + " IS NULL)," +
                    "CHECK (" + AccountEntry.COLUMN_EMAIL + " LIKE '%@%' OR " +
                    AccountEntry.COLUMN_EMAIL + " IS NULL)," +
                    "CHECK (" + AccountEntry.COLUMN_PHONE_NUMBER + " IS NOT NULL OR " +
                    AccountEntry.COLUMN_EMAIL + " IS NOT NULL)" +
                    ")";

    // SQL tạo bảng USERS
    private static final String SQL_CREATE_USERS =
            "CREATE TABLE " + UserEntry.TABLE_NAME + " (" +
                    UserEntry.COLUMN_USER_ID + " TEXT PRIMARY KEY," +
                    UserEntry.COLUMN_NAME + " TEXT," +
                    UserEntry.COLUMN_SEX + " TEXT CHECK(" + UserEntry.COLUMN_SEX + " IN ('M','F','Other'))," +
                    UserEntry.COLUMN_WEIGHT + " REAL CHECK(" + UserEntry.COLUMN_WEIGHT + " > 0)," +
                    UserEntry.COLUMN_AGE + " INTEGER CHECK(" + UserEntry.COLUMN_AGE + " > 0)," +
                    UserEntry.COLUMN_SLEEP_TIME + " TEXT," +
                    UserEntry.COLUMN_WAKE_TIME + " TEXT," +
                    UserEntry.COLUMN_DAILY_GOAL + " REAL DEFAULT 2000 CHECK(" +
                    UserEntry.COLUMN_DAILY_GOAL + " > 0)," +
                    "FOREIGN KEY (" + UserEntry.COLUMN_USER_ID + ") REFERENCES " +
                    AccountEntry.TABLE_NAME + "(" + AccountEntry.COLUMN_USER_ID + ") ON DELETE CASCADE" +
                    ")";

    // SQL tạo bảng WATER_INTAKE
    private static final String SQL_CREATE_WATER_INTAKE =
            "CREATE TABLE " + WaterIntakeEntry.TABLE_NAME + " (" +
                    WaterIntakeEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    WaterIntakeEntry.COLUMN_USER_ID + " TEXT NOT NULL," +
                    WaterIntakeEntry.COLUMN_AMOUNT + " REAL CHECK(" + WaterIntakeEntry.COLUMN_AMOUNT + " > 0)," +
                    WaterIntakeEntry.COLUMN_WATER_TIME + " INTEGER DEFAULT (strftime('%s','now') * 1000)," +
                    "FOREIGN KEY (" + WaterIntakeEntry.COLUMN_USER_ID + ") REFERENCES " +
                    UserEntry.TABLE_NAME + "(" + UserEntry.COLUMN_USER_ID + ") ON DELETE CASCADE" +
                    ")";

    // SQL tạo bảng Reminders
    private static final String SQL_CREATE_REMINDERS =
            "CREATE TABLE " + ReminderEntry.TABLE_NAME + " (" +
                    ReminderEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    ReminderEntry.COLUMN_USER_ID + " TEXT NOT NULL," +
                    ReminderEntry.COLUMN_REMINDER_TIME + " TEXT NOT NULL," +
                    ReminderEntry.COLUMN_STATUS + " INTEGER DEFAULT 1," +
                    ReminderEntry.COLUMN_ALARM_SOUND + " TEXT DEFAULT 'Dòng nước' " +
                    "CHECK(" + ReminderEntry.COLUMN_ALARM_SOUND + " IN " +
                    "('Dòng nước', 'Cổ điển', 'Giọt nước', 'Thả tiếng vang'))," +
                    "FOREIGN KEY (" + ReminderEntry.COLUMN_USER_ID + ") REFERENCES " +
                    UserEntry.TABLE_NAME + "(" + UserEntry.COLUMN_USER_ID + ") ON DELETE CASCADE" +
                    ")";

    // SQL xóa bảng
    private static final String SQL_DELETE_ACCOUNT = "DROP TABLE IF EXISTS " + AccountEntry.TABLE_NAME;
    private static final String SQL_DELETE_USERS = "DROP TABLE IF EXISTS " + UserEntry.TABLE_NAME;
    private static final String SQL_DELETE_WATER_INTAKE = "DROP TABLE IF EXISTS " + WaterIntakeEntry.TABLE_NAME;
    private static final String SQL_DELETE_REMINDERS = "DROP TABLE IF EXISTS " + ReminderEntry.TABLE_NAME;

    // Bật hỗ trợ khóa ngoại
    private static final String PRAGMA_FOREIGN_KEYS = "PRAGMA foreign_keys = ON";

    public WaterTrackerDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Creating database tables");

        // Bật hỗ trợ khóa ngoại
        db.execSQL(PRAGMA_FOREIGN_KEYS);

        // Tạo các bảng
        db.execSQL(SQL_CREATE_ACCOUNT);
        db.execSQL(SQL_CREATE_USERS);
        db.execSQL(SQL_CREATE_WATER_INTAKE);
        db.execSQL(SQL_CREATE_REMINDERS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);

        // Xóa tất cả các bảng và tạo lại
        db.execSQL(SQL_DELETE_REMINDERS);
        db.execSQL(SQL_DELETE_WATER_INTAKE);
        db.execSQL(SQL_DELETE_USERS);
        db.execSQL(SQL_DELETE_ACCOUNT);

        onCreate(db);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        // Bật hỗ trợ khóa ngoại
        db.execSQL(PRAGMA_FOREIGN_KEYS);
    }
}