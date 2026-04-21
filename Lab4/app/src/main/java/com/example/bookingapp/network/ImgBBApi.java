package com.example.bookingapp.network;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.*;

public interface ImgBBApi {
    @Multipart
    @POST("1/upload")
    Call<ImgBBResponse> uploadImage(@Query("key") String apiKey, @Part MultipartBody.Part image);
}