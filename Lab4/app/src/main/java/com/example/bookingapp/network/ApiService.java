package com.example.bookingapp.network;

import com.example.bookingapp.network.NewsResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {
    @GET("v2/top-headlines?category=business&country=us")
    Call<NewsResponse> getTopNews(@Query("apiKey") String apiKey);
}