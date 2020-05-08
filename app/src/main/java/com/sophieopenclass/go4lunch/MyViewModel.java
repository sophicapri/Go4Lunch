package com.sophieopenclass.go4lunch;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.sophieopenclass.go4lunch.models.json_to_java.RestaurantsResult;
import com.sophieopenclass.go4lunch.models.json_to_java.PlaceDetails;
import com.sophieopenclass.go4lunch.models.User;
import com.sophieopenclass.go4lunch.repository.RestaurantDataRepository;
import com.sophieopenclass.go4lunch.repository.UserDataRepository;

public class MyViewModel extends ViewModel {
    private RestaurantDataRepository restaurantDataSource;
    private UserDataRepository userDataSource;
    private LiveData<User> createdUserLiveData;

    public MyViewModel(RestaurantDataRepository restaurantDataSource, UserDataRepository userDataSource) {
        this.restaurantDataSource = restaurantDataSource;
        this.userDataSource = userDataSource;
    }

    // RESTAURANTS

    public LiveData<RestaurantsResult> getNearbyPlaces(String location, int radius) {
        return restaurantDataSource.getNearbyPlaces(location, radius);
    }

    public LiveData<PlaceDetails> getPlaceDetails(String placeId) {
        return restaurantDataSource.getPlaceDetails(placeId);
    }

    // USERS

    public void createUser(User user) {
        createdUserLiveData = userDataSource.createUser(user);
    }

    public LiveData<User> getUser(String uid) {
        return userDataSource.getUser(uid);
    }

    public Task<Void> updateUsername(String username, String uid) {
        return userDataSource.updateUsername(username, uid);
    }

    public Task<Void> deleteUser(String uid) {
        return userDataSource.deleteUser(uid);
    }

    public CollectionReference getUsersCollectionReference(){
        return userDataSource.getUsersCollectionReference();
    }

    public LiveData<User> getCreatedUserLiveData() {
        return createdUserLiveData;
    }

}
