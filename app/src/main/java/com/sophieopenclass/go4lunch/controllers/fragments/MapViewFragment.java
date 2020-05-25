package com.sophieopenclass.go4lunch.controllers.fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
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
import com.sophieopenclass.go4lunch.models.User;
import com.sophieopenclass.go4lunch.models.json_to_java.PlaceDetails;

import java.util.ArrayList;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static com.sophieopenclass.go4lunch.utils.Constants.PLACE_ID;

public class MapViewFragment extends Fragment implements OnMapReadyCallback, EasyPermissions.PermissionCallbacks {
    private static final String TAG = "MAIN ACTIVITY";
    private static final double AREA_MAP_AUTOCOMPLETE = 0.004;
    private static final String CAMERA_LOCATION = "cameraLocation";
    static final String PERMS = ACCESS_FINE_LOCATION;
    private MyViewModel viewModel;
    private GoogleMap mMap;
    static LocationManager locationManager;
    private Location currentLocation;
    static final int LOCATION_PERMISSION_REQUEST_CODE = 123;
    private static final float DEFAULT_ZOOM = 17.5f;
    private boolean autocompleteActive;
    private BaseActivity context;
    private Location cameraLocation;
    private List<AutocompletePrediction> predictionList;
    private String searchBarTextInput;
    private MainActivity activity;
    private FragmentMapBinding binding;
    private final AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

    public MapViewFragment() {
    }

    public static Fragment newInstance() {
        return new MapViewFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMapBinding.inflate(inflater, container, false);
        if (getActivity() != null) {
            context = (BaseActivity) getActivity();
            viewModel = (MyViewModel) ((BaseActivity) getActivity()).getViewModel();
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        }


        if (getActivity() != null) {
            activity = ((MainActivity) getActivity());
            activity.binding.searchBarMap.closeSearchBar.setOnClickListener(v -> {
                activity.binding.searchBarMap.searchBarMap.setVisibility(View.GONE);
                activity.binding.searchBarMap.searchBarInput.getText().clear();
                autocompleteActive = false;
            });
        }

        activity.binding.searchBarMap.searchBarInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchBarTextInput = s.toString();
                autocompleteActive = true;
                displayResultsAutocomplete(searchBarTextInput);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        binding.fab.setOnClickListener(v -> {
            if (requestLocationPermission()) {
                fetchLastLocation();
            }
        });
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (networkUnavailable()) {
            Snackbar.make(activity.binding.getRoot(), "No internet connection", BaseTransientBottomBar.LENGTH_INDEFINITE)
                    .setTextColor(getResources().getColor(R.color.quantum_white_100)).setDuration(5000).show();
        } else {
            if (requestLocationPermission()) {
                fetchLastLocation();
            }
        }
    }

    private boolean networkUnavailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = null;
        if (connectivityManager != null) {
            activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        }
        return activeNetworkInfo == null || !activeNetworkInfo.isConnected();
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
                    getPlaceDetailAutocompleteList(suggestionsList);
                }
            } else {
                Log.i(TAG, "Prediction fetching task unsuccessful");
            }
        });
    }

    private void getPlaceDetailAutocompleteList(List<String> suggestionsList) {
        List<PlaceDetails> placeDetailsList = new ArrayList<>();
        mMap.clear();
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
                    BaseActivity.currentLocation = currentLocation;
                    //
                    Log.i(TAG, "fetchLastLocation: HERE");
                    cameraLocation = currentLocation;
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

        mMap.setOnCameraMoveStartedListener(i -> {
            cameraLocation = new Location(CAMERA_LOCATION);
            cameraLocation.setLongitude(mMap.getCameraPosition().target.longitude);
            cameraLocation.setLatitude(mMap.getCameraPosition().target.latitude);
            if (!autocompleteActive)
                getNearbyPlaces(cameraLocation);
            else
                displayResultsAutocomplete(searchBarTextInput);
        });
    }

    // TODO : make this method and the ones below it abstract and move it to BaseActivity
    private boolean requestLocationPermission() {
        boolean locationAvailable = false;
        if (!EasyPermissions.hasPermissions(context, PERMS)) {
            EasyPermissions.requestPermissions(this,
                    "Cette application a besoin de l'accès à votre localisation pour fonctionner.",
                    LOCATION_PERMISSION_REQUEST_CODE, PERMS);
        } else if (!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            new AlertDialog.Builder(context)
                    .setMessage(R.string.gps_network_not_enabled)
                    .setPositiveButton(R.string.open_location_settings, (paramDialogInterface, paramInt) ->
                            context.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)))
                    .setNegativeButton(R.string.Cancel, null)
                    .show();
        } else
            locationAvailable = true;
        return locationAvailable;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> list) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (mMap != null)
                mMap.setMyLocationEnabled(true);
            fetchLastLocation();
        } else {
            Snackbar.make(binding.getRoot(), R.string.location_deactivated, BaseTransientBottomBar.LENGTH_INDEFINITE)
                    .setTextColor(getResources().getColor(R.color.quantum_white_100)).setDuration(5000).show();
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> list) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            Snackbar.make(binding.getRoot(), R.string.location_deactivated, BaseTransientBottomBar.LENGTH_INDEFINITE)
                    .setTextColor(getResources().getColor(R.color.quantum_white_100)).setDuration(5000).show();
        }
    }

    private void getNearbyPlaces(Location currentLocation) {
        if (networkUnavailable()) {
            if (getView() != null)
                Snackbar.make(getView(), getString(R.string.internet_unavailable), BaseTransientBottomBar.LENGTH_INDEFINITE)
                        .setDuration(5000).show();
        } else if (requestLocationPermission()) {
            viewModel.getNearbyPlaces(getLatLngString(currentLocation))
                    .observe(getViewLifecycleOwner(), restaurantsResult -> initMarkers(restaurantsResult.getPlaceDetails()));
        }
    }

    static String getLatLngString(Location currentLocation) {
        return currentLocation.getLatitude() + "," + currentLocation.getLongitude();
    }

    private void initMarkers(List<PlaceDetails> placeDetailsList) {
        for (PlaceDetails placeDetails : placeDetailsList) {
            viewModel.getUsersByPlaceIdAndDate(placeDetails.getPlaceId(), User.getTodaysDate()).observe(getViewLifecycleOwner(), users -> {
                int markerDrawable;
                if (users.isEmpty())
                    markerDrawable = R.drawable.ic_marker_red;
                else
                    markerDrawable = R.drawable.ic_marker_green;

                mMap.addMarker(new MarkerOptions().title(placeDetails.getName()).position(
                        new LatLng(placeDetails.getGeometry().getLocation().getLat(), placeDetails.getGeometry().getLocation().getLng()))
                        .icon(getBitmapFromVector(markerDrawable))).setTag(placeDetails.getPlaceId());
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
    }
}
