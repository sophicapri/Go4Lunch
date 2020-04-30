package com.sophieopenclass.go4lunch;

import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.sophieopenclass.go4lunch.models.POJO.PlaceDetails;
import com.sophieopenclass.go4lunch.models.POJO.Restaurants;
import com.sophieopenclass.go4lunch.repository.RestaurantDataRepository;
import com.sophieopenclass.go4lunch.repository.UserDataRepository;

public class MyViewModel extends ViewModel {
    private RestaurantDataRepository restaurantDataSource;
    private UserDataRepository userDataSource;

    public MyViewModel(RestaurantDataRepository restaurantDataSource, UserDataRepository userDataSource) {
        this.restaurantDataSource = restaurantDataSource;
        this.userDataSource = userDataSource;
    }

    // RESTAURANTS

    public LiveData<Restaurants> getNearbyPlaces(String location, int radius) {
        return restaurantDataSource.getNearbyPlaces(location, radius);
    }

    public LiveData<PlaceDetails> getPlaceDetails(String placeId) {
        return restaurantDataSource.getPlaceDetails(placeId);
    }

    // USERS

    public FirebaseUser getCurrentUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    public Task<Void> createUser(FirebaseUser user) {
        String urlPicture = (user.getPhotoUrl() != null) ? user.getPhotoUrl().toString() : null;
        String username = user.getDisplayName();
        String uid = user.getUid();

        return userDataSource.createUser(uid, username, urlPicture);
    }

    public Task<DocumentSnapshot> getUser(String uid) {
        return userDataSource.getUser(uid);
    }

    public Task<Void> updateUsername(String username, String uid) {
        return userDataSource.updateUsername(username, uid);
    }

    public Task<Void> deleteUser(String uid) {
        return userDataSource.deleteUser(uid);
    }

}
