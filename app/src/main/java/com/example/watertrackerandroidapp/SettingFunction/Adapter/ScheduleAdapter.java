package com.example.watertrackerandroidapp.SettingFunction.Adapter;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;


import com.example.watertrackerandroidapp.R;
import com.example.watertrackerandroidapp.SettingFunction.Item.ScheduleItem;


import java.util.List;


public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder> {


    private List<ScheduleItem> items;
    private OnItemChangeListener onItemChangeListener;


    // Interface để thông báo khi switch thay đổi hoặc mục bị xóa
    public interface OnItemChangeListener {
        void onItemChanged();
    }


    public ScheduleAdapter(List<ScheduleItem> items, OnItemChangeListener listener) {
        this.items = items;
        this.onItemChangeListener = listener;
    }


    @Override
    public ScheduleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.setting_schedule_item, parent, false);
        return new ScheduleViewHolder(view);
    }


    @Override
    public void onBindViewHolder(ScheduleViewHolder holder, int position) {
        ScheduleItem item = items.get(position);
        holder.tvTime.setText(item.getTime());
        holder.switchReminder.setChecked(item.isEnabled());


        holder.switchReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            item.setEnabled(isChecked);
            onItemChangeListener.onItemChanged(); // Thông báo để lưu vào SharedPreferences
        });
    }


    @Override
    public int getItemCount() {
        return items.size();
    }


    public List<ScheduleItem> getItems() {
        return items;
    }


    // ViewHolder
    static class ScheduleViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime;
        SwitchCompat switchReminder;


        public ScheduleViewHolder(View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tvTime);
            switchReminder = itemView.findViewById(R.id.switchReminder);
        }
    }
}
