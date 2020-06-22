package com.sophieopenclass.go4lunch.api;

import com.sophieopenclass.go4lunch.BuildConfig;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PlaceService {
    private static HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
    private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
    public static final String API_URL = "https://maps.googleapis.com/maps/api/place/";
    public static final String PHOTO_URL = "photo?maxwidth=400&&photoreference=";

    private static void initLogging(){
        logging.level(HttpLoggingInterceptor.Level.BODY);
        httpClient.addInterceptor(logging);
    }

    private static void initApiKey() {
        httpClient.addInterceptor(chain -> {
            Request original = chain.request();
            HttpUrl originalHttpUrl = original.url();

            HttpUrl url = originalHttpUrl.newBuilder()
                    .addQueryParameter("key", BuildConfig.API_KEY)
                    .build();

            // Request customization: add request headers
            Request.Builder requestBuilder = original.newBuilder()
                    .url(url);

            Request request = requestBuilder.build();
            return chain.proceed(request);
        });
    }

    public static <S> S createService(Class<S> serviceClass) {
        initApiKey();
        initLogging();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();

        return retrofit.create(serviceClass);
    }


}