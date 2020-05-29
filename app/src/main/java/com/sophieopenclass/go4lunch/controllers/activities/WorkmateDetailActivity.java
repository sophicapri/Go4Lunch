package com.sophieopenclass.go4lunch.controllers.activities;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.sophieopenclass.go4lunch.MyViewModel;
import com.sophieopenclass.go4lunch.R;
import com.sophieopenclass.go4lunch.base.BaseActivity;
import com.sophieopenclass.go4lunch.controllers.adapters.PreviousRestaurantsAdapter;
import com.sophieopenclass.go4lunch.databinding.ActivityWorkmateDetailBinding;
import com.sophieopenclass.go4lunch.models.Restaurant;
import com.sophieopenclass.go4lunch.models.User;
import com.sophieopenclass.go4lunch.models.json_to_java.PlaceDetails;
import com.sophieopenclass.go4lunch.utils.CalculateRatings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;

import static android.content.Intent.EXTRA_UID;
import static com.sophieopenclass.go4lunch.utils.DateFormatting.getTodayDateInString;

public class WorkmateDetailActivity extends BaseActivity<MyViewModel> {
    ActivityWorkmateDetailBinding binding;
    String uid = null;
    ArrayList<Restaurant> previousRestaurantList = new ArrayList<>();
    ArrayList<Restaurant> favRestaurantList = new ArrayList<>();
    int minus;
    User selectedUser;
    PreviousRestaurantsAdapter adapter;
    private boolean isFavorite = true;

    @Override
    public Class getViewModelClass() {
        return MyViewModel.class;
    }

    @Override
    protected View getFragmentLayout() {
        binding = ActivityWorkmateDetailBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (networkUnavailable()) {
            Snackbar.make(binding.getRoot(), getString(R.string.internet_unavailable), BaseTransientBottomBar.LENGTH_INDEFINITE)
                    .setDuration(5000).setTextColor(getResources().getColor(R.color.quantum_white_100)).show();
        } else if (getIntent().getExtras() != null && getIntent().hasExtra(EXTRA_UID)) {
            uid = (String) getIntent().getExtras().get(EXTRA_UID);
            viewModel.getUser(uid).observe(this, user -> {
                if (user != null)
                    initUI(user);
                else {
                    finish();
                    Toast.makeText(this, "Ce compte n'existe plus", Toast.LENGTH_SHORT).show();
                }
            });
        }
        binding.fabMessageUser.setOnClickListener(v -> startChatActivity(uid));
    }

    private void initUI(User user) {
        selectedUser = user;
        binding.userLunchToolbar.setTitle(user.getUsername());
        Glide.with(binding.workmateProfilePic.getContext())
                .load(user.getUrlPicture())
                .apply(RequestOptions.circleCropTransform())
                .into(binding.workmateProfilePic);

        if (user.getDatesAndRestaurants().get(getTodayDateInString()) != null) {
            displayTodayRestaurant(user);
            binding.noRestaurantSelectedToday.setVisibility(View.INVISIBLE);
        } else {
            binding.lunchOfTheDay.lunchOfTheDay.setVisibility(View.INVISIBLE);
            binding.noRestaurantSelectedToday.setVisibility(View.VISIBLE);
        }

        configureRecyclerView();
        displayFavoriteRestaurants(user);

        if (getCurrentUser() != null)
            if (user.getUid().equals(getCurrentUser().getUid())) {
                initCurrentUserProfileView();
            } else {
                initWorkmateProfileView();
            }
        binding.userLunchToolbar.setNavigationOnClickListener( v -> onBackPressed());
    }

    private void configureRecyclerView() {
        binding.recyclerViewRestaurants.setHasFixedSize(true);
        binding.recyclerViewRestaurants.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PreviousRestaurantsAdapter(previousRestaurantList, this, Glide.with(this));
        binding.recyclerViewRestaurants.setAdapter(adapter);
    }

    private void initCurrentUserProfileView() {
        binding.fabMessageUser.setVisibility(View.GONE);
        binding.toolbarName.setTitle(getString(R.string.my_lunch_toolbar_title));
        binding.textViewWorkmateFavorites.setVisibility(View.INVISIBLE);
        binding.favoritesAndPreviousScrollview.setVisibility(View.VISIBLE);
        binding.chipFavorites.setChecked(true);
        binding.favoritesAndPreviousScrollview.fullScroll(View.FOCUS_LEFT);
        binding.chipFavorites.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                isFavorite = true;
                if (favRestaurantList.isEmpty())
                    displayFavoriteRestaurants(selectedUser);
                else
                    updateRecyclerView(favRestaurantList);
                binding.favoritesAndPreviousScrollview.fullScroll(View.FOCUS_LEFT);
            }
        });

        binding.chipPreviousLunches.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                isFavorite = false;
                if (previousRestaurantList.isEmpty())
                    displayPreviousRestaurants(selectedUser);
                else
                    updateRecyclerView(previousRestaurantList);
                binding.favoritesAndPreviousScrollview.fullScroll(View.FOCUS_RIGHT);
            }
        });
    }

    private void initWorkmateProfileView() {
        binding.fabMessageUser.setVisibility(View.VISIBLE);
        binding.toolbarName.setTitle(selectedUser.getUsername());
        binding.textViewWorkmateFavorites.setVisibility(View.VISIBLE);
        binding.favoritesAndPreviousScrollview.setVisibility(View.INVISIBLE);
    }

    private void displayTodayRestaurant(User user) {
        Restaurant selectedRestaurant = user.getDatesAndRestaurants().get(getTodayDateInString());
        if (selectedRestaurant!= null) {
            binding.lunchOfTheDay.lunchOfTheDay.setVisibility(View.VISIBLE);
            binding.lunchOfTheDay.detailsRestaurantName.setText(selectedRestaurant.getName());
            binding.lunchOfTheDay.detailsRestaurantAddress.setText(selectedRestaurant.getAddress());
            Glide.with(binding.lunchOfTheDay.restaurantPhoto)
                    .load(selectedRestaurant.getUrlPhoto())
                    .apply(RequestOptions.circleCropTransform())
                    .into(binding.lunchOfTheDay.restaurantPhoto);

            binding.lunchOfTheDay.lunchOfTheDay.setOnClickListener(v -> onRestaurantClick(selectedRestaurant.getPlaceId()));
            displayStars(selectedRestaurant.getNumberOfStars());
        }
    }

    private void displayStars(double numberOfStars) {
        if (numberOfStars == 1)
            binding.lunchOfTheDay.detailOneStar.setVisibility(View.VISIBLE);
        if (numberOfStars == 2)
            binding.lunchOfTheDay.detailTwoStars.setVisibility(View.VISIBLE);
        if (numberOfStars == 3)
            binding.lunchOfTheDay.detailThreeStars.setVisibility(View.VISIBLE);
    }

    private void displayFavoriteRestaurants(User user) {
        favRestaurantList.addAll(user.getFavoriteRestaurants().values());
        isFavorite = true;
        updateRecyclerView(favRestaurantList);
    }

    private void displayPreviousRestaurants(User user) {
        for (String date : user.getDatesAndRestaurants().keySet()) {
            if (!date.equals(getTodayDateInString())) {
                if (user.getDatesAndRestaurants().get(date) != null) {
                    Objects.requireNonNull(user.getDatesAndRestaurants().get(date)).setDateOfLunch(date);
                    previousRestaurantList.add(user.getDatesAndRestaurants().get(date));
                }
            }
        }
        isFavorite = false;
        Collections.sort(previousRestaurantList, new RestaurantRecentComparator());
        updateRecyclerView(previousRestaurantList);
    }

    /**
     * Comparator to sort places from last added to first
     */
    public static class RestaurantRecentComparator implements Comparator<Restaurant> {
        @Override
        public int compare(Restaurant left, Restaurant right) {
            return right.getDateOfLunch().compareTo(left.getDateOfLunch());
        }
    }

    private void updateRecyclerView(ArrayList<Restaurant> placeDetailsList) {
        if (!placeDetailsList.isEmpty()) {
            binding.noRestaurantSelected.setVisibility(View.INVISIBLE);
        }else{
            binding.noRestaurantSelected.setVisibility(View.VISIBLE);
        }
        adapter.updateList(placeDetailsList, isFavorite);
    }

    private void startChatActivity(String uid) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(EXTRA_UID, uid);
        startActivity(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        previousRestaurantList.clear();
        favRestaurantList.clear();
    }
}
