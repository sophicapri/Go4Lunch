package com.sophieopenclass.go4lunch;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;

import com.firebase.ui.auth.AuthUI;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.navigation.NavigationView;
import com.sophieopenclass.go4lunch.databinding.ActivityMainBinding;

import java.util.Arrays;

public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {
    private ActivityMainBinding binding;
    private final int AUTOCOMPLETE_REQUEST_CODE = 123;
    public static PlacesClient placesClient;

    private Fragment fragmentMapView;
    private Fragment fragmentListView;
    private Fragment fragmentWorkmatesList;
    private static final int ACTIVITY_MY_LUNCH = 0;
    private static final int ACTIVITY_SETTINGS = 1;
    private static final int FRAGMENT_MAP_VIEW = 10;
    private static final int FRAGMENT_LIST_VIEW = 20;
    private static final int FRAGMENT_WORKMATES_LIST = 30;

    @Override
    public View getFragmentLayout() {
        return bindViews().getRoot();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(bindViews().getRoot());
        configureToolbar();
        configureDrawerLayout();
        configureNavigationView();
        initPlacesApi();

        showFragment(FRAGMENT_MAP_VIEW);
        binding.bottomNavView.setOnNavigationItemSelectedListener(this::onNavigationItemSelected);
    }

    private ActivityMainBinding bindViews() {
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        return binding;
    }

    private void configureToolbar() {
        setSupportActionBar(binding.myToolbar);
    }


    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 1)
            getSupportFragmentManager().popBackStack();
        super.onBackPressed();
        binding.bottomNavView.setSelectedItemId(R.id.map_view);
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
                AuthUI.getInstance().signOut(this);
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                startNewActivity(LoginPageActivity.class);
                finish();
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

    private void showFragment(int fragmentIdentifier) {
        switch (fragmentIdentifier) {
            case ACTIVITY_MY_LUNCH:
                startNewActivity(MyLunchActivity.class);
                break;
            case ACTIVITY_SETTINGS:
                startNewActivity(SettingsActivity.class);
                break;
            case FRAGMENT_MAP_VIEW:
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
        startActivity(intent);
    }

    private void showWorkmatesListFragment() {
        if (this.fragmentWorkmatesList == null)
            this.fragmentWorkmatesList = WorkmatesListFragment.newInstance();
        startTransactionFragment(fragmentWorkmatesList);
    }

    private void showListViewFragment() {
        if (this.fragmentListView == null) this.fragmentListView = ListViewFragment.newInstance();
        startTransactionFragment(fragmentListView);
    }

    private void showMapViewFragment() {
        if (this.fragmentMapView == null) this.fragmentMapView = MapViewFragment.newInstance();
        startTransactionFragment(fragmentMapView);
    }

    private void startTransactionFragment(Fragment fragment) {
        if (!(fragment instanceof MapViewFragment))
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.navigation_frame_layout, fragment).addToBackStack(null).commit();
        else {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.frame_layout, fragment).commit();
        }
    }

    private void initPlacesApi() {
        // Initialize the SDK
        Places.initialize(getApplicationContext(), BuildConfig.API_KEY);

        // Create a new Places client instance
        placesClient = Places.createClient(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_button, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.search_bar_menu)
            startAutocompleteActivity();
        return true;
    }

    private void startAutocompleteActivity() {
        Intent intent = new Autocomplete.IntentBuilder(
                AutocompleteActivityMode.OVERLAY, Arrays.asList(Place.Field.ID,
                Place.Field.NAME)).setTypeFilter(TypeFilter.ESTABLISHMENT)
                .build(this);
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                Log.i("TAG", "onActivityResult: " + place.getName() + ", " + place.getId());
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
