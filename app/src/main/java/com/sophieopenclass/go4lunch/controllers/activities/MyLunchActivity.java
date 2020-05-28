package com.sophieopenclass.go4lunch.controllers.activities;

import android.os.Build;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.sophieopenclass.go4lunch.MyViewModel;
import com.sophieopenclass.go4lunch.R;
import com.sophieopenclass.go4lunch.base.BaseActivity;
import com.sophieopenclass.go4lunch.databinding.ActivityMyLunchBinding;
import com.sophieopenclass.go4lunch.databinding.ActivityWorkmateDetailBinding;
import com.sophieopenclass.go4lunch.models.Restaurant;
import com.sophieopenclass.go4lunch.models.User;
import com.sophieopenclass.go4lunch.models.json_to_java.PlaceDetails;

import java.util.ArrayList;
import java.util.Objects;

import static android.content.Intent.EXTRA_UID;
import static com.sophieopenclass.go4lunch.utils.DateFormatting.getTodayDateInString;

public class MyLunchActivity extends BaseActivity<MyViewModel> {
    ActivityMyLunchBinding binding;
    String uid = null;
    ArrayList<PlaceDetails> placeDetailsList;
    int minus;
    User currentUser;

    @Override
    public Class getViewModelClass() {
        return MyViewModel.class;
    }

    @Override
    protected View getFragmentLayout() {
        binding = ActivityMyLunchBinding.inflate(getLayoutInflater());
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
            /*viewModel.getUser(uid).observe(this, user -> {
                if (user != null) {
                    initUI(user);
                    currentUser = user;
                }
            });
            
             */
        }
    }

    private void initUI(User user) {
    }
}
