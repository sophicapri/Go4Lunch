package com.sophieopenclass.go4lunch.controllers.fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

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

import static com.sophieopenclass.go4lunch.utils.DateFormatting.getTodayDateInString;

public class WorkmatesListFragment extends Fragment {

    private RecyclerViewWorkmatesBinding binding;
    private MyViewModel viewModel;
    private WorkmatesViewAdapter adapter;
    private String currentUserId;
    private MainActivity activity;
    private List<User> workmateFinalList = new ArrayList<>();

    public static Fragment newInstance() {
        return new WorkmatesListFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = RecyclerViewWorkmatesBinding.inflate(inflater, container, false);
        if (getActivity() != null) {
            activity = (MainActivity) getActivity();
            viewModel = activity.getViewModel();
            if (activity.getCurrentUser() != null)
                currentUserId = activity.getCurrentUser().getUid();
        }
        initSearchBar();
        binding.swipeRefreshView.setOnRefreshListener(this::initSwipeRefreshListener);
        return binding.getRoot();
    }

    private void initSwipeRefreshListener() {
        if (activity.networkUnavailable()) {
            Snackbar.make(binding.getRoot(), R.string.internet_unavailable, BaseTransientBottomBar.LENGTH_INDEFINITE)
                    .setTextColor(getResources().getColor(R.color.quantum_white_100)).setDuration(5000).show();
        } else
            updateWorkmatesList();
        binding.swipeRefreshView.setRefreshing(false);
    }

    private void initSearchBar() {
        TextWatcher textWatcher = getTextWatcher();
        activity.binding.searchBarWorkmates.searchBarInput.addTextChangedListener(textWatcher);
        activity.binding.searchBarWorkmates.closeSearchBar.setOnClickListener(v -> {
            closeSearchBar();
            adapter.updateList(workmateFinalList);
        });
    }

    private TextWatcher getTextWatcher() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable input) {
                viewModel.searchWorkmate(input.toString())
                        .observe(activity, users -> adapter.updateList(users));
            }
        };
    }

    private void closeSearchBar() {
        InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        activity.binding.searchBarWorkmates.searchBarWorkmates.setVisibility(View.GONE);
        activity.binding.searchBarWorkmates.searchBarInput.getText().clear();
        if (inputManager != null) {
            inputManager.hideSoftInputFromWindow(activity.binding.searchBarWorkmates.searchBarInput.getWindowToken(), 0);
        }
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        activity.orientationChanged = true;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (activity.networkUnavailable()) {
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
        activity.orientationChanged = false;
    }

    // Not using FirestoreRecyclerAdapter because it isn't possible to retrieve all the users
    // except one
    private void setUpRecyclerView() {
        adapter = new WorkmatesViewAdapter(((BaseActivity) getActivity()), Glide.with(this));
        binding.recyclerViewWorkmates.setHasFixedSize(true);
        binding.recyclerViewWorkmates.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewWorkmates.setAdapter(adapter);
    }

    private void updateWorkmatesList() {
        workmateFinalList.clear();
        List<User> usersWithoutCurrentLogged = new ArrayList<>();
        viewModel.getListUsers().observe(getViewLifecycleOwner(), users -> {
            for (User user : users) {
                if (!user.getUid().equals(currentUserId)) {
                    usersWithoutCurrentLogged.add(user);
                }
            }
            viewModel.populateAlgolia(usersWithoutCurrentLogged);

            //To display at the top of the list the workmates who have selected a restaurant
            List<User> workmatesWithoutRestaurant = new ArrayList<>();
            for (User user : usersWithoutCurrentLogged) {
                if (user.getDatesAndRestaurants().get(getTodayDateInString()) != null)
                    workmateFinalList.add(user);
                else
                    workmatesWithoutRestaurant.add(user);
            }
            workmateFinalList.addAll(workmatesWithoutRestaurant);
            adapter.updateList(workmateFinalList);
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        workmateFinalList.clear();
        closeSearchBar();
    }
}
