package com.sophieopenclass.go4lunch.controllers.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.sophieopenclass.go4lunch.MyViewModel;
import com.sophieopenclass.go4lunch.R;
import com.sophieopenclass.go4lunch.base.BaseActivity;
import com.sophieopenclass.go4lunch.controllers.adapters.RestaurantWorkmatesListAdapter;
import com.sophieopenclass.go4lunch.databinding.ActivityRestaurantDetailsBinding;
import com.sophieopenclass.go4lunch.models.Restaurant;
import com.sophieopenclass.go4lunch.models.User;
import com.sophieopenclass.go4lunch.models.json_to_java.PlaceDetails;

import java.util.List;
import java.util.Locale;

import static com.sophieopenclass.go4lunch.utils.Constants.PLACE_ID;
import static com.sophieopenclass.go4lunch.utils.Constants.PREF_LANGUAGE;
import static com.sophieopenclass.go4lunch.utils.Constants.REQUEST_CALL;
import static com.sophieopenclass.go4lunch.utils.DateFormatting.getTodayDateInString;

public class RestaurantDetailsActivity extends BaseActivity<MyViewModel> implements View.OnClickListener {
    private String currentAppLocale = sharedPrefs.getString(PREF_LANGUAGE, Locale.getDefault().getLanguage());
    private ActivityRestaurantDetailsBinding binding;
    private FirestoreRecyclerAdapter adapter;
    private String placeId;
    private PlaceDetails placeDetails;
    private Restaurant restaurant;
    private User currentUser;
    public static final String TAG = "com.sophie.Details";

    @Override
    public Class getViewModelClass() {
        return MyViewModel.class;
    }

    @Override
    protected View getFragmentLayout() {
        binding = ActivityRestaurantDetailsBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding.addRestaurant.setOnClickListener(this);
        binding.callBtn.setOnClickListener(this);
        binding.likeRestaurantBtn.setOnClickListener(this);
        binding.websiteBtn.setOnClickListener(this);
        binding.openingHoursTitle.setOnClickListener(this);
        binding.detailRestaurantToolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    @Override
    protected void onStart() {
        super.onStart();
        binding.oneStar.setVisibility(View.GONE);
        binding.twoStars.setVisibility(View.GONE);
        binding.threeStars.setVisibility(View.GONE);

        if (networkUnavailable()) {
            Snackbar.make(binding.getRoot(), getString(R.string.internet_unavailable), BaseTransientBottomBar.LENGTH_INDEFINITE)
                    .setDuration(5000).setTextColor(getResources().getColor(R.color.quantum_white_100)).show();
        } else if (getIntent().getExtras() != null) {
            if (getIntent().hasExtra(PLACE_ID)) {
                placeId = (String) getIntent().getExtras().get(PLACE_ID);
                if (placeId != null && !placeId.isEmpty())
                    viewModel.getPlaceDetails(placeId, currentAppLocale).observe(this, this::initUI);
                else {
                    Toast.makeText(this, R.string.error_unknown_error, Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
            setUpRecyclerView();
        }
    }

    private void initUI(PlaceDetails placeDetails) {
        if (placeDetails != null) {
            this.placeDetails = placeDetails;
            binding.detailsRestaurantName.setText(placeDetails.getName());
            binding.detailsRestaurantAddress.setText(placeDetails.getVicinity());
            displayButtons();
            int layoutParam = 0;
            int nbrOfPhotos = 1;

            // To center the image
            if (placeDetails.getPhotos() == null || placeDetails.getPhotos().size() == 1) {
                layoutParam = ViewGroup.LayoutParams.MATCH_PARENT;
            }

            if (placeDetails.getPhotos() != null && placeDetails.getPhotos().size() != 1) {
                nbrOfPhotos = placeDetails.getPhotos().size();
                layoutParam = ViewGroup.LayoutParams.WRAP_CONTENT;
            }

            // Display list of photos
            for (int i = 0; i < nbrOfPhotos; i++) {
                String urlPhoto = PlaceDetails.urlPhotoFormatter(placeDetails, i);
                ImageView newPhoto = new ImageView(this);
                binding.restaurantPhotosGroup.addView(newPhoto, layoutParam, ViewGroup.LayoutParams.MATCH_PARENT);
                Glide.with(newPhoto.getContext())
                        .load(urlPhoto)
                        .apply(RequestOptions.fitCenterTransform())
                        .into(newPhoto);
            }

            displayStars();
        } else {
            Toast.makeText(this, "Une erreur est survenue", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void displayButtons() {
        if (getCurrentUser() != null) {
            viewModel.getUser(getCurrentUser().getUid()).observe(this, user -> {
                currentUser = user;
                if (user.restaurantIsSelected(placeId))
                    binding.addRestaurant.setImageDrawable(getResources().getDrawable(R.drawable.ic_check_circle_black_24dp));
                else
                    binding.addRestaurant.setImageDrawable(getResources().getDrawable(R.drawable.ic_add_black_24dp));

                if (user.restaurantNotFavorite(placeId))
                    binding.likeRestaurantStar.setImageDrawable(getResources().getDrawable(R.drawable.ic_star_border_black_24dp));
                else
                    binding.likeRestaurantStar.setImageDrawable(getResources().getDrawable(R.drawable.ic_star_full_24dp));
            });
        }
    }

    private void displayStars() {
        int numberOfStars = 0;
        if (placeDetails.getRating() != null) {
            numberOfStars = PlaceDetails.getNumberOfStarsToDisplay(placeDetails.getRating());
            if (numberOfStars == 1)
                binding.oneStar.setVisibility(View.VISIBLE);
            if (numberOfStars == 2)
                binding.twoStars.setVisibility(View.VISIBLE);
            if (numberOfStars == 3)
                binding.threeStars.setVisibility(View.VISIBLE);
        }
        restaurant = new Restaurant(placeId, placeDetails.getName(), placeDetails.getVicinity(),
                PlaceDetails.urlPhotoFormatter(placeDetails, 0), numberOfStars);
    }

    private void setUpRecyclerView() {
        FirestoreRecyclerOptions<User> options = new FirestoreRecyclerOptions.Builder<User>()
                .setQuery(viewModel.getUsersEatingAtRestaurantQuery(placeId), User.class)
                .build();

        adapter = new RestaurantWorkmatesListAdapter(options, this, Glide.with(this));
        binding.detailRecyclerViewWorkmates.setLayoutManager(new LinearLayoutManager(this));
        binding.detailRecyclerViewWorkmates.setAdapter(adapter);
        adapter.startListening();
    }

    @Override
    public void onClick(View v) {
        if (v == binding.addRestaurant)
            viewModel.getUser(currentUser.getUid()).observe(this, this::handleRestaurantSelection);
        else if (v == binding.callBtn)
            callRestaurant();
        else if (v == binding.likeRestaurantBtn)
            viewModel.getUser(currentUser.getUid()).observe(this, this::handleLikeRestaurantClick);
        else if (v == binding.websiteBtn)
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
                        weekdays.append(getDayWithUpperCase(weekdaysArray.get(i), i)).append("\n");
                    else
                        weekdays.append(getDayWithUpperCase(weekdaysArray.get(i), i));
                }
                binding.weekdaysOpenings.setText(weekdays.toString());
            } else {
                binding.weekdaysOpenings.setText(R.string.opening_hours_unavailable);
            }
        }
    }

    public String getDayWithUpperCase(String openingHour, int index) {
        String[] daysLowerCase = getResources().getStringArray(R.array.days_lower_case);
        String[] daysUpperCase = getResources().getStringArray(R.array.days_with_upper_case);
        return openingHour.replace(daysLowerCase[index], daysUpperCase[index]);
    }

    private void handleRestaurantSelection(User currentUser) {
        if (!currentUser.restaurantIsSelected(placeId)) {
            binding.addRestaurant.setImageDrawable(getResources().getDrawable(R.drawable.ic_check_circle_black_24dp));
            viewModel.updateChosenRestaurant(currentUser.getUid(), restaurant, getTodayDateInString()).observe(this, restaurantAdded -> {
                if (restaurantAdded == null) {
                    Toast.makeText(this, R.string.an_error_happened, Toast.LENGTH_LONG).show();
                    binding.addRestaurant.setImageDrawable(getResources().getDrawable(R.drawable.ic_add_black_24dp));
                }
            });
        } else {
            binding.addRestaurant.setImageDrawable(getResources().getDrawable(R.drawable.ic_add_black_24dp));
            viewModel.deleteChosenRestaurant(currentUser.getUid(), getTodayDateInString());
        }
    }

    private void callRestaurant() {
        if (placeDetails.getInternationalPhoneNumber() != null) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL);
            } else {
                String dial = getString(R.string.prefix_call) + placeDetails.getInternationalPhoneNumber();
                new AlertDialog.Builder(this)
                        .setMessage(getString(R.string.would_you_like_to_call_place, placeDetails.getName()))
                        .setPositiveButton(R.string.confirm_call, (paramDialogInterface, paramInt) ->
                                startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(dial))))
                        .setNegativeButton(R.string.Cancel, null)
                        .show();
            }
        } else
            Toast.makeText(this, R.string.phone_number_not_available, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CALL) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                callRestaurant();
            } else {
                Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void visitWebsite(String urlWebsite) {
        if (urlWebsite != null) {
            new AlertDialog.Builder(this)
                    .setMessage(getString(R.string.visit_website, placeDetails.getName()))
                    .setPositiveButton(R.string.open_browser, (paramDialogInterface, paramInt) ->
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(urlWebsite))))
                    .setNegativeButton(R.string.Cancel, null)
                    .show();
        } else
            Toast.makeText(this, R.string.website_unavailable, Toast.LENGTH_LONG).show();
    }

    private void handleLikeRestaurantClick(User currentUser) {
        if (currentUser.restaurantNotFavorite(placeId)) {
            viewModel.addRestaurantToFavorites(restaurant, currentUser.getUid());
            binding.likeRestaurantStar.setImageDrawable(getResources().getDrawable(R.drawable.ic_star_full_24dp));
            Toast.makeText(this, R.string.added_to_favorites, Toast.LENGTH_SHORT).show();
        } else {
            viewModel.deleteRestaurantFromFavorites(placeId, currentUser.getUid());
            binding.likeRestaurantStar.setImageDrawable(getResources().getDrawable(R.drawable.ic_star_border_black_24dp));
            Toast.makeText(this, R.string.deleted_from_fav, Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onStop() {
        super.onStop();
        if (adapter != null)
            adapter.stopListening();
    }

}
