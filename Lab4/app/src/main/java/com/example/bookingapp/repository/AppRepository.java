package com.example.bookingapp.repository;

import android.content.Context;
import android.net.Uri;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.bookingapp.db.*;
import com.example.bookingapp.network.*;
import com.example.bookingapp.utils.NetworkUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.io.*;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import okhttp3.*;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AppRepository {
    private final AppDatabase db;
    private final ApiService apiService;
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Context context;
    private final String IMGBB_KEY = "0eef29db090395b0129436a426e08ef8";

    private final ImgBBApi imgBBApi = new Retrofit.Builder()
            .baseUrl("https://api.imgbb.com/")
            .addConverterFactory(GsonConverterFactory.create()).build().create(ImgBBApi.class);

    public interface OnCompleteListener { void onSuccess(); void onFailed(String err); }
    public interface OnApartmentsLoaded { void onLoaded(List<Apartment> list); }
    public interface OnBookingsLoaded { void onLoaded(List<Booking> list); }

    public AppRepository(Context context) {
        this.context = context;
        this.db = AppDatabase.getInstance(context);
        this.apiService = RetrofitClient.getApiService();
    }

    public void subscribeToApartments(OnApartmentsLoaded listener) {
        firestore.collection("apartments").addSnapshotListener((value, error) -> {
            if (error != null) return;
            List<Apartment> list = new ArrayList<>();
            if (value != null) {
                for (QueryDocumentSnapshot doc : value) {
                    list.add(doc.toObject(Apartment.class));
                }
                executor.execute(() -> {
                    for (Apartment a : list) db.apartmentDao().insert(a);
                    listener.onLoaded(list);
                });
            }
        });
    }

    public void uploadAndSave(Apartment a, Uri imageUri, OnCompleteListener listener) {
        if (imageUri == null || imageUri.toString().startsWith("http")) {
            saveToCloud(a, listener);
            return;
        }
        executor.execute(() -> {
            try {
                File file = uriToFile(imageUri);
                RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), file);
                MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), reqFile);
                imgBBApi.uploadImage(IMGBB_KEY, body).enqueue(new retrofit2.Callback<ImgBBResponse>() {
                    @Override
                    public void onResponse(retrofit2.Call<ImgBBResponse> call, retrofit2.Response<ImgBBResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            a.imageUrl = response.body().data.url;
                            saveToCloud(a, listener);
                        } else listener.onFailed("ImgBB Error");
                    }
                    @Override public void onFailure(retrofit2.Call<ImgBBResponse> call, Throwable t) { listener.onFailed(t.getMessage()); }
                });
            } catch (Exception e) { listener.onFailed(e.getMessage()); }
        });
    }

    private void saveToCloud(Apartment a, OnCompleteListener listener) {
        if (a.id == 0) a.id = (int)(System.currentTimeMillis()/1000);
        firestore.collection("apartments").document(String.valueOf(a.id)).set(a)
                .addOnSuccessListener(v -> executor.execute(() -> { db.apartmentDao().insert(a); listener.onSuccess(); }))
                .addOnFailureListener(e -> listener.onFailed(e.getMessage()));
    }

    public void deleteApartment(Apartment a) {
        firestore.collection("apartments").document(String.valueOf(a.id)).delete();
        executor.execute(() -> db.apartmentDao().delete(a));
    }

    public LiveData<List<NewsArticle>> getNews() {
        MutableLiveData<List<NewsArticle>> data = new MutableLiveData<>();
        apiService.getTopNews("b58f176032c44dd8bf10bd774a98ce11").enqueue(new retrofit2.Callback<NewsResponse>() {
            @Override public void onResponse(retrofit2.Call<NewsResponse> c, retrofit2.Response<NewsResponse> r) {
                if (r.isSuccessful() && r.body() != null) executor.execute(() -> {
                    db.newsDao().deleteAll(); db.newsDao().insertAll(r.body().articles);
                    data.postValue(db.newsDao().getAll());
                });
            }
            @Override public void onFailure(retrofit2.Call<NewsResponse> c, Throwable t) {
                executor.execute(() -> data.postValue(db.newsDao().getAll()));
            }
        });
        return data;
    }

    public void createBooking(Apartment a, String d) {
        String currentUid = FirebaseAuth.getInstance().getUid();
        if (currentUid == null) return;

        executor.execute(() -> {
            Booking b = new Booking();
            b.apartmentTitle = a.title;
            b.bookingDate = d;
            b.userId = currentUid;
            db.bookingDao().insert(b);
        });
    }

    public void getAllBookings(OnBookingsLoaded l) {
        String currentUid = FirebaseAuth.getInstance().getUid();
        executor.execute(() -> l.onLoaded(db.bookingDao().getAllForUser(currentUid)));
    }

    public void deleteBooking(Booking b) {
        executor.execute(() -> db.bookingDao().delete(b));
    }

    private File uriToFile(Uri uri) throws Exception {
        InputStream is = context.getContentResolver().openInputStream(uri);
        File file = new File(context.getCacheDir(), System.currentTimeMillis() + "_t.jpg");
        FileOutputStream os = new FileOutputStream(file);
        byte[] buffer = new byte[1024]; int read;
        while ((read = is.read(buffer)) != -1) os.write(buffer, 0, read);
        os.flush(); is.close(); os.close(); return file;
    }
}