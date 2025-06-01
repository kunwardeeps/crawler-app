package com.example.crawlerapp.util;

import android.text.TextUtils;
import com.example.crawlerapp.model.MonitorConfig;

public class MonitorValidator {
    public static boolean isValid(MonitorConfig config) {
        return !TextUtils.isEmpty(config.getName()) &&
                !TextUtils.isEmpty(config.getUrl()) &&
                config.getFrequencyMinutes() > 0;
    }
}
