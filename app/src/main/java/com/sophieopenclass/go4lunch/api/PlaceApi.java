package com.sophieopenclass.go4lunch.api;
import com.sophieopenclass.go4lunch.BuildConfig;
import com.sophieopenclass.go4lunch.models.json_to_java.RestaurantsResult;
import com.sophieopenclass.go4lunch.models.json_to_java.PlaceDetailsResult;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface PlaceApi {
    String apiKey = BuildConfig.API_KEY;


    @GET("nearbysearch/json?&type=restaurant&key=" + apiKey)
    Call<RestaurantsResult> getNearbyPlaces(@Query("location") String location,
                                            @Query("radius") int radius);

    @GET("details/json?&key=" + apiKey)
    Call<PlaceDetailsResult> getPlaceDetails(@Query("place_id") String placeId);

    @GET("photo?maxwidth=400&key=" + apiKey)
    Call<PlaceDetailsResult> getRestaurantPhoto(@Query("photoreference") String photoReference);
}

