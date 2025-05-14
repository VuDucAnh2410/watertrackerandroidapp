package com.example.watertrackerandroidapp.StatisticsFunction.adapter;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.watertrackerandroidapp.R;
import com.example.watertrackerandroidapp.StatisticsFunction.StatisticsActivity.CalendarDay;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder> {
    private Context context;
    private List<CalendarDay> calendarDays;
    private Map<String, Integer> dailyIntakeMap;
    private int dailyTarget;
    private Calendar currentDate;
    private OnDayClickListener onDayClickListener;
    private int selectedPosition = -1;

    public CalendarAdapter(Context context, List<CalendarDay> calendarDays, Map<String, Integer> dailyIntakeMap, int dailyTarget) {
        this.context = context;
        this.calendarDays = calendarDays;
        this.dailyIntakeMap = dailyIntakeMap;
        this.dailyTarget = dailyTarget;
        this.currentDate = Calendar.getInstance();
    }

    public void setOnDayClickListener(OnDayClickListener listener) {
        this.onDayClickListener = listener;
    }

    public void setCalendarDays(List<CalendarDay> calendarDays) {
        this.calendarDays = calendarDays;
        notifyDataSetChanged();
    }

    public void updateDailyIntake(Map<String, Integer> dailyIntakeMap) {
        this.dailyIntakeMap = dailyIntakeMap;
        notifyDataSetChanged();
    }

    public void setDailyTarget(int dailyTarget) {
        this.dailyTarget = dailyTarget;
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.stats_item_calendar_day, parent, false);
        return new CalendarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
        CalendarDay day = calendarDays.get(position);

        if (day.isCurrentMonth()) {
            holder.tvDay.setText(String.valueOf(day.getDay()));
            holder.tvDay.setVisibility(View.VISIBLE);

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, currentDate.get(Calendar.YEAR));
            calendar.set(Calendar.MONTH, currentDate.get(Calendar.MONTH));
            calendar.set(Calendar.DAY_OF_MONTH, day.getDay());

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String dateStr = dateFormat.format(calendar.getTime());

            if (dailyIntakeMap.containsKey(dateStr) && dailyIntakeMap.get(dateStr) >= dailyTarget) {
                holder.tvDay.setBackgroundResource(R.drawable.circle_day_background);
            } else {
                holder.tvDay.setBackgroundResource(R.drawable.circle_day_background);
            }

            if (position == selectedPosition) {
                holder.tvDay.setBackgroundResource(R.drawable.circle_selected_day_background);
            }

            Calendar today = Calendar.getInstance();
            boolean isToday = calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    calendar.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                    calendar.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH);

            if (isToday) {
                holder.tvDay.setTextColor(ContextCompat.getColor(context, R.color.errorRed));
                holder.tvDay.setPaintFlags(holder.tvDay.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                holder.tvDay.setTextColor(ContextCompat.getColor(context, R.color.black));
                holder.tvDay.setPaintFlags(holder.tvDay.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            }

            holder.itemView.setOnClickListener(v -> {
                selectedPosition = position;
                notifyDataSetChanged();
                if (onDayClickListener != null) {
                    onDayClickListener.onDayClick(day, dateStr);
                }
            });
        } else {
            holder.tvDay.setVisibility(View.INVISIBLE);
            holder.itemView.setOnClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return calendarDays.size();
    }

    static class CalendarViewHolder extends RecyclerView.ViewHolder {
        TextView tvDay;

        public CalendarViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDay = itemView.findViewById(R.id.tvDay);
        }
    }

    public interface OnDayClickListener {
        void onDayClick(CalendarDay day, String dateStr);
    }
}