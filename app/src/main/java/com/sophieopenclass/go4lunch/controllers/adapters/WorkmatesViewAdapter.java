package com.sophieopenclass.go4lunch.controllers.adapters;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.ObservableSnapshotArray;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.sophieopenclass.go4lunch.R;
import com.sophieopenclass.go4lunch.databinding.FragmentWorkmatesListBinding;
import com.sophieopenclass.go4lunch.models.User;
import com.sophieopenclass.go4lunch.utils.Constants.Controller;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

import static com.sophieopenclass.go4lunch.listeners.Listeners.OnWorkmateClickListener;
import static com.sophieopenclass.go4lunch.utils.Constants.RESTAURANT_ACTIVITY;
import static com.sophieopenclass.go4lunch.utils.Constants.WORKMATES_FRAGMENT;

public class WorkmatesViewAdapter extends RecyclerView.Adapter<WorkmatesViewAdapter.UserViewHolder> {
    private OnWorkmateClickListener onWorkmateClickListener;
    private List<User> workmates = new ArrayList<>();
    private RequestManager glide;

    public WorkmatesViewAdapter(OnWorkmateClickListener onWorkmateClickListener, RequestManager glide) {
        this.onWorkmateClickListener = onWorkmateClickListener;
        this.glide = glide;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_workmates_list,
                parent, false);
        return new UserViewHolder(view, onWorkmateClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.bind(workmates.get(position));
    }

    @Override
    public int getItemCount() {
        return workmates.size();
    }

    public void updateList(List<User> workmates){
        this.workmates = workmates;
        notifyDataSetChanged();
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
            itemView.setOnClickListener(v -> onWorkmateClickListener.onWorkmateClick(workmates.get(getBindingAdapterPosition()).getUid()));
        }

        void bind(User model) {
            String placeId = model.getDatesAndPlaceIds().get(User.getTodaysDate());
            glide.load(model.getUrlPicture())
                    .apply(RequestOptions.circleCropTransform())
                    .into(binding.workmateProfilePic);

            if (placeId != null) {
                binding.workmatesChoice.setText(context.getResources()
                        .getString(R.string.workmates_eating_at, model.getUsername(), model.getChosenRestaurantName()));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    binding.workmatesChoice.setTextAppearance(R.style.TextStyleNormal);
                }
                else
                    binding.workmatesChoice.setTextColor(context.getResources().getColor(R.color.quantum_black_100));
            } else {
                binding.workmatesChoice.setText(context.getResources()
                        .getString(R.string.workmate_hasnt_chosen, model.getUsername()));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    binding.workmatesChoice.setTextAppearance(R.style.TextStyleItalic);
                } else
                    binding.workmatesChoice.setTextColor(context.getResources().getColor(R.color.quantum_grey700));
            }

        }
    }
}
