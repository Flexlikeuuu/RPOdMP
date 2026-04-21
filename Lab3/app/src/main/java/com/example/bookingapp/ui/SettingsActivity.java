package com.example.bookingapp.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import com.example.bookingapp.R;
import com.example.bookingapp.utils.LocaleHelper;

public class SettingsActivity extends AppCompatActivity {
    private SharedPreferences prefs;
    private String langAtStart;

    @Override
    protected void attachBaseContext(Context newBase) {
        // Установка локали при создании
        super.attachBaseContext(LocaleHelper.setLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        langAtStart = LocaleHelper.getPersistedLanguage(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Логика темы
        Switch switchTheme = findViewById(R.id.switchTheme);
        switchTheme.setChecked(prefs.getBoolean("is_dark", false));
        switchTheme.setOnCheckedChangeListener((v, isChecked) -> {
            prefs.edit().putBoolean("is_dark", isChecked).apply();
            AppCompatDelegate.setDefaultNightMode(isChecked ?
                    AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        });

        // Логика языка
        RadioGroup rg = findViewById(R.id.radioGroupLanguage);
        if (langAtStart.equals("ru")) {
            ((RadioButton)findViewById(R.id.radioRussian)).setChecked(true);
        } else {
            ((RadioButton)findViewById(R.id.radioEnglish)).setChecked(true);
        }

        rg.setOnCheckedChangeListener((group, id) -> {
            String newLang = (id == R.id.radioRussian) ? "ru" : "en";
            if (!newLang.equals(LocaleHelper.getPersistedLanguage(this))) {
                LocaleHelper.setPersistedLanguage(this, newLang);
                // Пересоздаем только эту активити, MainActivity пересоздастся сама при возврате
                recreate();
            }
        });
    }
}