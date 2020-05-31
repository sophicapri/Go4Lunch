package com.sophieopenclass.go4lunch.controllers.fragments;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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

import com.algolia.search.saas.Client;
import com.algolia.search.saas.Index;
import com.algolia.search.saas.Query;
import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.sophieopenclass.go4lunch.BuildConfig;
import com.sophieopenclass.go4lunch.MyViewModel;
import com.sophieopenclass.go4lunch.R;
import com.sophieopenclass.go4lunch.base.BaseActivity;
import com.sophieopenclass.go4lunch.controllers.activities.MainActivity;
import com.sophieopenclass.go4lunch.controllers.adapters.WorkmatesViewAdapter;
import com.sophieopenclass.go4lunch.databinding.RecyclerViewWorkmatesBinding;
import com.sophieopenclass.go4lunch.models.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.sophieopenclass.go4lunch.base.BaseActivity.ORIENTATION_CHANGED;
import static com.sophieopenclass.go4lunch.utils.Constants.HITS_ALGOLIA;
import static com.sophieopenclass.go4lunch.utils.Constants.INDEX_WORKMATES;
import static com.sophieopenclass.go4lunch.utils.Constants.UID_FIELD;
import static com.sophieopenclass.go4lunch.utils.DateFormatting.getTodayDateInString;

public class WorkmatesListFragment extends Fragment {

    private RecyclerViewWorkmatesBinding binding;
    private MyViewModel viewModel;
    private WorkmatesViewAdapter adapter;
    private String currentUserId;
    private MainActivity activity;
    private Index index;
    private List<User> workmateFinalList = new ArrayList<>();

    public static Fragment newInstance() {
        return new WorkmatesListFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = RecyclerViewWorkmatesBinding.inflate(inflater, container, false);

        initAlgolia();

        if (getActivity() != null) {
            activity = (MainActivity) getActivity();
            viewModel = ((MainActivity) getActivity()).getViewModel();

            if (activity.getCurrentUser() != null)
                currentUserId = activity.getCurrentUser().getUid();
        }

        initSearchBar();
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

    private void initAlgolia() {
        Client client = new Client(BuildConfig.ALGOLIA_APP_ID, BuildConfig.ALGOLIA_API_KEY);
        index = client.getIndex(INDEX_WORKMATES);
    }

    private void initSearchBar() {
        InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        activity.binding.searchBarWorkmates.closeSearchBar.setOnClickListener(v -> {
            activity.binding.searchBarWorkmates.searchBarWorkmates.setVisibility(View.GONE);
            activity.binding.searchBarWorkmates.searchBarInput.getText().clear();
            adapter.updateList(workmateFinalList);
            if (inputManager != null) {
                inputManager.hideSoftInputFromWindow(activity.binding.searchBarWorkmates.searchBarInput.getWindowToken(), 0);
            }
        });

        activity.binding.searchBarWorkmates.searchBarInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Query query = new Query(s.toString()).setAttributesToRetrieve(UID_FIELD)
                        .setHitsPerPage(20);

                index.searchAsync(query, (jsonObject, e) -> {
                    if (jsonObject != null)
                        try {
                            JSONArray hits = jsonObject.getJSONArray(HITS_ALGOLIA);
                            List<User> users = new ArrayList<>();
                            for (int i = 0; i < hits.length(); i++) {
                                for (User user : workmateFinalList) {
                                    if (hits.getJSONObject(i).getString(UID_FIELD).equals(user.getUid()))
                                        users.add(user);
                                }
                            }
                            adapter.updateList(users);
                        } catch (JSONException ex) {
                            ex.printStackTrace();
                        }

                });

            }
        });

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
        workmateFinalList.clear();
        List<User> usersWithoutCurrentLogged = new ArrayList<>();
        viewModel.getListUsers().observe(getViewLifecycleOwner(), users -> {
            for (User user : users) {
                if (!user.getUid().equals(currentUserId)) {
                    usersWithoutCurrentLogged.add(user);
                    populateAlgolia(user);
                }
            }

            //To display the workmates who have selected a restaurant at the top of the list
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

    private void populateAlgolia(User user) {
        Gson gson = new Gson();
        String jsonUser = gson.toJson(user);
        index.getObjectAsync(user.getUid(), (content, error) -> {
            if (content != null) {
                try {
                    index.saveObjectAsync(new JSONObject(jsonUser), user.getUid(), null);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    index.addObjectAsync(new JSONObject(jsonUser), user.getUid(), null);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        workmateFinalList.clear();
    }
}
