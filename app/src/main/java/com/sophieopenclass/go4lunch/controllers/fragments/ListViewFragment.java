package com.sophieopenclass.go4lunch.controllers.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.sophieopenclass.go4lunch.MyViewModel;
import com.sophieopenclass.go4lunch.base.BaseActivity;
import com.sophieopenclass.go4lunch.controllers.activities.MainActivity;
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

public class ListViewFragment extends Fragment {
    public static final String TAG = "listviewfragment";
    private MyViewModel viewModel;
    private RecyclerViewRestaurantsBinding binding;
    private BaseActivity context;
    private List<AutocompletePrediction> predictionList;
    //private EndlessRecyclerViewScrollListener scrollListener;


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

        MainActivity activity = ((MainActivity) getActivity());
        if (activity != null) {
            initSearchBar(activity);
        }
        return binding.getRoot();
    }

    private void initSearchBar(MainActivity activity) {
        activity.binding.closeSearchBar.setOnClickListener(v -> activity.binding.searchBar.setVisibility(View.GONE));
        final AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();
        activity.binding.searchBarInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                FindAutocompletePredictionsRequest predictionsRequest = FindAutocompletePredictionsRequest.builder()
                        .setTypeFilter(TypeFilter.ESTABLISHMENT)
                        .setSessionToken(token)
                        .setOrigin(new LatLng(context.currentLocation.getLatitude(), context.currentLocation.getLongitude()))
                        .setQuery(s.toString())
                        .build();
                activity.placesClient.findAutocompletePredictions(predictionsRequest).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FindAutocompletePredictionsResponse predictionsResponse = task.getResult();
                        if (predictionsResponse != null) {
                            predictionList = predictionsResponse.getAutocompletePredictions();
                            List<String> suggestionsList = new ArrayList<>();
                            for (int i = 0; i < predictionList.size(); i++) {
                                AutocompletePrediction prediction = predictionList.get(i);
                                if (prediction.getPlaceTypes().contains(Place.Type.RESTAURANT)) {
                                    suggestionsList.add(prediction.getPlaceId());
                                }
                            }
                            getPlaceDetailAutocompleteList(suggestionsList);
                        }
                    } else {
                        Log.i(TAG, "Prediction fetching task unsuccessful");
                    }
                });
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void getPlaceDetailAutocompleteList(List<String> suggestionsList) {
        List<PlaceDetails> placeDetailsList = new ArrayList<>();
        for (String placeId : suggestionsList)
            viewModel.getPlaceDetails(placeId).observe(getViewLifecycleOwner(), placeDetails -> {
                placeDetailsList.add(placeDetails);
                if (placeDetailsList.size() == suggestionsList.size())
                    getFullPlaceDetails(placeDetailsList);
            });
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
                .observe(getViewLifecycleOwner(), restaurantsResult -> getFullPlaceDetails(restaurantsResult.getPlaceDetails()));

    }

    // Nearby Search doesn't return all the fields required in a PlaceDetails, therefore another
    // query is necessary to retrieve the missing fields (ex : opening hours)
    private void getFullPlaceDetails(List<PlaceDetails> placeDetailsList) {
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
        ListViewAdapter adapter = new ListViewAdapter(restaurants, context, Glide.with(this));
        binding.recyclerViewRestaurants.setAdapter(adapter);
    }
}
