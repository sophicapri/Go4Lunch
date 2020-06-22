package com.sophieopenclass.go4lunch.api;

import com.sophieopenclass.go4lunch.BuildConfig;
import com.sophieopenclass.go4lunch.models.json_to_java.RestaurantsResult;
import com.sophieopenclass.go4lunch.models.json_to_java.PlaceDetailsResult;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface PlaceApi {

    @GET("nearbysearch/json?rankby=distance&type=restaurant")
    Call<RestaurantsResult> getNearbyPlaces(@Query("location") String location);

    @GET("nearbysearch/json")
    Call<RestaurantsResult> getMoreNearbyPlaces(@Query("pagetoken") String nextPageToken);

    @GET("details/json")
    Call<PlaceDetailsResult> getPlaceDetails(@Query("place_id") String placeId,
                                            @Query("language") String language);
}

