package com.sophieopenclass.go4lunch.api;
import com.sophieopenclass.go4lunch.BuildConfig;
import com.sophieopenclass.go4lunch.models.POJO.PlaceDetails;
import com.sophieopenclass.go4lunch.models.POJO.Restaurants;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface PlaceApi {
    String apiKey = BuildConfig.API_KEY;


    @GET("nearbysearch/json?&type=restaurant&key=" + apiKey)
    Call<Restaurants> getNearbyPlaces(@Query("location") String location,
                                      @Query("radius") int radius);

    @GET("details/json?&key=" + apiKey)
    Call<PlaceDetails> getPlaceDetails(@Query("place_id") String placeId);
}

