package com.example.crawlerapp.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.crawlerapp.R;
import com.example.crawlerapp.model.MonitorConfig;
import com.example.crawlerapp.storage.MonitorStorage;
import java.util.List;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.ExistingPeriodicWorkPolicy;
import java.util.concurrent.TimeUnit;
import androidx.appcompat.widget.SwitchCompat;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private EditText selectorInputForResult;
    private String sessionCookies;
    private final ActivityResultLauncher<Intent> selectorPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK && selectorInputForResult != null) {
            Intent data = result.getData();
            if (data != null) {
                String selector = data.getStringExtra(SelectorPickerActivity.EXTRA_SELECTOR);
                if (selector != null) selectorInputForResult.setText(selector);
            }
        }
    });
    private final ActivityResultLauncher<Intent> loginWebViewLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK) {
            Intent data = result.getData();
            if (data != null) {
                sessionCookies = data.getStringExtra("cookies");
            }
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        Button addMonitorButton = findViewById(R.id.addMonitorButton);
        addMonitorButton.setOnClickListener(v -> showAddMonitorDialog());
        scheduleBackgroundMonitor();
        loadMonitors();
    }

    private void scheduleBackgroundMonitor() {
        PeriodicWorkRequest monitorWork = new PeriodicWorkRequest.Builder(
                com.example.crawlerapp.worker.MonitorWorker.class,
                15, TimeUnit.MINUTES)
                .build();
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "monitor_worker",
                ExistingPeriodicWorkPolicy.KEEP,
                monitorWork);
    }

    private void loadMonitors() {
        try {
            List<MonitorConfig> configs = MonitorStorage.getAllConfigs(this);
            MonitorAdapter adapter = new MonitorAdapter(configs);
            adapter.setOnMonitorDeleteListener(config -> new AlertDialog.Builder(this)
                .setTitle("Delete Monitor")
                .setMessage("Delete monitor '" + config.getName() + "'?")
                .setPositiveButton("Delete", (d, w) -> {
                    try {
                        MonitorStorage.deleteConfig(this, config.getName());
                        loadMonitors();
                    } catch (Exception e) {
                        // Handle error
                    }
                })
                .setNegativeButton("Cancel", null)
                .show()
            );
            adapter.setOnMonitorEditListener(this::showEditMonitorDialog);
            adapter.setOnMonitorHistoryListener(config -> showMonitorHistoryDialog(config));
            recyclerView.setAdapter(adapter);
        } catch (Exception e) {
            // Handle error
        }
    }

    private void showAddMonitorDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_monitor, null);
        EditText nameInput = dialogView.findViewById(R.id.inputName);
        EditText urlInput = dialogView.findViewById(R.id.inputUrl);
        EditText selectorInput = dialogView.findViewById(R.id.inputSelector);
        EditText freqInput = dialogView.findViewById(R.id.inputFrequency);
        EditText usernameInput = dialogView.findViewById(R.id.inputUsername);
        EditText passwordInput = dialogView.findViewById(R.id.inputPassword);
        EditText loginUrlInput = dialogView.findViewById(R.id.inputLoginUrl);
        Button pickSelectorBtn = dialogView.findViewById(R.id.btnPickSelector);
        Button loginWebViewBtn = dialogView.findViewById(R.id.btnLoginWebView);
        Button pickStartDateTimeBtn = dialogView.findViewById(R.id.btnPickStartDateTime);
        TextView tvStartDateTime = dialogView.findViewById(R.id.tvStartDateTime);
        SwitchCompat switchEnabled = dialogView.findViewById(R.id.switchEnabled);
        pickSelectorBtn.setOnClickListener(v -> {
            String url = urlInput.getText().toString();
            if (url.isEmpty()) {
                urlInput.setError("Enter URL first");
                urlInput.requestFocus();
                return;
            }
            selectorInputForResult = selectorInput;
            Intent intent = new Intent(this, SelectorPickerActivity.class);
            intent.putExtra(SelectorPickerActivity.EXTRA_URL, url);
            selectorPickerLauncher.launch(intent);
        });
        loginWebViewBtn.setOnClickListener(v -> {
            String loginUrl = loginUrlInput.getText().toString();
            if (loginUrl.isEmpty()) {
                loginUrlInput.setError("Enter login URL first");
                loginUrlInput.requestFocus();
                return;
            }
            Intent intent = new Intent(this, LoginWebViewActivity.class);
            intent.putExtra(LoginWebViewActivity.EXTRA_LOGIN_URL, loginUrl);
            loginWebViewLauncher.launch(intent);
        });
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Add Monitor")
                .setView(dialogView)
                .setPositiveButton("Add", null)
                .setNegativeButton("Cancel", null)
                .setNeutralButton("Test", null)
                .create();
        dialog.setOnShowListener(d -> {
            Button addBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            Button testBtn = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
            addBtn.setOnClickListener(v -> {
                String name = nameInput.getText().toString();
                String url = urlInput.getText().toString();
                String selector = selectorInput.getText().toString();
                int freq = 10;
                String username = usernameInput.getText().toString();
                String password = passwordInput.getText().toString();
                String loginUrl = loginUrlInput.getText().toString();
                boolean enabled = switchEnabled.isChecked();
                long startDT = 0L; // Default to 0 for new
                try { freq = Integer.parseInt(freqInput.getText().toString()); } catch (Exception ignored) {}
                MonitorConfig config = new MonitorConfig(name, url, username, password, selector, freq, enabled, sessionCookies, loginUrl, startDT);
                try {
                    MonitorStorage.saveConfig(MainActivity.this, config);
                    loadMonitors();
                    dialog.dismiss();
                } catch (Exception e) {
                    // Handle error
                }
            });
            testBtn.setOnClickListener(v -> {
                String name = nameInput.getText().toString();
                String url = urlInput.getText().toString();
                String selector = selectorInput.getText().toString();
                int freq = 10;
                String username = usernameInput.getText().toString();
                String password = passwordInput.getText().toString();
                String loginUrl = loginUrlInput.getText().toString();
                boolean enabled = switchEnabled.isChecked();
                long startDT = 0L;
                try { freq = Integer.parseInt(freqInput.getText().toString()); } catch (Exception ignored) {}
                MonitorConfig config = new MonitorConfig(name, url, username, password, selector, freq, enabled, sessionCookies, loginUrl, startDT);
                new Thread(() -> {
                    try {
                        com.example.crawlerapp.monitor.WebPageMonitor monitor = new com.example.crawlerapp.monitor.WebPageMonitor();
                        com.example.crawlerapp.model.MonitorResult result = monitor.check(MainActivity.this, config);
                        runOnUiThread(() -> {
                            if (result.getDetails() != null && !result.getDetails().isEmpty()) {
                                com.example.crawlerapp.util.NotificationUtil.sendNotification(
                                    MainActivity.this,
                                    "Test Result: " + name,
                                    result.getDetails().get(0)
                                );
                            } else if (result.getMessage() != null && !result.getMessage().isEmpty()) {
                                com.example.crawlerapp.util.NotificationUtil.sendNotification(
                                    MainActivity.this,
                                    "Test Result: " + name,
                                    result.getMessage()
                                );
                            } else {
                                com.example.crawlerapp.util.NotificationUtil.sendNotification(
                                    MainActivity.this,
                                    "Test Result: " + name,
                                    "No result returned."
                                );
                            }
                        });
                    } catch (Exception e) {
                        runOnUiThread(() -> {
                            com.example.crawlerapp.util.NotificationUtil.sendNotification(MainActivity.this, "Test Failed", e.getMessage());
                            android.widget.Toast.makeText(MainActivity.this, "Test failed: " + e.getMessage(), android.widget.Toast.LENGTH_LONG).show();
                        });
                    }
                }).start();
            });
        });
        dialog.show();
    }

    private void showEditMonitorDialog(MonitorConfig config) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_monitor, null);
        EditText nameInput = dialogView.findViewById(R.id.inputName);
        EditText urlInput = dialogView.findViewById(R.id.inputUrl);
        EditText selectorInput = dialogView.findViewById(R.id.inputSelector);
        EditText freqInput = dialogView.findViewById(R.id.inputFrequency);
        EditText usernameInput = dialogView.findViewById(R.id.inputUsername);
        EditText passwordInput = dialogView.findViewById(R.id.inputPassword);
        EditText loginUrlInput = dialogView.findViewById(R.id.inputLoginUrl);
        Button pickSelectorBtn = dialogView.findViewById(R.id.btnPickSelector);
        Button loginWebViewBtn = dialogView.findViewById(R.id.btnLoginWebView);
        Button pickStartDateTimeBtn = dialogView.findViewById(R.id.btnPickStartDateTime);
        TextView tvStartDateTime = dialogView.findViewById(R.id.tvStartDateTime);
        SwitchCompat switchEnabled = dialogView.findViewById(R.id.switchEnabled);
        nameInput.setText(config.getName());
        urlInput.setText(config.getUrl());
        selectorInput.setText(config.getSelector());
        freqInput.setText(String.valueOf(config.getFrequencyMinutes()));
        usernameInput.setText(config.getUsername());
        passwordInput.setText(config.getPassword());
        loginUrlInput.setText(config.getLoginUrl());
        long startDateTime = config.getStartDateTime();
        if (startDateTime > 0) {
            java.text.DateFormat df = android.text.format.DateFormat.getDateFormat(this);
            java.text.DateFormat tf = android.text.format.DateFormat.getTimeFormat(this);
            java.util.Date date = new java.util.Date(startDateTime);
            tvStartDateTime.setText(df.format(date) + " " + tf.format(date));
        } else {
            tvStartDateTime.setText("Not set");
        }
        switchEnabled.setChecked(config.isEnabled());
        final long[] selectedStartDateTime = {startDateTime};
        pickSelectorBtn.setOnClickListener(v -> {
            String url = urlInput.getText().toString();
            if (url.isEmpty()) {
                urlInput.setError("Enter URL first");
                urlInput.requestFocus();
                return;
            }
            selectorInputForResult = selectorInput;
            Intent intent = new Intent(this, SelectorPickerActivity.class);
            intent.putExtra(SelectorPickerActivity.EXTRA_URL, url);
            selectorPickerLauncher.launch(intent);
        });
        loginWebViewBtn.setOnClickListener(v -> {
            String loginUrl = loginUrlInput.getText().toString();
            if (loginUrl.isEmpty()) {
                loginUrlInput.setError("Enter login URL first");
                loginUrlInput.requestFocus();
                return;
            }
            Intent intent = new Intent(this, LoginWebViewActivity.class);
            intent.putExtra(LoginWebViewActivity.EXTRA_LOGIN_URL, loginUrl);
            loginWebViewLauncher.launch(intent);
        });
        pickStartDateTimeBtn.setOnClickListener(v -> {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            if (selectedStartDateTime[0] > 0) cal.setTimeInMillis(selectedStartDateTime[0]);
            new android.app.DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                cal.set(java.util.Calendar.YEAR, year);
                cal.set(java.util.Calendar.MONTH, month);
                cal.set(java.util.Calendar.DAY_OF_MONTH, dayOfMonth);
                new android.app.TimePickerDialog(this, (timeView, hour, minute) -> {
                    cal.set(java.util.Calendar.HOUR_OF_DAY, hour);
                    cal.set(java.util.Calendar.MINUTE, minute);
                    cal.set(java.util.Calendar.SECOND, 0);
                    selectedStartDateTime[0] = cal.getTimeInMillis();
                    java.text.DateFormat df = android.text.format.DateFormat.getDateFormat(this);
                    java.text.DateFormat tf = android.text.format.DateFormat.getTimeFormat(this);
                    tvStartDateTime.setText(df.format(cal.getTime()) + " " + tf.format(cal.getTime()));
                }, cal.get(java.util.Calendar.HOUR_OF_DAY), cal.get(java.util.Calendar.MINUTE), true).show();
            }, cal.get(java.util.Calendar.YEAR), cal.get(java.util.Calendar.MONTH), cal.get(java.util.Calendar.DAY_OF_MONTH)).show();
        });
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Edit Monitor")
                .setView(dialogView)
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", null)
                .setNeutralButton("Test", null)
                .create();
        dialog.setOnShowListener(d -> {
            Button saveBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            Button testBtn = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
            saveBtn.setOnClickListener(v -> {
                String name = nameInput.getText().toString();
                String url = urlInput.getText().toString();
                String selector = selectorInput.getText().toString();
                int freq = 10;
                String username = usernameInput.getText().toString();
                String password = passwordInput.getText().toString();
                String loginUrl = loginUrlInput.getText().toString();
                boolean enabled = switchEnabled.isChecked();
                long startDT = selectedStartDateTime[0];
                try { freq = Integer.parseInt(freqInput.getText().toString()); } catch (Exception ignored) {}
                MonitorConfig newConfig = new MonitorConfig(name, url, username, password, selector, freq, enabled, sessionCookies, loginUrl, startDT);
                try {
                    MonitorStorage.deleteConfig(MainActivity.this, config.getName());
                    MonitorStorage.saveConfig(MainActivity.this, newConfig);
                    loadMonitors();
                    dialog.dismiss();
                } catch (Exception e) {
                    // Handle error
                }
            });
            testBtn.setOnClickListener(v -> {
                String name = nameInput.getText().toString();
                String url = urlInput.getText().toString();
                String selector = selectorInput.getText().toString();
                int freq = 10;
                String username = usernameInput.getText().toString();
                String password = passwordInput.getText().toString();
                String loginUrl = loginUrlInput.getText().toString();
                boolean enabled = switchEnabled.isChecked();
                long startDT = selectedStartDateTime[0];
                try { freq = Integer.parseInt(freqInput.getText().toString()); } catch (Exception ignored) {}
                MonitorConfig testConfig = new MonitorConfig(name, url, username, password, selector, freq, enabled, sessionCookies, loginUrl, startDT);
                new Thread(() -> {
                    try {
                        com.example.crawlerapp.monitor.WebPageMonitor monitor = new com.example.crawlerapp.monitor.WebPageMonitor();
                        com.example.crawlerapp.model.MonitorResult result = monitor.check(MainActivity.this, testConfig);
                        runOnUiThread(() -> {
                            if (result.getDetails() != null && !result.getDetails().isEmpty()) {
                                com.example.crawlerapp.util.NotificationUtil.sendNotification(
                                    MainActivity.this,
                                    "Test Result: " + name,
                                    result.getDetails().get(0)
                                );
                            } else if (result.getMessage() != null && !result.getMessage().isEmpty()) {
                                com.example.crawlerapp.util.NotificationUtil.sendNotification(
                                    MainActivity.this,
                                    "Test Result: " + name,
                                    result.getMessage()
                                );
                            } else {
                                com.example.crawlerapp.util.NotificationUtil.sendNotification(
                                    MainActivity.this,
                                    "Test Result: " + name,
                                    "No result returned."
                                );
                            }
                        });
                    } catch (Exception e) {
                        runOnUiThread(() -> {
                            com.example.crawlerapp.util.NotificationUtil.sendNotification(MainActivity.this, "Test Failed", e.getMessage());
                            android.widget.Toast.makeText(MainActivity.this, "Test failed: " + e.getMessage(), android.widget.Toast.LENGTH_LONG).show();
                        });
                    }
                }).start();
            });
        });
        dialog.show();
    }

    private void showMonitorHistoryDialog(MonitorConfig config) {
        // Fetch history entries for this monitor
        new Thread(() -> {
            List<com.example.crawlerapp.model.MonitorHistoryEntry> historyEntries = null;
            try {
                com.example.crawlerapp.model.AppDatabase db = com.example.crawlerapp.model.AppDatabase.getInstance(this);
                historyEntries = db.monitorHistoryDao().getHistoryForMonitor(config.getName());
            } catch (Exception e) {
                // Handle error
            }
            List<com.example.crawlerapp.model.MonitorHistoryEntry> finalHistoryEntries = historyEntries;
            runOnUiThread(() -> {
                StringBuilder sb = new StringBuilder();
                if (finalHistoryEntries != null && !finalHistoryEntries.isEmpty()) {
                    java.text.DateFormat df = android.text.format.DateFormat.getDateFormat(this);
                    java.text.DateFormat tf = android.text.format.DateFormat.getTimeFormat(this);
                    for (com.example.crawlerapp.model.MonitorHistoryEntry entry : finalHistoryEntries) {
                        java.util.Date date = new java.util.Date(entry.timestamp);
                        sb.append(df.format(date)).append(" ").append(tf.format(date)).append(": ");
                        sb.append(entry.value != null ? entry.value : "-");
                        sb.append("\n\n");
                    }
                } else {
                    sb.append("No history found.");
                }
                new AlertDialog.Builder(this)
                    .setTitle("History for " + config.getName())
                    .setMessage(sb.toString())
                    .setPositiveButton("OK", null)
                    .show();
            });
        }).start();
    }
}
