package com.sophieopenclass.go4lunch.injection;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sophieopenclass.go4lunch.MyViewModel;
import com.sophieopenclass.go4lunch.utils.ViewModelFactory;
import com.sophieopenclass.go4lunch.api.PlaceApi;
import com.sophieopenclass.go4lunch.api.PlaceService;
import com.sophieopenclass.go4lunch.repository.RestaurantDataRepository;
import com.sophieopenclass.go4lunch.repository.UserDataRepository;

public class Injection {
    private static MyViewModel viewModel;

    private static RestaurantDataRepository provideRestaurantDataSource(){
        PlaceApi placeApi = PlaceService.createService(PlaceApi.class);
        return new RestaurantDataRepository(placeApi);
    }

    private static UserDataRepository provideUserDataSource(String userCollectionName) {
        CollectionReference collectionReference = FirebaseFirestore.getInstance().collection(userCollectionName);
        return new UserDataRepository(collectionReference);
    }

    public static ViewModelFactory provideViewModelFactory(String userCollectionName) {
        RestaurantDataRepository restaurantDataSource = provideRestaurantDataSource();
        UserDataRepository userDataSource = provideUserDataSource(userCollectionName);
        return new ViewModelFactory(restaurantDataSource, userDataSource);
    }

    public static void setViewModel(MyViewModel pViewModel){
        viewModel = pViewModel;
    }

    public static MyViewModel getViewModel(){
        return viewModel;
    }
}
