package com.example.bookingapp.ui;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.bumptech.glide.Glide;
import com.example.bookingapp.R;
import com.example.bookingapp.db.Apartment;
import com.example.bookingapp.db.AppDatabase;
import com.example.bookingapp.repository.AppRepository;
import com.example.bookingapp.utils.LocaleHelper;
import com.google.android.material.textfield.TextInputEditText;
import java.util.Calendar;
import java.util.concurrent.Executors;

public class DetailsActivity extends AppCompatActivity {
    private TextInputEditText etTitle, etDesc, etPrice, etDate;
    private ImageView ivSelectImage;
    private AppRepository repo;
    private Uri localImageUri = null;
    private Apartment existingApartment = null;

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    localImageUri = result.getData().getData();
                    Glide.with(this).load(localImageUri).into(ivSelectImage);
                }
            });

    @Override protected void attachBaseContext(Context n) { super.attachBaseContext(LocaleHelper.setLocale(n)); }

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        repo = new AppRepository(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish()); // НАЗАД

        etTitle = findViewById(R.id.etTitle);
        etDesc = findViewById(R.id.etDesc);
        etPrice = findViewById(R.id.etPrice);
        etDate = findViewById(R.id.etDate);
        ivSelectImage = findViewById(R.id.ivSelectImage);

        findViewById(R.id.btnPickImage).setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryLauncher.launch(i);
        });

        if (getIntent().hasExtra("apartment_id")) {
            int id = getIntent().getIntExtra("apartment_id", -1);
            loadData(id);
        }

        etDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(this, (view, y, m, d) -> etDate.setText(d + "/" + (m+1) + "/" + y),
                    c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        findViewById(R.id.btnSave).setOnClickListener(v -> save());
    }

    private void loadData(int id) {
        Executors.newSingleThreadExecutor().execute(() -> {
            existingApartment = AppDatabase.getInstance(this).apartmentDao().getById(id);
            if (existingApartment != null) runOnUiThread(() -> {
                etTitle.setText(existingApartment.title);
                etDesc.setText(existingApartment.description);
                etPrice.setText(existingApartment.price);
                etDate.setText(existingApartment.date);
                Glide.with(this).load(existingApartment.imageUrl).into(ivSelectImage);
            });
        });
    }

    private void save() {
        String t = etTitle.getText().toString();
        if (t.isEmpty()) return;
        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Saving..."); pd.show();

        Apartment a = (existingApartment != null) ? existingApartment : new Apartment();
        a.title = t; a.description = etDesc.getText().toString();
        a.price = etPrice.getText().toString(); a.date = etDate.getText().toString();

        repo.uploadAndSave(a, localImageUri, new AppRepository.OnCompleteListener() {
            @Override public void onSuccess() { pd.dismiss(); finish(); }
            @Override public void onFailed(String e) { pd.dismiss(); Toast.makeText(DetailsActivity.this, e, Toast.LENGTH_SHORT).show(); }
        });
    }
}