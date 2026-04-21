package com.example.bookingapp.repository;

import android.content.Context;
import android.net.Uri;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.bookingapp.db.*;
import com.example.bookingapp.network.*;
import com.example.bookingapp.utils.NetworkUtils;
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

    public AppRepository(Context context) {
        this.context = context;
        this.db = AppDatabase.getInstance(context);
        this.apiService = RetrofitClient.getApiService();
    }

    public void uploadAndSave(Apartment a, Uri imageUri, OnCompleteListener listener) {
        // Если это редактирование и фото не менялось
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
                        } else listener.onFailed("Ошибка загрузки фото: " + response.code());
                    }
                    @Override public void onFailure(retrofit2.Call<ImgBBResponse> call, Throwable t) { listener.onFailed(t.getMessage()); }
                });
            } catch (Exception e) { listener.onFailed(e.getMessage()); }
        });
    }

    private void saveToCloud(Apartment a, OnCompleteListener listener) {
        // Генерация ID, если его нет (для новых записей)
        if (a.id == 0) {
            a.id = (int) (System.currentTimeMillis() / 1000);
        }

        String docId = String.valueOf(a.id);

        firestore.collection("apartments").document(docId)
                .set(a)
                .addOnSuccessListener(v -> {
                    executor.execute(() -> {
                        db.apartmentDao().insert(a);
                        listener.onSuccess();
                    });
                })
                .addOnFailureListener(e -> listener.onFailed(e.getMessage()));
    }

    public void getAllApartmentsRemote(OnApartmentsLoaded listener) {
        if (NetworkUtils.isOnline(context)) {
            firestore.collection("apartments").get().addOnSuccessListener(query -> {
                List<Apartment> list = new ArrayList<>();
                for (QueryDocumentSnapshot doc : query) {
                    list.add(doc.toObject(Apartment.class));
                }
                executor.execute(() -> {
                    for (Apartment apt : list) db.apartmentDao().insert(apt);
                    listener.onLoaded(db.apartmentDao().getAll());
                });
            }).addOnFailureListener(e -> {
                executor.execute(() -> listener.onLoaded(db.apartmentDao().getAll()));
            });
        } else {
            executor.execute(() -> listener.onLoaded(db.apartmentDao().getAll()));
        }
    }

    private File uriToFile(Uri uri) throws Exception {
        InputStream is = context.getContentResolver().openInputStream(uri);
        File file = new File(context.getCacheDir(), System.currentTimeMillis() + "_temp.jpg");
        FileOutputStream os = new FileOutputStream(file);
        byte[] buffer = new byte[1024];
        int read;
        while ((read = is.read(buffer)) != -1) os.write(buffer, 0, read);
        os.flush();
        os.close();
        is.close();
        return file;
    }

    public void deleteApartment(Apartment a) {
        firestore.collection("apartments").document(String.valueOf(a.id)).delete();
        executor.execute(() -> db.apartmentDao().delete(a));
    }

    public LiveData<List<NewsArticle>> getNews() {
        MutableLiveData<List<NewsArticle>> data = new MutableLiveData<>();
        apiService.getTopNews("b58f176032c44dd8bf10bd774a98ce11").enqueue(new retrofit2.Callback<NewsResponse>() {
            @Override public void onResponse(retrofit2.Call<NewsResponse> c, retrofit2.Response<NewsResponse> r) {
                if (r.isSuccessful() && r.body() != null) {
                    executor.execute(() -> {
                        db.newsDao().deleteAll();
                        db.newsDao().insertAll(r.body().articles);
                        data.postValue(db.newsDao().getAll());
                    });
                }
            }
            @Override public void onFailure(retrofit2.Call<NewsResponse> c, Throwable t) {
                executor.execute(() -> data.postValue(db.newsDao().getAll()));
            }
        });
        return data;
    }

    public void createBooking(Apartment a, String d) {
        executor.execute(() -> {
            Booking b = new Booking();
            b.apartmentTitle = a.title;
            b.bookingDate = d;
            db.bookingDao().insert(b);
        });
    }

    public void getAllBookings(OnBookingsLoaded l) {
        executor.execute(() -> l.onLoaded(db.bookingDao().getAll()));
    }

    public void deleteBooking(Booking b) {
        executor.execute(() -> db.bookingDao().delete(b));
    }

    public interface OnBookingsLoaded { void onLoaded(List<Booking> list); }
}