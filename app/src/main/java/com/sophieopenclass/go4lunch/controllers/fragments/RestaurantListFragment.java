package com.sophieopenclass.go4lunch.controllers.fragments;

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
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.sophieopenclass.go4lunch.MyViewModel;
import com.sophieopenclass.go4lunch.base.BaseActivity;
import com.sophieopenclass.go4lunch.controllers.activities.MainActivity;
import com.sophieopenclass.go4lunch.controllers.adapters.ListViewAdapter;
import com.sophieopenclass.go4lunch.databinding.RecyclerViewRestaurantsBinding;
import com.sophieopenclass.go4lunch.models.User;
import com.sophieopenclass.go4lunch.models.json_to_java.PlaceDetails;
import com.sophieopenclass.go4lunch.utils.EndlessRecyclerViewScrollListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.sophieopenclass.go4lunch.controllers.fragments.MapViewFragment.getLatLngString;

public class RestaurantListFragment extends Fragment {
    public static final String TAG = "restaurantListFrag";
    public static final double AREA_LIST_AUTOCOMPLETE = 0.02;
    private MyViewModel viewModel;
    private RecyclerViewRestaurantsBinding binding;
    private BaseActivity context;
    private List<AutocompletePrediction> predictionList;
    private LinearLayoutManager linearLayoutManager;
    private boolean autocompleteActive = false;
    private String nextPageToken;
    private ListViewAdapter adapter;
    private List<PlaceDetails> restaurants = new ArrayList<>();
    private final AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();
    private MainActivity activity;

    public static Fragment newInstance() {
        return new RestaurantListFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = RecyclerViewRestaurantsBinding.inflate(inflater, container, false);
        if (getActivity() != null) {
            context = (BaseActivity) getActivity();
            viewModel = (MyViewModel) ((BaseActivity) getActivity()).getViewModel();
        }
        activity = ((MainActivity) getActivity());
        if (activity != null) {
            initSearchBar(activity);
            activity.binding.progressBar.setVisibility(View.VISIBLE);
        }
        return binding.getRoot();
    }

    private void initSearchBar(MainActivity activity) {
        activity.binding.searchBarListView.closeSearchBar.setOnClickListener(v -> {
            activity.binding.searchBarListView.searchBarListView.setVisibility(View.GONE);
            autocompleteActive = false;
            observePlaces(null);
        });
        activity.binding.searchBarListView.searchBarInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                autocompleteActive = true;
                restaurants.clear();
                adapter.notifyDataSetChanged();
                displayResultsAutocomplete(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void displayResultsAutocomplete(String textInput) {
        LatLng northEast = new LatLng(BaseActivity.currentLocation.getLatitude() - AREA_LIST_AUTOCOMPLETE,
                BaseActivity.currentLocation.getLongitude() - (AREA_LIST_AUTOCOMPLETE));
        LatLng southWest = new LatLng(BaseActivity.currentLocation.getLatitude() + (AREA_LIST_AUTOCOMPLETE),
                BaseActivity.currentLocation.getLongitude() + (AREA_LIST_AUTOCOMPLETE));

        FindAutocompletePredictionsRequest predictionsRequest = FindAutocompletePredictionsRequest.builder()
                .setTypeFilter(TypeFilter.ESTABLISHMENT)
                .setSessionToken(token)
                .setLocationRestriction(RectangularBounds.newInstance(northEast, southWest))
                .setQuery(textInput)
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

    private void getPlaceDetailAutocompleteList(List<String> suggestionsList) {
        List<PlaceDetails> placeDetailsList = new ArrayList<>();
        for (String placeId : suggestionsList)
            viewModel.getPlaceDetails(placeId).observe(getViewLifecycleOwner(), placeDetails -> {
                placeDetailsList.add(placeDetails);
                if (placeDetailsList.size() == suggestionsList.size()) {
                    getFullPlaceDetails(placeDetailsList);
                }
            });
    }

    @Override
    public void onResume() {
        if (restaurants.isEmpty())
            observePlaces(null);
        super.onResume();
    }

    private void observePlaces(String nextPageToken) {
        if (nextPageToken == null)
            viewModel.getNearbyPlaces(getLatLngString(BaseActivity.currentLocation))
                    .observe(getViewLifecycleOwner(), restaurantsResult -> {
                        getFullPlaceDetails(restaurantsResult.getPlaceDetails());
                        this.nextPageToken = restaurantsResult.getNextPageToken();
                    });
        else
            viewModel.getMoreNearbyPlaces(nextPageToken).observe(getViewLifecycleOwner()
                    , restaurantsResult -> {
                        getFullPlaceDetails(restaurantsResult.getPlaceDetails());
                        if (this.nextPageToken.equals(restaurantsResult.getNextPageToken()))
                            this.nextPageToken = null;
                        else
                            this.nextPageToken = restaurantsResult.getNextPageToken();
                    });
    }

    // Nearby Search doesn't return all the fields required in a PlaceDetails, therefore another
    // query is necessary to retrieve the missing fields (ex : openingHours)

    // The viewModel calls are imbricated and not called one after the other because the variables do not get initialised
    // fast enough before being used for another viewModel.
    private void getFullPlaceDetails(List<PlaceDetails> placeDetailsList) {
        ArrayList<PlaceDetails> completePlaceDetailsList = new ArrayList<>();
        for (PlaceDetails placeDetails : placeDetailsList) {
            viewModel.getPlaceDetails(placeDetails.getPlaceId())
                    .observe(getViewLifecycleOwner(), restaurant ->
                            viewModel.getUsersByPlaceIdAndDate(placeDetails.getPlaceId(), User.getTodaysDate())
                                    .observe(getViewLifecycleOwner(), users -> {
                                        restaurant.setNbrOfWorkmates(users.size());
                                        // add setNbrOfStars
                                        completePlaceDetailsList.add(restaurant);
                                        if (completePlaceDetailsList.size() == placeDetailsList.size()) {
                                            Collections.sort(completePlaceDetailsList, new NearestRestaurantComparator());
                                            if ((!restaurants.isEmpty()) && adapter != null) {
                                                int indexStart = restaurants.size() - 1;
                                                this.restaurants.addAll(completePlaceDetailsList);
                                                adapter.notifyItemRangeInserted(indexStart, completePlaceDetailsList.size());
                                            } else
                                                updateRecyclerView(completePlaceDetailsList);
                                        }
                                    }));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        restaurants.clear();
    }

    /**
     * Comparator to sort places from nearest to furthest
     */
    public static class NearestRestaurantComparator implements Comparator<PlaceDetails> {
        @Override
        public int compare(PlaceDetails left, PlaceDetails right) {
            return left.getDistance() - right.getDistance();
        }
    }

    private void updateRecyclerView(ArrayList<PlaceDetails> restaurants) {
        activity.binding.progressBar.setVisibility(View.GONE);
        linearLayoutManager = new LinearLayoutManager(getContext());
        binding.recyclerViewRestaurants.setHasFixedSize(true);
        binding.recyclerViewRestaurants.setLayoutManager(linearLayoutManager);

        this.restaurants = restaurants;
        adapter = new ListViewAdapter(this.restaurants, context, Glide.with(this));
        binding.recyclerViewRestaurants.setAdapter(adapter);
        EndlessRecyclerViewScrollListener scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                loadNextDataFromApi();
            }
        };
        binding.recyclerViewRestaurants.addOnScrollListener(scrollListener);
    }

    private void loadNextDataFromApi() {
        if (!autocompleteActive) {
            if (nextPageToken != null)
                observePlaces(nextPageToken);
        }

    }
}
