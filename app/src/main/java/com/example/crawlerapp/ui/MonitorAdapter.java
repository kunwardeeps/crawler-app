package com.example.crawlerapp.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.crawlerapp.R;
import com.example.crawlerapp.model.MonitorConfig;
import java.util.List;

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
        holder.itemView.setOnLongClickListener(v -> {
            if (deleteListener != null) deleteListener.onDelete(config);
            return true;
        });
        holder.itemView.setOnClickListener(v -> {
            if (editListener != null) editListener.onEdit(config);
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
        TextView name, url, frequency;
        ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.monitorName);
            url = itemView.findViewById(R.id.monitorUrl);
            frequency = itemView.findViewById(R.id.monitorFrequency);
        }
    }
}
