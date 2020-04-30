package com.sophieopenclass.go4lunch.repository;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.sophieopenclass.go4lunch.models.User;

public class UserDataRepository {
    private CollectionReference collectionReference;

    public UserDataRepository(CollectionReference collectionReference){
        this.collectionReference = collectionReference;
    }

    public Task<Void> createUser(String uid, String username, String urlPicture) {
        User userToCreate = new User(uid, username, urlPicture);

        return collectionReference.document(uid).set(userToCreate);
    }

    public Task<DocumentSnapshot> getUser(String uid){
        return collectionReference.document(uid).get();
    }

    public Task<Void> updateUsername(String username, String uid) {
        return collectionReference.document(uid).update("username", username);
    }

    public Task<Void> deleteUser(String uid) {
        return collectionReference.document(uid).delete();
    }
}
