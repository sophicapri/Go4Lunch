package com.sophieopenclass.go4lunch.controllers.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.sophieopenclass.go4lunch.MyViewModel;
import com.sophieopenclass.go4lunch.base.BaseActivity;

import static com.sophieopenclass.go4lunch.utils.Constants.MY_LUNCH;
import static com.sophieopenclass.go4lunch.utils.Constants.PLACE_ID;

public class MyLunchActivity extends BaseActivity <MyViewModel> {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //if (getCurrentUser()!= null)
           /* viewModel.getPlaceId(getCurrentUser().getUid()).observe(this, placeId -> {
                if(placeId != null)
                    startRestaurantDetailActivity(placeId);
            });

            */
    }

    private void startRestaurantDetailActivity(String placeId) {
        Intent intent = new Intent(this, RestaurantDetailsActivity.class);
        intent.putExtra(PLACE_ID, placeId);
        intent.putExtra(MY_LUNCH, MY_LUNCH);
        startActivity(intent);
    }

    @Override
    public Class getViewModelClass() {
        return MyViewModel.class;
    }

    @Override
    public View getFragmentLayout() {
        return null;
    }
}
