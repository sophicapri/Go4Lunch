package com.sophieopenclass.go4lunch.view.fragments;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.maps.android.SphericalUtil;
import com.sophieopenclass.go4lunch.AppController;
import com.sophieopenclass.go4lunch.MyViewModel;
import com.sophieopenclass.go4lunch.R;
import com.sophieopenclass.go4lunch.view.activities.MainActivity;
import com.sophieopenclass.go4lunch.view.adapters.RestaurantListAdapter;
import com.sophieopenclass.go4lunch.databinding.RecyclerViewRestaurantsBinding;
import com.sophieopenclass.go4lunch.models.json_to_java.PlaceDetails;
import com.sophieopenclass.go4lunch.utils.PreferenceHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.sophieopenclass.go4lunch.utils.Constants.HEADING_NORTH_WEST;
import static com.sophieopenclass.go4lunch.utils.Constants.HEADING_SOUTH_WEST;
import static com.sophieopenclass.go4lunch.utils.DateFormatting.getTodayDateInString;

public class RestaurantListFragment extends Fragment {
    public static final String TAG = "com.sophie.LIST_RESTO";
    private static final double RADIUS = 500;
    private MyViewModel viewModel;
    private RecyclerViewRestaurantsBinding binding;
    private List<AutocompletePrediction> predictionList;
    private LinearLayoutManager linearLayoutManager;
    private boolean autocompleteActive = false;
    private String nextPageToken;
    private RestaurantListAdapter adapter;
    private ArrayList<PlaceDetails> restaurantList = new ArrayList<>();
    private final AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();
    private MainActivity context;
    private int visibleThreshold = 5;
    private boolean searchBarInputEmpty = false;
    private int bottomProgressBarPosition;
    private boolean isLoading;
    private TextWatcher textWatcher;
    private String currentAppLocale = PreferenceHelper.getCurrentLocale();


    public static Fragment newInstance() {
        return new RestaurantListFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = RecyclerViewRestaurantsBinding.inflate(inflater, container, false);
        if (getActivity() != null)
            context = (MainActivity) getActivity();
        viewModel = context.getViewModel();
        context.binding.progressBar.setVisibility(View.VISIBLE);
        initSearchBar(context);
        configureRecyclerView();
        binding.swipeRefreshView.setOnRefreshListener(() -> {
            observePlaces(nextPageToken);
            binding.swipeRefreshView.setRefreshing(false);
        });
        return binding.getRoot();
    }

    private void configureRecyclerView() {
        linearLayoutManager = new LinearLayoutManager(getContext());
        binding.recyclerViewRestaurants.setHasFixedSize(true);
        binding.recyclerViewRestaurants.setLayoutManager(linearLayoutManager);
        adapter = new RestaurantListAdapter(restaurantList, context, Glide.with(this));
        binding.recyclerViewRestaurants.setAdapter(adapter);
        initScrollListener();
    }

    private void initSearchBar(MainActivity activity) {
        activity.binding.searchBarRestaurantList.closeSearchBar.setOnClickListener(v -> {
            closeSearchBar();
            //To refresh the page only if the user typed something into the search bar
            if (autocompleteActive) {
                adapter.clearList();
                autocompleteActive = false;
                activity.binding.progressBar.setVisibility(View.VISIBLE);
                observePlaces(null);
            }
        });

        textWatcher = new TextWatcher() {
            //to stop the TextWatcher from firing multiple times
            boolean isOnTextChanged = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                isOnTextChanged = true;
            }

            @Override
            public void afterTextChanged(Editable s) {
                adapter.clearList();
                if (!activity.orientationChanged && isOnTextChanged) {
                    isOnTextChanged = false;
                    autocompleteActive = true;
                    if (!s.toString().isEmpty()) {
                        displayResultsAutocomplete(s.toString());
                        searchBarInputEmpty = false;
                    } else
                        searchBarInputEmpty = true;
                }
            }
        };
        activity.binding.searchBarRestaurantList.searchBarInput.addTextChangedListener(textWatcher);
    }

    private void displayResultsAutocomplete(String textInput) {
        FindAutocompletePredictionsRequest predictionsRequest = FindAutocompletePredictionsRequest.builder()
                .setTypeFilter(TypeFilter.ESTABLISHMENT)
                .setSessionToken(token)
                .setLocationRestriction(getRectangularBounds())
                .setQuery(textInput)
                .build();

        context.placesClient.findAutocompletePredictions(predictionsRequest).addOnCompleteListener(task -> {
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

    /**
     * The "heading" params of the SphericalUtil method represent where each corner is in degrees.
     * See visual representation here : https://i.stack.imgur.com/GkFzJ.png;
     */
    private RectangularBounds getRectangularBounds() {
        double distanceFromCenterToCorner = RADIUS * Math.sqrt(2.0);
        Location currentLocation = AppController.getInstance().getCurrentLocation();
        LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        LatLng northEastCorner =
                SphericalUtil.computeOffset(latLng, distanceFromCenterToCorner, HEADING_NORTH_WEST);
        LatLng southWestCorner =
                SphericalUtil.computeOffset(latLng, distanceFromCenterToCorner, HEADING_SOUTH_WEST);
        return RectangularBounds.newInstance(southWestCorner, northEastCorner);
    }

    private void getPlaceDetailAutocompleteList(List<String> suggestionsList) {
        viewModel.getPlaceDetailsList(suggestionsList, currentAppLocale).observe(getViewLifecycleOwner(), placeDetailsList -> {
            if (!placeDetailsList.isEmpty())
                getFullPlaceDetails(placeDetailsList);
        });
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        context.orientationChanged = true;
    }

    @Override
    public void onResume() {
        if (restaurantList.isEmpty() && !autocompleteActive) {
            context.binding.progressBar.setVisibility(View.VISIBLE);
            observePlaces(null);
            context.binding.searchBarRestaurantList.searchBarInput.getText().clear();
        }

        context.orientationChanged = false;
        super.onResume();
    }

    private void observePlaces(String nextPageToken) {
        if (context.networkUnavailable()) {
            Snackbar.make(binding.getRoot(), getString(R.string.internet_unavailable), BaseTransientBottomBar.LENGTH_INDEFINITE)
                    .setDuration(5000).setTextColor(getResources().getColor(R.color.quantum_white_100)).show();
            context.binding.progressBar.setVisibility(View.GONE);
            return;
        }

        if (context.requestLocationAccess())
            if (AppController.getInstance().getCurrentLocation() != null) {
                if (nextPageToken == null)
                    viewModel.getNearbyPlaces(AppController.getInstance().getLatLngString())
                            .observe(getViewLifecycleOwner(), restaurantsResult -> {
                                if (restaurantsResult != null) {
                                    getFullPlaceDetails(restaurantsResult.getPlaceDetails());
                                    this.nextPageToken = restaurantsResult.getNextPageToken();
                                }
                            });
                else
                    viewModel.getMoreNearbyPlaces(nextPageToken).observe(getViewLifecycleOwner(), restaurantsResult -> {
                        getFullPlaceDetails(restaurantsResult.getPlaceDetails());
                        // Check to not return the same result twice
                        if (this.nextPageToken.equals(restaurantsResult.getNextPageToken()))
                            this.nextPageToken = null;
                        else
                            this.nextPageToken = restaurantsResult.getNextPageToken();
                    });
            }
    }

    // Nearby Search doesn't return all the fields required in a PlaceDetails, therefore another
    // query is necessary to retrieve the missing field (-> openingHours)
    // -
    // The viewModel calls are inside each other and not called one after the other because the variables do not get initialised
    // fast enough before being used for another viewModel.
    private void getFullPlaceDetails(List<PlaceDetails> placeDetailsList) {
        ArrayList<PlaceDetails> completePlaceDetailsList = new ArrayList<>();
        for (PlaceDetails placeDetails : placeDetailsList) {
            viewModel.getPlaceDetails(placeDetails.getPlaceId(), currentAppLocale)
                    .observe(getViewLifecycleOwner(), restaurant ->
                            viewModel.getUsersEatingAtRestaurantToday(placeDetails.getPlaceId(), getTodayDateInString())
                                    .observe(getViewLifecycleOwner(), users -> {
                                        if (restaurant != null) {
                                            restaurant.setNbrOfWorkmates(users.size());
                                            completePlaceDetailsList.add(restaurant);
                                        }
                                        ////
                                        if (completePlaceDetailsList.size() == placeDetailsList.size()) {
                                            Collections.sort(completePlaceDetailsList, new NearestRestaurantComparator());
                                            if (!restaurantList.isEmpty() && !autocompleteActive) {
                                                restaurantList.remove(bottomProgressBarPosition);
                                                adapter.notifyItemRemoved(bottomProgressBarPosition);
                                                restaurantList.addAll(completePlaceDetailsList);
                                                adapter.notifyDataSetChanged();
                                                isLoading = false;
                                            } else if (!autocompleteActive) {
                                                this.restaurantList.addAll(completePlaceDetailsList);
                                                adapter.updateList(restaurantList);
                                            } else if (!searchBarInputEmpty) {
                                                adapter.updateList(completePlaceDetailsList);
                                            }
                                            context.binding.progressBar.setVisibility(View.GONE);
                                        }
                                    }));
        }
    }

    // Clear list of restaurants so that we don't display the same results below the previous ones.
    @Override
    public void onDestroy() {
        super.onDestroy();
        context.binding.progressBar.setVisibility(View.GONE);
        restaurantList.clear();
        context.binding.searchBarRestaurantList.searchBarInput.removeTextChangedListener(textWatcher);
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

    private void initScrollListener() {
        RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int lastVisibleItem = linearLayoutManager.findLastCompletelyVisibleItemPosition();
                if (!isLoading) {
                    if (lastVisibleItem == restaurantList.size() - visibleThreshold) {
                        loadNextDataFromApi();
                        isLoading = true;
                    }
                }
            }
        };
        binding.recyclerViewRestaurants.addOnScrollListener(scrollListener);
    }

    private void loadNextDataFromApi() {
        if (!autocompleteActive) {
            if (nextPageToken != null) {
                binding.recyclerViewRestaurants.post(() -> {
                    restaurantList.add(null);
                    adapter.notifyItemInserted(restaurantList.size() - 1);
                });

                Handler handler = new Handler();
                handler.postDelayed(() -> {
                    bottomProgressBarPosition = restaurantList.size() - 1;
                    observePlaces(nextPageToken);
                }, 2000);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        closeSearchBar();
    }

    private void closeSearchBar() {
        context.binding.searchBarRestaurantList.searchBarRestaurantList.setVisibility(View.GONE);
        context.binding.searchBarRestaurantList.searchBarInput.getText().clear();
        InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputManager != null)
            inputManager.hideSoftInputFromWindow(context.binding.searchBarMap.searchBarInput.getWindowToken(), 0);
    }

    public void onPermissionsGranted() {
        FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnCompleteListener(getLocationTask -> {
            if (getLocationTask.isSuccessful()) {
                AppController.getInstance().setCurrentLocation(getLocationTask.getResult());
                observePlaces(null);
            } else
                Toast.makeText(getActivity(), R.string.cant_get_location, Toast.LENGTH_SHORT).show();
        });
    }

    public void onPermissionsDenied() {
        Snackbar.make(binding.getRoot(), R.string.location_unavailable, BaseTransientBottomBar.LENGTH_INDEFINITE)
                .setTextColor(getResources().getColor(R.color.quantum_white_100)).setDuration(5000).show();
    }
}
