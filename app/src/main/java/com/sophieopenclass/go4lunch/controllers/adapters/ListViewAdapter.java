package com.sophieopenclass.go4lunch.controllers.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.sophieopenclass.go4lunch.MyViewModel;
import com.sophieopenclass.go4lunch.R;
import com.sophieopenclass.go4lunch.base.BaseActivity;
import com.sophieopenclass.go4lunch.databinding.FragmentListViewBinding;

import com.sophieopenclass.go4lunch.models.json_to_java.OpeningHours;
import com.sophieopenclass.go4lunch.models.json_to_java.PlaceDetails;

import java.util.ArrayList;
import java.util.List;

import static com.sophieopenclass.go4lunch.listeners.Listeners.*;

public class ListViewAdapter extends RecyclerView.Adapter<ListViewAdapter.ListViewHolder> {
    private List<PlaceDetails> placeDetailsList;
    private OnRestaurantClickListener onRestaurantClickListener;
    private ArrayList<Integer> usersEatingAtRestaurant;

    public ListViewAdapter(List<PlaceDetails> placeDetailsList, ArrayList<Integer> usersEatingAtRestaurant, OnRestaurantClickListener onRestaurantClickListener) {
        this.placeDetailsList = placeDetailsList;
        this.onRestaurantClickListener = onRestaurantClickListener;
        this.usersEatingAtRestaurant = usersEatingAtRestaurant;
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
        holder.bind(placeDetailsList.get(position), usersEatingAtRestaurant.get(position));
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

        void bind(PlaceDetails placeDetails, Integer usersEatingAtRestaurant) {
            BaseActivity context = (BaseActivity) itemView.getContext();
            Resources res = context.getResources();

            restaurantName.setText(placeDetails.getName());
            typeOfRestaurant.setText(res.getString(R.string.restaurant_type, placeDetails.getTypes().get(0)));
            restaurantAddress.setText(placeDetails.getVicinity());

            if (placeDetails.getOpeningHours() != null)
                if (placeDetails.getOpeningHours().getOpenNow())
                    displayOpeningHours(placeDetails);
                else
                    openingHours.setText("Fermé");

            String urlPhoto = PlaceDetails.urlPhotoFormatter(placeDetails, 0);
            Glide.with(restaurantPhoto.getContext())
                    .load(urlPhoto)
                    .apply(RequestOptions.centerCropTransform())
                    .into(restaurantPhoto);

            Location restaurantLocation = new Location(placeDetails.getName());
            restaurantLocation.setLatitude(placeDetails.getGeometry().getLocation().getLat());
            restaurantLocation.setLongitude(placeDetails.getGeometry().getLocation().getLng());
            int distance = (int) restaurantLocation.distanceTo(context.currentLocation);
            restaurantDistance.setText(res.getString(R.string.distance, distance));
            nbrOfWorkmates.setText(res.getString(R.string.nbr_of_workmates, usersEatingAtRestaurant));

            // TODO: find out how to calculate the rating
            oneStar.setVisibility(View.VISIBLE);
            twoStars.setVisibility(View.VISIBLE);
            threeStars.setVisibility(View.INVISIBLE);
        }

        private void displayOpeningHours(PlaceDetails placeDetails) {
            int today = OpeningHours.getTodaysDay();
            if (today >= 0 && placeDetails.getOpeningHours().getPeriods()!= null) {
                String time = placeDetails.getOpeningHours().getPeriods().get(today).getClose().getTime();
                String finalTime = time.substring(0,2) + "h" + time.substring(2);
                openingHours.setText("Ouvert jusqu'à " + finalTime);
            } else
                openingHours.setText("Ouvert");

        }
    }


}
