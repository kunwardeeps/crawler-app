package com.example.crawlerapp.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.crawlerapp.R;
import com.example.crawlerapp.model.AppDatabase;
import com.example.crawlerapp.model.MonitorConfig;
import com.example.crawlerapp.model.MonitorHistoryEntry;
import com.example.crawlerapp.storage.MonitorStorage;
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

    public interface OnMonitorEditListener {
        void onEdit(MonitorConfig config);
    }
    private OnMonitorEditListener editListener;
    public void setOnMonitorEditListener(OnMonitorEditListener listener) {
        this.editListener = listener;
    }

    public interface OnMonitorHistoryListener {
        void onViewHistory(MonitorConfig config);
    }
    private OnMonitorHistoryListener historyListener;
    public void setOnMonitorHistoryListener(OnMonitorHistoryListener listener) {
        this.historyListener = listener;
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
        // Bind enabled switch
        Switch switchEnabled = holder.itemView.findViewById(R.id.switchMonitorEnabled);
        switchEnabled.setChecked(config.isEnabled());
        switchEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (config.isEnabled() != isChecked) {
                config.setEnabled(isChecked);
                // Save the updated config
                try {
                    MonitorStorage.saveConfig(holder.itemView.getContext(), config);
                } catch (Exception e) {
                    // Optionally show error
                }
            }
        });
        holder.itemView.setOnClickListener(v -> {
            if (editListener != null) editListener.onEdit(config);
        });
        ImageButton historyButton = holder.itemView.findViewById(R.id.monitorHistoryButton);
        historyButton.setOnClickListener(v -> {
            if (historyListener != null) historyListener.onViewHistory(config);
        });
    }

    @Override
    public int getItemCount() {
        return monitorList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public TextView url;
        public TextView frequency;
        public TextView lastChecked;
        public ImageButton deleteButton;
        public ImageButton historyButton;

        public ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.monitorName);
            url = itemView.findViewById(R.id.monitorUrl);
            frequency = itemView.findViewById(R.id.monitorFrequency);
            lastChecked = itemView.findViewById(R.id.monitorLastChecked);
            deleteButton = itemView.findViewById(R.id.monitorDeleteButton);
            historyButton = itemView.findViewById(R.id.monitorHistoryButton);
        }
    }
}

