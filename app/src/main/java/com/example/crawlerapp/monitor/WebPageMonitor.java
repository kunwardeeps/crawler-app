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
            Document doc = Jsoup.connect(config.getUrl()).get();
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
