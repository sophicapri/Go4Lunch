package com.sophieopenclass.go4lunch.controllers.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sophieopenclass.go4lunch.MyViewModel;
import com.sophieopenclass.go4lunch.base.BaseActivity;
import com.sophieopenclass.go4lunch.controllers.activities.RestaurantDetailsActivity;
import com.sophieopenclass.go4lunch.controllers.adapters.ListViewAdapter;
import com.sophieopenclass.go4lunch.databinding.RecyclerViewRestaurantsBinding;
import com.sophieopenclass.go4lunch.listeners.Listeners;
import com.sophieopenclass.go4lunch.models.User;
import com.sophieopenclass.go4lunch.models.json_to_java.PlaceDetails;
import com.sophieopenclass.go4lunch.models.json_to_java.RestaurantsResult;

import java.util.ArrayList;
import java.util.List;

import static com.sophieopenclass.go4lunch.controllers.fragments.MapViewFragment.getLatLngString;
import static com.sophieopenclass.go4lunch.utils.Constants.PLACE_ID;

public class ListViewFragment extends Fragment {
    private MyViewModel viewModel;
    private RecyclerViewRestaurantsBinding binding;
    private BaseActivity context;
    private ArrayList<Integer> usersEatingAtRestaurant;

    public static Fragment newInstance() {
        return new ListViewFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = RecyclerViewRestaurantsBinding.inflate(inflater, container, false);
        if (getActivity() != null) {
            context = (BaseActivity) getActivity();
            viewModel = (MyViewModel) ((BaseActivity) getActivity()).getViewModel();
        }
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        observePlaces();
    }

    private void observePlaces() {
        binding.recyclerViewRestaurants.setHasFixedSize(true);
        binding.recyclerViewRestaurants.setLayoutManager(new LinearLayoutManager(getContext()));


        viewModel.getNearbyPlaces(getLatLngString(context.currentLocation))
                .observe(getViewLifecycleOwner(), this::getFullPlaceDetails);

    }

    // Nearby Search doesn't return all the fields required in a PlaceDetails, therefore another
    // query is necessary to retrieve the missing fields (ex : opening hours)
    private void getFullPlaceDetails(RestaurantsResult restaurantsResult) {
        List<PlaceDetails> placeDetailsList = restaurantsResult.getPlaceDetails();
        ArrayList<PlaceDetails> completePlaceDetailsList = new ArrayList<>();

        for (PlaceDetails placeDetails : placeDetailsList) {
            viewModel.getPlaceDetails(placeDetails.getPlaceId())
                    .observe(getViewLifecycleOwner(), restaurant -> {
                        viewModel.getUsersByPlaceIdDate(placeDetails.getPlaceId(), User.getTodaysDate())
                                .observe(getViewLifecycleOwner(), users -> {
                                    restaurant.setNbrOfWorkmates(users.size());
                                    completePlaceDetailsList.add(restaurant);
                                    if (completePlaceDetailsList.size() == placeDetailsList.size()) {
                                        updateRecyclerView(completePlaceDetailsList);
                                    }
                                });
                    });
        }
    }

    private void updateRecyclerView(ArrayList<PlaceDetails> restaurants) {
        ListViewAdapter adapter = new ListViewAdapter(restaurants, context);
        binding.recyclerViewRestaurants.setAdapter(adapter);
    }
}
