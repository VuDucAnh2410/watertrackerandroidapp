package com.example.watertrackerandroidapp.Setting;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.watertrackerandroidapp.R;

import java.util.ArrayList;

public class SettingSoundOption extends AppCompatActivity {

    private ListView listViewSounds;
    private ArrayList<String> sounds;
    private String selectedSound = "Dòng nước"; // Âm thanh mặc định

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_sound);

        listViewSounds = findViewById(R.id.listViewSounds);
        sounds = new ArrayList<>();
        sounds.add("Dòng nước");
        sounds.add("Cổ điển");
        sounds.add("Giọt nước");
        sounds.add("Thả tiếng vang");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.activity_setting_soundoption, R.id.tvSoundName, sounds) {
            @Override
            public View getView(int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView tvSoundName = view.findViewById(R.id.tvSoundName);
                CheckBox checkBox = view.findViewById(R.id.checkBox);

                tvSoundName.setText(sounds.get(position));
                checkBox.setChecked(sounds.get(position).equals(selectedSound));

                checkBox.setOnClickListener(v -> {
                    selectedSound = sounds.get(position);
                    notifyDataSetChanged();
                });

                return view;
            }
        };

        listViewSounds.setAdapter(adapter);

        listViewSounds.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
            selectedSound = sounds.get(position);
            adapter.notifyDataSetChanged();
        });
    }

    public void onCloseClick(View view) {
        finish();
    }
}
