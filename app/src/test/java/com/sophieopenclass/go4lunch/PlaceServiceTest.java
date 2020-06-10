package com.sophieopenclass.go4lunch;

import com.google.gson.Gson;
import com.sophieopenclass.go4lunch.api.PlaceService;
import com.sophieopenclass.go4lunch.models.json_to_java.PlaceDetails;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


public class PlaceServiceTest {
    private PlaceService placeService;
    private MockWebServer mockWebServer;

    @Test
    public void getPlaceDetails() {
        Gson gson = new Gson();
        MockWebServer server = new MockWebServer();
        server.enqueue(new MockResponse().setBody(gson.toString()));
    }


    @Before
    public void setup() {
        try {
            mockWebServer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        placeService = new Retrofit.Builder()
                .baseUrl(mockWebServer.url("/"))
                .addConverterFactory(GsonConverterFactory.create())
                .client(new OkHttpClient.Builder().build())
                .build()
                .create(PlaceService.class);
    }

    @After
    public void teardown() {
        try {
            mockWebServer.shutdown();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}