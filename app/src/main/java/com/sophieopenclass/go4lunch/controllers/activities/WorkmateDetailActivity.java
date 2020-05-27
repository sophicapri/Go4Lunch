package com.sophieopenclass.go4lunch.controllers.activities;

import android.content.Intent;
import android.os.Build;
import android.view.View;
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
    ArrayList<PlaceDetails> placeDetailsList;
    int minus;
    User selectedUser;

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
        binding.chatWithWorkmateBtn.setOnClickListener(v -> startChatActivity(uid));
    }

    private void initUI(User user) {
        selectedUser = user;
        Glide.with(binding.workmateProfilePic.getContext())
                .load(user.getUrlPicture())
                .apply(RequestOptions.circleCropTransform())
                .into(binding.workmateProfilePic);

        if (user.getDatesAndRestaurants().get(getTodayDateInString()) != null) {
            String todayPlaceId = ((Restaurant) Objects.requireNonNull(user.getDatesAndRestaurants()
                    .get(getTodayDateInString()))).getPlaceId();
            displayTodaysRestaurant(todayPlaceId);
        } else {
            binding.workmateDetailLunch.workmateDetailLunch.setVisibility(View.GONE);
            binding.workmateLunchTextview.setText(R.string.no_restaurant_chosen);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                binding.workmateLunchTextview.setTextAppearance(R.style.TextStyleItalic);
            } else
                binding.workmateLunchTextview.setTextColor(getResources().getColor(R.color.quantum_grey700));
        }

        displayPreviousRestaurants(user);
        //
        if (getCurrentUser() != null)
            if (user.getUid().equals(getCurrentUser().getUid())) {
                binding.chatWithWorkmateBtn.setVisibility(View.GONE);
                binding.userLunchToolbar.setTitle(R.string.my_lunch_toolbar_title);
            } else {
                binding.chatWithWorkmateBtn.setText(getString(R.string.chat_with, user.getUsername()));
                binding.userLunchToolbar.setTitle(user.getUsername());
            }
    }

    private void displayTodaysRestaurant(String todaysPlaceId) {
        viewModel.getPlaceDetails(todaysPlaceId).observe(this, placeDetails -> {
            binding.workmateDetailLunch.workmateDetailLunch.setVisibility(View.VISIBLE);
            binding.workmateDetailLunch.detailsRestaurantName.setText(placeDetails.getName());
            binding.workmateDetailLunch.detailsRestaurantAddress.setText(placeDetails.getVicinity());
            String urlPhoto = PlaceDetails.urlPhotoFormatter(placeDetails, 0);
            Glide.with(binding.workmateDetailLunch.restaurantPhoto)
                    .load(urlPhoto)
                    .apply(RequestOptions.centerCropTransform())
                    .into(binding.workmateDetailLunch.restaurantPhoto);

            binding.workmateLunchTextview.setText(getString(R.string.detail_lunch_textview));
            if (binding.workmateDetailLunch.workmateDetailLunch.getVisibility() == View.VISIBLE)
                binding.workmateDetailLunch.workmateDetailLunch.setOnClickListener(v -> onRestaurantClick(todaysPlaceId));

            displayStars(placeDetails.getRating());
        });
    }

    private void displayStars(double rating) {
        int numberOfStars = CalculateRatings.getNumberOfStarsToDisplay(rating);
        if (numberOfStars == 1)
            binding.workmateDetailLunch.detailOneStar.setVisibility(View.VISIBLE);
        if (numberOfStars == 2)
            binding.workmateDetailLunch.detailTwoStars.setVisibility(View.VISIBLE);
        if (numberOfStars == 3)
            binding.workmateDetailLunch.detailThreeStars.setVisibility(View.VISIBLE);
    }

    // @minus = To know if the user has selected a restaurant on the current day
    // and retrieve the number of previous restaurant accordingly
    private void displayPreviousRestaurants(User user) {
        binding.previousRestaurantsRecyclerview.setHasFixedSize(true);
        binding.previousRestaurantsRecyclerview.setLayoutManager(new LinearLayoutManager(this));
        placeDetailsList = new ArrayList<>();
        String placeId = "";

        for (String date : user.getDatesAndRestaurants().keySet()) {
            if (!date.equals(getTodayDateInString())) {
                if (user.getDatesAndRestaurants().get(date) != null)
                placeId = Objects.requireNonNull(user.getDatesAndRestaurants().get(date)).getPlaceId();
                viewModel.getPlaceDetails(placeId).observe(this, placeDetails -> {
                    //
                    if (placeDetails != null) {
                        placeDetails.setDateOfLunch(date);
                        placeDetailsList.add(placeDetails);
                        if (placeDetailsList.size() == user.getDatesAndRestaurants().values().size() - minus) {
                            Collections.sort(placeDetailsList, new RestaurantRecentComparator());
                            updateRecyclerView(placeDetailsList);
                        }
                    }
                });
            } else {
                minus++;
            }
        }
    }

    /**
     * Comparator to sort places from last added to first
     */
    public static class RestaurantRecentComparator implements Comparator<PlaceDetails> {
        @Override
        public int compare(PlaceDetails left, PlaceDetails right) {
            return right.getDateOfLunch().compareTo(left.getDateOfLunch());
        }
    }

    private void updateRecyclerView(ArrayList<PlaceDetails> placeDetailsList) {
        if (!placeDetailsList.isEmpty())
            binding.noPreviousRestaurants.setVisibility(View.GONE);
        PreviousRestaurantsAdapter adapter = new PreviousRestaurantsAdapter(placeDetailsList, this, Glide.with(this));
        binding.previousRestaurantsRecyclerview.setAdapter(adapter);
    }

    private void startChatActivity(String uid) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(EXTRA_UID, uid);
        startActivity(intent);
    }
}
