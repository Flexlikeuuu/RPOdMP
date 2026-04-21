package com.example.bookingapp.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.*;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.bookingapp.R;
import com.example.bookingapp.adapter.*;
import com.example.bookingapp.db.Apartment;
import com.example.bookingapp.repository.AppRepository;
import com.example.bookingapp.utils.*;
import com.example.bookingapp.viewmodel.MainViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private MainViewModel viewModel;
    private AppRepository repository;
    private RecyclerView recyclerView;
    private ApartmentAdapter apartmentAdapter;
    private List<Apartment> fullList = new ArrayList<>();
    private FloatingActionButton btnAdd;
    private Toolbar toolbar;
    private String currentLang;

    // ДОБАВЛЕНО: Храним ID текущей выбранной вкладки
    private int currentTabId = R.id.nav_apartments;

    @Override protected void attachBaseContext(Context n) { super.attachBaseContext(LocaleHelper.setLocale(n)); }

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        currentLang = LocaleHelper.getPersistedLanguage(this);
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        repository = new AppRepository(this);

        setupScheduledNotifications();
        checkNotificationPermission();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));

        recyclerView = findViewById(R.id.mainRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        btnAdd = findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(v -> startActivity(new Intent(this, DetailsActivity.class)));

        BottomNavigationView nav = findViewById(R.id.bottom_navigation);
        nav.setOnItemSelectedListener(item -> {
            currentTabId = item.getItemId(); // Сохраняем выбор
            refreshCurrentTab();
            return true;
        });

        showApartments();
    }

    // ДОБАВЛЕНО: Этот метод вызывается каждый раз, когда вы возвращаетесь в MainActivity
    @Override
    protected void onResume() {
        super.onResume();
        // Обновляем данные на текущей вкладке, чтобы увидеть изменения после сохранения
        refreshCurrentTab();
    }

    // ДОБАВЛЕНО: Логика обновления текущей вкладки
    private void refreshCurrentTab() {
        if (currentTabId == R.id.nav_apartments) {
            showApartments();
        } else if (currentTabId == R.id.nav_news) {
            showNews();
        } else if (currentTabId == R.id.nav_history) {
            showMyBookings();
        }
    }

    private void setupScheduledNotifications() {
        PeriodicWorkRequest notificationRequest =
                new PeriodicWorkRequest.Builder(NotificationWorker.class, 24, TimeUnit.HOURS).build();
        WorkManager.getInstance(this).enqueueUniquePeriodicWork("booking_reminder", ExistingPeriodicWorkPolicy.KEEP, notificationRequest);
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    private void showApartments() {
        toolbar.setTitle(R.string.apartments_list);
        btnAdd.setVisibility(View.VISIBLE);
        repository.getAllApartmentsRemote(list -> {
            this.fullList = list;
            runOnUiThread(() -> {
                apartmentAdapter = new ApartmentAdapter(list, new ApartmentAdapter.OnItemClickListener() {
                    @Override public void onEditClick(Apartment a) {
                        Intent i = new Intent(MainActivity.this, DetailsActivity.class);
                        i.putExtra("apartment_id", a.id);
                        startActivity(i);
                    }
                    @Override public void onDeleteClick(Apartment a) {
                        repository.deleteApartment(a);
                        showApartments();
                    }
                    @Override public void onBookClick(Apartment a) {
                        String d = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(new Date());
                        viewModel.bookApartment(a, d);
                        Toast.makeText(MainActivity.this, R.string.booking_success, Toast.LENGTH_SHORT).show();
                    }
                });
                recyclerView.setAdapter(apartmentAdapter);
            });
        });
    }

    private void showNews() {
        toolbar.setTitle(R.string.news_title);
        btnAdd.setVisibility(View.GONE);
        viewModel.getNews().observe(this, art -> {
            if (art != null && currentTabId == R.id.nav_news) {
                recyclerView.setAdapter(new NewsAdapter(art));
            }
        });
    }

    private void showMyBookings() {
        toolbar.setTitle(R.string.my_bookings_title);
        btnAdd.setVisibility(View.GONE);
        repository.getAllBookings(list -> runOnUiThread(() -> {
            if (currentTabId == R.id.nav_history) {
                recyclerView.setAdapter(new BookingAdapter(list, b -> {
                    repository.deleteBooking(b);
                    showMyBookings();
                }));
            }
        }));
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuItem si = menu.findItem(R.id.action_search);
        SearchView sv = (SearchView) si.getActionView();
        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String q) { return false; }
            @Override public boolean onQueryTextChange(String n) {
                List<Apartment> f = SearchUtils.filter(fullList, n);
                if (apartmentAdapter != null) apartmentAdapter.updateList(f);
                return true;
            }
        });
        return true;
    }

    @Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.sort_price) {
            Collections.sort(fullList, (a, b) -> {
                double p1 = Double.parseDouble(a.price.replaceAll("[^\\d.]", "0"));
                double p2 = Double.parseDouble(b.price.replaceAll("[^\\d.]", "0"));
                return Double.compare(p1, p2);
            });
        } else if (id == R.id.sort_title) {
            Collections.sort(fullList, (a, b) -> a.title.toLowerCase().compareTo(b.title.toLowerCase()));
        }
        if (apartmentAdapter != null) apartmentAdapter.notifyDataSetChanged();
        return super.onOptionsItemSelected(item);
    }

    @Override protected void onRestart() {
        super.onRestart();
        if (!currentLang.equals(LocaleHelper.getPersistedLanguage(this))) {
            finish();
            startActivity(getIntent());
        }
    }
}