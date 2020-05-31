package com.sophieopenclass.go4lunch.controllers.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.maps.android.SphericalUtil;
import com.sophieopenclass.go4lunch.MyViewModel;
import com.sophieopenclass.go4lunch.R;
import com.sophieopenclass.go4lunch.base.BaseActivity;
import com.sophieopenclass.go4lunch.controllers.activities.MainActivity;
import com.sophieopenclass.go4lunch.controllers.adapters.RestaurantListAdapter;
import com.sophieopenclass.go4lunch.databinding.RecyclerViewRestaurantsBinding;
import com.sophieopenclass.go4lunch.models.User;
import com.sophieopenclass.go4lunch.models.json_to_java.PlaceDetails;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

import static com.sophieopenclass.go4lunch.base.BaseActivity.ORIENTATION_CHANGED;
import static com.sophieopenclass.go4lunch.controllers.fragments.MapViewFragment.PERMS;
import static com.sophieopenclass.go4lunch.controllers.fragments.MapViewFragment.getLatLngString;
import static com.sophieopenclass.go4lunch.utils.DateFormatting.getTodayDateInString;

public class RestaurantListFragment extends Fragment implements EasyPermissions.PermissionCallbacks {
    public static final String TAG = "RESTAURANT LIST";
    private static final double RADIUS = 500;
    private MyViewModel viewModel;
    private RecyclerViewRestaurantsBinding binding;
    private BaseActivity context;
    private List<AutocompletePrediction> predictionList;
    private LinearLayoutManager linearLayoutManager;
    private boolean autocompleteActive = false;
    private String nextPageToken;
    private RestaurantListAdapter adapter;
    private ArrayList<PlaceDetails> restaurants = new ArrayList<>();
    private final AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();
    private MainActivity activity;
    private int visibleThreshold = 5;
    private static final double HEADING_NORTH_WEST = 45.0;
    private static final double HEADING_SOUTH_WEST = 225.0;
    private boolean isLoading;


    public static Fragment newInstance() {
        return new RestaurantListFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate: ");
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
            activity.binding.progressBar.setVisibility(View.VISIBLE);
            initSearchBar(activity);
        }

        binding.swipeRefreshView.setOnRefreshListener(() -> {
            observePlaces(nextPageToken);
            binding.swipeRefreshView.setRefreshing(false);
        });
        Log.i(TAG, "onCreateView: ");
        return binding.getRoot();
    }

    private void initSearchBar(MainActivity activity) {
        activity.binding.searchBarRestaurantList.closeSearchBar.setOnClickListener(v -> {
            activity.binding.searchBarRestaurantList.searchBarRestaurantList.setVisibility(View.GONE);
            if (!activity.binding.searchBarRestaurantList.searchBarInput.getText().toString().isEmpty())
                activity.binding.searchBarRestaurantList.searchBarInput.getText().clear();
            //To not refresh the page if the search bar has been opened but the user didn't search for a restaurant
            if (autocompleteActive) {
                restaurants.clear();
                if (adapter != null)
                    adapter.updateList(restaurants);
                autocompleteActive = false;
            }
            observePlaces(null);
        });

        activity.binding.searchBarRestaurantList.searchBarInput.addTextChangedListener(new TextWatcher() {
            //to stop the TextWatcher from firing multiple times
            // - not working -
            boolean isOnTextChanged = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Log.i(TAG, "beforeTextChanged: ");
                isOnTextChanged = true;

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!ORIENTATION_CHANGED && isOnTextChanged) {
                    Log.i(TAG, "onTextChanged:");
                    isOnTextChanged = false;
                    autocompleteActive = true;
                    restaurants.clear();
                    adapter.updateList(restaurants);
                    if (!s.toString().isEmpty())
                        displayResultsAutocomplete(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.i(TAG, "afterTextChanged: ");
            }
        });
    }

    /**
     * The "heading" params of the SphericalUtil method represent where each corner is in degrees.
     * See visual representation here : https://i.stack.imgur.com/GkFzJ.png;
     */
    private RectangularBounds getRectangularBounds() {
        double distanceFromCenterToCorner = RADIUS * Math.sqrt(2.0);
        LatLng latLng = new LatLng(BaseActivity.sCurrentLocation.getLatitude(), BaseActivity.sCurrentLocation.getLongitude());
        LatLng northEastCorner =
                SphericalUtil.computeOffset(latLng, distanceFromCenterToCorner, HEADING_NORTH_WEST);
        LatLng southWestCorner =
                SphericalUtil.computeOffset(latLng, distanceFromCenterToCorner, HEADING_SOUTH_WEST);
        return RectangularBounds.newInstance(southWestCorner, northEastCorner);
    }

    private void displayResultsAutocomplete(String textInput) {
        FindAutocompletePredictionsRequest predictionsRequest = FindAutocompletePredictionsRequest.builder()
                .setTypeFilter(TypeFilter.ESTABLISHMENT)
                .setSessionToken(token)
                .setLocationRestriction(getRectangularBounds())
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
                    Gson gson = new Gson();
                    System.out.println("GSON" + gson.toJson(suggestionsList));
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
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        ORIENTATION_CHANGED = true;
    }

    @Override
    public void onResume() {
        if (restaurants.isEmpty() || autocompleteActive) {
            activity.binding.progressBar.setVisibility(View.VISIBLE);
            observePlaces(null);
            // To delete autocomplete results
            autocompleteActive = false;
            activity.binding.searchBarRestaurantList.searchBarInput.getText().clear();
        }

        ORIENTATION_CHANGED = false;
        super.onResume();
    }

    private void observePlaces(String nextPageToken) {
        if (context.networkUnavailable()) {
            Snackbar.make(binding.getRoot(), getString(R.string.internet_unavailable), BaseTransientBottomBar.LENGTH_INDEFINITE)
                    .setDuration(5000).setTextColor(getResources().getColor(R.color.quantum_white_100)).show();
            activity.binding.progressBar.setVisibility(View.GONE);
            return;
        }

        if (context.requestLocationPermission())
            if (BaseActivity.sCurrentLocation != null)
                if (nextPageToken == null)
                    viewModel.getNearbyPlaces(getLatLngString(BaseActivity.sCurrentLocation))
                            .observe(getViewLifecycleOwner(), restaurantsResult -> {
                                getFullPlaceDetails(restaurantsResult.getPlaceDetails());
                                this.nextPageToken = restaurantsResult.getNextPageToken();
                            });
                else
                    viewModel.getMoreNearbyPlaces(nextPageToken).observe(getViewLifecycleOwner()
                            , restaurantsResult -> {
                                getFullPlaceDetails(restaurantsResult.getPlaceDetails());
                                // Check to not return the same result twice
                                if (this.nextPageToken.equals(restaurantsResult.getNextPageToken()))
                                    this.nextPageToken = null;
                                else
                                    this.nextPageToken = restaurantsResult.getNextPageToken();
                            });

    }


    // Method to use during development. The one below demands too many requests
   /* private void getFullPlaceDetails(List<PlaceDetails> placeDetailsList) {
        ArrayList<PlaceDetails> completePlaceDetailsList = new ArrayList<>();
        for (PlaceDetails restaurant : placeDetailsList) {
            viewModel.getUsersByPlaceIdAndDate(restaurant.getPlaceId(), getTodayDateInString())
                    .observe(getViewLifecycleOwner(), users -> {
                        restaurant.setNbrOfWorkmates(users.size());
                        completePlaceDetailsList.add(restaurant);
                        if (completePlaceDetailsList.size() == placeDetailsList.size()) {
                            Collections.sort(completePlaceDetailsList, new NearestRestaurantComparator());
                            if ((!restaurants.isEmpty()) && adapter != null) {
                                int indexStart = restaurants.size() - 1;
                                this.restaurants.addAll(completePlaceDetailsList);
                                if (!autocompleteActive)
                                    adapter.notifyItemRangeInserted(indexStart, completePlaceDetailsList.size());
                                else {
                                    adapter.notifyItemRemoved(0);
                                    adapter.notifyItemRangeInserted(0, completePlaceDetailsList.size());
                                }
                            } else
                                updateRecyclerView(completePlaceDetailsList);
                        }
                    });
        }
    }
    */


    // Nearby Search doesn't return all the fields required in a PlaceDetails, therefore another
    // query is necessary to retrieve the missing fields (ex : openingHours)
    // -
    // The viewModel calls are inside each other and not called one after the other because the variables do not get initialised
    // fast enough before being used for another viewModel.

    // Method that gets the openings hours details.
    private void getFullPlaceDetails(List<PlaceDetails> placeDetailsList) {
        ArrayList<PlaceDetails> completePlaceDetailsList = new ArrayList<>();
        for (PlaceDetails placeDetails : placeDetailsList) {
            viewModel.getPlaceDetails(placeDetails.getPlaceId())
                    .observe(getViewLifecycleOwner(), restaurant ->
                            viewModel.getUsersByPlaceIdAndDate(placeDetails.getPlaceId(), getTodayDateInString())
                                    .observe(getViewLifecycleOwner(), users -> {
                                        Log.i(TAG, "getFullPlaceDetails: " + restaurant);
                                        if (restaurant != null)
                                            restaurant.setNbrOfWorkmates(users.size());
                                        completePlaceDetailsList.add(restaurant);
                                        if (completePlaceDetailsList.size() == placeDetailsList.size()) {
                                            Collections.sort(completePlaceDetailsList, new NearestRestaurantComparator());
                                            if ((!restaurants.isEmpty()) && adapter != null) {
                                                int indexStart = restaurants.size() - 1;
                                                this.restaurants.addAll(completePlaceDetailsList);
                                                if (!autocompleteActive)
                                                    adapter.notifyItemRangeInserted(indexStart, completePlaceDetailsList.size());
                                                else {
                                                    adapter.notifyItemRangeInserted(0, completePlaceDetailsList.size());
                                                }
                                            } else
                                                updateRecyclerView(completePlaceDetailsList);
                                        }
                                    }));
        }
    }

    // Clear list of restaurants so that we don't display the same results below the previous ones.
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        restaurants.clear();
        Log.i(TAG, "onDestroyView: ");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy: ");
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
        adapter = new RestaurantListAdapter(this.restaurants, context, Glide.with(this));
        binding.recyclerViewRestaurants.setAdapter(adapter);

        initScrollListener();
    }

    private void initScrollListener() {
        RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int lastVisibleItem = linearLayoutManager.findLastCompletelyVisibleItemPosition();
                if (!isLoading) {
                    if (lastVisibleItem == restaurants.size() - visibleThreshold) {
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
                restaurants.add(null);
                adapter.notifyItemInserted(restaurants.size() - 1);
                Handler handler = new Handler();
                handler.postDelayed(() -> {
                    restaurants.remove(restaurants.size() - 1);
                    int scrollPosition = restaurants.size();
                    adapter.notifyItemRemoved(scrollPosition);
                    observePlaces(nextPageToken);
                    adapter.notifyDataSetChanged();
                    isLoading = false;
                }, 2000);

            }
        }
    }


    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        if (EasyPermissions.hasPermissions(context, PERMS))
            observePlaces(null);
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        Snackbar.make(binding.getRoot(), R.string.location_unavailable, BaseTransientBottomBar.LENGTH_INDEFINITE)
                .setTextColor(getResources().getColor(R.color.quantum_white_100)).setDuration(5000).show();
    }
}
