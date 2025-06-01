package com.example.crawlerapp.storage;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;
import com.example.crawlerapp.model.MonitorConfig;
import com.google.gson.Gson;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MonitorStorage {
    private static final String PREF_NAME = "monitor_configs";
    private static SharedPreferences getPrefs(Context context) throws GeneralSecurityException, IOException {
        MasterKey masterKey = new MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build();
        return EncryptedSharedPreferences.create(
                context,
                PREF_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        );
    }

    public static void saveConfig(Context context, MonitorConfig config) throws GeneralSecurityException, IOException {
        SharedPreferences prefs = getPrefs(context);
        prefs.edit().putString(config.getName(), new Gson().toJson(config)).apply();
    }

    public static List<MonitorConfig> getAllConfigs(Context context) throws GeneralSecurityException, IOException {
        SharedPreferences prefs = getPrefs(context);
        List<MonitorConfig> configs = new ArrayList<>();
        for (Map.Entry<String, ?> entry : prefs.getAll().entrySet()) {
            configs.add(new Gson().fromJson(entry.getValue().toString(), MonitorConfig.class));
        }
        return configs;
    }

    public static void deleteConfig(Context context, String name) throws GeneralSecurityException, IOException {
        SharedPreferences prefs = getPrefs(context);
        prefs.edit().remove(name).apply();
    }
}
