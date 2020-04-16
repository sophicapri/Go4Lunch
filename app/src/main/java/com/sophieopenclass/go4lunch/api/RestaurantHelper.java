package com.sophieopenclass.go4lunch.api;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

class RestaurantHelper {
    private static final String COLLECTION_NAME = "restaurants";

    // --- COLLECTION REFERENCE ---

    private static CollectionReference getRestaurantsCollection(){
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME);
    }

    // --- CREATE ---

/*    public static Task<Void> createRestaurant(String uid, String username, String urlPicture) {
        // 1 - Create Obj
        Restaurant restaurantToCreate = new Restaurant(uid, username, urlPicture);

        return RestaurantHelper.getRestaurantsCollection().document(uid).set(restaurantToCreate);
    }

 */

    // --- GET ---

    public static Task<DocumentSnapshot> getRestaurant(String uid){
        return RestaurantHelper.getRestaurantsCollection().document(uid).get();
    }
}
