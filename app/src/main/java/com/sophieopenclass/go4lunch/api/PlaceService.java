package com.sophieopenclass.go4lunch.api;

import com.sophieopenclass.go4lunch.BuildConfig;
import com.sophieopenclass.go4lunch.models.POJO.PlaceDetails;
import com.sophieopenclass.go4lunch.models.POJO.Restaurants;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class PlaceService {
    private static HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
    private static OkHttpClient.Builder httpClient;


    private static void initLogging(){
        logging.level(HttpLoggingInterceptor.Level.BODY);
        httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(logging);
    }

    public static <S> S createService(Class<S> serviceClass) {
        initLogging();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://maps.googleapis.com/maps/api/place/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();

        return retrofit.create(serviceClass);
    }

}