package com.example.crawlerapp.model;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface MonitorHistoryDao {
    @Insert
    void insert(MonitorHistoryEntry entry);

    @Query("SELECT * FROM monitor_history WHERE monitorName = :monitorName ORDER BY timestamp DESC")
    List<MonitorHistoryEntry> getHistoryForMonitor(String monitorName);

    @Query("SELECT * FROM monitor_history WHERE monitorName = :monitorName ORDER BY timestamp DESC LIMIT 1")
    MonitorHistoryEntry getLastEntryForMonitor(String monitorName);
}

