package com.sophieopenclass.go4lunch.controllers.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseUser;
import com.sophieopenclass.go4lunch.R;
import com.sophieopenclass.go4lunch.databinding.FragmentWorkmatesListBinding;
import com.sophieopenclass.go4lunch.models.User;
import com.sophieopenclass.go4lunch.utils.Constants;
import com.sophieopenclass.go4lunch.utils.Constants.Controller;

public class WorkmatesViewAdapter extends FirestoreRecyclerAdapter<User, WorkmatesViewAdapter.UserViewHolder> {
    private FirebaseUser currentUser;
    private OnWorkmateClickListener onWorkmateClickListener;
    private User selectedUser;
    private int controller;

    public WorkmatesViewAdapter(@NonNull FirestoreRecyclerOptions<User> options, FirebaseUser currentUser, @Controller int controller,
                                OnWorkmateClickListener onWorkmateClickListener) {
        super(options);
        this.currentUser = currentUser;
        this.onWorkmateClickListener = onWorkmateClickListener;
        this.controller = controller;
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
        selectedUser = model;
        if (currentUser == null /* provisoire */ ||
                !model.getUid().equals(currentUser.getUid()))
            holder.bind(model);
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView profilePhoto;
        TextView restaurantPicked;
        OnWorkmateClickListener onWorkmateClickListener;

        UserViewHolder(@NonNull View itemView, OnWorkmateClickListener onWorkmateClickListener) {
            super(itemView);
            profilePhoto = FragmentWorkmatesListBinding.bind(itemView).workmateProfilePic;
            restaurantPicked = FragmentWorkmatesListBinding.bind(itemView).workmatesChoice;
            this.onWorkmateClickListener = onWorkmateClickListener;
            itemView.setOnClickListener(v -> onWorkmateClickListener.onWorkmateClick(selectedUser.getUid()));
        }

        void bind(User model) {
            Glide.with(profilePhoto.getContext())
                    .load(model.getUrlPicture())
                    .apply(RequestOptions.circleCropTransform())
                    .into(profilePhoto);

            if (controller == Constants.WORKMATES_FRAGMENT) {
                if (model.getChosenRestaurant() != null) {
                    restaurantPicked.setText(model.getUsername() + " mange chez " + model.getChosenRestaurant().getName());
                } else {
                    restaurantPicked.setText(model.getUsername() + " n'a pas encore choisi de restaurant");
                    restaurantPicked.setTextColor(profilePhoto.getContext().getResources()
                            .getColor(R.color.quantum_grey700));
                    //TODO : unable to turn text to italic dynamically.
                }
            } else {
                restaurantPicked.setText(" mange ici !");
            }
        }
    }

    public interface OnWorkmateClickListener {
        void onWorkmateClick(String uid);
    }
}
