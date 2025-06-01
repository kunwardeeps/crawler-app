package com.example.crawlerapp.worker;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.example.crawlerapp.model.MonitorConfig;
import com.example.crawlerapp.model.MonitorResult;
import com.example.crawlerapp.monitor.WebPageMonitor;
import com.example.crawlerapp.util.NotificationUtil;
import com.example.crawlerapp.model.AppDatabase;
import com.example.crawlerapp.model.MonitorHistoryEntry;
import java.util.concurrent.Executors;

public class MonitorWorker extends Worker {
    public MonitorWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            // Load all monitor configs from storage
            java.util.List<MonitorConfig> configs = com.example.crawlerapp.storage.MonitorStorage.getAllConfigs(getApplicationContext());
            WebPageMonitor monitor = new WebPageMonitor();
            for (MonitorConfig config : configs) {
                if (config.isEnabled()) {
                    MonitorResult result = monitor.check(getApplicationContext(), config);
                    // Save history entry for every check
                    if (result.getDetails() != null && !result.getDetails().isEmpty()) {
                        String value = result.getDetails().get(0);
                        long timestamp = System.currentTimeMillis();
                        MonitorHistoryEntry entry = new MonitorHistoryEntry(config.getName(), value, timestamp);
                        // Room DB operations must be off the main thread
                        Executors.newSingleThreadExecutor().execute(() -> {
                            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                            db.monitorHistoryDao().insert(entry);
                        });
                    }
                    if (result.isChanged()) {
                        NotificationUtil.sendNotification(getApplicationContext(), config.getName(), result.getMessage());
                    }
                }
            }
            return Result.success();
        } catch (Exception e) {
            return Result.failure();
        }
    }
}
