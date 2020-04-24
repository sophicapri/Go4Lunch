package com.sophieopenclass.go4lunch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.sophieopenclass.go4lunch.databinding.FragmentMapBinding;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MapViewFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = "Map";
    private GoogleMap mMap;
    private SupportMapFragment mapFragment;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationManager locationManager;
    public static Location currentLocation;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 123;
    private static final float DEFAULT_ZOOM = 15f;
    private Context context;

    public MapViewFragment() {
        // Required empty public constructor
    }

    public static Fragment newInstance() {
        return new MapViewFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        com.sophieopenclass.go4lunch.databinding.FragmentMapBinding binding = FragmentMapBinding.inflate(getLayoutInflater());

        if (getActivity() != null) {
            context = getActivity();
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        }
        fetchLastLocation();
        binding.fab.setOnClickListener(v -> fetchLastLocation());

        return binding.getRoot();
    }

    private void fetchLastLocation() {
        askPermission();
        initMap();

        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnCompleteListener(task1 -> {
            if (task1.isSuccessful()) {
                currentLocation = task1.getResult();
                if (currentLocation != null) {
                    moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude())
                    );
                }
            } else {
                Toast.makeText(getActivity(), "unable to get current location", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void askPermission() {
        if (ActivityCompat.checkSelfPermission(context, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (getActivity() != null)
                ActivityCompat.requestPermissions(getActivity(), new String[]{ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else if (!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
        new AlertDialog.Builder(context)
                .setMessage(R.string.gps_network_not_enabled)
                .setPositiveButton(R.string.open_location_settings, (paramDialogInterface, paramInt) -> context.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))).setNegativeButton(R.string.Cancel, null)
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchLastLocation();
            } else {
                Toast.makeText(getActivity(), "Chargement de la carte des restaurants alentours impossible," +
                        "activer le partage de localisation?", Toast.LENGTH_LONG).show();

            }
        }
    }

    private void moveCamera(LatLng latLng) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, MapViewFragment.DEFAULT_ZOOM));
        mMap.addMarker(new MarkerOptions().position(latLng).title("I'm here"));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(false);
    }

    private void initMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        if (getFragmentManager() != null) {
            mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        }
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

}
