package com.example.crawlerapp.model;

import java.util.List;

public class MonitorResult {
    private boolean changed;
    private String message;
    private List<String> details;

    public MonitorResult(boolean changed, String message, List<String> details) {
        this.changed = changed;
        this.message = message;
        this.details = details;
    }

    public boolean isChanged() { return changed; }
    public void setChanged(boolean changed) { this.changed = changed; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public java.util.List<String> getDetails() { return details; }
    public void setDetails(java.util.List<String> details) { this.details = details; }
}
