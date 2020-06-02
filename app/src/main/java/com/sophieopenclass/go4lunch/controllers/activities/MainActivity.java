package com.sophieopenclass.go4lunch.controllers.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.navigation.NavigationView;
import com.sophieopenclass.go4lunch.BuildConfig;
import com.sophieopenclass.go4lunch.MyViewModel;
import com.sophieopenclass.go4lunch.R;
import com.sophieopenclass.go4lunch.base.BaseActivity;
import com.sophieopenclass.go4lunch.controllers.fragments.MapViewFragment;
import com.sophieopenclass.go4lunch.controllers.fragments.RestaurantListFragment;
import com.sophieopenclass.go4lunch.controllers.fragments.WorkmatesListFragment;
import com.sophieopenclass.go4lunch.databinding.ActivityMainBinding;
import com.sophieopenclass.go4lunch.models.User;

import static android.content.Intent.EXTRA_UID;
import static com.sophieopenclass.go4lunch.utils.Constants.*;
import static com.sophieopenclass.go4lunch.utils.Constants.ACTIVITY_MY_LUNCH;
import static com.sophieopenclass.go4lunch.utils.Constants.ACTIVITY_SETTINGS;
import static com.sophieopenclass.go4lunch.utils.Constants.FRAGMENT_MAP_VIEW;
import static com.sophieopenclass.go4lunch.utils.Constants.FRAGMENT_RESTAURANT_LIST_VIEW;
import static com.sophieopenclass.go4lunch.utils.Constants.FRAGMENT_WORKMATES_LIST;

public class MainActivity extends BaseActivity<MyViewModel> implements NavigationView.OnNavigationItemSelectedListener {
    private static boolean RESTART_STATE = false;
    public ActivityMainBinding binding;
    private User currentUser;
    private Fragment fragmentMapView;
    private Fragment fragmentRestaurantList;
    private Fragment fragmentWorkmatesList;
    public PlacesClient placesClient;

    @Override
    public View getFragmentLayout() {
        checkCurrentLocale();
        return bindViews().getRoot();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        configureToolbar();
        configureDrawerLayout();
        configureNavigationView();
        initPlacesApi();
        Log.i(TAG, "onCreate: ");
        if (getCurrentUser() != null)
            viewModel.getUser(getCurrentUser().getUid()).observe(this, user -> {
                currentUser = user;
                handleDrawerUI(user);
            });
        binding.bottomNavView.setOnNavigationItemSelectedListener(this::onNavigationItemSelected);

        binding.searchBarRestaurantList.searchBarRestaurantList.setVisibility(View.GONE);
        binding.searchBarMap.searchBarMap.setVisibility(View.GONE);
        binding.searchBarWorkmates.searchBarWorkmates.setVisibility(View.GONE);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        // to display the map after the user accepted the permissions
        RESTART_STATE = true;
    }

    @Override
    public Class getViewModelClass() {
        return MyViewModel.class;
    }

    private void handleDrawerUI(User user) {
        View drawerView = binding.navigationView.getHeaderView(0);
        ImageView profilePic = drawerView.findViewById(R.id.profile_pic);
        TextView username = drawerView.findViewById(R.id.profile_username);
        TextView email = drawerView.findViewById(R.id.profile_email);

        if (user.getUrlPicture() != null) {
            Glide.with(profilePic.getContext())
                    .load(user.getUrlPicture())
                    .apply(RequestOptions.circleCropTransform())
                    .into(profilePic);

            username.setText(user.getUsername());
            email.setText(user.getEmail());
        }
    }

    private ActivityMainBinding bindViews() {
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        return binding;
    }

    private void configureToolbar() {
        setSupportActionBar(binding.myToolbar);
    }

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
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume: ");

        if (!ORIENTATION_CHANGED && !RESTART_STATE) {
            showFragment(FRAGMENT_MAP_VIEW);
            Log.i(TAG, "onStart: ");
            // To activate notifications by default when first launching the app OR
            // activate the notifications if the user signed out and logged back in
            if (sharedPrefs.contains(PREF_REMINDER) || sharedPrefs.getBoolean(PREF_REMINDER, false))
                activateReminder();
        }

        if (RESTART_STATE)
            RESTART_STATE = false;



        // Update UI
        if (SettingsActivity.localeHasChanged || SettingsActivity.profileHasChanged) {
            finish();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            SettingsActivity.localeHasChanged = false;
            SettingsActivity.profileHasChanged = false;
        }
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
                showFragment(FRAGMENT_RESTAURANT_LIST_VIEW);
                break;
            case R.id.workmates_view:
                showFragment(FRAGMENT_WORKMATES_LIST);
                break;
            default:
                break;
        }
        //binding.bottomNavView.setSelectedItemId();
        binding.drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
            return;
        }

        if (fragmentMapView == null || !fragmentMapView.isVisible())
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
                showMapViewFragment();
                break;
            case FRAGMENT_RESTAURANT_LIST_VIEW:
                showRestaurantListFragment();
                break;
            case FRAGMENT_WORKMATES_LIST:
                showWorkmatesListFragment();
                break;
        }
    }

    private void startNewActivity(Class activity) {
        Intent intent = new Intent(this, activity);
        if (activity.equals(WorkmateDetailActivity.class))
            intent.putExtra(EXTRA_UID, currentUser.getUid());
        startActivity(intent);
    }

    private void showMapViewFragment() {
        if (this.fragmentMapView == null) this.fragmentMapView = MapViewFragment.newInstance();
        startTransactionFragment(fragmentMapView);
    }

    private void showRestaurantListFragment() {
        if (this.fragmentRestaurantList == null)
            this.fragmentRestaurantList = RestaurantListFragment.newInstance();
        startTransactionFragment(fragmentRestaurantList);
    }

    private void showWorkmatesListFragment() {
        if (this.fragmentWorkmatesList == null)
            this.fragmentWorkmatesList = WorkmatesListFragment.newInstance();
        startTransactionFragment(fragmentWorkmatesList);
    }

    private void startTransactionFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame_layout, fragment, fragment.getClass().getSimpleName()).commit();

        updateUI(fragment);

        Log.i(TAG, "startTransactionFragment: Name " + fragment.getClass().getSimpleName());
        //getSupportFragmentManager().findFragmentByTag()

        Log.i(TAG, ": view " + binding.frameLayout.getTag());
       // Log.i(TAG, "onResume: find fragment " +         FragmentManager.findFragment(binding.frameLayout));

    }

    private void updateUI(Fragment fragment) {
        if (fragment instanceof WorkmatesListFragment)
            binding.myToolbar.setTitle(R.string.available_workmates);
        else
            binding.myToolbar.setTitle(R.string.im_hungry);

        binding.searchBarRestaurantList.searchBarRestaurantList.setVisibility(View.GONE);
        binding.searchBarMap.searchBarMap.setVisibility(View.GONE);
        binding.searchBarWorkmates.searchBarWorkmates.setVisibility(View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_button, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        EditText focusedEditText = null;

        if (item.getItemId() == R.id.search_bar_menu)
            if (fragmentMapView.isVisible()) {
                binding.searchBarMap.searchBarMap.setVisibility(View.VISIBLE);
                focusedEditText = binding.searchBarMap.searchBarInput;
            } else if (fragmentRestaurantList != null && fragmentRestaurantList.isVisible()) {
                binding.searchBarRestaurantList.searchBarRestaurantList.setVisibility(View.VISIBLE);
                focusedEditText = binding.searchBarRestaurantList.searchBarInput;
            } else if (fragmentWorkmatesList != null && fragmentWorkmatesList.isVisible()) {
                binding.searchBarWorkmates.searchBarWorkmates.setVisibility(View.VISIBLE);
                focusedEditText = binding.searchBarWorkmates.searchBarInput;
            }

        if (focusedEditText != null)
            focusedEditText.requestFocus();
        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputManager != null) {
            inputManager.showSoftInput(focusedEditText, InputMethodManager.SHOW_IMPLICIT);
        }
        return true;
    }
}
