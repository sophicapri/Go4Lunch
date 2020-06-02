package com.sophieopenclass.go4lunch.repository;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.sophieopenclass.go4lunch.api.PlaceApi;
import com.sophieopenclass.go4lunch.models.json_to_java.PlaceDetails;
import com.sophieopenclass.go4lunch.models.json_to_java.PlaceDetailsResult;
import com.sophieopenclass.go4lunch.models.json_to_java.RestaurantsResult;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RestaurantDataRepository {
    private PlaceApi placeApi;

    public RestaurantDataRepository(PlaceApi placeApi) {
        this.placeApi = placeApi;
    }

    public MutableLiveData<RestaurantsResult> getNearbyPlaces(String location) {
        MutableLiveData<RestaurantsResult> restaurantsData = new MutableLiveData<>();
        placeApi.getNearbyPlaces(location).enqueue(new Callback<RestaurantsResult>() {
            @Override
            public void onResponse(@NonNull Call<RestaurantsResult> call,
                                   @NonNull Response<RestaurantsResult> response) {
                if (response.isSuccessful()) {
                    restaurantsData.setValue(response.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<RestaurantsResult> call, @NonNull Throwable t) {
                restaurantsData.setValue(null);
            }
        });
        return restaurantsData;
    }

    public MutableLiveData<PlaceDetails> getPlaceDetails(String placeId, String language) {
        MutableLiveData<PlaceDetails> placeDetails = new MutableLiveData<>();
        placeApi.getPlaceDetails(placeId, language).enqueue(new Callback<PlaceDetailsResult>() {
            @Override
            public void onResponse(@NonNull Call<PlaceDetailsResult> call,
                                   @NonNull Response<PlaceDetailsResult> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        placeDetails.setValue(response.body().getPlaceDetails());
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<PlaceDetailsResult> call, @NonNull Throwable t) {
                System.out.println("repo :" + t.getMessage());
            }
        });
        return placeDetails;
    }


    public MutableLiveData<List<PlaceDetails>> getPlaceDetailsList(List<String> placeIds, String language) {
        MutableLiveData<List<PlaceDetails>> placeDetails = new MutableLiveData<>();
        List<PlaceDetails> result = new ArrayList<>();

        for (String placeId : placeIds) {
            placeApi.getPlaceDetails(placeId, language).enqueue(new Callback<PlaceDetailsResult>() {
                @Override
                public void onResponse(@NonNull Call<PlaceDetailsResult> call,
                                       @NonNull Response<PlaceDetailsResult> response) {
                    if (response.isSuccessful()) {
                        if (response.body() != null) {
                            result.add(response.body().getPlaceDetails());
                            if(result.size() == placeIds.size())
                                placeDetails.setValue(result);
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<PlaceDetailsResult> call, @NonNull Throwable t) {
                    System.out.println("repo :" + t.getMessage());
                }
            });
        }
        return placeDetails;
    }


    public MutableLiveData<RestaurantsResult> getMoreNearbyPlaces(String nextPageToken) {
        MutableLiveData<RestaurantsResult> restaurantsData = new MutableLiveData<>();
        placeApi.getMoreNearbyPlaces(nextPageToken).enqueue(new Callback<RestaurantsResult>() {
            @Override
            public void onResponse(@NonNull Call<RestaurantsResult> call,
                                   @NonNull Response<RestaurantsResult> response) {
                if (response.isSuccessful()) {
                    restaurantsData.setValue(response.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<RestaurantsResult> call, @NonNull Throwable t) {
                restaurantsData.setValue(null);
            }
        });
        return restaurantsData;
    }
}
