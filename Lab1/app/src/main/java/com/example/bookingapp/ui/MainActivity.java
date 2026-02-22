package com.example.bookingapp.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bookingapp.R;
import com.example.bookingapp.adapter.ApartmentAdapter;
import com.example.bookingapp.db.Apartment;
import com.example.bookingapp.db.AppDatabase;
import com.example.bookingapp.utils.LocaleHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private AppDatabase db;
    private ApartmentAdapter adapter;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        loadSettings();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = AppDatabase.getInstance(this);
        
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getString(R.string.apartments_list));
        toolbar.setNavigationOnClickListener(v -> {
            startActivity(new Intent(this, SettingsActivity.class));
        });

        recyclerView = findViewById(R.id.recyclerView);
        tvEmpty = findViewById(R.id.tvEmpty);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        FloatingActionButton btnAdd = findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(this, DetailsActivity.class);
            startActivity(intent);
        });

        adapter = new ApartmentAdapter(null);
        adapter.setOnItemClickListener(new ApartmentAdapter.OnItemClickListener() {
            @Override
            public void onEditClick(Apartment apartment) {
                Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
                intent.putExtra("apartment_id", apartment.id);
                startActivity(intent);
            }

            @Override
            public void onDeleteClick(Apartment apartment) {
                db.apartmentDao().delete(apartment);
                loadData();
            }

            @Override
            public void onItemClick(Apartment apartment) {
                // Можно добавить просмотр деталей
            }
        });
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Проверяем, изменился ли язык
        String savedLang = LocaleHelper.getPersistedLanguage(this);
        Configuration config = getResources().getConfiguration();
        String currentLang = config.locale.getLanguage();
        
        if (!savedLang.equals(currentLang)) {
            // Язык изменился, пересоздаем активити
            recreate();
            return;
        }
        
        // Обновляем язык и интерфейс
        loadSettings();
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle(getString(R.string.apartments_list));
        }
        TextView tvEmpty = findViewById(R.id.tvEmpty);
        if (tvEmpty != null) {
            tvEmpty.setText(getString(R.string.no_apartments));
        }
        loadData();
    }

    private void loadData() {
        List<Apartment> list = db.apartmentDao().getAll();
        adapter.updateList(list);
        
        if (list.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void loadSettings() {
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        boolean isDark = prefs.getBoolean("is_dark", false);
        if (isDark) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}
