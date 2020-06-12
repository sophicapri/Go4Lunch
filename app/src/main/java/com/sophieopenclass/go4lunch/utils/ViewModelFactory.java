package com.sophieopenclass.go4lunch.utils;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.sophieopenclass.go4lunch.MyViewModel;
import com.sophieopenclass.go4lunch.repository.MessageDataRepository;
import com.sophieopenclass.go4lunch.repository.RestaurantDataRepository;
import com.sophieopenclass.go4lunch.repository.UserDataRepository;

public class ViewModelFactory implements ViewModelProvider.Factory {
    private RestaurantDataRepository restaurantDataSource;
    private UserDataRepository userDataSource;
    private MessageDataRepository messageDataSource;

    public ViewModelFactory() {}

    public ViewModelFactory(RestaurantDataRepository restaurantDataSource,
                            UserDataRepository userDataSource, MessageDataRepository messageDataSource) {
        this.restaurantDataSource = restaurantDataSource;
        this.userDataSource = userDataSource;
        this.messageDataSource = messageDataSource;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(MyViewModel.class)) {
            return (T) new MyViewModel(restaurantDataSource, userDataSource, messageDataSource);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
