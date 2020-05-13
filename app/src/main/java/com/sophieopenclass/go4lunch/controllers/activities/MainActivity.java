package com.sophieopenclass.go4lunch.controllers.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.navigation.NavigationView;
import com.sophieopenclass.go4lunch.BuildConfig;
import com.sophieopenclass.go4lunch.MyViewModel;
import com.sophieopenclass.go4lunch.base.BaseActivity;
import com.sophieopenclass.go4lunch.R;
import com.sophieopenclass.go4lunch.controllers.fragments.ListViewFragment;
import com.sophieopenclass.go4lunch.controllers.fragments.MapViewFragment;
import com.sophieopenclass.go4lunch.controllers.fragments.WorkmatesListFragment;
import com.sophieopenclass.go4lunch.databinding.ActivityMainBinding;
import com.sophieopenclass.go4lunch.databinding.NavHeaderBinding;
import com.sophieopenclass.go4lunch.models.User;

import java.util.Arrays;

import static com.sophieopenclass.go4lunch.utils.Constants.MY_LUNCH;
import static com.sophieopenclass.go4lunch.utils.Constants.PLACE_ID;
import static com.sophieopenclass.go4lunch.utils.Constants.UID;

public class MainActivity extends BaseActivity<MyViewModel> implements NavigationView.OnNavigationItemSelectedListener {
    private ActivityMainBinding binding;
    private final int AUTOCOMPLETE_REQUEST_CODE = 123;
    private String placeId;
    private User currentUser;
    private Fragment fragmentMapView;
    private Fragment fragmentListView;
    private Fragment fragmentWorkmatesList;
    private static final int ACTIVITY_MY_LUNCH = 3;
    private static final int ACTIVITY_SETTINGS = 1;
    private static final int FRAGMENT_MAP_VIEW = 10;
    private static final int FRAGMENT_LIST_VIEW = 20;
    private static final int FRAGMENT_WORKMATES_LIST = 30;
    private PlacesClient placesClient;

    @Override
    public View getFragmentLayout() {
        return bindViews().getRoot();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        configureToolbar();
        configureDrawerLayout();
        configureNavigationView();
        initPlacesApi();
        handleDrawerUI();
        showFragment(FRAGMENT_MAP_VIEW);
        if (getCurrentUser() != null)
            viewModel.getUser(getCurrentUser().getUid()).observe(this, user -> currentUser = user);

        binding.bottomNavView.setOnNavigationItemSelectedListener(this::onNavigationItemSelected);
    }

    @Override
    public Class getViewModelClass() {
        return MyViewModel.class;
    }

    private void handleDrawerUI() {
        View drawerView = binding.navigationView.getHeaderView(0);
        ImageView profilePic = drawerView.findViewById(R.id.profile_pic);
        TextView username = drawerView.findViewById(R.id.profile_username);
        TextView email = drawerView.findViewById(R.id.profile_email);

        if (getCurrentUser() != null) {
            if (getCurrentUser().getPhotoUrl() != null) {
                Glide.with(profilePic.getContext())
                        .load(getCurrentUser().getPhotoUrl())
                        .apply(RequestOptions.circleCropTransform())
                        .into(profilePic);
            }

            username.setText(getCurrentUser().getDisplayName());
            email.setText(getCurrentUser().getEmail());
        }

        binding.navigationView.setBackgroundResource(R.drawable.ic_drawer_logo);
    }

    private ActivityMainBinding bindViews() {
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        return binding;
    }

    private void configureToolbar() {
        setSupportActionBar(binding.myToolbar);
    }

    // TODO:
    private void initPlacesApi() {
        // Initialize the SDK
        Places.initialize(getApplicationContext(), BuildConfig.API_KEY);

        // Create a new Places client instance
        placesClient = Places.createClient(this);
    }

    private void configureDrawerLayout() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, binding.drawerLayout, binding.myToolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        binding.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void configureNavigationView() {
        binding.navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.my_lunch:
                showFragment(ACTIVITY_MY_LUNCH);
                break;
            case R.id.settings:
                showFragment(ACTIVITY_SETTINGS);
                break;
            case R.id.sign_out:
                signOut();
                break;
            case R.id.map_view:
                showFragment(FRAGMENT_MAP_VIEW);
                break;
            case R.id.list_view:
                showFragment(FRAGMENT_LIST_VIEW);
                break;
            case R.id.workmates_view:
                showFragment(FRAGMENT_WORKMATES_LIST);
                break;
            default:
                break;
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void signOut() {
        AuthUI.getInstance().signOut(this).addOnSuccessListener(aVoid -> {
            startNewActivity(LoginPageActivity.class);
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
            return;
        }

        if (!fragmentMapView.isVisible())
            binding.bottomNavView.setSelectedItemId(R.id.map_view);
        else
            super.onBackPressed();
    }

    private void showFragment(int controllerIdentifier) {
        switch (controllerIdentifier) {
            case ACTIVITY_MY_LUNCH:
                startNewActivity(WorkmateDetailActivity.class);
                break;
            case ACTIVITY_SETTINGS:
                startNewActivity(SettingsActivity.class);
                break;
            case FRAGMENT_MAP_VIEW:
                //TODO: pourquoi l'affichage de la map est lent ?
                showMapViewFragment();
                break;
            case FRAGMENT_LIST_VIEW:
                showListViewFragment();
                break;
            case FRAGMENT_WORKMATES_LIST:
                showWorkmatesListFragment();
                break;
        }
    }

    private void startNewActivity(Class activity) {
        Intent intent = new Intent(this, activity);
        if (activity == RestaurantDetailsActivity.class)
            intent.putExtra(PLACE_ID, placeId);
        if (activity.equals(WorkmateDetailActivity.class))
            intent.putExtra(UID, currentUser.getUid());
        startActivity(intent);
    }

    private void showMapViewFragment() {
        if (this.fragmentMapView == null) this.fragmentMapView = MapViewFragment.newInstance();
        startTransactionFragment(fragmentMapView);
    }

    private void showListViewFragment() {
        if (this.fragmentListView == null) this.fragmentListView = ListViewFragment.newInstance();
        startTransactionFragment(fragmentListView);
    }

    private void showWorkmatesListFragment() {
        if (this.fragmentWorkmatesList == null)
            this.fragmentWorkmatesList = WorkmatesListFragment.newInstance();
        startTransactionFragment(fragmentWorkmatesList);
    }

    private void startTransactionFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame_layout, fragment).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_button, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.search_bar_menu &&
                (fragmentWorkmatesList == null || !fragmentWorkmatesList.isVisible()))
            startAutocompleteActivity();
        return true;
    }

    private void startAutocompleteActivity() {
        LatLng northEast = new LatLng(this.currentLocation.getLatitude() - (0.05),
                this.currentLocation.getLongitude() - (0.05));
        LatLng southWest = new LatLng(this.currentLocation.getLatitude() + (0.05),
                this.currentLocation.getLongitude() + (0.05));

        Intent intent = new Autocomplete.IntentBuilder(
                AutocompleteActivityMode.OVERLAY, Arrays.asList(Place.Field.ID,
                Place.Field.NAME)).setTypeFilter(TypeFilter.ESTABLISHMENT)
                .setLocationBias(RectangularBounds.newInstance(northEast, southWest))
                .build(this);
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                placeId = place.getId();
                startNewActivity(RestaurantDetailsActivity.class);
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // handle error
                Log.i("TAG", "onActivityResult: error ");
            } else if (resultCode == RESULT_CANCELED) {
                // the user canceled the operation
                Log.i("TAG", "onActivityResult: user canceled operation ");
            }
        }
    }
}
