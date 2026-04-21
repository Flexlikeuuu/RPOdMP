package com.example.bookingapp.utils;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import java.util.Locale;

public class LocaleHelper {
    public static Context setLocale(Context c) { return updateResources(c, getPersistedLanguage(c)); }
    public static String getPersistedLanguage(Context c) {
        return c.getSharedPreferences("app_prefs", Context.MODE_PRIVATE).getString("language", "en");
    }
    public static void setPersistedLanguage(Context c, String l) {
        c.getSharedPreferences("app_prefs", Context.MODE_PRIVATE).edit().putString("language", l).apply();
    }
    private static Context updateResources(Context c, String l) {
        Locale loc = new Locale(l); Locale.setDefault(loc);
        Configuration conf = new Configuration(c.getResources().getConfiguration());
        conf.setLocale(loc);
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) ? c.createConfigurationContext(conf) : c;
    }
}