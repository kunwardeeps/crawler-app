package com.example.crawlerapp.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private MonitorAdapter adapter;
    private EditText selectorInputForResult;
    private EditText urlInputForResult;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        Button addMonitorButton = findViewById(R.id.addMonitorButton);
        addMonitorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddMonitorDialog();
            }
        });
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
            adapter = new MonitorAdapter(configs);
            adapter.setOnMonitorDeleteListener(config -> {
                new AlertDialog.Builder(this)
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
                    .show();
            });
            adapter.setOnMonitorEditListener(config -> showEditMonitorDialog(config));
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
        Button pickSelectorBtn = dialogView.findViewById(R.id.btnPickSelector);
        pickSelectorBtn.setOnClickListener(v -> {
            String url = urlInput.getText().toString();
            if (url.isEmpty()) {
                urlInput.setError("Enter URL first");
                urlInput.requestFocus();
                return;
            }
            selectorInputForResult = selectorInput;
            urlInputForResult = urlInput;
            Intent intent = new Intent(this, SelectorPickerActivity.class);
            intent.putExtra(SelectorPickerActivity.EXTRA_URL, url);
            selectorPickerLauncher.launch(intent);
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
                try { freq = Integer.parseInt(freqInput.getText().toString()); } catch (Exception ignored) {}
                MonitorConfig config = new MonitorConfig(name, url, username, password, selector, freq, true);
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
                try { freq = Integer.parseInt(freqInput.getText().toString()); } catch (Exception ignored) {}
                MonitorConfig config = new MonitorConfig(name, url, username, password, selector, freq, true);
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
        Button pickSelectorBtn = dialogView.findViewById(R.id.btnPickSelector);
        nameInput.setText(config.getName());
        urlInput.setText(config.getUrl());
        selectorInput.setText(config.getSelector());
        freqInput.setText(String.valueOf(config.getFrequencyMinutes()));
        usernameInput.setText(config.getUsername());
        passwordInput.setText(config.getPassword());
        pickSelectorBtn.setOnClickListener(v -> {
            String url = urlInput.getText().toString();
            if (url.isEmpty()) {
                urlInput.setError("Enter URL first");
                urlInput.requestFocus();
                return;
            }
            selectorInputForResult = selectorInput;
            urlInputForResult = urlInput;
            Intent intent = new Intent(this, SelectorPickerActivity.class);
            intent.putExtra(SelectorPickerActivity.EXTRA_URL, url);
            selectorPickerLauncher.launch(intent);
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
                try { freq = Integer.parseInt(freqInput.getText().toString()); } catch (Exception ignored) {}
                MonitorConfig newConfig = new MonitorConfig(name, url, username, password, selector, freq, true);
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
                try { freq = Integer.parseInt(freqInput.getText().toString()); } catch (Exception ignored) {}
                MonitorConfig testConfig = new MonitorConfig(name, url, username, password, selector, freq, true);
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
}
