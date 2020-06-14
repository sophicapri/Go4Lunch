package com.sophieopenclass.go4lunch;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
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

import java.util.List;

import static org.mockito.Mockito.*;

@RunWith(JUnit4.class)
public class PlaceServiceTest {
    private MyViewModel viewModel;
    @Mock
    ChatDataRepository messageDataSource;

    @Mock
    RestaurantDataRepository restaurantDataSource;

    @Mock
    UserDataRepository userDataSource;


    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        viewModel = new MyViewModel(restaurantDataSource, userDataSource, messageDataSource);
    }

    @Test
    public void test_get_nearby_places(){
        LiveData<RestaurantsResult> restaurantsResult = new MutableLiveData<>(mock(RestaurantsResult.class));
        when(viewModel.getNearbyPlaces(anyString())).thenReturn(restaurantsResult);
    }

    @Test
    public void test_get_more_nearby_places(){
        LiveData<RestaurantsResult> restaurantsResult = new MutableLiveData<>();
        when(viewModel.getMoreNearbyPlaces(anyString())).thenReturn(restaurantsResult);
    }

    @Test
    public void test_get_place_details(){
        LiveData<PlaceDetails> placeDetails = new MutableLiveData<>();
        when(viewModel.getPlaceDetails(anyString(), anyString())).thenReturn(placeDetails);
    }

    @Test
    public void test_get_place_details_list(){
        LiveData<List<PlaceDetails>> placeDetails = new MutableLiveData<>();
        when(viewModel.getPlaceDetailsList(anyList(), anyString())).thenReturn(placeDetails);
    }

    @Test
    public void test_update_chosen_restaurant(){
        LiveData<Restaurant> placeDetails = new MutableLiveData<>();
        when(viewModel.updateChosenRestaurant(anyString(), any(), anyString())).thenReturn(placeDetails);
    }

    @Test
    public void test_get_user(){
        LiveData<User> user = new MutableLiveData<>();
        when(viewModel.getUser(anyString())).thenReturn(user);
    }

    @Test
    public void test_get_list_users(){
        LiveData<List<User>> users = new MutableLiveData<>();
        when(viewModel.getListUsers()).thenReturn(users);
    }

    @Test
    public void test_get_users_eating_at_restaurant(){
        LiveData<List<User>> users = new MutableLiveData<>();
        when(viewModel.getUsersEatingAtRestaurantToday(anyString(), anyString())).thenReturn(users);
    }

    @Test
    public void test_create_chat(){
        LiveData<Boolean> success = new MutableLiveData<>();
        when(viewModel.createChat(anyString(), anyString())).thenReturn(success);
    }

    @Test
    public void test_create_message_for_chat(){
        LiveData<Message> message = new MutableLiveData<>();
        when(viewModel.createMessageForChat(anyString(), anyString(), anyString())).thenReturn(message);
    }

    @Test
    public void test_get_chat_id(){
        LiveData<String> chatId = new MutableLiveData<>();
        when(viewModel.getChatId(anyString(), anyString())).thenReturn(chatId);
    }
}