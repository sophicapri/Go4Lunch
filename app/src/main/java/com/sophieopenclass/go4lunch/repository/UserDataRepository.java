package com.sophieopenclass.go4lunch.repository;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.sophieopenclass.go4lunch.models.User;

import static com.firebase.ui.auth.AuthUI.TAG;

public class UserDataRepository {
    private CollectionReference collectionReference;

    public UserDataRepository(CollectionReference collectionReference) {
        this.collectionReference = collectionReference;
    }

    public CollectionReference getUsersCollectionReference() {
        return collectionReference;
    }

    public MutableLiveData<User> createUser(User user) {
        MutableLiveData<User> userToCreate = new MutableLiveData<>();
        collectionReference.document(user.getUid()).set(userToCreate).addOnCompleteListener(userCreationTask -> {
            if (userCreationTask.isSuccessful())
                userToCreate.setValue(user);
            else if (userCreationTask.getException() != null)
                Log.e(TAG, "createUser: " + (userCreationTask.getException().getMessage()));
        });
        return userToCreate;
    }

    public MutableLiveData<User> getUser(String uid) {
        MutableLiveData<User> userData = new MutableLiveData<>();
        collectionReference.document(uid).get().addOnSuccessListener(documentSnapshot ->
                userData.postValue(documentSnapshot.toObject(User.class)));
        return userData;
    }


    // TODO change to MutableLiveData
    public Task<Void> updateUsername(String username, String uid) {
        return collectionReference.document(uid).update("username", username);
    }

    public Task<Void> deleteUser(String uid) {
        return collectionReference.document(uid).delete();
    }
}
