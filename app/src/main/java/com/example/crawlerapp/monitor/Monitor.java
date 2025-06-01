package com.example.crawlerapp.monitor;

import android.content.Context;
import com.example.crawlerapp.model.MonitorConfig;
import com.example.crawlerapp.model.MonitorResult;

public interface Monitor {
    MonitorResult check(Context context, MonitorConfig config);
}
