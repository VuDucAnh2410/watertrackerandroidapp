package com.example.watertrackerandroidapp.SettingFunction.Item;

public class ScheduleItem {
    private String time;
    private boolean isEnabled;

    public ScheduleItem(String time, boolean isEnabled) {
        this.time = time;
        this.isEnabled = isEnabled;
    }

    public String getTime() {
        return time;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
    }
}
