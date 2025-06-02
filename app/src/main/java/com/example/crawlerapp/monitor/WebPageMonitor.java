package com.example.crawlerapp.monitor;

import android.content.Context;
import com.example.crawlerapp.model.MonitorConfig;
import com.example.crawlerapp.model.MonitorResult;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.Collections;

public class WebPageMonitor implements Monitor {
    @Override
    public MonitorResult check(Context context, MonitorConfig config) {
        try {
            org.jsoup.Connection connection = Jsoup.connect(config.getUrl());
            if (config.getCookies() != null && !config.getCookies().isEmpty()) {
                // Parse cookies string (format: key1=value1; key2=value2)
                String[] cookiePairs = config.getCookies().split(";");
                for (String pair : cookiePairs) {
                    String[] kv = pair.trim().split("=", 2);
                    if (kv.length == 2) {
                        connection.cookie(kv[0], kv[1]);
                    }
                }
            }
            Document doc = connection.get();
            Elements elements = doc.select(config.getSelector());
            boolean changed = !elements.isEmpty();
            String message = changed ? "Change detected" : "No change";
            String currentValue = elements.text();
            // Always output the current value, regardless of change
            return new MonitorResult(changed, message + ". Current value: " + currentValue, java.util.Collections.singletonList(currentValue));
        } catch (IOException e) {
            return new MonitorResult(false, "Error: " + e.getMessage(), java.util.Collections.emptyList());
        }
    }
}
