package com.example.watertrackerandroidapp.DataBase;

import android.provider.BaseColumns;

public final class WaterTrackerContract {

    private WaterTrackerContract() {}

    public static class AccountEntry implements BaseColumns {
        public static final String TABLE_NAME = "ACCOUNT";
        public static final String COLUMN_ACCOUNT_ID = "AccountID";
        public static final String COLUMN_EMAIL = "Email";
        public static final String COLUMN_PHONE = "Phone";
        public static final String COLUMN_PASSWORD = "Password";
        public static final String COLUMN_TYPE = "Type";
        public static final String COLUMN_ACTIVE = "Active";
        public static final String COLUMN_CREATED_DATE = "CreatedDate";
    }

    public static class UserEntry implements BaseColumns {
        public static final String TABLE_NAME = "USERS";
        public static final String COLUMN_USER_ID = "UserID";
        public static final String COLUMN_ACCOUNT_ID = "AccountID";
        public static final String COLUMN_FULL_NAME = "FullName";
        public static final String COLUMN_GENDER = "Gender";
        public static final String COLUMN_WEIGHT = "Weight";
        public static final String COLUMN_AGE = "Age";
        public static final String COLUMN_DAILY_TARGET = "DailyTarget";
        public static final String COLUMN_WAKE_TIME = "WakeTime";
        public static final String COLUMN_SLEEP_TIME = "SleepTime";
    }

    public static class WaterIntakeEntry implements BaseColumns {
        public static final String TABLE_NAME = "WATER_INTAKE";
        public static final String COLUMN_INTAKE_ID = "IntakeID";
        public static final String COLUMN_USER_ID = "UserID";
        public static final String COLUMN_AMOUNT = "Amount";
        public static final String COLUMN_DRINK_TYPE = "DrinkType";
        public static final String COLUMN_INTAKE_TIME = "IntakeTime";
        public static final String COLUMN_INTAKE_DATE = "IntakeDate";
    }

    public static class ReminderEntry implements BaseColumns {
        public static final String TABLE_NAME = "Reminders";
        public static final String COLUMN_REMINDER_ID = "ReminderID";
        public static final String COLUMN_USER_ID = "UserID";
        public static final String COLUMN_REMINDER_TIME = "ReminderTime";
        public static final String COLUMN_SOUND = "Sound";
        public static final String COLUMN_ACTIVE = "Active";
        public static final String COLUMN_DAYS = "Days";
    }
}

