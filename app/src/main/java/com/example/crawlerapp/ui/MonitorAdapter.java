package com.example.crawlerapp.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.crawlerapp.R;
import com.example.crawlerapp.model.AppDatabase;
import com.example.crawlerapp.model.MonitorConfig;
import com.example.crawlerapp.model.MonitorHistoryEntry;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class MonitorAdapter extends RecyclerView.Adapter<MonitorAdapter.ViewHolder> {
    private final List<MonitorConfig> monitorList;

    public MonitorAdapter(List<MonitorConfig> monitorList) {
        this.monitorList = monitorList;
    }

    public interface OnMonitorDeleteListener {
        void onDelete(MonitorConfig config);
    }
    private OnMonitorDeleteListener deleteListener;
    public void setOnMonitorDeleteListener(OnMonitorDeleteListener listener) {
        this.deleteListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_monitor, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MonitorConfig config = monitorList.get(position);
        holder.name.setText(config.getName());
        holder.url.setText(config.getUrl());
        holder.frequency.setText("Every " + config.getFrequencyMinutes() + " min");
        // Fetch and display last checked timestamp
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(holder.itemView.getContext());
            MonitorHistoryEntry lastEntry = db.monitorHistoryDao().getLastEntryForMonitor(config.getName());
            holder.itemView.post(() -> {
                if (lastEntry != null) {
                    String formatted = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new java.util.Date(lastEntry.timestamp));
                    holder.lastChecked.setText("Last checked: " + formatted);
                } else {
                    holder.lastChecked.setText("Last checked: -");
                }
            });
        }).start();
        holder.itemView.setOnLongClickListener(v -> {
            if (deleteListener != null) deleteListener.onDelete(config);
            return true;
        });
        holder.itemView.setOnClickListener(v -> {
            // Show trend/history dialog
            Context context = holder.itemView.getContext();
            new Thread(() -> {
                AppDatabase db = AppDatabase.getInstance(context);
                List<MonitorHistoryEntry> history = db.monitorHistoryDao().getHistoryForMonitor(config.getName());
                StringBuilder sb = new StringBuilder();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                for (MonitorHistoryEntry entry : history) {
                    sb.append(sdf.format(new java.util.Date(entry.timestamp)))
                      .append(": ")
                      .append(entry.value)
                      .append("\n");
                }
                String historyText = history.isEmpty() ? "No history available." : sb.toString();
                holder.itemView.post(() -> {
                    new AlertDialog.Builder(context)
                        .setTitle("Trend History: " + config.getName())
                        .setMessage(historyText)
                        .setPositiveButton("OK", null)
                        .show();
                });
            }).start();
        });
        holder.editButton.setOnClickListener(v -> {
            if (editListener != null) editListener.onEdit(config);
        });
        holder.deleteButton.setOnClickListener(v -> {
            if (deleteListener != null) deleteListener.onDelete(config);
        });
    }

    public interface OnMonitorEditListener {
        void onEdit(MonitorConfig config);
    }
    private OnMonitorEditListener editListener;
    public void setOnMonitorEditListener(OnMonitorEditListener listener) {
        this.editListener = listener;
    }

    @Override
    public int getItemCount() {
        return monitorList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, url, frequency, lastChecked;
        ImageButton editButton, deleteButton;
        ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.monitorName);
            url = itemView.findViewById(R.id.monitorUrl);
            frequency = itemView.findViewById(R.id.monitorFrequency);
            lastChecked = itemView.findViewById(R.id.monitorLastChecked);
            editButton = itemView.findViewById(R.id.monitorEditButton);
            deleteButton = itemView.findViewById(R.id.monitorDeleteButton);
        }
    }
}
