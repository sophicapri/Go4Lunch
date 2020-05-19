package com.sophieopenclass.go4lunch.controllers.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.sophieopenclass.go4lunch.MyViewModel;
import com.sophieopenclass.go4lunch.base.BaseActivity;
import com.sophieopenclass.go4lunch.controllers.adapters.WorkmatesViewAdapter;
import com.sophieopenclass.go4lunch.databinding.RecyclerViewWorkmatesBinding;
import com.sophieopenclass.go4lunch.models.User;
import com.sophieopenclass.go4lunch.models.json_to_java.PlaceDetails;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class WorkmatesListFragment extends Fragment {
    public static final String TAG = "workmatesFrag";
    private RecyclerViewWorkmatesBinding binding;
    private MyViewModel viewModel;
    private WorkmatesViewAdapter adapter;
    private BaseActivity baseActivity;
    private String currentUserId;

    public static Fragment newInstance() {
        return new WorkmatesListFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = RecyclerViewWorkmatesBinding.inflate(inflater, container, false);
        if (getActivity() != null) {
            baseActivity = (BaseActivity) getActivity();
            viewModel = (MyViewModel) ((BaseActivity) getActivity()).getViewModel();
        }
        if (baseActivity.getCurrentUser() != null)
            currentUserId = baseActivity.getCurrentUser().getUid();

        binding.swipeRefreshView.setOnRefreshListener(() -> {
            updateWorkmatesList();
            binding.swipeRefreshView.setRefreshing(false);
        });
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        setUpRecyclerView();
        updateWorkmatesList();
    }

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
