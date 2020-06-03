package com.sophieopenclass.go4lunch.controllers.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.sophieopenclass.go4lunch.R;
import com.sophieopenclass.go4lunch.databinding.FragmentWorkmatesListBinding;
import com.sophieopenclass.go4lunch.models.User;

import static com.sophieopenclass.go4lunch.listeners.Listeners.OnWorkmateClickListener;

public class RestaurantWorkmatesListAdapter extends FirestoreRecyclerAdapter<User, RestaurantWorkmatesListAdapter.UserViewHolder> {
    public static final String TAG = "com.sophie.Adapter";
    private FirebaseUser currentUser;
    private OnWorkmateClickListener onWorkmateClickListener;
    private RequestManager glide;

    public RestaurantWorkmatesListAdapter(@NonNull FirestoreRecyclerOptions<User> options,
                                          OnWorkmateClickListener onWorkmateClickListener, RequestManager glide) {
        super(options);
        this.onWorkmateClickListener = onWorkmateClickListener;
        this.currentUser = FirebaseAuth.getInstance().getCurrentUser();
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
    protected void onBindViewHolder(@NonNull UserViewHolder holder, int position, @NonNull User model) {
        holder.bind(model);
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
            glide.load(model.getUrlPicture())
                    .apply(RequestOptions.circleCropTransform())
                    .into(binding.workmateProfilePic);

            if (model.getUid().equals(currentUser.getUid()))
                binding.workmatesChoice.setText(context.getResources().getString(R.string.you_are_eating_here));
            else
                binding.workmatesChoice.setText(context.getResources().getString(R.string.workmate_is_eating_here, model.getUsername()));
        }
    }
}