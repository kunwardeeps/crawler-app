package com.example.crawlerapp.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "monitor_history")
public class MonitorHistoryEntry {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String monitorName;
    public String value;
    public long timestamp;

    public MonitorHistoryEntry(String monitorName, String value, long timestamp) {
        this.monitorName = monitorName;
        this.value = value;
        this.timestamp = timestamp;
    }
}

