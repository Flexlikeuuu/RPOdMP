package com.example.bookingapp.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import com.example.bookingapp.R;
import com.example.bookingapp.utils.LocaleHelper;
import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {
    private SharedPreferences prefs;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        Switch switchTheme = findViewById(R.id.switchTheme);
        boolean isDark = prefs.getBoolean("is_dark", false);
        switchTheme.setChecked(isDark);

        switchTheme.setOnCheckedChangeListener((v, isChecked) -> {
            prefs.edit().putBoolean("is_dark", isChecked).apply();
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        RadioGroup radioGroupLanguage = findViewById(R.id.radioGroupLanguage);
        RadioButton radioEnglish = findViewById(R.id.radioEnglish);
        RadioButton radioRussian = findViewById(R.id.radioRussian);

        String currentLang = LocaleHelper.getPersistedLanguage(this);
        if (currentLang.equals("ru")) {
            radioRussian.setChecked(true);
        } else {
            radioEnglish.setChecked(true);
        }

        radioGroupLanguage.setOnCheckedChangeListener((group, checkedId) -> {
            String lang = checkedId == R.id.radioRussian ? "ru" : "en";
            LocaleHelper.setPersistedLanguage(this, lang);
            // Перезапускаем приложение для применения языка
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        });
    }
}
