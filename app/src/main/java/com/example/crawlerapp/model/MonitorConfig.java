package com.example.crawlerapp.model;

import java.io.Serializable;

public class MonitorConfig implements Serializable {
    private String name;
    private String url;
    private String username;
    private String password;
    private String selector;
    private int frequencyMinutes;
    private boolean enabled;

    public MonitorConfig(String name, String url, String username, String password, String selector, int frequencyMinutes, boolean enabled) {
        this.name = name;
        this.url = url;
        this.username = username;
        this.password = password;
        this.selector = selector;
        this.frequencyMinutes = frequencyMinutes;
        this.enabled = enabled;
    }

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getSelector() { return selector; }
    public void setSelector(String selector) { this.selector = selector; }
    public int getFrequencyMinutes() { return frequencyMinutes; }
    public void setFrequencyMinutes(int frequencyMinutes) { this.frequencyMinutes = frequencyMinutes; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
