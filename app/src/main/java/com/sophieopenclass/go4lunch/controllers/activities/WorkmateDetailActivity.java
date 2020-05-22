package com.sophieopenclass.go4lunch.controllers.activities;

import android.content.Intent;
import android.os.Build;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.sophieopenclass.go4lunch.MyViewModel;
import com.sophieopenclass.go4lunch.R;
import com.sophieopenclass.go4lunch.base.BaseActivity;
import com.sophieopenclass.go4lunch.controllers.adapters.PreviousRestaurantsAdapter;
import com.sophieopenclass.go4lunch.databinding.ActivityWorkmateDetailBinding;
import com.sophieopenclass.go4lunch.models.User;
import com.sophieopenclass.go4lunch.models.json_to_java.PlaceDetails;
import com.sophieopenclass.go4lunch.utils.CalculateRatings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static android.content.Intent.EXTRA_UID;

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
        if (getIntent().getExtras() != null && getIntent().hasExtra(EXTRA_UID)) {
            uid = (String) getIntent().getExtras().get(EXTRA_UID);
            viewModel.getUser(uid).observe(this, this::initUI);
        }
        binding.chatWithWorkmateBtn.setOnClickListener(v -> startChatActivity(uid));
    }

    private void initUI(User user) {
        selectedUser = user;
        binding.workmatesDetailName.setText(user.getUsername());
        Glide.with(binding.workmateProfilePic.getContext())
                .load(user.getUrlPicture())
                .apply(RequestOptions.circleCropTransform())
                .into(binding.workmateProfilePic);


        String todaysPlaceId = user.getDatesAndPlaceIds().get(User.getTodaysDate());
        if (todaysPlaceId != null) {
            displayTodaysRestaurant(todaysPlaceId);
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
                binding.myLunchToolbar.setVisibility(View.VISIBLE);
            } else {
                binding.chatWithWorkmateBtn.setText(getString(R.string.chat_with, user.getUsername()));
                binding.myLunchToolbar.setVisibility(View.GONE);
            }
    }

    private void displayTodaysRestaurant(String todaysPlaceId) {
        viewModel.getPlaceDetails(todaysPlaceId).observe(this, placeDetails -> {
            binding.workmateDetailLunch.workmateDetailLunch.setVisibility(View.VISIBLE);
            binding.workmateDetailLunch.detailsRestaurantName.setText(placeDetails.getName());
            binding.workmateDetailLunch.detailsTypeOfRestaurant.setText(getString(R.string.restaurant_type, placeDetails.getTypes().get(0)));
            binding.workmateDetailLunch.detailsRestaurantAddress.setText(placeDetails.getVicinity());
            String urlPhoto = PlaceDetails.urlPhotoFormatter(placeDetails, 0);
            Glide.with(binding.workmateDetailLunch.restaurantPhoto)
                    .load(urlPhoto)
                    .apply(RequestOptions.centerCropTransform())
                    .into(binding.workmateDetailLunch.restaurantPhoto);

            binding.workmateLunchTextview.setText(getString(R.string.detail_lunch_textview));
            if (binding.workmateDetailLunch.workmateDetailLunch.getVisibility() == View.VISIBLE)
                binding.workmateDetailLunch.workmateDetailLunch.setOnClickListener(v -> onRestaurantClick(todaysPlaceId));
        });

        displayStars(todaysPlaceId);
    }

    private void displayStars(String placeId) {
        viewModel.getListUsers().observe(this, users -> viewModel.getNumberOfLikesByPlaceId(placeId)
                .observe(this, likes -> {
            int numberOfStars = CalculateRatings.getNumberOfStarsToDisplay(users.size(), likes);
            if (numberOfStars == 1)
                binding.workmateDetailLunch.detailOneStar.setVisibility(View.VISIBLE);
            if (numberOfStars == 2)
                binding.workmateDetailLunch.detailTwoStars.setVisibility(View.VISIBLE);
            if (numberOfStars == 3)
                binding.workmateDetailLunch.detailThreeStars.setVisibility(View.VISIBLE);
        }));
    }

    private void displayPreviousRestaurants(User user) {
        binding.previousRestaurantsRecyclerview.setHasFixedSize(true);
        binding.previousRestaurantsRecyclerview.setLayoutManager(new LinearLayoutManager(this));
        placeDetailsList = new ArrayList<>();
        String placeId;

        for (String date : user.getDatesAndPlaceIds().keySet()) {
            if (!date.equals(User.getTodaysDate())) {
                placeId = user.getDatesAndPlaceIds().get(date);
                viewModel.getPlaceDetails(placeId).observe(this, placeDetails -> {
                    placeDetails.setDateOfLunch(date);
                    // Add number of stars
                    viewModel.getListUsers().observe(this, allUsers ->
                            viewModel.getNumberOfLikesByPlaceId(placeDetails.getPlaceId()).observe(this, likes -> {
                        int numberOfStars = CalculateRatings.getNumberOfStarsToDisplay(allUsers.size(), likes);
                        placeDetails.setNumberOfStars(numberOfStars);
                        placeDetailsList.add(placeDetails);
                        if (placeDetailsList.size() == user.getDatesAndPlaceIds().values().size() - minus) {
                            Collections.sort(placeDetailsList, new RestaurantRecentComparator());
                            updateRecyclerView(placeDetailsList);
                        }
                    }));
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
