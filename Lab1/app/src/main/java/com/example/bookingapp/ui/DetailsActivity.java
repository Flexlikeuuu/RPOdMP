package com.example.bookingapp.ui;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import com.example.bookingapp.R;
import com.example.bookingapp.db.Apartment;
import com.example.bookingapp.db.AppDatabase;
import com.example.bookingapp.utils.LocaleHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DetailsActivity extends AppCompatActivity {
    private TextInputEditText etTitle, etDesc, etPrice, etDate;
    private AppDatabase db;
    private int apartmentId = -1;
    private Calendar calendar = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        loadSettings();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        db = AppDatabase.getInstance(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        etTitle = findViewById(R.id.etTitle);
        etDesc = findViewById(R.id.etDesc);
        etPrice = findViewById(R.id.etPrice);
        etDate = findViewById(R.id.etDate);
        MaterialButton btnSave = findViewById(R.id.btnSave);

        apartmentId = getIntent().getIntExtra("apartment_id", -1);
        
        if (apartmentId != -1) {
            toolbar.setTitle(getString(R.string.edit_apartment));
            loadApartment();
        } else {
            toolbar.setTitle(getString(R.string.new_apartment));
        }

        etDate.setOnClickListener(v -> showDatePicker());
        etDate.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) showDatePicker();
        });

        btnSave.setOnClickListener(v -> saveApartment());
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
                calendar.set(year, month, dayOfMonth);
                etDate.setText(dateFormat.format(calendar.getTime()));
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void loadApartment() {
        new Thread(() -> {
            List<Apartment> apartments = db.apartmentDao().getAll();
            Apartment apartment = null;
            for (Apartment a : apartments) {
                if (a.id == apartmentId) {
                    apartment = a;
                    break;
                }
            }
            
            if (apartment != null) {
                Apartment finalApartment = apartment;
                runOnUiThread(() -> {
                    etTitle.setText(finalApartment.title);
                    etDesc.setText(finalApartment.description);
                    etPrice.setText(finalApartment.price);
                    if (finalApartment.date != null) {
                        etDate.setText(finalApartment.date);
                    }
                });
            }
        }).start();
    }

    private void saveApartment() {
        String title = etTitle.getText().toString().trim();
        String desc = etDesc.getText().toString().trim();
        String price = etPrice.getText().toString().trim();
        String date = etDate.getText().toString().trim();

        if (title.isEmpty() || desc.isEmpty() || price.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            Apartment apartment;
            if (apartmentId != -1) {
                List<Apartment> apartments = db.apartmentDao().getAll();
                apartment = null;
                for (Apartment a : apartments) {
                    if (a.id == apartmentId) {
                        apartment = a;
                        break;
                    }
                }
                if (apartment == null) {
                    apartment = new Apartment();
                    apartment.id = apartmentId;
                }
            } else {
                apartment = new Apartment();
            }
            
            apartment.title = title;
            apartment.description = desc;
            apartment.price = price;
            apartment.date = date;

            if (apartmentId != -1) {
                db.apartmentDao().update(apartment);
            } else {
                db.apartmentDao().insert(apartment);
            }

            runOnUiThread(() -> {
                Toast.makeText(this, getString(R.string.saved), Toast.LENGTH_SHORT).show();
                finish();
            });
        }).start();
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
