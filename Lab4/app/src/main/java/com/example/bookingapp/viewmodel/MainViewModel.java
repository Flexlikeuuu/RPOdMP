package com.example.bookingapp.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.bookingapp.db.NewsArticle;
import com.example.bookingapp.db.Apartment;
import com.example.bookingapp.repository.AppRepository;
import java.util.List;

public class MainViewModel extends AndroidViewModel {
    private AppRepository repository;
    private LiveData<List<NewsArticle>> news;

    public MainViewModel(Application application) {
        super(application);
        repository = new AppRepository(application);
        news = repository.getNews();
    }

    public LiveData<List<NewsArticle>> getNews() {
        return news;
    }

    public void bookApartment(Apartment apartment, String date) {
        repository.createBooking(apartment, date);
    }
}