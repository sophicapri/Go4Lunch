package com.sophieopenclass.go4lunch.controllers.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.sophieopenclass.go4lunch.MyViewModel;
import com.sophieopenclass.go4lunch.base.BaseActivity;
import com.sophieopenclass.go4lunch.controllers.activities.RestaurantDetailsActivity;
import com.sophieopenclass.go4lunch.controllers.adapters.ListViewAdapter;
import com.sophieopenclass.go4lunch.databinding.RecyclerViewRestaurantsBinding;
import com.sophieopenclass.go4lunch.models.json_to_java.RestaurantsResult;

import static com.sophieopenclass.go4lunch.controllers.fragments.MapViewFragment.currentLocation;
import static com.sophieopenclass.go4lunch.controllers.fragments.MapViewFragment.getLatLngString;
import static com.sophieopenclass.go4lunch.controllers.fragments.MapViewFragment.getRadius;

public class ListViewFragment extends Fragment implements ListViewAdapter.OnRestaurantClickListener {
    MyViewModel viewModel;
    RecyclerViewRestaurantsBinding binding;

    public static Fragment newInstance() {
        return new ListViewFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = RecyclerViewRestaurantsBinding.inflate(inflater, container, false);
        if (getActivity() != null)
            viewModel = (MyViewModel)((BaseActivity) getActivity()).getViewModel();

        observePlaces();
        return binding.getRoot();
    }

    private void observePlaces() {
        binding.recyclerViewRestaurants.setHasFixedSize(true);
        binding.recyclerViewRestaurants.setLayoutManager(new LinearLayoutManager(getContext()));
        viewModel.getNearbyPlaces(getLatLngString(currentLocation), getRadius())
                .observe(getViewLifecycleOwner(), this::updateRecyclerView);
    }

    private void updateRecyclerView(RestaurantsResult restaurants) {
        binding.recyclerViewRestaurants.setAdapter(new ListViewAdapter(restaurants.getPlaceDetails(), this));
    }

    @Override
    public void onRestaurantClick(String placeId) {
        Intent intent = new Intent(getActivity(), RestaurantDetailsActivity.class);
        intent.putExtra("placeId", placeId);
        startActivity(intent);
    }
}
