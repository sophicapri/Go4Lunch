package com.sophieopenclass.go4lunch.controllers.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.sophieopenclass.go4lunch.MyViewModel;
import com.sophieopenclass.go4lunch.R;
import com.sophieopenclass.go4lunch.base.BaseActivity;
import com.sophieopenclass.go4lunch.databinding.ActivityWorkmateDetailBinding;
import com.sophieopenclass.go4lunch.models.User;
import com.sophieopenclass.go4lunch.models.json_to_java.PlaceDetails;

import static com.sophieopenclass.go4lunch.utils.Constants.PLACE_ID;
import static com.sophieopenclass.go4lunch.utils.Constants.UID;

public class WorkmateDetailActivity extends BaseActivity<MyViewModel> {
    ActivityWorkmateDetailBinding binding;
    String uid = null;

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
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent().getExtras() != null && getIntent().hasExtra(UID)) {
            uid = (String) getIntent().getExtras().get(UID);
            viewModel.getUser(uid).observe(this, this::initUI);
        }
        binding.chatWithWorkmateBtn.setOnClickListener(v -> startChatActivity(uid));
    }

    private void initUI(User user) {
        binding.workmatesDetailName.setText(user.getUsername());
        Glide.with(binding.workmateProfilePic.getContext())
                .load(user.getUrlPicture())
                .apply(RequestOptions.circleCropTransform())
                .into(binding.workmateProfilePic);

        viewModel.getPlaceId(user.getUid()).observe(this, placeId -> {
            if (placeId != null) {
                viewModel.getPlaceDetails(placeId).observe(this, placeDetails -> {
                    binding.workmateDetailLunch.setVisibility(View.VISIBLE);
                    binding.detailsRestaurantName.setText(placeDetails.getName());
                    binding.detailsTypeOfRestaurant.setText(getString(R.string.restaurant_type, placeDetails.getTypes().get(0)));
                    binding.detailsRestaurantAddress.setText(placeDetails.getVicinity());
                    String urlPhoto = PlaceDetails.urlPhotoFormatter(placeDetails, 0);
                    Glide.with(binding.restaurantPhoto)
                            .load(urlPhoto)
                            .apply(RequestOptions.centerCropTransform())
                            .into(binding.restaurantPhoto);
                    if (getCurrentUser() != null)
                        if (user.getUid().equals(getCurrentUser().getUid()))
                            binding.workmateLunchTextview.setText(getString(R.string.your_lunch_textview));
                    if (binding.workmateDetailLunch.getVisibility() == View.VISIBLE)
                        binding.workmateDetailLunch.setOnClickListener(v -> startRestaurantActivity(placeId));
                    // TODO : handle stars
                });
            } else {
                binding.workmateDetailLunch.setVisibility(View.GONE);
                binding.workmateLunchTextview.setText(R.string.no_restaurant_chosen);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    binding.workmateLunchTextview.setTextAppearance(R.style.TextStyleItalic);
                } else
                    binding.workmateLunchTextview.setTextColor(getResources().getColor(R.color.quantum_grey700));
            }
            binding.chatWithWorkmateBtn.setText(getString(R.string.chat_with, user.getUsername()));
            if (getCurrentUser() != null)
                if (user.getUid().equals(getCurrentUser().getUid()))
                    binding.chatWithWorkmateBtn.setVisibility(View.GONE);
        });
    }

    private void startChatActivity(String uid) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(UID, uid);
        startActivity(intent);
    }

    private void startRestaurantActivity(String placeId) {
        Intent intent = new Intent(this, RestaurantDetailsActivity.class);
        intent.putExtra(PLACE_ID, placeId);
        startActivity(intent);
    }
}
