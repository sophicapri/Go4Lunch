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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.sophieopenclass.go4lunch.MyViewModel;
import com.sophieopenclass.go4lunch.R;
import com.sophieopenclass.go4lunch.databinding.FragmentWorkmatesListBinding;
import com.sophieopenclass.go4lunch.models.User;
import com.sophieopenclass.go4lunch.models.UserPlaceId;
import com.sophieopenclass.go4lunch.utils.Constants.Controller;

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
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_workmates_list,
                parent, false);
        return new UserViewHolder(view, onWorkmateClickListener);
    }

    /**
     *
     * @param model  - the model received is either a User or a UserPlaceId (extending User) depending
     *               on which activity/fragment is called. The ViewModel is called to get all the information
     *               on the user.
     */
    @Override
    protected void onBindViewHolder(@NonNull UserViewHolder holder, int position, @NonNull User model) {
        if (controller == RESTAURANT_ACTIVITY)
            viewModel.getUser(model.getUid()).observe((LifecycleOwner) holder.context, holder::bind);
        else if (controller == WORKMATES_FRAGMENT) {
            if (!model.getUid().equals(currentUser.getUid())) {
                holder.bind(model);
            }
        }
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView profilePhoto;
        TextView restaurantPicked;
        OnWorkmateClickListener onWorkmateClickListener;
        Context context;

        UserViewHolder(@NonNull View itemView, OnWorkmateClickListener onWorkmateClickListener) {
            super(itemView);
            context = itemView.getContext();
            profilePhoto = FragmentWorkmatesListBinding.bind(itemView).workmateProfilePic;
            restaurantPicked = FragmentWorkmatesListBinding.bind(itemView).workmatesChoice;
            this.onWorkmateClickListener = onWorkmateClickListener;
            itemView.setOnClickListener(v -> onWorkmateClickListener.onWorkmateClick(getItem(getBindingAdapterPosition()).getUid()));
        }

        void bind(User model) {
            viewModel.getPlaceId(model.getUid()).observe((LifecycleOwner) context, placeId ->
            {
                Glide.with(profilePhoto.getContext())
                        .load(model.getUrlPicture())
                        .apply(RequestOptions.circleCropTransform())
                        .into(profilePhoto);

                if (controller == WORKMATES_FRAGMENT) {
                    if (placeId != null) {
                        viewModel.getPlaceDetails(placeId).observe((LifecycleOwner) context, placeDetails ->
                                restaurantPicked.setText(context.getResources()
                                        .getString(R.string.workmates_eating_at, model.getUsername(), placeDetails.getName())));
                    } else {
                        restaurantPicked.setText(context.getResources()
                                .getString(R.string.workmate_hasnt_chosen, model.getUsername()));
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            restaurantPicked.setTextAppearance(R.style.TextStyleItalic);
                        } else
                            restaurantPicked.setTextColor(context.getResources().getColor(R.color.quantum_grey700));
                    }
                } else {
                    if (model.getUid().equals(currentUser.getUid()))
                        restaurantPicked.setText(context.getResources().getString(R.string.you_are_eating_here));
                    else
                        restaurantPicked.setText(context.getResources().getString(R.string.workmate_is_eating_here, model.getUsername()));
                }
            });
        }
    }

    public interface OnWorkmateClickListener {
        void onWorkmateClick(String uid);
    }

    public void setViewModel(MyViewModel viewModel) {
        this.viewModel = viewModel;
    }
}
