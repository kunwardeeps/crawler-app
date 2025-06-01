package com.example.crawlerapp.worker;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.example.crawlerapp.model.MonitorConfig;
import com.example.crawlerapp.model.MonitorResult;
import com.example.crawlerapp.monitor.WebPageMonitor;
import com.example.crawlerapp.util.NotificationUtil;

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
