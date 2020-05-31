package com.sophieopenclass.go4lunch.controllers.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
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
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
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
import com.sophieopenclass.go4lunch.MyViewModel;
import com.sophieopenclass.go4lunch.R;
import com.sophieopenclass.go4lunch.base.BaseActivity;
import com.sophieopenclass.go4lunch.controllers.activities.MainActivity;
import com.sophieopenclass.go4lunch.controllers.activities.RestaurantDetailsActivity;
import com.sophieopenclass.go4lunch.databinding.FragmentMapBinding;
import com.sophieopenclass.go4lunch.models.json_to_java.PlaceDetails;

import java.util.ArrayList;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.app.Activity.RESULT_OK;
import static com.sophieopenclass.go4lunch.base.BaseActivity.LOCATION_REQUEST_CODE;
import static com.sophieopenclass.go4lunch.base.BaseActivity.ORIENTATION_CHANGED;
import static com.sophieopenclass.go4lunch.utils.Constants.PLACE_ID;
import static com.sophieopenclass.go4lunch.utils.DateFormatting.getTodayDateInString;

public class MapViewFragment extends Fragment implements OnMapReadyCallback, EasyPermissions.PermissionCallbacks {
    private static final String TAG = "MAP FRAGMENT";
    private static final String CAMERA_LOCATION = "cameraLocation";
    static final String PERMS = ACCESS_FINE_LOCATION;
    private MyViewModel viewModel;
    private GoogleMap mMap;
    private Location currentLocation;
    private static final float DEFAULT_ZOOM = 17.5f;
    private boolean autocompleteActive;
    private BaseActivity context;
    private Location cameraLocation;
    private List<AutocompletePrediction> predictionList;
    private String searchBarTextInput;
    private MainActivity activity;
    private FragmentMapBinding binding;
    private TextWatcher textWatcher;
    private final AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

    public MapViewFragment() {
    }

    public static Fragment newInstance() {
        return new MapViewFragment();
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate: ");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMapBinding.inflate(inflater, container, false);

        Log.i(TAG, "onCreateView: ");
        if (getActivity() != null) {
            context = (BaseActivity) getActivity();
            viewModel = (MyViewModel) context.getViewModel();
        }

        if (getActivity() != null) {
            activity = ((MainActivity) getActivity());
            activity.binding.searchBarMap.closeSearchBar.setOnClickListener(v -> {
                activity.binding.searchBarMap.searchBarMap.setVisibility(View.GONE);
                activity.binding.searchBarMap.searchBarInput.getText().clear();
                InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (inputManager != null) {
                    inputManager.hideSoftInputFromWindow(activity.binding.searchBarMap.searchBarInput.getWindowToken(), 0);
                }
                getNearbyPlaces(cameraLocation);
                autocompleteActive = false;
            });
        }
            textWatcher = new TextWatcher() {
            //to stop the TextWatcher from firing multiple times
            boolean isOnTextChanged = false;

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                isOnTextChanged = true;
                Log.i(TAG, "onTextChanged: ");
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Log.i(TAG, "beforeTextChanged: ");
            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.i(TAG, "afterTextChanged: ");
                if (!ORIENTATION_CHANGED && isOnTextChanged) {
                    mMap.clear();
                    isOnTextChanged = false;
                    searchBarTextInput = s.toString();
                    autocompleteActive = true;
                    Log.i(TAG, "afterTextChanged: " + s.toString());
                    displayResultsAutocomplete(searchBarTextInput);
                }
            }
        };
        activity.binding.searchBarMap.searchBarInput.addTextChangedListener(textWatcher);

        binding.fab.setOnClickListener(v -> {
            if (context.requestLocationPermission()) {
                fetchLastLocation();
            }
        });
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        ORIENTATION_CHANGED = false;
        Log.i(TAG, "onResume: ");
        if (context.networkUnavailable()) {
            Snackbar.make(activity.binding.getRoot(), getString(R.string.internet_unavailable), BaseTransientBottomBar.LENGTH_INDEFINITE)
                    .setTextColor(getResources().getColor(R.color.quantum_white_100)).setDuration(5000).show();
        } else {
            if (context.requestLocationPermission()) {
                fetchLastLocation();
            }
        }
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        // To stop the TextWatcher from firing
        ORIENTATION_CHANGED = true;
    }

    private void displayResultsAutocomplete(String searchBarTextInput) {
        LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;
        double northEastLat = bounds.northeast.latitude;
        double southWestLat = bounds.southwest.latitude;
        double northEastLng = bounds.northeast.longitude;
        double southWestLng = bounds.southwest.longitude;

        LatLng northEast = new LatLng(northEastLat, northEastLng);
        LatLng southWest = new LatLng(southWestLat, southWestLng);

        FindAutocompletePredictionsRequest predictionsRequest = FindAutocompletePredictionsRequest.builder()
                .setTypeFilter(TypeFilter.ESTABLISHMENT)
                .setSessionToken(token)
                .setLocationRestriction(RectangularBounds.newInstance(southWest, northEast))
                .setQuery(searchBarTextInput)
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
                    Log.i(TAG, "displayResultsAutocomplete: " + suggestionsList.toString());
                    getPlaceDetailAutocompleteList(suggestionsList);
                }
            } else {
                Log.i(TAG, "Prediction fetching task unsuccessful");
            }
        });
    }

    private void getPlaceDetailAutocompleteList(List<String> suggestionsList) {
        List<PlaceDetails> placeDetailsList = new ArrayList<>();
        Log.d(TAG, "getPlaceDetailAutocompleteList: " + suggestionsList.toString());
        for (String placeId : suggestionsList)
            viewModel.getPlaceDetails(placeId).observe(context, placeDetails -> {
                placeDetailsList.add(placeDetails);
                if (placeDetailsList.size() == suggestionsList.size()) {
                    initMarkers(placeDetailsList);
                }
            });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style));
        mMap.setOnInfoWindowClickListener(this::startRestaurantActivity);
    }

    private void fetchLastLocation() {
        FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        initMap();
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnCompleteListener(task1 -> {
            if (task1.isSuccessful()) {
                currentLocation = task1.getResult();
                if (currentLocation != null) {
                    configureMap(currentLocation);
                    cameraLocation = currentLocation;
                    //Init the current location for the entire app
                    BaseActivity.sCurrentLocation = currentLocation;
                }
            } else {
                Toast.makeText(getActivity(), R.string.cant_get_location, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void configureMap(Location currentLocation) {
        LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
        if (!autocompleteActive)
            getNearbyPlaces(currentLocation);
        cameraLocation = new Location(currentLocation);
        Log.i(TAG, "configureMap: befoe");
        mMap.setOnCameraMoveStartedListener(i -> {
            cameraLocation = new Location(CAMERA_LOCATION);
            cameraLocation.setLongitude(mMap.getCameraPosition().target.longitude);
            cameraLocation.setLatitude(mMap.getCameraPosition().target.latitude);
            Log.i(TAG, "configureMap: on camera move");
            if (!autocompleteActive)
                getNearbyPlaces(cameraLocation);
            else
                displayResultsAutocomplete(activity.binding.searchBarMap.searchBarInput.getText().toString());
        });
    }

    private void getNearbyPlaces(Location currentLocation) {
        if (context.networkUnavailable()) {
            if (getView() != null)
                Snackbar.make(getView(), getString(R.string.internet_unavailable), BaseTransientBottomBar.LENGTH_INDEFINITE)
                        .setDuration(5000).show();
        } else if (context.requestLocationPermission()) {
            viewModel.getNearbyPlaces(getLatLngString(currentLocation))
                    .observe(getViewLifecycleOwner(), restaurantsResult -> initMarkers(restaurantsResult.getPlaceDetails()));
        }
    }

    static String getLatLngString(Location currentLocation) {
        return currentLocation.getLatitude() + "," + currentLocation.getLongitude();
    }

    private void initMarkers(List<PlaceDetails> placeDetailsList) {
        for (PlaceDetails placeDetails : placeDetailsList) {
            viewModel.getUsersByPlaceIdAndDate(placeDetails.getPlaceId(), getTodayDateInString()).observe(getViewLifecycleOwner(), users -> {
                int markerDrawable;
                if (users.isEmpty())
                    markerDrawable = R.drawable.ic_marker_red;
                else
                    markerDrawable = R.drawable.ic_marker_green;

                Marker marker = mMap.addMarker(new MarkerOptions().title(placeDetails.getName())
                        .position(new LatLng(placeDetails.getGeometry().getLocation().getLat(),
                                placeDetails.getGeometry().getLocation().getLng()))
                        .icon(getBitmapFromVector(markerDrawable)));
                marker.setTag(placeDetails.getPlaceId());
            });
        }
    }

    private void startRestaurantActivity(Marker marker) {
        if (marker.getTag() != null) {
            Intent intent = new Intent(getActivity(), RestaurantDetailsActivity.class);
            intent.putExtra(PLACE_ID, marker.getTag().toString());
            startActivity(intent);
        }
    }

    private BitmapDescriptor getBitmapFromVector(int drawableId) {
        Drawable drawable = ResourcesCompat.getDrawable(getResources(), drawableId, null);
        Bitmap bitmap = null;
        if (drawable != null) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                drawable = (DrawableCompat.wrap(drawable)).mutate();
            }
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
        }
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cameraLocation = null;
        activity.binding.progressBar.setVisibility(View.GONE);
        activity.binding.searchBarMap.searchBarInput.removeTextChangedListener(textWatcher);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        if (EasyPermissions.hasPermissions(context, PERMS)) {
            if (mMap != null)
                mMap.setMyLocationEnabled(true);
            fetchLastLocation();
            Log.i(TAG, "onPermissionsGranted: ");
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        Snackbar.make(binding.getRoot(), R.string.location_unavailable, BaseTransientBottomBar.LENGTH_INDEFINITE)
                .setTextColor(getResources().getColor(R.color.quantum_white_100)).setDuration(5000).show();

    }
}
