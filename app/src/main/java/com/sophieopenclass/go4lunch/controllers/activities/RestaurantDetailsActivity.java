package com.sophieopenclass.go4lunch.controllers.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.sophieopenclass.go4lunch.MyViewModel;
import com.sophieopenclass.go4lunch.R;
import com.sophieopenclass.go4lunch.base.BaseActivity;
import com.sophieopenclass.go4lunch.controllers.adapters.WorkmatesViewAdapter;
import com.sophieopenclass.go4lunch.databinding.ActivityRestaurantDetailsBinding;
import com.sophieopenclass.go4lunch.models.User;
import com.sophieopenclass.go4lunch.models.json_to_java.PlaceDetails;

import java.util.List;

import static com.sophieopenclass.go4lunch.utils.Constants.MY_LUNCH;
import static com.sophieopenclass.go4lunch.utils.Constants.PLACE_ID;
import static com.sophieopenclass.go4lunch.utils.Constants.RESTAURANT_ACTIVITY;

public class RestaurantDetailsActivity extends BaseActivity<MyViewModel> implements View.OnClickListener {
    public static final int REQUEST_CALL = 567;
    private ActivityRestaurantDetailsBinding binding;
    private FirestoreRecyclerAdapter adapter;
    private String placeId;
    private PlaceDetails placeDetails;
    private User currentUser;

    @Override
    protected View getFragmentLayout() {
        binding = ActivityRestaurantDetailsBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding.addRestaurant.setOnClickListener(this);
        binding.callTextview.setOnClickListener(this);
        binding.callBtn.setOnClickListener(this);
        binding.likeRestaurantBtn.setOnClickListener(this);
        binding.likeTextview.setOnClickListener(this);
        binding.websiteBtn.setOnClickListener(this);
        binding.websiteTextview.setOnClickListener(this);
        binding.openingHoursTitle.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (getIntent().getExtras() != null) {
            if (getIntent().hasExtra(PLACE_ID)) {
                placeId = (String) getIntent().getExtras().get(PLACE_ID);

                if (placeId != null && !placeId.matches("")) {
                    viewModel.getPlaceDetails(placeId).observe(this, this::displayRestaurant);
                } else {
                    binding.addRestaurant.setVisibility(View.GONE);
                }
            }
            if (getIntent().hasExtra(MY_LUNCH)) {
                binding.myLunchToolbar.setVisibility(View.VISIBLE);
            } else
                binding.myLunchToolbar.setVisibility(View.GONE);
        }

        if (getCurrentUser() != null) {
            viewModel.getUser(getCurrentUser().getUid()).observe(this, user -> {
                currentUser = user;
            });
            viewModel.getPlaceIdByDate(getCurrentUser().getUid(), User.getTodaysDate()).observe(this, placeId -> {
                if (this.placeId.equals(placeId))
                    binding.addRestaurant.setImageDrawable(getResources().getDrawable(R.drawable.ic_check_circle_black_24dp));
                else
                    binding.addRestaurant.setImageDrawable(getResources().getDrawable(R.drawable.ic_add_black_24dp));
            });
        }

        setUpRecyclerView();
    }

    private void setUpRecyclerView() {
        FirestoreRecyclerOptions<User> options = new FirestoreRecyclerOptions.Builder<User>()
                .setQuery(viewModel.getCollectionReference().whereEqualTo("datesAndPlaceIds." +User.getTodaysDate(), placeId), User.class)
                .build();
        adapter = new WorkmatesViewAdapter(options, RESTAURANT_ACTIVITY, this, Glide.with(this));
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
        this.placeDetails = placeDetails;
        binding.detailsRestaurantName.setText(placeDetails.getName());
        binding.detailsTypeOfRestaurant.setText(getString(R.string.restaurant_type, placeDetails.getTypes().get(0)));
        binding.detailsRestaurantAddress.setText(placeDetails.getVicinity());

        int nbrOfPhotos = 1;
        int layoutParam = ViewGroup.LayoutParams.MATCH_PARENT;

        if (placeDetails.getPhotos() != null) {
            nbrOfPhotos = placeDetails.getPhotos().size();
            layoutParam = ViewGroup.LayoutParams.WRAP_CONTENT;
        }

        for (int i = 0; i < nbrOfPhotos; i++) {
            String urlPhoto = PlaceDetails.urlPhotoFormatter(placeDetails, i);
            ImageView newPhoto = new ImageView(this);
            binding.restaurantPhotosGroup.addView(newPhoto, layoutParam, ViewGroup.LayoutParams.MATCH_PARENT);
            Glide.with(newPhoto.getContext())
                    .load(urlPhoto)
                    .apply(RequestOptions.fitCenterTransform())
                    .into(newPhoto);
        }
    }

    @Override
    public void onClick(View v) {
        if (v == binding.addRestaurant)
            viewModel.getPlaceIdByDate(currentUser.getUid(), User.getTodaysDate() ).observe(this, this::handleRestaurantSelection);
        else if (v == binding.callBtn || v == binding.callTextview)
            callRestaurant();
        else if (v == binding.likeRestaurantBtn || v == binding.likeTextview) {
            //likeRestaurant();
        } else if (v == binding.websiteBtn || v == binding.websiteTextview)
            visitWebsite(placeDetails.getWebsite());
        else if (v == binding.openingHoursTitle)
            displayOpeningHours();
    }

    private void displayOpeningHours() {
        if (binding.weekdaysOpenings.getVisibility() == View.VISIBLE)
            binding.weekdaysOpenings.setVisibility(View.GONE);
        else {
            binding.weekdaysOpenings.setVisibility(View.VISIBLE);
            StringBuilder weekdays = new StringBuilder();
            if (placeDetails.getOpeningHours() != null) {
                List<String> weekdaysArray = placeDetails.getOpeningHours().getWeekdayText();
                for (int i = 0; i < weekdaysArray.size(); i++) {
                    if (i != weekdaysArray.size() - 1)
                        weekdays.append(weekdaysArray.get(i)).append("\n");
                    else
                        weekdays.append(weekdaysArray.get(i));
                }
                binding.weekdaysOpenings.setText(weekdays.toString());
            } else {
                binding.weekdaysOpenings.setText(R.string.opening_hours_unavailable);
            }
        }
    }

    private void handleRestaurantSelection(String currentUserPlaceId) {
        if (!placeId.equals(currentUserPlaceId)) {
            binding.addRestaurant.setImageDrawable(getResources().getDrawable(R.drawable.ic_check_circle_black_24dp));
            if (currentUserPlaceId == null)
                viewModel.updateUserPlaceId(currentUser.getUid(), placeId, User.getTodaysDate()).observe(this, placeId -> {
                    if (placeId == null) {
                        Toast.makeText(this, "Une erreur est survenue", Toast.LENGTH_LONG).show();
                        viewModel.updateRestaurantName(currentUser.getUid(), "");
                        binding.addRestaurant.setImageDrawable(getResources().getDrawable(R.drawable.ic_add_black_24dp));
                    }
                });
            else
                viewModel.updateUserPlaceId(currentUser.getUid(), placeId, User.getTodaysDate());
            viewModel.updateRestaurantName(currentUser.getUid(), placeDetails.getName());
        } else {
            binding.addRestaurant.setImageDrawable(getResources().getDrawable(R.drawable.ic_add_black_24dp));
            viewModel.deletePlaceId(currentUser.getUid(), User.getTodaysDate());
            viewModel.updateRestaurantName(currentUser.getUid(), "");
        }
    }

    private void callRestaurant() {
        if (placeDetails.getInternationalPhoneNumber() != null) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL);
            } else {
                String dial = "tel:" + placeDetails.getInternationalPhoneNumber();
                startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(dial)));
            }
        } else
            Toast.makeText(this, "N° de téléphone non renseigné", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CALL) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                callRestaurant();
            } else {
                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void visitWebsite(String urlWebsite) {
        if (urlWebsite != null) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlWebsite));
            startActivity(intent);
        } else
            Toast.makeText(this, "Site web non renseigné", Toast.LENGTH_LONG).show();
    }

   /* private void likeRestaurant() {
        if () {
            binding.likeRestaurantBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_star_full_24dp));
        } else {
            binding.likeRestaurantBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_star_border_black_24dp));
        }
    }
    */

}
