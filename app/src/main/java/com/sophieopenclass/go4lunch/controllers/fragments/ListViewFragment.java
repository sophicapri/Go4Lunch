package com.sophieopenclass.go4lunch.controllers.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sophieopenclass.go4lunch.MyViewModel;
import com.sophieopenclass.go4lunch.base.BaseActivity;
import com.sophieopenclass.go4lunch.controllers.activities.RestaurantDetailsActivity;
import com.sophieopenclass.go4lunch.controllers.adapters.ListViewAdapter;
import com.sophieopenclass.go4lunch.databinding.RecyclerViewRestaurantsBinding;
import com.sophieopenclass.go4lunch.models.json_to_java.RestaurantsResult;

import static com.sophieopenclass.go4lunch.controllers.fragments.MapViewFragment.getLatLngString;
import static com.sophieopenclass.go4lunch.utils.Constants.PLACE_ID;

public class ListViewFragment extends Fragment implements ListViewAdapter.OnRestaurantClickListener {
    private MyViewModel viewModel;
    private RecyclerViewRestaurantsBinding binding;
    private BaseActivity context;

    public static Fragment newInstance() {
        return new ListViewFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = RecyclerViewRestaurantsBinding.inflate(inflater, container, false);
        if (getActivity() != null) {
            context = (BaseActivity) getActivity();
            viewModel = (MyViewModel) ((BaseActivity) getActivity()).getViewModel();
        }
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        observePlaces(null);
    }

    private void observePlaces(String nextPageToken) {
        binding.recyclerViewRestaurants.setHasFixedSize(true);
        binding.recyclerViewRestaurants.setLayoutManager(new LinearLayoutManager(getContext()));

        if (nextPageToken == null)
        viewModel.getNearbyPlaces(getLatLngString(context.currentLocation))
                .observe(getViewLifecycleOwner(), this::updateRecyclerView);
        else
            viewModel.getMoreNearbyPlaces(nextPageToken).observe(getViewLifecycleOwner(), this::updateRecyclerView);
    }

    private void updateRecyclerView(RestaurantsResult restaurants) {
        ListViewAdapter adapter = new ListViewAdapter(restaurants.getPlaceDetails(), this);
        adapter.setViewModel(viewModel);
        binding.recyclerViewRestaurants.setAdapter(adapter);

        //TODO : manage data update better
        binding.recyclerViewRestaurants.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!recyclerView.canScrollVertically(1))
                    if (restaurants.getNextPageToken() != null)
                    observePlaces(restaurants.getNextPageToken());
            }
        });

    }

    @Override
    public void onRestaurantClick(String placeId) {
        Intent intent = new Intent(getActivity(), RestaurantDetailsActivity.class);
        intent.putExtra(PLACE_ID, placeId);
        startActivity(intent);
    }
}
