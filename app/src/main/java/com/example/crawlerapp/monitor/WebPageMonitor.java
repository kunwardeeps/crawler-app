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
            org.jsoup.nodes.Document doc = org.jsoup.Jsoup.connect(config.getUrl()).get();
            org.jsoup.select.Elements elements = doc.select(config.getSelector());
            boolean changed = !elements.isEmpty();
            String message = changed ? "Change detected" : "No change";
            return new com.example.crawlerapp.model.MonitorResult(changed, message, java.util.Collections.singletonList(elements.text()));
        } catch (java.io.IOException e) {
            return new com.example.crawlerapp.model.MonitorResult(false, "Error: " + e.getMessage(), java.util.Collections.emptyList());
        }
    }
}
