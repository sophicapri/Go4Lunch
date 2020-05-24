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
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.sophieopenclass.go4lunch.MyViewModel;
import com.sophieopenclass.go4lunch.R;
import com.sophieopenclass.go4lunch.base.BaseActivity;
import com.sophieopenclass.go4lunch.controllers.adapters.WorkmatesListAdapter;
import com.sophieopenclass.go4lunch.databinding.ActivityRestaurantDetailsBinding;
import com.sophieopenclass.go4lunch.models.User;
import com.sophieopenclass.go4lunch.models.json_to_java.PlaceDetails;
import com.sophieopenclass.go4lunch.utils.CalculateRatings;

import java.util.List;
import java.util.Locale;

import static com.sophieopenclass.go4lunch.utils.Constants.DATES_AND_PLACE_IDS_FIELD;
import static com.sophieopenclass.go4lunch.utils.Constants.PLACE_ID;

public class RestaurantDetailsActivity extends BaseActivity<MyViewModel> implements View.OnClickListener {
    public static final int REQUEST_CALL = 567;
    public static final int MAX_PHOTOS = 3;
    private ActivityRestaurantDetailsBinding binding;
    private FirestoreRecyclerAdapter adapter;
    private String placeId;
    private PlaceDetails placeDetails;
    private User currentUser;

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
    }

    @Override
    protected void onStart() {
        super.onStart();
        binding.oneStar.setVisibility(View.GONE);
        binding.twoStars.setVisibility(View.GONE);
        binding.threeStars.setVisibility(View.GONE);

        if (getIntent().getExtras() != null) {
            if (getIntent().hasExtra(PLACE_ID)) {
                placeId = (String) getIntent().getExtras().get(PLACE_ID);
                if (placeId != null && !placeId.isEmpty())
                    viewModel.getPlaceDetails(placeId).observe(this, this::initUI);
                else {
                    Toast.makeText(this, R.string.error_unknown_error, Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
        setUpRecyclerView();
    }


    private void initUI(PlaceDetails placeDetails) {
        this.placeDetails = placeDetails;
        binding.detailsRestaurantName.setText(placeDetails.getName());
        binding.detailsRestaurantAddress.setText(placeDetails.getVicinity());
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
        for (int i = 0; i < nbrOfPhotos && i < MAX_PHOTOS; i++) {
            String urlPhoto = PlaceDetails.urlPhotoFormatter(placeDetails, i);
            ImageView newPhoto = new ImageView(this);
            binding.restaurantPhotosGroup.addView(newPhoto, layoutParam, ViewGroup.LayoutParams.MATCH_PARENT);
            Glide.with(newPhoto.getContext())
                    .load(urlPhoto)
                    .apply(RequestOptions.fitCenterTransform())
                    .into(newPhoto);
        }

        displayStars();
        displayButtons();
    }

    private void displayButtons() {
        if (getCurrentUser() != null) {
            viewModel.getUser(getCurrentUser().getUid()).observe(this, user -> {
                currentUser = user;
                if (placeId.equals(user.getDatesAndPlaceIds().get(User.getTodaysDate())))
                    binding.addRestaurant.setImageDrawable(getResources().getDrawable(R.drawable.ic_check_circle_black_24dp));
                else
                    binding.addRestaurant.setImageDrawable(getResources().getDrawable(R.drawable.ic_add_black_24dp));

                if (!user.getFavoriteRestaurantIds().contains(placeId))
                    binding.likeRestaurantStar.setImageDrawable(getResources().getDrawable(R.drawable.ic_star_border_black_24dp));
                else
                    binding.likeRestaurantStar.setImageDrawable(getResources().getDrawable(R.drawable.ic_star_full_24dp));
            });
        }
    }

    private void displayStars() {
        if (placeDetails.getRating() != null) {
            int numberOfStars = CalculateRatings.getNumberOfStarsToDisplay(placeDetails.getRating());
            if (numberOfStars == 1)
                binding.oneStar.setVisibility(View.VISIBLE);
            if (numberOfStars == 2)
                binding.twoStars.setVisibility(View.VISIBLE);
            if (numberOfStars == 3)
                binding.threeStars.setVisibility(View.VISIBLE);
        }
    }

    private void setUpRecyclerView() {
        FirestoreRecyclerOptions<User> options = new FirestoreRecyclerOptions.Builder<User>()
                .setQuery(viewModel.getCollectionReference()
                        .whereEqualTo(DATES_AND_PLACE_IDS_FIELD + User.getTodaysDate(), placeId), User.class)
                .build();
        adapter = new WorkmatesListAdapter(options, this, Glide.with(this));
        binding.detailRecyclerViewWorkmates.setHasFixedSize(true);
        binding.detailRecyclerViewWorkmates.setLayoutManager(new LinearLayoutManager(this));
        binding.detailRecyclerViewWorkmates.setAdapter(adapter);
        adapter.startListening();
    }

    @Override
    public void onClick(View v) {
        if (v == binding.addRestaurant)
            viewModel.getPlaceIdByDate(currentUser.getUid(), User.getTodaysDate()).observe(this, this::handleRestaurantSelection);
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
                        if (!Locale.getDefault().getLanguage().equals(Locale.FRANCE.getLanguage()))
                            weekdays.append(weekdaysArray.get(i)).append("\n");
                        else
                            weekdays.append(getDayInFrench(weekdaysArray.get(i), i)).append("\n");
                    else if (!Locale.getDefault().getLanguage().equals(Locale.FRANCE.getLanguage()))
                        weekdays.append(weekdaysArray.get(i));
                    else
                        weekdays.append(getDayInFrench(weekdaysArray.get(i), i));
                }
                binding.weekdaysOpenings.setText(weekdays.toString());
            } else {
                binding.weekdaysOpenings.setText(R.string.opening_hours_unavailable);
            }
        }
    }

    public String getDayInFrench(String openingHour, int index) {
        String[] daysInEnglish = getResources().getStringArray(R.array.days_in_english);
        String[] daysInFrench = getResources().getStringArray(R.array.days_in_french);
        String result = openingHour.replace(daysInEnglish[index], daysInFrench[index]);
        if (result.contains(getString(R.string.closed)))
            result = result.replace(getString(R.string.closed), getString(R.string.closed_in_french));
        return result;
    }

    private void handleRestaurantSelection(String currentUserPlaceId) {
        if (!placeId.equals(currentUserPlaceId)) {
            binding.addRestaurant.setImageDrawable(getResources().getDrawable(R.drawable.ic_check_circle_black_24dp));
            if (currentUserPlaceId == null)
                viewModel.updateUserPlaceId(currentUser.getUid(), placeId, User.getTodaysDate()).observe(this, placeId -> {
                    if (placeId == null) {
                        Toast.makeText(this, R.string.an_error_happened, Toast.LENGTH_LONG).show();
                        viewModel.updateRestaurantChosen(currentUser.getUid(), "", "");
                        binding.addRestaurant.setImageDrawable(getResources().getDrawable(R.drawable.ic_add_black_24dp));
                    }
                });
            else
                viewModel.updateUserPlaceId(currentUser.getUid(), placeId, User.getTodaysDate());
            viewModel.updateRestaurantChosen(currentUser.getUid(), placeDetails.getName(), placeDetails.getVicinity());
        } else {
            binding.addRestaurant.setImageDrawable(getResources().getDrawable(R.drawable.ic_add_black_24dp));
            viewModel.deletePlaceId(currentUser.getUid(), User.getTodaysDate());
            viewModel.updateRestaurantChosen(currentUser.getUid(), "", "");
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
        if (!currentUser.getFavoriteRestaurantIds().contains(placeId)) {
            viewModel.addRestaurantToFavorites(placeId, currentUser.getUid());
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
