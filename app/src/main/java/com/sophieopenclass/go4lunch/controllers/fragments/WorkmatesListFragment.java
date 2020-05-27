package com.sophieopenclass.go4lunch.controllers.fragments;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.sophieopenclass.go4lunch.MyViewModel;
import com.sophieopenclass.go4lunch.R;
import com.sophieopenclass.go4lunch.base.BaseActivity;
import com.sophieopenclass.go4lunch.controllers.activities.MainActivity;
import com.sophieopenclass.go4lunch.controllers.adapters.WorkmatesViewAdapter;
import com.sophieopenclass.go4lunch.databinding.RecyclerViewWorkmatesBinding;
import com.sophieopenclass.go4lunch.models.User;

import java.util.ArrayList;
import java.util.List;

import static com.sophieopenclass.go4lunch.base.BaseActivity.ORIENTATION_CHANGED;
import static com.sophieopenclass.go4lunch.utils.DateFormatting.getTodayDateInString;

public class WorkmatesListFragment extends Fragment {
    private RecyclerViewWorkmatesBinding binding;
    private MyViewModel viewModel;
    private WorkmatesViewAdapter adapter;
    private String currentUserId;
    private MainActivity activity;

    public static Fragment newInstance() {
        return new WorkmatesListFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = RecyclerViewWorkmatesBinding.inflate(inflater, container, false);

        if (getActivity() != null) {
            activity = (MainActivity) getActivity();
            viewModel = ((MainActivity) getActivity()).getViewModel();

            if (activity.getCurrentUser() != null)
                currentUserId = activity.getCurrentUser().getUid();
            initSearchBar();
        }

        binding.swipeRefreshView.setOnRefreshListener(() -> {
            if (networkUnavailable()) {
                Snackbar.make(binding.getRoot(), R.string.internet_unavailable, BaseTransientBottomBar.LENGTH_INDEFINITE)
                        .setTextColor(getResources().getColor(R.color.quantum_white_100)).setDuration(5000).show();
            } else
                updateWorkmatesList();
            binding.swipeRefreshView.setRefreshing(false);
        });
        return binding.getRoot();
    }

    private void initSearchBar() {
        activity.binding.searchBarWorkmates.closeSearchBar.setOnClickListener(v ->
                activity.binding.searchBarWorkmates.searchBarWorkmates.setVisibility(View.GONE));
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        ORIENTATION_CHANGED = true;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (networkUnavailable()) {
            Snackbar.make(binding.getRoot(), getString(R.string.internet_unavailable), BaseTransientBottomBar.LENGTH_INDEFINITE)
                    .setTextColor(getResources().getColor(R.color.quantum_white_100)).setDuration(5000).show();
        } else {
            setUpRecyclerView();
            updateWorkmatesList();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ORIENTATION_CHANGED = false;
    }

    private boolean networkUnavailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = null;
        if (connectivityManager != null) {
            activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        }
        return activeNetworkInfo == null || !activeNetworkInfo.isConnected();
    }

    // Not using FirestoreRecyclerAdapter because it isn't possible to retrieve all the users
    // except one.
    private void setUpRecyclerView() {
        adapter = new WorkmatesViewAdapter(((BaseActivity) getActivity()), Glide.with(this));
        binding.recyclerViewWorkmates.setHasFixedSize(true);
        binding.recyclerViewWorkmates.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewWorkmates.setAdapter(adapter);
    }

    private void updateWorkmatesList() {
        List<User> usersWithoutCurrentLogged = new ArrayList<>();
        viewModel.getListUsers().observe(getViewLifecycleOwner(), users -> {
            for (User user : users) {
                if (!user.getUid().equals(currentUserId))
                    usersWithoutCurrentLogged.add(user);
            }
            List<User> orderedList = new ArrayList<>();
            List<User> workmatesWithoutRestaurant = new ArrayList<>();
            for (User user : usersWithoutCurrentLogged) {
                if (user.getDatesAndRestaurants().get(getTodayDateInString()) != null)
                    orderedList.add(user);
                else
                    workmatesWithoutRestaurant.add(user);
            }
            orderedList.addAll(workmatesWithoutRestaurant);
            adapter.updateList(orderedList);
        });
    }
}
