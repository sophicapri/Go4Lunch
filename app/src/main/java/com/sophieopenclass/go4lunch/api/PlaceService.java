package com.sophieopenclass.go4lunch.api;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PlaceService {
    private static HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
    private static OkHttpClient.Builder httpClient;
    public static final String API_URL = "https://maps.googleapis.com/maps/api/place/";
    public static final String PHOTO_URL = "photo?maxwidth=400&&photoreference=";

    private static void initLogging(){
        httpClient = new OkHttpClient.Builder();

        logging.level(HttpLoggingInterceptor.Level.BODY);
        //httpClient.addInterceptor(logging);
    }

    public static <S> S createService(Class<S> serviceClass) {
        initLogging();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();

        return retrofit.create(serviceClass);
    }
}