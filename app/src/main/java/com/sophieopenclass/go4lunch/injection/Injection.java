package com.sophieopenclass.go4lunch.injection;

import com.algolia.search.saas.Client;
import com.algolia.search.saas.Index;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sophieopenclass.go4lunch.BuildConfig;
import com.sophieopenclass.go4lunch.api.PlaceApi;
import com.sophieopenclass.go4lunch.api.PlaceService;
import com.sophieopenclass.go4lunch.repository.AlgoliaDataRepository;
import com.sophieopenclass.go4lunch.repository.ChatDataRepository;
import com.sophieopenclass.go4lunch.repository.RestaurantDataRepository;
import com.sophieopenclass.go4lunch.repository.UserDataRepository;
import com.sophieopenclass.go4lunch.utils.ViewModelFactory;

import static com.sophieopenclass.go4lunch.utils.Constants.CHAT_COLLECTION_NAME;
import static com.sophieopenclass.go4lunch.utils.Constants.INDEX_WORKMATES;
import static com.sophieopenclass.go4lunch.utils.Constants.USER_COLLECTION_NAME;

public class Injection {
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


    private static AlgoliaDataRepository provideAlgoliaDataSource() {
        Client client = new Client(BuildConfig.ALGOLIA_APP_ID, BuildConfig.ALGOLIA_API_KEY);
        Index index = client.getIndex(INDEX_WORKMATES);
        return new AlgoliaDataRepository(index);
    }

    public static ViewModelFactory provideViewModelFactory() {
        RestaurantDataRepository restaurantDataSource = provideRestaurantDataSource();
        UserDataRepository userDataSource = provideUserDataSource();
        ChatDataRepository messageDataSource = provideMessageDataSource();
        AlgoliaDataRepository algoliaDataSource = provideAlgoliaDataSource();
        return new ViewModelFactory(restaurantDataSource, userDataSource, messageDataSource, algoliaDataSource);
    }
}
