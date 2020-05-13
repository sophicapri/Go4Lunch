package com.sophieopenclass.go4lunch;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.CollectionReference;
import com.sophieopenclass.go4lunch.models.json_to_java.RestaurantsResult;
import com.sophieopenclass.go4lunch.models.json_to_java.PlaceDetails;
import com.sophieopenclass.go4lunch.models.User;
import com.sophieopenclass.go4lunch.repository.RestaurantDataRepository;
import com.sophieopenclass.go4lunch.repository.UserDataRepository;

import java.util.List;

public class MyViewModel extends ViewModel {
    private RestaurantDataRepository restaurantDataSource;
    private UserDataRepository userDataSource;
    private LiveData<User> createdUserLiveData;

    public MyViewModel(RestaurantDataRepository restaurantDataSource, UserDataRepository userDataSource) {
        this.restaurantDataSource = restaurantDataSource;
        this.userDataSource = userDataSource;
    }

    // RESTAURANTS

    public LiveData<RestaurantsResult> getNearbyPlaces(String location) {
        return restaurantDataSource.getNearbyPlaces(location);
    }

    public LiveData<RestaurantsResult> getMoreNearbyPlaces(String nextPageToken) {
        return restaurantDataSource.getMoreNearbyPlaces(nextPageToken);
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

    public void updateUserPlaceId(String uid, String placeId) {
        userDataSource.updateUserPlaceId(uid, placeId);
    }

    public CollectionReference getCollectionReference(String userCollection) {
        return userDataSource.getCollectionReference(userCollection);
    }

    public LiveData<List<User>> getUsersByPlaceId(String placeId) {
        return userDataSource.getUsersByPlaceId(placeId);
    }

    public LiveData<User> getCreatedUserLiveData() {
        return createdUserLiveData;
    }

    public LiveData<String> getPlaceId(String userId) {
        return userDataSource.getPlaceId(userId);
    }

        public LiveData<String> updateUsername(String username, String uid) {
        return userDataSource.updateUsername(username, uid);
    }

    public void deleteUser(String uid) {
        userDataSource.deleteUser(uid);
    }

    public LiveData<String> addUserPlaceId(String uid, String placeId) {
        return userDataSource.addUserPlaceId(uid, placeId);
    }
}
