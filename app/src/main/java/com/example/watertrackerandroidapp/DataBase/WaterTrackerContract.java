package com.example.watertrackerandroidapp.DataBase;

import android.provider.BaseColumns;

public final class WaterTrackerContract {
    // Ngăn việc khởi tạo lớp này
    private WaterTrackerContract() {}

    // Bảng ACCOUNT
    public static class AccountEntry implements BaseColumns {
        public static final String TABLE_NAME = "account";
        public static final String COLUMN_USER_ID = "user_id";
        public static final String COLUMN_PHONE_NUMBER = "phone_number";
        public static final String COLUMN_EMAIL = "email";
        public static final String COLUMN_PASSWORD = "password";
        public static final String COLUMN_CREATE_AT = "create_at";
    }

    // Bảng USERS
    public static class UserEntry implements BaseColumns {
        public static final String TABLE_NAME = "users";
        public static final String COLUMN_USER_ID = "user_id";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_SEX = "sex";
        public static final String COLUMN_WEIGHT = "weight";
        public static final String COLUMN_AGE = "age";
        public static final String COLUMN_SLEEP_TIME = "sleep_time";
        public static final String COLUMN_WAKE_TIME = "wake_time";
        public static final String COLUMN_DAILY_GOAL = "daily_goal";
    }

    // Bảng WATER_INTAKE
    public static class WaterIntakeEntry implements BaseColumns {
        public static final String TABLE_NAME = "water_intake";
        public static final String COLUMN_USER_ID = "user_id";
        public static final String COLUMN_AMOUNT = "amount";
        public static final String COLUMN_WATER_TIME = "water_time";
    }

    // Bảng Reminders
    public static class ReminderEntry implements BaseColumns {
        public static final String TABLE_NAME = "reminders";
        public static final String COLUMN_USER_ID = "user_id";
        public static final String COLUMN_REMINDER_TIME = "reminder_time";
        public static final String COLUMN_STATUS = "status";
        public static final String COLUMN_ALARM_SOUND = "alarm_sound";
    }
}