package com.example.watertrackerandroidapp.StatisticsFunction.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.watertrackerandroidapp.R;

import java.util.List;

public class MonthAdapter extends RecyclerView.Adapter<MonthAdapter.MonthViewHolder> {
    private final Context context;
    private final List<String> months;
    private int selectedPosition;
    private OnMonthClickListener onMonthClickListener;

    public MonthAdapter(Context context, List<String> months, int initialSelectedMonth) {
        this.context = context;
        this.months = months;
        this.selectedPosition = Math.max(0, Math.min(initialSelectedMonth, months.size() - 1));
    }

    public void setOnMonthClickListener(OnMonthClickListener listener) {
        this.onMonthClickListener = listener;
    }

    @NonNull
    @Override
    public MonthViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.stats_item_month, parent, false);
        return new MonthViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MonthViewHolder holder, int position) {
        int currentPosition = holder.getAdapterPosition();
        holder.tvMonth.setText(months.get(currentPosition));

        if (currentPosition == selectedPosition) {
            holder.tvMonth.setTextColor(ContextCompat.getColor(context, R.color.lightBlue));
            holder.tvMonth.setBackgroundResource(R.drawable.bg_selected_month);
        } else {
            holder.tvMonth.setTextColor(ContextCompat.getColor(context, R.color.black));
            holder.tvMonth.setBackgroundResource(android.R.color.transparent);
        }

        holder.itemView.setOnClickListener(v -> {
            int newPosition = holder.getAdapterPosition();
            if (newPosition != RecyclerView.NO_POSITION && newPosition != selectedPosition) {
                int previousSelected = selectedPosition;
                selectedPosition = newPosition;
                notifyItemChanged(previousSelected);
                notifyItemChanged(selectedPosition);
                if (onMonthClickListener != null) {
                    onMonthClickListener.onMonthClick(newPosition);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return months.size();
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public void setSelectedPosition(int position) {
        int previousSelected = selectedPosition;
        selectedPosition = position;
        notifyItemChanged(previousSelected);
        notifyItemChanged(selectedPosition);
    }

    static class MonthViewHolder extends RecyclerView.ViewHolder {
        TextView tvMonth;

        public MonthViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMonth = itemView.findViewById(R.id.tvMonth);
        }
    }

    public interface OnMonthClickListener {
        void onMonthClick(int position);
    }
}