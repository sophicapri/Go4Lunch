package com.sophieopenclass.go4lunch.controllers.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.sophieopenclass.go4lunch.BuildConfig;
import com.sophieopenclass.go4lunch.R;
import com.sophieopenclass.go4lunch.databinding.FragmentListViewBinding;
import com.sophieopenclass.go4lunch.models.json_to_java.PlaceDetails;

import java.util.List;

import static com.sophieopenclass.go4lunch.api.PlaceService.API_URL;
import static com.sophieopenclass.go4lunch.api.PlaceService.PHOTO_URL;

public class ListViewAdapter extends RecyclerView.Adapter<ListViewAdapter.ListViewHolder> {
    private List<PlaceDetails> placeDetailsList;
    private OnRestaurantClickListener onRestaurantClickListener;

    public ListViewAdapter(List<PlaceDetails> placeDetailsList, OnRestaurantClickListener onRestaurantClickListener) {
        this.placeDetailsList = placeDetailsList;
        this.onRestaurantClickListener = onRestaurantClickListener;
    }

    @NonNull
    @Override
    public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_list_view,
                parent, false);
        return new ListViewHolder(view, onRestaurantClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ListViewHolder holder, int position) {
        holder.bind(placeDetailsList.get(position));
    }

    @Override
    public int getItemCount() {
        return placeDetailsList.size();
    }

    class ListViewHolder extends RecyclerView.ViewHolder {
        TextView restaurantName;
        TextView typeOfRestaurant;
        TextView restaurantAddress;
        TextView openingHours;
        ImageView restaurantPhoto;
        TextView restaurantDistance;
        TextView nbrOfWorkmates;
        ImageView oneStar;
        ImageView twoStars;
        ImageView threeStars;
        OnRestaurantClickListener onRestaurantClickListener;

        ListViewHolder(@NonNull View itemView, OnRestaurantClickListener onRestaurantClickListener) {
            super(itemView);
            this.onRestaurantClickListener = onRestaurantClickListener;
            FragmentListViewBinding binding = FragmentListViewBinding.bind(itemView);
            restaurantName = binding.restaurantName;
            typeOfRestaurant = binding.typeOfRestaurant;
            restaurantAddress = binding.restaurantAddress;
            openingHours = binding.openingHours;
            restaurantPhoto = binding.restaurantPhoto;
            restaurantDistance = binding.restaurantDistance;
            nbrOfWorkmates = binding.nbrOfWorkmates;
            oneStar = binding.oneStar;
            twoStars = binding.twoStars;
            threeStars = binding.threeStars;
            itemView.setOnClickListener(v -> onRestaurantClickListener
                    .onRestaurantClick(placeDetailsList.get(getBindingAdapterPosition()).getPlaceId()));
        }

        void bind(PlaceDetails placeDetails) {
            String photoReference = placeDetails.getPhotos().get(0).getPhotoReference();
            String urlPhoto = API_URL + PHOTO_URL + photoReference + "&key=" + BuildConfig.API_KEY;

            restaurantName.setText(placeDetails.getName());
            typeOfRestaurant.setText(placeDetails.getTypes().get(0) + " - ");
            restaurantAddress.setText(placeDetails.getVicinity());

            // TODO: find out how to display correctly
            if (placeDetails.getOpeningHours() != null && placeDetails.getOpeningHours().getOpenNow())
            openingHours.setText("Ouvert");
            else
                openingHours.setText("Fermé");

            Glide.with(restaurantPhoto.getContext())
                    .load(urlPhoto)
                    .apply(RequestOptions.centerCropTransform())
                    .into(restaurantPhoto);

            // TODO: find out how to calculate the distance
            restaurantDistance.setText("distance");

            // TODO: figure out how to know how many workmates chose a restaurant
            nbrOfWorkmates.setText("7");

            oneStar.setVisibility(View.VISIBLE);
            twoStars.setVisibility(View.VISIBLE);
            threeStars.setVisibility(View.INVISIBLE);
        }
    }

    public interface OnRestaurantClickListener {
        void onRestaurantClick(String placeId);
    }
}
