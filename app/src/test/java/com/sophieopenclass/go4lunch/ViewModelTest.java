package com.sophieopenclass.go4lunch;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.MutableLiveData;

import com.sophieopenclass.go4lunch.models.Message;
import com.sophieopenclass.go4lunch.models.Restaurant;
import com.sophieopenclass.go4lunch.models.User;
import com.sophieopenclass.go4lunch.models.json_to_java.PlaceDetails;
import com.sophieopenclass.go4lunch.models.json_to_java.RestaurantsResult;
import com.sophieopenclass.go4lunch.repository.ChatDataRepository;
import com.sophieopenclass.go4lunch.repository.RestaurantDataRepository;
import com.sophieopenclass.go4lunch.repository.UserDataRepository;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(JUnit4.class)
public class ViewModelTest {
    private MyViewModel viewModel;
    @Mock
    ChatDataRepository chatDataSource;

    @Mock
    RestaurantDataRepository restaurantDataSource;

    @Mock
    UserDataRepository userDataSource;

    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        viewModel = new MyViewModel(restaurantDataSource, userDataSource, chatDataSource);
    }

    @Test
    public void test_get_nearby_places() {
        MutableLiveData<RestaurantsResult> expectedRestaurantsResult = new MutableLiveData<>(mock(RestaurantsResult.class));
        when(restaurantDataSource.getNearbyPlaces(anyString())).thenReturn(expectedRestaurantsResult);
        viewModel.getNearbyPlaces(anyString())
                .observeForever(restaurantsResult -> assertSame(expectedRestaurantsResult.getValue(), restaurantsResult));
    }

    @Test
    public void test_get_more_nearby_places() {
        MutableLiveData<RestaurantsResult> expectedRestaurantsResult = new MutableLiveData<>(mock(RestaurantsResult.class));
        when(restaurantDataSource.getMoreNearbyPlaces(anyString())).thenReturn(expectedRestaurantsResult);

        viewModel.getMoreNearbyPlaces(anyString())
                .observeForever(restaurantsResult -> assertSame(expectedRestaurantsResult.getValue(), restaurantsResult));
    }

    @Test
    public void test_get_place_details() {
        MutableLiveData<PlaceDetails> expectedPlaceDetails = new MutableLiveData<>(mock(PlaceDetails.class));
        when(restaurantDataSource.getPlaceDetails(anyString(), anyString())).thenReturn(expectedPlaceDetails);
        viewModel.getPlaceDetails(anyString(), anyString()).observeForever(placeDetails ->
                assertSame(expectedPlaceDetails.getValue(), placeDetails));
    }

    @Test
    public void test_get_place_details_list() {
        List<PlaceDetails> mockList = new ArrayList<>();
        mockList.add(mock(PlaceDetails.class));
        MutableLiveData<List<PlaceDetails>> expectedPlaceDetailsList = new MutableLiveData<>();
        expectedPlaceDetailsList.setValue(mockList);
        when(restaurantDataSource.getPlaceDetailsList(anyList(), anyString())).thenReturn(expectedPlaceDetailsList);
        viewModel.getPlaceDetailsList(anyList(), anyString()).observeForever(placeDetailsList ->
                assertSame(expectedPlaceDetailsList.getValue(), placeDetailsList));
    }

    @Test
    public void test_update_chosen_restaurant() {
        MutableLiveData<Restaurant> expectedRestaurant = new MutableLiveData<>(mock(Restaurant.class));
        when(userDataSource.updateChosenRestaurant(anyString(), any(), anyString())).thenReturn(expectedRestaurant);
        viewModel.updateChosenRestaurant(anyString(), any(), anyString()).observeForever(restaurant ->
                assertSame(expectedRestaurant.getValue(), restaurant));
    }

    @Test
    public void test_get_user() {
        MutableLiveData<User> expectedUser = new MutableLiveData<>(mock(User.class));
        when(userDataSource.getUser(anyString())).thenReturn(expectedUser);
        viewModel.getUser(anyString()).observeForever(user -> assertSame(expectedUser.getValue(), user));
    }

    @Test
    public void test_get_list_users() {
        List<User> mockList = new ArrayList<>();
        mockList.add(mock(User.class));
        MutableLiveData<List<User>> expectedUsers = new MutableLiveData<>();
        expectedUsers.setValue(mockList);
        when(userDataSource.getListUsers()).thenReturn(expectedUsers);
        viewModel.getListUsers().observeForever(users -> assertSame(expectedUsers.getValue(), users));
    }

    @Test
    public void test_get_users_eating_at_restaurant() {
        List<User> mockList = new ArrayList<>();
        mockList.add(mock(User.class));
        MutableLiveData<List<User>> expectedUsers = new MutableLiveData<>();
        expectedUsers.setValue(mockList);
        when(userDataSource.getUsersEatingAtRestaurantToday(anyString(), anyString())).thenReturn(expectedUsers);
        viewModel.getUsersEatingAtRestaurantToday(anyString(), anyString())
                .observeForever(users -> assertSame(expectedUsers.getValue(), users));
    }

    @Test
    public void test_create_chat() {
        MutableLiveData<Boolean> expectedBoolean = new MutableLiveData<>();
        when(chatDataSource.createChat(anyString(), anyString())).thenReturn(expectedBoolean);
        viewModel.createChat(anyString(), anyString())
                .observeForever(aBoolean -> assertSame(expectedBoolean.getValue(), aBoolean));
    }

    @Test
    public void test_create_message_for_chat() {
        MutableLiveData<Message> expectedMessage = new MutableLiveData<>(mock(Message.class));
        when(chatDataSource.createMessageForChat(anyString(), anyString(), anyString())).thenReturn(expectedMessage);
        viewModel.createMessageForChat(anyString(), anyString(), anyString())
                .observeForever(message -> assertSame(expectedMessage.getValue(), message));
    }

    @Test
    public void test_get_chat_id() {
        MutableLiveData<String> expectedChatId = new MutableLiveData<>();
        when(chatDataSource.getChatId(anyString(), anyString())).thenReturn(expectedChatId);
        viewModel.getChatId(anyString(), anyString())
                .observeForever(chatId -> assertSame(expectedChatId.getValue(), chatId));

    }
}