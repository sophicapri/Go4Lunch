package com.sophieopenclass.go4lunch.controllers.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.sophieopenclass.go4lunch.MyViewModel;
import com.sophieopenclass.go4lunch.databinding.FragmentListViewBinding;
import com.sophieopenclass.go4lunch.injection.Injection;
import com.sophieopenclass.go4lunch.models.POJO.Restaurants;

import static com.sophieopenclass.go4lunch.controllers.fragments.MapViewFragment.currentLocation;
import static com.sophieopenclass.go4lunch.controllers.fragments.MapViewFragment.getLatLngString;
import static com.sophieopenclass.go4lunch.controllers.fragments.MapViewFragment.getRadius;

public class ListViewFragment extends Fragment {
    MyViewModel viewModel;

    public static Fragment newInstance() {
        return new ListViewFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentListViewBinding binding = FragmentListViewBinding.inflate(getLayoutInflater());
        viewModel = Injection.getViewModel();
        observePlaces();
        return binding.getRoot();
    }

    private void observePlaces() {
        viewModel.getNearbyPlaces(getLatLngString(currentLocation), getRadius())
                .observe(getViewLifecycleOwner(), this::updateRecyclerView);
    }

    private void updateRecyclerView(Restaurants restaurants) {

    }
}
