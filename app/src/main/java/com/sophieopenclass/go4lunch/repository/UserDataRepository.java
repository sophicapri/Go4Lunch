package com.sophieopenclass.go4lunch.repository;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.firestore.CollectionReference;
import com.sophieopenclass.go4lunch.models.User;
import com.sophieopenclass.go4lunch.models.UserPlaceId;

import java.util.List;

import static com.firebase.ui.auth.AuthUI.TAG;
import static com.sophieopenclass.go4lunch.injection.Injection.USER_COLLECTION_NAME;
import static com.sophieopenclass.go4lunch.utils.Constants.PLACE_ID;

public class UserDataRepository {
    private CollectionReference userCollectionRef;
    private CollectionReference placesCollectionRef;

    public UserDataRepository(CollectionReference userCollectionRef, CollectionReference placesCollectionRef) {
        this.userCollectionRef = userCollectionRef;
        this.placesCollectionRef = placesCollectionRef;
    }

    public CollectionReference getCollectionReference(String userCollection) {
        if (userCollection.equals(USER_COLLECTION_NAME))
            return userCollectionRef;
        else
            return placesCollectionRef;
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

    public MutableLiveData<List<User>> getUsersByPlaceId(String placeId) {
        MutableLiveData<List<User>> users = new MutableLiveData<>();
        placesCollectionRef.whereEqualTo("placeId", placeId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful())
                if (task.getResult() != null)
                    users.postValue(task.getResult().toObjects(User.class));
                else if (task.getException() != null)
                    Log.e(TAG, "getUsersByPlaceId: " + (task.getException().getMessage()));
        });
        return users;
    }

    public MutableLiveData<String> getPlaceId(String userId) {
        MutableLiveData<String> placeId = new MutableLiveData<>();
        placesCollectionRef.document(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful())
                if (task.getResult() != null)
                    placeId.postValue(task.getResult().getString("placeId"));
                else if (task.getException() != null)
                    Log.e(TAG, "getPlaceId: " + (task.getException().getMessage()));
        });
        return placeId;
    }

    //TODO : how to write the method properly ? do I return a void value ?
    public void updateUserPlaceId(String uid, String placeId) {
        MutableLiveData<String> newPlaceId = new MutableLiveData<>();
        placesCollectionRef.document(uid).update(PLACE_ID, placeId).addOnCompleteListener(updatePlaceId -> {
            if (updatePlaceId.isSuccessful())
                newPlaceId.setValue(placeId);
            else if (updatePlaceId.getException() != null)
                Log.e(TAG, "updatePlaceId: " + (updatePlaceId.getException().getMessage()));
        });
    }


    public MutableLiveData<String> updateUsername(String username, String uid) {
        MutableLiveData<String> newUsername = new MutableLiveData<>();
        userCollectionRef.document(uid).update("username", username).addOnCompleteListener(updateUsername -> {
            if (updateUsername.isSuccessful())
                newUsername.setValue(username);
            else if (updateUsername.getException() != null)
                Log.e(TAG, "updatePlaceId: " + (updateUsername.getException().getMessage()));
        });
        return newUsername;
    }


    // TODO : is this good practice ?
    public MutableLiveData<Void> deleteUser(String uid) {
        MutableLiveData<Void> deletedUser = new MutableLiveData<>();

        userCollectionRef.document(uid).delete().addOnCompleteListener(deleteUser -> {
            if (deleteUser.isSuccessful())
                Log.i(TAG, "updatePlaceId: " + (deleteUser.isSuccessful()));
            else if (deleteUser.getException() != null)
                Log.e(TAG, "updatePlaceId: " + (deleteUser.getException().getMessage()));
        });
        return deletedUser;
    }

    public MutableLiveData<String> addUserPlaceId(String uid, String placeId) {
        MutableLiveData<String> newPlaceId = new MutableLiveData<>();
        UserPlaceId userPlaceId = new UserPlaceId(uid, placeId);
        placesCollectionRef.document(uid).get().addOnCompleteListener(uidTask -> {
            if (uidTask.isSuccessful()) {
                if (uidTask.getResult() != null)
                    placesCollectionRef.document(uid).set(userPlaceId).addOnCompleteListener(userCreationTask -> {
                        if (userCreationTask.isSuccessful())
                            newPlaceId.setValue(placeId);
                        else if (userCreationTask.getException() != null)
                            Log.e(TAG, " addPlaceId: " + userCreationTask.getException().getMessage());
                    });
                else
                    newPlaceId.setValue(placeId);
            } else if (uidTask.getException() != null)
                Log.e(TAG, " addPlaceId: " + uidTask.getException().getMessage());
        });
        return newPlaceId;
    }
}
