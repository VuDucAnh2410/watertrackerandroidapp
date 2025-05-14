package com.example.watertrackerandroidapp.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.watertrackerandroidapp.Models.WaterIntake;
import com.example.watertrackerandroidapp.R;

import java.util.List;

public class WaterHistoryAdapter extends RecyclerView.Adapter<WaterHistoryAdapter.ViewHolder> {
    private List<WaterIntake> waterIntakeList;
    private Context context;

    public WaterHistoryAdapter(Context context, List<WaterIntake> waterIntakeList) {
        this.context = context;
        this.waterIntakeList = waterIntakeList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.home_item_water_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WaterIntake waterIntake = waterIntakeList.get(position);

        if (waterIntake.isScheduled()) {
            holder.ivIcon.setImageResource(R.drawable.ic_alarm);
            holder.tvTime.setText("Lần tới");
            holder.tvDrinkType.setText(""); // Không hiển thị loại đồ uống cho lịch tự động
        } else {
            holder.ivIcon.setImageResource(R.drawable.ic_water_drop);
            holder.tvTime.setText(waterIntake.getIntakeTime());
            holder.tvDrinkType.setText(waterIntake.getDrinkType());
        }

        holder.tvAmount.setText(waterIntake.getAmount() + " ml");
    }

    @Override
    public int getItemCount() {
        return waterIntakeList.size();
    }

    public void updateData(List<WaterIntake> newData) {
        this.waterIntakeList = newData;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvTime, tvAmount, tvDrinkType;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvDrinkType = itemView.findViewById(R.id.tvDrinkType);
        }
    }
}