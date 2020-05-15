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

    private static RestaurantDataRepository provideRestaurantDataSource(){
        PlaceApi placeApi = PlaceService.createService(PlaceApi.class);
        return new RestaurantDataRepository(placeApi);
    }

    private static UserDataRepository provideUserDataSource() {
        CollectionReference userCollectionReference = FirebaseFirestore.getInstance().collection(USER_COLLECTION_NAME);
        return new UserDataRepository(userCollectionReference);
    }

    public static ViewModelFactory provideViewModelFactory() {
        RestaurantDataRepository restaurantDataSource = provideRestaurantDataSource();
        UserDataRepository userDataSource = provideUserDataSource();
        return new ViewModelFactory(restaurantDataSource, userDataSource);
    }
}
