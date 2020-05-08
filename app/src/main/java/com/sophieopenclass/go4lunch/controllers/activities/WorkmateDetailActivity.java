package com.sophieopenclass.go4lunch.controllers.activities;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.sophieopenclass.go4lunch.MyViewModel;
import com.sophieopenclass.go4lunch.base.BaseActivity;
import com.sophieopenclass.go4lunch.databinding.ActivityWorkmateDetailBinding;
import com.sophieopenclass.go4lunch.models.User;

public class WorkmateDetailActivity extends BaseActivity<MyViewModel> {
    ActivityWorkmateDetailBinding binding;

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

        if (getIntent().getExtras() != null && getIntent().hasExtra("uid")) {
            String uid = (String) getIntent().getExtras().get("uid");
            viewModel.getUser(uid).observe(this, this::initUI);
        }
    }

    private void initUI(User user) {
        binding.workmatesDetailText.setText(user.getUsername());

        System.out.println(user.getChosenRestaurant().getName() + "detail");
        if (user.getRestaurantChosen() != null)
            binding.workmatesDetailRestaurant.setText(user.getRestaurantChosen().getName());
        else
            binding.workmatesDetailRestaurant.setText("Pas de restaurant choisi");
    }
}
