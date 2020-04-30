package com.sophieopenclass.go4lunch.utils;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.sophieopenclass.go4lunch.MyViewModel;
import com.sophieopenclass.go4lunch.repository.RestaurantDataRepository;
import com.sophieopenclass.go4lunch.repository.UserDataRepository;

public class ViewModelFactory implements ViewModelProvider.Factory {
    private final RestaurantDataRepository restaurantDataSource;
    private final UserDataRepository userDataSource;

    public ViewModelFactory(RestaurantDataRepository restaurantDataSource,
                            UserDataRepository userDataSource) {
        this.restaurantDataSource = restaurantDataSource;
        this.userDataSource = userDataSource;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(MyViewModel.class)) {
            return (T) new MyViewModel(restaurantDataSource, userDataSource);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
