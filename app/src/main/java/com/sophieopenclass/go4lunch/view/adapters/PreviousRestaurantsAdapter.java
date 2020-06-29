package com.sophieopenclass.go4lunch.view.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.sophieopenclass.go4lunch.R;
import com.sophieopenclass.go4lunch.databinding.WorkmatesRestaurantPreviewBinding;
import com.sophieopenclass.go4lunch.listeners.Listeners;
import com.sophieopenclass.go4lunch.models.Restaurant;

import java.util.List;

import static com.sophieopenclass.go4lunch.utils.DateFormatting.formatLocaleDate;

public class PreviousRestaurantsAdapter extends RecyclerView.Adapter<PreviousRestaurantsAdapter.WorkmatesDetailHolder> {
    private List<Restaurant> restaurantList;
    private Listeners.OnRestaurantClickListener onRestaurantClickListener;
    private RequestManager glide;
    private boolean isFavorite;


    public PreviousRestaurantsAdapter(List<Restaurant> restaurantList, Listeners.OnRestaurantClickListener onRestaurantClickListener, RequestManager glide) {
        this.restaurantList = restaurantList;
        this.onRestaurantClickListener = onRestaurantClickListener;
        this.glide = glide;
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
        holder.bind(restaurantList.get(position));
    }

    @Override
    public int getItemCount() {
        return restaurantList.size();
    }

    public void updateList(List<Restaurant> restaurantList, boolean isFavorite) {
        this.isFavorite = isFavorite;
        this.restaurantList = restaurantList;
        notifyDataSetChanged();
    }

    class WorkmatesDetailHolder extends RecyclerView.ViewHolder {
        Listeners.OnRestaurantClickListener onRestaurantClickListener;
        WorkmatesRestaurantPreviewBinding binding;

        WorkmatesDetailHolder(@NonNull View itemView, Listeners.OnRestaurantClickListener onRestaurantClickListener) {
            super(itemView);
            binding = WorkmatesRestaurantPreviewBinding.bind(itemView);
            this.onRestaurantClickListener = onRestaurantClickListener;
            itemView.setOnClickListener(v -> onRestaurantClickListener
                    .onRestaurantClick(restaurantList.get(getBindingAdapterPosition()).getPlaceId()));
        }

        void bind(Restaurant restaurant) {
            binding.dateOfPreviousLunch.setVisibility(View.VISIBLE);
            if (!isFavorite)
                binding.dateOfPreviousLunch.setText(formatLocaleDate(restaurant.getDateOfLunch()));
            else
                binding.dateOfPreviousLunch.setVisibility(View.GONE);
            binding.detailsRestaurantName.setText(restaurant.getName());
            binding.detailsRestaurantAddress.setText(restaurant.getAddress());
            glide.load(restaurant.getUrlPhoto()).apply(RequestOptions.circleCropTransform())
                    .into(binding.restaurantPhoto);

            if (restaurant.getNumberOfStars() == 1)
                binding.detailOneStar.setVisibility(View.VISIBLE);
            if (restaurant.getNumberOfStars() == 2)
                binding.detailTwoStars.setVisibility(View.VISIBLE);
            if (restaurant.getNumberOfStars() == 3)
                binding.detailThreeStars.setVisibility(View.VISIBLE);

        }
    }
}
