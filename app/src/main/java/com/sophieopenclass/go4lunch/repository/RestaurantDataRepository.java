package com.sophieopenclass.go4lunch.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sophieopenclass.go4lunch.api.PlaceApi;
import com.sophieopenclass.go4lunch.models.POJO.PlaceDetails;
import com.sophieopenclass.go4lunch.models.POJO.Restaurants;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RestaurantDataRepository {
    private static final String COLLECTION_NAME = "restaurants";
    private static RestaurantDataRepository restaurantDataRepository;
    private PlaceApi placeApi;

    public RestaurantDataRepository(PlaceApi placeApi){
        this.placeApi = placeApi;
        //placeApi = PlaceService.createService(PlaceApi.class);
    }

    /*public static RestaurantRepository getInstance(){
        if (restaurantRepository == null){
            restaurantRepository = new RestaurantRepository();
        }
        return restaurantRepository;
    }
     */

    public MutableLiveData<Restaurants> getNearbyPlaces(String location, int radius){
        MutableLiveData<Restaurants> restaurantsData = new MutableLiveData<>();
        placeApi.getNearbyPlaces(location, radius).enqueue(new Callback<Restaurants>() {
            @Override
            public void onResponse(@NonNull Call<Restaurants> call,
                                   @NonNull Response<Restaurants> response) {
                if (response.isSuccessful()){
                    restaurantsData.setValue(response.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Restaurants> call, @NonNull Throwable t) {
                restaurantsData.setValue(null);
            }
        });
        return restaurantsData;
    }

    public MutableLiveData<PlaceDetails> getPlaceDetails(String placeId){
        MutableLiveData<PlaceDetails> placeDetails = new MutableLiveData<>();
        placeApi.getPlaceDetails(placeId).enqueue(new Callback<PlaceDetails>() {
            @Override
            public void onResponse(@NonNull Call<PlaceDetails> call,
                                   @NonNull Response<PlaceDetails> response) {
                if (response.isSuccessful()){
                    placeDetails.setValue(response.body());
                    System.out.println("repo :" + response.errorBody());
                }
            }

            @Override
            public void onFailure(@NonNull Call<PlaceDetails> call, @NonNull Throwable t) {
                placeDetails.setValue(null);
            }
        });
        return placeDetails;
    }


    /*
    private static CollectionReference getRestaurantsCollection(){
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME);
    }

    public static Task<DocumentSnapshot> getRestaurant(String uid){
        return getRestaurantsCollection().document(uid).get();
    }
     */
}
