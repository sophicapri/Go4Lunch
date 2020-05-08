package com.sophieopenclass.go4lunch.controllers.activities;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.sophieopenclass.go4lunch.BuildConfig;
import com.sophieopenclass.go4lunch.MyViewModel;
import com.sophieopenclass.go4lunch.base.BaseActivity;
import com.sophieopenclass.go4lunch.controllers.adapters.WorkmatesViewAdapter;
import com.sophieopenclass.go4lunch.databinding.ActivityRestaurantDetailsBinding;
import com.sophieopenclass.go4lunch.models.User;
import com.sophieopenclass.go4lunch.models.json_to_java.PlaceDetails;

import static com.sophieopenclass.go4lunch.api.PlaceService.API_URL;
import static com.sophieopenclass.go4lunch.api.PlaceService.PHOTO_URL;
import static com.sophieopenclass.go4lunch.utils.Constants.RESTAURANT_ACTIVITY;

public class RestaurantDetailsActivity extends BaseActivity<MyViewModel> implements WorkmatesViewAdapter.OnWorkmateClickListener {
    private ActivityRestaurantDetailsBinding binding;
    private String urlPhoto;
    private FirestoreRecyclerAdapter adapter;
    private String placeId;


    @Override
    protected View getFragmentLayout() {
        binding = ActivityRestaurantDetailsBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent().getExtras() != null && getIntent().hasExtra("placeId")) {
            placeId = (String) getIntent().getExtras().get("placeId");
            viewModel.getPlaceDetails(placeId).observe(this, this::displayRestaurant);
        }

        setUpRecyclerView();
    }

    private void setUpRecyclerView() {
        FirestoreRecyclerOptions<User> options = new FirestoreRecyclerOptions.Builder<User>()
                .setQuery(viewModel.getUsersCollectionReference().whereEqualTo("placeId", placeId), User.class)
                .build();
        adapter = new WorkmatesViewAdapter(options, getCurrentUser(), RESTAURANT_ACTIVITY, this);
        binding.detailRecyclerViewWorkmates.setHasFixedSize(true);
        binding.detailRecyclerViewWorkmates.setLayoutManager(new LinearLayoutManager(this));
        binding.detailRecyclerViewWorkmates.setAdapter(adapter);
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapter != null)
            adapter.stopListening();
    }

    @Override
    public Class getViewModelClass() {
        return MyViewModel.class;
    }

    private void displayRestaurant(PlaceDetails placeDetails) {
        String photoReference = placeDetails.getPhotos().get(0).getPhotoReference();
        urlPhoto = API_URL + PHOTO_URL + photoReference + "&key=" + BuildConfig.API_KEY;

        binding.detailsRestaurantName.setText(placeDetails.getName());
        binding.detailsTypeOfRestaurant.setText(placeDetails.getTypes().get(0) + " - ");
        binding.detailsRestaurantAddress.setText(placeDetails.getVicinity());

        Glide.with(binding.detailRestaurantPhoto.getContext())
                .load(urlPhoto)
                .apply(RequestOptions.centerCropTransform())
                .into(binding.detailRestaurantPhoto);
    }

    @Override
    public void onWorkmateClick(String uid) {

    }
}
