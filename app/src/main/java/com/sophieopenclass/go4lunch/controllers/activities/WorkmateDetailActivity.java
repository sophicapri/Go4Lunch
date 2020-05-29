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
        minus = 0;
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
            String todayPlaceId = Objects.requireNonNull(user.getDatesAndRestaurants()
                    .get(getTodayDateInString())).getPlaceId();
            displayTodayRestaurant(todayPlaceId);
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
    }

    private void configureRecyclerView() {
        binding.recyclerViewRestaurants.setHasFixedSize(true);
        binding.recyclerViewRestaurants.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PreviousRestaurantsAdapter(previousRestaurantList, this, Glide.with(this));
        binding.recyclerViewRestaurants.setAdapter(adapter);
    }

    private void initCurrentUserProfileView() {
        binding.fabMessageUser.setVisibility(View.GONE);
        binding.userLunchToolbar.setTitle(getString(R.string.my_lunch_toolbar_title));
        binding.textViewWorkmateFavorites.setVisibility(View.INVISIBLE);
        binding.favoritesAndPreviousTitle.setVisibility(View.VISIBLE);


        binding.chipFavorites.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Log.i(TAG, "onClick: fav");
                    if (favRestaurantList.isEmpty())
                        displayFavoriteRestaurants(selectedUser);
                    else
                        updateRecyclerView(favRestaurantList);
                }
            }
        });

        binding.chipPreviousLunches.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Log.i(TAG, "onClick: previous");
                    if (previousRestaurantList.isEmpty())
                        displayPreviousRestaurants(selectedUser);
                    else
                        updateRecyclerView(previousRestaurantList);
                }
            }
        });

 /*
        binding.chipFavorites.setOnClickListener(v -> {
            if (!binding.chipFavorites.isChecked()) {
                Log.i(TAG, "onClick: fav");
                isFavorite = true;
                if (favRestaurantList.isEmpty())
                    displayFavoriteRestaurants(selectedUser);
                else
                    updateRecyclerView(favRestaurantList);
                binding.chipFavorites.setChecked(true);
            }
        });
*/

 /*
        binding.chipPreviousLunches.setOnClickListener(v -> {
            if (!binding.chipPreviousLunches.isChecked()) {
                Log.i(TAG, "onClick: previous");
                isFavorite = false;
                if (previousRestaurantList.isEmpty())
                    displayPreviousRestaurants(selectedUser);
                else
                    updateRecyclerView(previousRestaurantList);
                binding.chipPreviousLunches.setChecked(true);
            }
        });

 */
    }

    private void initWorkmateProfileView() {
        binding.fabMessageUser.setVisibility(View.VISIBLE);
        binding.userLunchToolbar.setTitle(selectedUser.getUsername());
        binding.textViewWorkmateFavorites.setVisibility(View.VISIBLE);
        binding.favoritesAndPreviousTitle.setVisibility(View.INVISIBLE);
    }

    private void displayTodayRestaurant(String todaysPlaceId) {
        viewModel.getPlaceDetails(todaysPlaceId).observe(this, placeDetails -> {
            binding.lunchOfTheDay.lunchOfTheDay.setVisibility(View.VISIBLE);
            binding.lunchOfTheDay.detailsRestaurantName.setText(placeDetails.getName());
            binding.lunchOfTheDay.detailsRestaurantAddress.setText(placeDetails.getVicinity());
            String urlPhoto = PlaceDetails.urlPhotoFormatter(placeDetails, 0);
            Glide.with(binding.lunchOfTheDay.restaurantPhoto)
                    .load(urlPhoto)
                    .apply(RequestOptions.circleCropTransform())
                    .into(binding.lunchOfTheDay.restaurantPhoto);

            binding.lunchOfTheDay.lunchOfTheDay.setOnClickListener(v -> onRestaurantClick(todaysPlaceId));
            displayStars(placeDetails.getRating());
        });
    }

    private void displayStars(double rating) {
        int numberOfStars = CalculateRatings.getNumberOfStarsToDisplay(rating);
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

    // @minus = To know if the user has selected a restaurant on the current day
    // and retrieve the number of previous restaurant accordingly
    private void displayPreviousRestaurants(User user) {
        for (String date : user.getDatesAndRestaurants().keySet()) {
            if (!date.equals(getTodayDateInString())) {
                if (user.getDatesAndRestaurants().get(date) != null) {
                    Objects.requireNonNull(user.getDatesAndRestaurants().get(date)).setDateOfLunch(date);
                    previousRestaurantList.add(user.getDatesAndRestaurants().get(date));
                }
            }
        }
        Log.i(TAG, "displayPreviousRestaurants: ");
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
}
