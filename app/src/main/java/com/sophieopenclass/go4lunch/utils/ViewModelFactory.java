package com.sophieopenclass.go4lunch.utils;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.sophieopenclass.go4lunch.MyViewModel;
import com.sophieopenclass.go4lunch.repository.AlgoliaDataRepository;
import com.sophieopenclass.go4lunch.repository.ChatDataRepository;
import com.sophieopenclass.go4lunch.repository.RestaurantDataRepository;
import com.sophieopenclass.go4lunch.repository.UserDataRepository;

public class ViewModelFactory implements ViewModelProvider.Factory {
    private RestaurantDataRepository restaurantDataSource;
    private UserDataRepository userDataSource;
    private ChatDataRepository chatDataSource;
    private AlgoliaDataRepository algoliaDataSource;

    public ViewModelFactory(RestaurantDataRepository restaurantDataSource,
                            UserDataRepository userDataSource, ChatDataRepository chatDataSource, AlgoliaDataRepository algoliaDataSource) {
        this.restaurantDataSource = restaurantDataSource;
        this.userDataSource = userDataSource;
        this.chatDataSource = chatDataSource;
        this.algoliaDataSource = algoliaDataSource;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(MyViewModel.class)) {
            return (T) new MyViewModel(restaurantDataSource, userDataSource, chatDataSource, algoliaDataSource);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
