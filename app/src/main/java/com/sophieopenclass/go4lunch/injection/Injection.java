package com.sophieopenclass.go4lunch.injection;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sophieopenclass.go4lunch.api.PlaceApi;
import com.sophieopenclass.go4lunch.api.PlaceService;
import com.sophieopenclass.go4lunch.repository.ChatDataRepository;
import com.sophieopenclass.go4lunch.repository.RestaurantDataRepository;
import com.sophieopenclass.go4lunch.repository.UserDataRepository;
import com.sophieopenclass.go4lunch.utils.ViewModelFactory;

public class Injection {
    public static final String USER_COLLECTION_NAME = "users";
    private static final String CHAT_COLLECTION_NAME = "conversations";

    private static RestaurantDataRepository provideRestaurantDataSource() {
        PlaceApi placeApi = PlaceService.createService(PlaceApi.class);
        return new RestaurantDataRepository(placeApi);
    }

    private static UserDataRepository provideUserDataSource() {
        CollectionReference userCollectionReference = FirebaseFirestore.getInstance().collection(USER_COLLECTION_NAME);
        return new UserDataRepository(userCollectionReference);
    }

    private static ChatDataRepository provideMessageDataSource() {
        CollectionReference chatCollectionRef = FirebaseFirestore.getInstance().collection(CHAT_COLLECTION_NAME);
        return new ChatDataRepository(chatCollectionRef);
    }

    public static ViewModelFactory provideViewModelFactory() {
        RestaurantDataRepository restaurantDataSource = provideRestaurantDataSource();
        UserDataRepository userDataSource = provideUserDataSource();
        ChatDataRepository messageDataSource = provideMessageDataSource();
        return new ViewModelFactory(restaurantDataSource, userDataSource, messageDataSource);
    }
}
