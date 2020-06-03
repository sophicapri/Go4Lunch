package com.sophieopenclass.go4lunch.repository;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.sophieopenclass.go4lunch.models.Restaurant;
import com.sophieopenclass.go4lunch.models.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.firebase.ui.auth.AuthUI.TAG;
import static com.sophieopenclass.go4lunch.utils.Constants.DATES_AND_RESTAURANTS_FIELD;
import static com.sophieopenclass.go4lunch.utils.Constants.FAVORITE_RESTAURANTS_FIELD;
import static com.sophieopenclass.go4lunch.utils.Constants.PLACE_ID_FIELD;
import static com.sophieopenclass.go4lunch.utils.Constants.USERNAME_FIELD;

public class UserDataRepository {
    private CollectionReference userCollectionRef;

    public UserDataRepository(CollectionReference userCollectionRef) {
        this.userCollectionRef = userCollectionRef;
    }

    public CollectionReference getCollectionReference() {
        return userCollectionRef;
    }

    public MutableLiveData<User> createUser(User user) {
        MutableLiveData<User> userToCreate = new MutableLiveData<>();
        userCollectionRef.document(user.getUid()).get().addOnCompleteListener(uidTask -> {
            if (uidTask.isSuccessful()) {
                if (uidTask.getResult() != null)
                    userCollectionRef.document(user.getUid()).set(user).addOnCompleteListener(userCreationTask -> {
                        if (userCreationTask.isSuccessful())
                            userToCreate.setValue(user);
                        else if (userCreationTask.getException() != null)
                            Log.e(TAG, " createUser: " + userCreationTask.getException().getMessage());
                    });
                else
                    userToCreate.setValue(user);
            } else if (uidTask.getException() != null)
                Log.e(TAG, " createUser: " + uidTask.getException().getMessage());
        });
        return userToCreate;
    }

    public MutableLiveData<User> getUser(String uid) {
        MutableLiveData<User> userData = new MutableLiveData<>();
        userCollectionRef.document(uid).get().addOnCompleteListener(task -> {
            if (task.isSuccessful())
                if (task.getResult() != null)
                    userData.postValue(task.getResult().toObject(User.class));
                else if (task.getException() != null)
                    Log.e(TAG, "getUser" + (task.getException().getMessage()));
        });
        return userData;
    }

    public MutableLiveData<List<User>> getListUsers() {
        MutableLiveData<List<User>> users = new MutableLiveData<>();
        userCollectionRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful())
                if (task.getResult() != null)
                    users.postValue(task.getResult().toObjects(User.class));
                else if (task.getException() != null)
                    Log.e(TAG, "getListUsers: " + (task.getException().getMessage()));
        });
        return users;
    }


    public MutableLiveData<List<User>> getUsersByPlaceIdAndDate(String placeId, String date) {
        MutableLiveData<List<User>> users = new MutableLiveData<>();
        userCollectionRef.whereArrayContains(DATES_AND_RESTAURANTS_FIELD + date + PLACE_ID_FIELD, placeId)
                .get().addOnCompleteListener(task -> {
            if (task.isSuccessful())
                if (task.getResult() != null)
                    users.postValue(task.getResult().toObjects(User.class));
                else if (task.getException() != null)
                    Log.e(TAG, "getUsersByPlaceId: " + (task.getException().getMessage()));
        });
        return users;
    }


    public void addRestaurantToFavorites(Restaurant restaurant, String userId) {
        userCollectionRef.document(userId).update(FAVORITE_RESTAURANTS_FIELD + restaurant.getPlaceId(), restaurant)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful())
                        Log.i(TAG, "addToFavorites: " + (task.isSuccessful()));
                });
    }

    public void updateUsername(String username, String uid) {
        MutableLiveData<String> newUsername = new MutableLiveData<>();
        userCollectionRef.document(uid).update(USERNAME_FIELD, username).addOnCompleteListener(updateUsername -> {
            if (updateUsername.isSuccessful())
                newUsername.setValue(username);
            else if (updateUsername.getException() != null)
                Log.e(TAG, "updatePlaceId: " + (updateUsername.getException().getMessage()));
        });
    }

    public MutableLiveData<String> updateUrlPicture(String urlPicture, String uid) {
        MutableLiveData<String> newUrlPicture = new MutableLiveData<>();
        userCollectionRef.document(uid).update("urlPicture", urlPicture).addOnCompleteListener(updateUrlPicture -> {
            if (updateUrlPicture.isSuccessful())
                newUrlPicture.setValue(urlPicture);
            else if (updateUrlPicture.getException() != null)
                Log.e(TAG, "updateUrlPicture: " + (updateUrlPicture.getException().getMessage()));
        });
        return newUrlPicture;
    }

    public void deleteUser(String uid) {
        userCollectionRef.document(uid).delete().addOnCompleteListener(deleteUser -> {
            if (deleteUser.isSuccessful())
                Log.i(TAG, "deleteUser: " + (deleteUser.isSuccessful()));
            else if (deleteUser.getException() != null)
                Log.e(TAG, "deleteUser: " + (deleteUser.getException().getMessage()));
        });
    }

    public void deleteChosenRestaurant(String uid, String date) {
        Map<String, Object> updates = new HashMap<>();
        updates.put((DATES_AND_RESTAURANTS_FIELD + date), FieldValue.delete());
        userCollectionRef.document(uid).get().addOnCompleteListener(uidTask -> {
            if (uidTask.isSuccessful()) {
                if (uidTask.getResult() != null)
                    userCollectionRef.document(uid).update(updates).addOnCompleteListener(deleteRestaurant -> {
                        if (deleteRestaurant.isSuccessful())
                            Log.i(TAG, "deleteRestaurant: " + (deleteRestaurant.isSuccessful()));
                        else if (deleteRestaurant.getException() != null)
                            Log.e(TAG, "deleteRestaurant: " + (deleteRestaurant.getException().getMessage()));
                    });
                else if (uidTask.getException() != null)
                    Log.e(TAG, " deleteRestaurant: " + uidTask.getException().getMessage());
            }
        });
    }

    public MutableLiveData<Restaurant> updateChosenRestaurant(String uid, Restaurant restaurant, String date) {
        MutableLiveData<Restaurant> newRestaurant = new MutableLiveData<>();
        Map<String, Object> updates = new HashMap<>();
        updates.put((DATES_AND_RESTAURANTS_FIELD + date), restaurant);
        userCollectionRef.document(uid).get().addOnCompleteListener(uidTask -> {
            if (uidTask.isSuccessful()) {
                if (uidTask.getResult() != null)
                    userCollectionRef.document(uid).update(updates).addOnCompleteListener(addPlaceIdTask -> {
                        if (addPlaceIdTask.isSuccessful())
                            newRestaurant.setValue(restaurant);
                        else if (addPlaceIdTask.getException() != null)
                            Log.e("com.sophie.REPO", " addPlaceId: " + addPlaceIdTask.getException().getMessage());
                    });
                else
                    newRestaurant.setValue(restaurant);
            } else if (uidTask.getException() != null)
                Log.e("com.sophie.REPO", " addPlaceId: " + uidTask.getException().getMessage());
        });
        return newRestaurant;
    }

    public void deleteRestaurantFromFavorites(String placeId, String userId) {
        userCollectionRef.document(userId).update(FAVORITE_RESTAURANTS_FIELD + placeId, FieldValue.delete())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful())
                        Log.i(TAG, "deleteFromFavorites: " + (task.isSuccessful()));
                });
    }
}
