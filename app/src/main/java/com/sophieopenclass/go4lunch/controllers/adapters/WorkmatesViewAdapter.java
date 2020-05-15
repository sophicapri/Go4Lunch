package com.sophieopenclass.go4lunch.controllers.adapters;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.ObservableSnapshotArray;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.sophieopenclass.go4lunch.MyViewModel;
import com.sophieopenclass.go4lunch.R;
import com.sophieopenclass.go4lunch.databinding.FragmentWorkmatesListBinding;
import com.sophieopenclass.go4lunch.models.User;
import com.sophieopenclass.go4lunch.utils.Constants.Controller;

import static com.sophieopenclass.go4lunch.listeners.Listeners.*;
import static com.sophieopenclass.go4lunch.utils.Constants.RESTAURANT_ACTIVITY;
import static com.sophieopenclass.go4lunch.utils.Constants.WORKMATES_FRAGMENT;

public class WorkmatesViewAdapter extends FirestoreRecyclerAdapter<User, WorkmatesViewAdapter.UserViewHolder> {
    private FirebaseUser currentUser;
    private OnWorkmateClickListener onWorkmateClickListener;
    private int controller;
    private MyViewModel viewModel;

    public WorkmatesViewAdapter(@NonNull FirestoreRecyclerOptions<User> options, @Controller int controller,
                                OnWorkmateClickListener onWorkmateClickListener) {
        super(options);
        this.controller = controller;
        this.onWorkmateClickListener = onWorkmateClickListener;
        this.currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @NonNull
    @Override
    public ObservableSnapshotArray<User> getSnapshots() {
        return super.getSnapshots();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_workmates_list,
                parent, false);
        return new UserViewHolder(view, onWorkmateClickListener);
    }


    @Override
    protected void onBindViewHolder(@NonNull UserViewHolder holder, int position, @NonNull User model) {
        if (controller == RESTAURANT_ACTIVITY)
            holder.bind(model);
        else if (controller == WORKMATES_FRAGMENT) {
            if (!model.getUid().equals(currentUser.getUid())) {
                holder.bind(model);
            }
        }
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        OnWorkmateClickListener onWorkmateClickListener;
        FragmentWorkmatesListBinding binding;
        Context context;

        UserViewHolder(@NonNull View itemView, OnWorkmateClickListener onWorkmateClickListener) {
            super(itemView);
            context = itemView.getContext();
            binding = FragmentWorkmatesListBinding.bind(itemView);
            this.onWorkmateClickListener = onWorkmateClickListener;
            itemView.setOnClickListener(v -> onWorkmateClickListener.onWorkmateClick(getItem(getBindingAdapterPosition()).getUid()));
        }

        void bind(User model) {
            String placeId = model.getDatesAndPlaceIds().get(User.getTodaysDate());
            Glide.with(binding.workmateProfilePic.getContext())
                    .load(model.getUrlPicture())
                    .apply(RequestOptions.circleCropTransform())
                    .into(binding.workmateProfilePic);

            if (controller == WORKMATES_FRAGMENT) {
                if (placeId != null) {
                    binding.workmatesChoice.setText(context.getResources()
                            .getString(R.string.workmates_eating_at, model.getUsername(), model.getChosenRestaurantName()));
                } else {
                    binding.workmatesChoice.setText(context.getResources()
                            .getString(R.string.workmate_hasnt_chosen, model.getUsername()));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        binding.workmatesChoice.setTextAppearance(R.style.TextStyleItalic);
                    } else
                        binding.workmatesChoice.setTextColor(context.getResources().getColor(R.color.quantum_grey700));
                }
            } else {
                if (model.getUid().equals(currentUser.getUid()))
                    binding.workmatesChoice.setText(context.getResources().getString(R.string.you_are_eating_here));
                else
                    binding.workmatesChoice.setText(context.getResources().getString(R.string.workmate_is_eating_here, model.getUsername()));
            }
        }
    }

    public void setViewModel(MyViewModel viewModel) {
        this.viewModel = viewModel;
    }
}
