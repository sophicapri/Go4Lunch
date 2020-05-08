package com.sophieopenclass.go4lunch.controllers.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.sophieopenclass.go4lunch.MyViewModel;
import com.sophieopenclass.go4lunch.base.BaseActivity;
import com.sophieopenclass.go4lunch.controllers.activities.WorkmateDetailActivity;
import com.sophieopenclass.go4lunch.controllers.adapters.WorkmatesViewAdapter;
import com.sophieopenclass.go4lunch.databinding.RecyclerViewWorkmatesBinding;
import com.sophieopenclass.go4lunch.injection.Injection;
import com.sophieopenclass.go4lunch.models.User;
import com.sophieopenclass.go4lunch.models.json_to_java.PlaceDetails;
import com.sophieopenclass.go4lunch.utils.Constants;

import java.util.Arrays;

import static com.sophieopenclass.go4lunch.utils.Constants.WORKMATES_FRAGMENT;

public class WorkmatesListFragment extends Fragment implements WorkmatesViewAdapter.OnWorkmateClickListener {
    private RecyclerViewWorkmatesBinding binding;
    private MyViewModel viewModel;
    private FirestoreRecyclerAdapter adapter;
    private FirebaseUser currentUser;

    public static Fragment newInstance() {
        return new WorkmatesListFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = RecyclerViewWorkmatesBinding.inflate(inflater, container, false);

        if (getActivity() != null) {
            viewModel = (MyViewModel) ((BaseActivity) getActivity()).getViewModel();
            currentUser = ((BaseActivity) getActivity()).getCurrentUser();
        }

        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();

        // PROVISOIRE :
        viewModel.getUser("234").observe(getViewLifecycleOwner(), this::addPlaceDetailToDummyUser);

        setUpRecyclerView();
    }

    private void addPlaceDetailToDummyUser(User user) {
        viewModel.getPlaceDetails(user.getPlaceId()).observe(getViewLifecycleOwner(), user::setRestaurantChosen);
    }

    private void setUpRecyclerView() {
        FirestoreRecyclerOptions<User> options = new FirestoreRecyclerOptions.Builder<User>()
                .setQuery(viewModel.getUsersCollectionReference(), User.class)
                .build();
        adapter = new WorkmatesViewAdapter(options, currentUser,  WORKMATES_FRAGMENT, this);
        binding.recyclerViewWorkmates.setHasFixedSize(true);
        binding.recyclerViewWorkmates.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewWorkmates.setAdapter(adapter);
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapter != null)
            adapter.stopListening();
    }

    @Override
    public void onWorkmateClick(String uid) {
        Intent intent = new Intent(getContext(), WorkmateDetailActivity.class);
        intent.putExtra("uid", uid);
        startActivity(intent);
    }
}
