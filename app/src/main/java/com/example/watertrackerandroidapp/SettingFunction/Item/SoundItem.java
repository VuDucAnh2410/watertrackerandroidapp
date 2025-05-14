package com.example.watertrackerandroidapp.SettingFunction.Item;

public class SoundItem {
    private String soundName;
    private String soundId;

    public SoundItem(String soundName, String soundId) {
        this.soundName = soundName;
        this.soundId = soundId;
    }

    public String getSoundName() {
        return soundName;
    }

    public String getSoundId() {
        return soundId;
    }
}
