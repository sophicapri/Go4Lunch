package com.sophieopenclass.go4lunch.controllers.fragments;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.sophieopenclass.go4lunch.BuildConfig;
import com.sophieopenclass.go4lunch.MyViewModel;
import com.sophieopenclass.go4lunch.R;
import com.sophieopenclass.go4lunch.base.BaseActivity;
import com.sophieopenclass.go4lunch.controllers.activities.RestaurantDetailsActivity;
import com.sophieopenclass.go4lunch.databinding.FragmentMapBinding;
import com.sophieopenclass.go4lunch.models.json_to_java.RestaurantsResult;
import com.sophieopenclass.go4lunch.models.json_to_java.PlaceDetails;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static com.sophieopenclass.go4lunch.api.PlaceService.API_URL;
import static com.sophieopenclass.go4lunch.api.PlaceService.PHOTO_URL;

public class MapViewFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = "Map";
    private MyViewModel viewModel;
    private GoogleMap mMap;
    private SupportMapFragment mapFragment;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationManager locationManager;
    public static Location currentLocation;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 123;
    private static final float DEFAULT_ZOOM = 15f;
    private Context context;
    private int cameraPosition = (int) DEFAULT_ZOOM;

    public MapViewFragment() {
        // Required empty public constructor
    }

    public static Fragment newInstance() {
        return new MapViewFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentMapBinding binding = FragmentMapBinding.inflate(inflater, container, false);

        if (getActivity() != null) {
            context = getActivity();
            viewModel = (MyViewModel) ((BaseActivity)getActivity()).getViewModel();
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        }

        if (ActivityCompat.checkSelfPermission(context, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            requestPermission();
        else
            fetchLastLocation();

        binding.fab.setOnClickListener(v -> fetchLastLocation());
        return binding.getRoot();
    }

    private void fetchLastLocation() {
        if (!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
            requestLocationActivation();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);

        initMap();
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnCompleteListener(task1 -> {
            if (task1.isSuccessful()) {
                currentLocation = task1.getResult();
                if (currentLocation != null) {
                    configureMap(currentLocation);
                }
            } else {
                Toast.makeText(getActivity(), "unable to get current location", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void configureMap(Location currentLocation) {
        LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
        mMap.addMarker(new MarkerOptions().position(latLng).title("I'm here")).setTag("my_position");
        executeHttpRequestWithRetrofit();
    }

    private void requestPermission() {
        requestPermissions(new String[]{ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchLastLocation();
            } else {
                // TODO : Display pop up informing the user that the app won't work properly
            }
        }
    }

    private void requestLocationActivation() {
        new AlertDialog.Builder(context)
                .setMessage(R.string.gps_network_not_enabled)
                .setPositiveButton(R.string.open_location_settings, (paramDialogInterface, paramInt) ->
                        context.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)))
                .setNegativeButton(R.string.Cancel, null)
                .show();
    }

    private void initMap() {
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
    }

    // TODO : ne récupère que 20 restaurants à la fois
    private void executeHttpRequestWithRetrofit() {
        System.out.println(currentLocation);
        viewModel.getNearbyPlaces(getLatLngString(currentLocation), getRadius())
                .observe(getViewLifecycleOwner(), this::initMarkers);
    }

    public static String getLatLngString(Location currentLocation) {
        return currentLocation.getLatitude() + "," + currentLocation.getLongitude();
    }

    public static int getRadius() {
        return ((int) DEFAULT_ZOOM * 50);
    }

    private void initMarkers(RestaurantsResult restaurants) {
        for (PlaceDetails placeDetails : restaurants.getPlaceDetails()) {
            mMap.addMarker(new MarkerOptions().title(placeDetails.getName()).position(
                    new LatLng(placeDetails.getGeometry().getLocation().getLat(), placeDetails.getGeometry().getLocation().getLng()))
                    .icon(vectorToBitmap(R.drawable.ic_marker_tmp, Color.parseColor("#00FF0C")))).setTag(placeDetails.getPlaceId());
        }

        mMap.setOnInfoWindowClickListener(marker -> {
            if (marker.getTag() != "my_position" && marker.getTag() != null) {
                Intent intent = new Intent(getActivity(), RestaurantDetailsActivity.class);
                intent.putExtra("placeId", marker.getTag().toString());
                startActivity(intent);
            }
        });
    }

    private BitmapDescriptor vectorToBitmap(@DrawableRes int id, @ColorInt int color) {
        Drawable vectorDrawable = ResourcesCompat.getDrawable(getResources(), id, null);
        Bitmap bitmap = null;
        if (vectorDrawable != null) {
            bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                    vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            DrawableCompat.setTint(vectorDrawable, color);
            vectorDrawable.draw(canvas);
        }
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
}
