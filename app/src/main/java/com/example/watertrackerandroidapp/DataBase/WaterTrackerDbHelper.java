package com.example.watertrackerandroidapp.DataBase;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.watertrackerandroidapp.DataBase.WaterTrackerContract.AccountEntry;
import com.example.watertrackerandroidapp.DataBase.WaterTrackerContract.UserEntry;
import com.example.watertrackerandroidapp.DataBase.WaterTrackerContract.WaterIntakeEntry;
import com.example.watertrackerandroidapp.DataBase.WaterTrackerContract.ReminderEntry;

import android.util.Log;
public class WaterTrackerDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "watertracker.db";
    private static final int DATABASE_VERSION = 1;

    public WaterTrackerDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        Log.d("Database", "Database opened: " + db.getPath());
    }

    public void ensureDatabaseExists() {
        SQLiteDatabase db = getWritableDatabase();
        Log.d("Database", "Database created/opened at: " + db.getPath());
        db.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {


        // Create ACCOUNT table
        final String SQL_CREATE_ACCOUNT_TABLE = "CREATE TABLE " + AccountEntry.TABLE_NAME + " (" +
                AccountEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                AccountEntry.COLUMN_ACCOUNT_ID + " TEXT NOT NULL, " +
                AccountEntry.COLUMN_EMAIL + " TEXT, " +
                AccountEntry.COLUMN_PHONE + " TEXT, " +
                AccountEntry.COLUMN_PASSWORD + " TEXT NOT NULL, " +
                AccountEntry.COLUMN_TYPE + " TEXT, " +
                AccountEntry.COLUMN_ACTIVE + " INTEGER DEFAULT 1, " +
                AccountEntry.COLUMN_CREATED_DATE + " TEXT, " +
                "UNIQUE (" + AccountEntry.COLUMN_EMAIL + "), " +
                "UNIQUE (" + AccountEntry.COLUMN_PHONE + "))";

        // Create USERS table
        final String SQL_CREATE_USERS_TABLE = "CREATE TABLE " + UserEntry.TABLE_NAME + " (" +
                UserEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                UserEntry.COLUMN_USER_ID + " TEXT NOT NULL, " +
                UserEntry.COLUMN_ACCOUNT_ID + " TEXT NOT NULL, " +
                UserEntry.COLUMN_FULL_NAME + " TEXT, " +
                UserEntry.COLUMN_GENDER + " TEXT, " +
                UserEntry.COLUMN_WEIGHT + " REAL, " +
                UserEntry.COLUMN_AGE + " INTEGER, " +
                UserEntry.COLUMN_DAILY_TARGET + " INTEGER DEFAULT 2000, " +
                UserEntry.COLUMN_WAKE_TIME + " TEXT DEFAULT '06:00', " +
                UserEntry.COLUMN_SLEEP_TIME + " TEXT DEFAULT '22:00', " +
                "FOREIGN KEY (" + UserEntry.COLUMN_ACCOUNT_ID + ") REFERENCES " +
                AccountEntry.TABLE_NAME + " (" + AccountEntry.COLUMN_ACCOUNT_ID + "))";

        // Create WATER_INTAKE table
        final String SQL_CREATE_WATER_INTAKE_TABLE = "CREATE TABLE " + WaterIntakeEntry.TABLE_NAME + " (" +
                WaterIntakeEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                WaterIntakeEntry.COLUMN_INTAKE_ID + " TEXT NOT NULL, " +
                WaterIntakeEntry.COLUMN_USER_ID + " TEXT NOT NULL, " +
                WaterIntakeEntry.COLUMN_AMOUNT + " INTEGER NOT NULL, " +
                WaterIntakeEntry.COLUMN_DRINK_TYPE + " TEXT DEFAULT 'Water', " +
                WaterIntakeEntry.COLUMN_INTAKE_TIME + " TEXT NOT NULL, " +
                WaterIntakeEntry.COLUMN_INTAKE_DATE + " TEXT NOT NULL, " +
                "FOREIGN KEY (" + WaterIntakeEntry.COLUMN_USER_ID + ") REFERENCES " +
                UserEntry.TABLE_NAME + " (" + UserEntry.COLUMN_USER_ID + "))";

        // Create Reminders table
        final String SQL_CREATE_REMINDERS_TABLE = "CREATE TABLE " + ReminderEntry.TABLE_NAME + " (" +
                ReminderEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ReminderEntry.COLUMN_REMINDER_ID + " TEXT NOT NULL, " +
                ReminderEntry.COLUMN_USER_ID + " TEXT NOT NULL, " +
                ReminderEntry.COLUMN_REMINDER_TIME + " TEXT NOT NULL, " +
                ReminderEntry.COLUMN_SOUND + " TEXT DEFAULT 'default', " +
                ReminderEntry.COLUMN_ACTIVE + " INTEGER DEFAULT 1, " +
                ReminderEntry.COLUMN_DAYS + " TEXT DEFAULT 'all', " +
                "FOREIGN KEY (" + ReminderEntry.COLUMN_USER_ID + ") REFERENCES " +
                UserEntry.TABLE_NAME + " (" + UserEntry.COLUMN_USER_ID + "))";

        db.execSQL(SQL_CREATE_ACCOUNT_TABLE);
        db.execSQL(SQL_CREATE_USERS_TABLE);
        db.execSQL(SQL_CREATE_WATER_INTAKE_TABLE);
        db.execSQL(SQL_CREATE_REMINDERS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + ReminderEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + WaterIntakeEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + UserEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + AccountEntry.TABLE_NAME);
        onCreate(db);
    }

}

