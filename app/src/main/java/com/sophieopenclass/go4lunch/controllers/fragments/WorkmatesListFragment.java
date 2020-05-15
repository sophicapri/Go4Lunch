package com.sophieopenclass.go4lunch.controllers.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.sophieopenclass.go4lunch.MyViewModel;
import com.sophieopenclass.go4lunch.base.BaseActivity;
import com.sophieopenclass.go4lunch.controllers.adapters.WorkmatesViewAdapter;
import com.sophieopenclass.go4lunch.databinding.RecyclerViewWorkmatesBinding;
import com.sophieopenclass.go4lunch.models.User;

import static com.sophieopenclass.go4lunch.injection.Injection.USER_COLLECTION_NAME;
import static com.sophieopenclass.go4lunch.utils.Constants.WORKMATES_FRAGMENT;

public class WorkmatesListFragment extends Fragment {
    private RecyclerViewWorkmatesBinding binding;
    private MyViewModel viewModel;
    private FirestoreRecyclerAdapter adapter;

    public static Fragment newInstance() {
        return new WorkmatesListFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = RecyclerViewWorkmatesBinding.inflate(inflater, container, false);

        if (getActivity() != null) {
            viewModel = (MyViewModel) ((BaseActivity) getActivity()).getViewModel();
        }
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        setUpRecyclerView();
    }

    private void setUpRecyclerView() {
        FirestoreRecyclerOptions<User> options = new FirestoreRecyclerOptions.Builder<User>()
                .setQuery(viewModel.getCollectionReference(), User.class)
                .build();
        adapter = new WorkmatesViewAdapter(options,  WORKMATES_FRAGMENT, ((BaseActivity) getActivity()));
        ((WorkmatesViewAdapter) adapter).setViewModel(viewModel);
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
}
