package com.sophieopenclass.go4lunch.controllers.activities;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.sophieopenclass.go4lunch.base.BaseActivity;
import com.sophieopenclass.go4lunch.databinding.ActivityRestaurantDetailsBinding;
import com.sophieopenclass.go4lunch.models.POJO.PlaceDetails;

public class RestaurantDetailsActivity extends BaseActivity {
    private ActivityRestaurantDetailsBinding binding;

    @Override
    protected View getFragmentLayout() {
        binding = ActivityRestaurantDetailsBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent().getExtras() != null && getIntent().hasExtra("placeId")) {
            String placeId = (String) getIntent().getExtras().get("placeId");
            viewModel.getPlaceDetails(placeId).observe(this, this::displayRestaurant);
        }
    }

    private void displayRestaurant(PlaceDetails placeDetails) {
        System.out.println(placeDetails.getName());
        binding.restaurantName.setText(placeDetails.getName());
    }
}
