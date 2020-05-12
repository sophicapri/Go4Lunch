package com.sophieopenclass.go4lunch.injection;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.sophieopenclass.go4lunch.MyViewModel;
import com.sophieopenclass.go4lunch.utils.ViewModelFactory;
import com.sophieopenclass.go4lunch.api.PlaceApi;
import com.sophieopenclass.go4lunch.api.PlaceService;
import com.sophieopenclass.go4lunch.repository.RestaurantDataRepository;
import com.sophieopenclass.go4lunch.repository.UserDataRepository;

public class Injection {
    public static final String USER_COLLECTION_NAME = "users";
    public static final String PLACES_COLLECTION_NAME = "placesAndUsers";

    private static RestaurantDataRepository provideRestaurantDataSource(){
        PlaceApi placeApi = PlaceService.createService(PlaceApi.class);
        return new RestaurantDataRepository(placeApi);
    }

    private static UserDataRepository provideUserDataSource() {
        CollectionReference userCollectionReference = FirebaseFirestore.getInstance().collection(USER_COLLECTION_NAME);
        CollectionReference placesCollectionReference = FirebaseFirestore.getInstance().collection(PLACES_COLLECTION_NAME);
        return new UserDataRepository(userCollectionReference, placesCollectionReference);
    }

    public static ViewModelFactory provideViewModelFactory() {
        RestaurantDataRepository restaurantDataSource = provideRestaurantDataSource();
        UserDataRepository userDataSource = provideUserDataSource();
        return new ViewModelFactory(restaurantDataSource, userDataSource);
    }
}
