package com.example.bookingapp.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bookingapp.utils.LocaleHelper;
import com.google.firebase.auth.FirebaseAuth;

public class SplashActivity extends AppCompatActivity {
    @Override protected void attachBaseContext(Context n) { super.attachBaseContext(LocaleHelper.setLocale(n)); }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.example.bookingapp.R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // ПРОВЕРКА АВТОРИЗАЦИИ
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                startActivity(new Intent(this, MainActivity.class));
            } else {
                startActivity(new Intent(this, LoginActivity.class));
            }
            finish();
        }, 2000);
    }
}