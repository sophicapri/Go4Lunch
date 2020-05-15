package com.sophieopenclass.go4lunch.controllers.adapters;

import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.sophieopenclass.go4lunch.R;
import com.sophieopenclass.go4lunch.base.BaseActivity;
import com.sophieopenclass.go4lunch.databinding.WorkmatesRestaurantPreviewBinding;
import com.sophieopenclass.go4lunch.listeners.Listeners;
import com.sophieopenclass.go4lunch.models.json_to_java.PlaceDetails;

import java.util.ArrayList;

public class PreviousRestaurantsAdapter extends RecyclerView.Adapter<PreviousRestaurantsAdapter.WorkmatesDetailHolder> {
    private ArrayList<PlaceDetails> placeDetailsList;
    private Listeners.OnRestaurantClickListener onRestaurantClickListener;


    public PreviousRestaurantsAdapter(ArrayList<PlaceDetails> placeDetailsList, Listeners.OnRestaurantClickListener onRestaurantClickListener) {
        this.placeDetailsList = placeDetailsList;
        this.onRestaurantClickListener = onRestaurantClickListener;
    }

    @NonNull
    @Override
    public WorkmatesDetailHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.workmates_restaurant_preview,
                parent, false);
        return new WorkmatesDetailHolder(view, onRestaurantClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkmatesDetailHolder holder, int position) {
        holder.bind(placeDetailsList.get(position));
    }

    @Override
    public int getItemCount() {
        return placeDetailsList.size();
    }

    class WorkmatesDetailHolder extends RecyclerView.ViewHolder {
        Listeners.OnRestaurantClickListener onRestaurantClickListener;
        WorkmatesRestaurantPreviewBinding binding;

        WorkmatesDetailHolder(@NonNull View itemView, Listeners.OnRestaurantClickListener onRestaurantClickListener) {
            super(itemView);
            binding = WorkmatesRestaurantPreviewBinding.bind(itemView);
            this.onRestaurantClickListener = onRestaurantClickListener;
            itemView.setOnClickListener(v -> onRestaurantClickListener
                    .onRestaurantClick(placeDetailsList.get(getBindingAdapterPosition()).getPlaceId()));
        }

        void bind(PlaceDetails placeDetails) {
            BaseActivity context = (BaseActivity) itemView.getContext();
            Resources res = context.getResources();

            binding.detailsRestaurantName.setText(placeDetails.getName());
            binding.detailsTypeOfRestaurant.setText(res.getString(R.string.restaurant_type, placeDetails.getTypes().get(0)));
            binding.detailsRestaurantAddress.setText(placeDetails.getVicinity());
            String urlPhoto = PlaceDetails.urlPhotoFormatter(placeDetails, 0);
            Glide.with(binding.restaurantPhoto)
                    .load(urlPhoto)
                    .apply(RequestOptions.centerCropTransform())
                    .into(binding.restaurantPhoto);
        }
    }
}
