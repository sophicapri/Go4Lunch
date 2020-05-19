package com.sophieopenclass.go4lunch;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.Query;
import com.sophieopenclass.go4lunch.models.Message;
import com.sophieopenclass.go4lunch.models.json_to_java.RestaurantsResult;
import com.sophieopenclass.go4lunch.models.json_to_java.PlaceDetails;
import com.sophieopenclass.go4lunch.models.User;
import com.sophieopenclass.go4lunch.repository.MessageDataRepository;
import com.sophieopenclass.go4lunch.repository.RestaurantDataRepository;
import com.sophieopenclass.go4lunch.repository.UserDataRepository;

import java.util.List;

public class MyViewModel extends ViewModel {
    private RestaurantDataRepository restaurantDataSource;
    private UserDataRepository userDataSource;
    private MessageDataRepository messageDataSource;
    private LiveData<User> createdUserLiveData;

    public MyViewModel(RestaurantDataRepository restaurantDataSource, UserDataRepository userDataSource,
                       MessageDataRepository messageDataSource) {
        this.restaurantDataSource = restaurantDataSource;
        this.userDataSource = userDataSource;
        this.messageDataSource = messageDataSource;
    }

    // RESTAURANTS

    public LiveData<RestaurantsResult> getNearbyPlaces(String location) {
        return restaurantDataSource.getNearbyPlaces(location);
    }

    public LiveData<RestaurantsResult> getMoreNearbyPlaces(String pageToken) {
        return restaurantDataSource.getMoreNearbyPlaces(pageToken);
    }

    public LiveData<PlaceDetails> getPlaceDetails(String placeId) {
        return restaurantDataSource.getPlaceDetails(placeId);
    }

    // USERS

    public void createUser(User user) {
        createdUserLiveData = userDataSource.createUser(user);
    }

    public LiveData<User> getUser(String uid) {
        return userDataSource.getUser(uid);
    }

    public CollectionReference getCollectionReference() {
        return userDataSource.getCollectionReference();
    }

    public LiveData<List<User>> getListUsers() {
        return userDataSource.getListUsers();
    }

    public LiveData<List<User>> getUsersByPlaceIdDate(String placeId, String date) {
        return userDataSource.getUsersByPlaceIdDate(placeId, date);
    }

    public LiveData<String> updateUserPlaceId(String uid, String placeId, String date) {
        return userDataSource.updateUserPlaceId(uid, placeId, date);
    }

    public void updateRestaurantName(String uid, String restaurantName) {
        userDataSource.updateRestaurantName(uid, restaurantName);
    }

    public void deleteDatesAndPlaceIdsField(String uid) {
        userDataSource.deleteDatesAndPlaceIdsField(uid);
    }


    public void deletePlaceId(String uid, String date) {
        userDataSource.deletePlaceId(uid, date);
    }

    public LiveData<User> getCreatedUserLiveData() {
        return createdUserLiveData;
    }


    public LiveData<String> getPlaceIdByDate(String userId, String date) {
        return userDataSource.getPlaceIdByDate(userId, date);
    }


    public LiveData<String> updateUsername(String username, String uid) {
        return userDataSource.updateUsername(username, uid);
    }

    public void deleteUser(String uid) {
        userDataSource.deleteUser(uid);
    }


    // CHAT

    public LiveData<Message> createMessageForChat(String textMessage, String userSenderId, String chatId) {
        return messageDataSource.createMessageForChat(textMessage, userSenderId,chatId);
    }

    public LiveData<Message> createMessageWithImageForChat(String urlImage, String textMessage, String userSenderId) {
        return messageDataSource.createMessageWithImageForChat(urlImage, textMessage, userSenderId);
    }

    public LiveData<String> getChatId(String currentUserId, String workmateId) {
        return messageDataSource.getChatId(currentUserId, workmateId);
    }


    public Query getMessagesQuery(String chatId) {
        return messageDataSource.getMessagesQuery(chatId);
    }

    public LiveData<Boolean> createChat(String currentUserId, String workmateId) {
       return messageDataSource.createChat(currentUserId, workmateId);
    }

}
