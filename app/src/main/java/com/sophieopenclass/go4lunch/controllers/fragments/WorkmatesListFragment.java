package com.sophieopenclass.go4lunch.controllers.fragments;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.sophieopenclass.go4lunch.MyViewModel;
import com.sophieopenclass.go4lunch.base.BaseActivity;
import com.sophieopenclass.go4lunch.controllers.activities.MainActivity;
import com.sophieopenclass.go4lunch.controllers.adapters.WorkmatesViewAdapter;
import com.sophieopenclass.go4lunch.databinding.RecyclerViewWorkmatesBinding;
import com.sophieopenclass.go4lunch.models.User;

import java.util.ArrayList;
import java.util.List;

public class WorkmatesListFragment extends Fragment {
    private RecyclerViewWorkmatesBinding binding;
    private MyViewModel viewModel;
    private WorkmatesViewAdapter adapter;
    private String currentUserId;

    public static Fragment newInstance() {
        return new WorkmatesListFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = RecyclerViewWorkmatesBinding.inflate(inflater, container, false);

        if (getActivity() != null) {
            MainActivity activity = (MainActivity) getActivity();
            viewModel = ((MainActivity) getActivity()).getViewModel();

            if (activity.getCurrentUser() != null)
                currentUserId = activity.getCurrentUser().getUid();
            initSearchBar(activity);
        }

        binding.swipeRefreshView.setOnRefreshListener(() -> {
            updateWorkmatesList();
            binding.swipeRefreshView.setRefreshing(false);
        });
        return binding.getRoot();
    }

    private void initSearchBar(MainActivity activity) {
        activity.binding.searchBarWorkmates.closeSearchBar.setOnClickListener(v ->
                activity.binding.searchBarWorkmates.searchBarWorkmates.setVisibility(View.GONE));
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!isNetworkAvailable()) {
            Toast.makeText(getContext(), "No internet connection", Toast.LENGTH_SHORT).show();
        } else {
            setUpRecyclerView();
            updateWorkmatesList();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
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
                if (user.getDatesAndPlaceIds().get(User.getTodaysDate()) != null)
                    orderedList.add(user);
                else
                    workmatesWithoutRestaurant.add(user);
            }
            orderedList.addAll(workmatesWithoutRestaurant);
            adapter.updateList(orderedList);
        });
    }
}
