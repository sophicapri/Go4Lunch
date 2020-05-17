package com.sophieopenclass.go4lunch.controllers.adapters;

import android.content.res.Resources;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.sophieopenclass.go4lunch.R;
import com.sophieopenclass.go4lunch.base.BaseActivity;
import com.sophieopenclass.go4lunch.databinding.FragmentListViewBinding;
import com.sophieopenclass.go4lunch.models.json_to_java.OpeningHours;
import com.sophieopenclass.go4lunch.models.json_to_java.PlaceDetails;

import java.util.Calendar;
import java.util.List;

import static com.sophieopenclass.go4lunch.listeners.Listeners.OnRestaurantClickListener;

public class ListViewAdapter extends RecyclerView.Adapter<ListViewAdapter.ListViewHolder> {
    private List<PlaceDetails> placeDetailsList;
    private OnRestaurantClickListener onRestaurantClickListener;
    private RequestManager glide;

    public ListViewAdapter(List<PlaceDetails> placeDetailsList, OnRestaurantClickListener onRestaurantClickListener, RequestManager glide) {
        this.placeDetailsList = placeDetailsList;
        this.onRestaurantClickListener = onRestaurantClickListener;
        this.glide = glide;
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
        FragmentListViewBinding binding;
        OnRestaurantClickListener onRestaurantClickListener;
        BaseActivity context;
        Resources res;

        ListViewHolder(@NonNull View itemView, OnRestaurantClickListener onRestaurantClickListener) {
            super(itemView);
            this.onRestaurantClickListener = onRestaurantClickListener;
            binding = FragmentListViewBinding.bind(itemView);
            itemView.setOnClickListener(v -> onRestaurantClickListener
                    .onRestaurantClick(placeDetailsList.get(getBindingAdapterPosition()).getPlaceId()));

            context = (BaseActivity) itemView.getContext();
            res = context.getResources();
        }

        void bind(PlaceDetails placeDetails) {
            binding.restaurantName.setText(placeDetails.getName());
            binding.typeOfRestaurant.setText(res.getString(R.string.restaurant_type, placeDetails.getTypes().get(0)));
            binding.restaurantAddress.setText(placeDetails.getVicinity());

            if (placeDetails.getOpeningHours() != null)
                if (placeDetails.getOpeningHours().getOpenNow())
                    displayOpeningHours(placeDetails);
                else
                    binding.openingHours.setText(R.string.close);

            String urlPhoto = PlaceDetails.urlPhotoFormatter(placeDetails, 0);
            glide.load(urlPhoto).apply(RequestOptions.centerCropTransform())
                    .into(binding.restaurantPhoto);

            Location restaurantLocation = new Location(placeDetails.getName());
            restaurantLocation.setLatitude(placeDetails.getGeometry().getLocation().getLat());
            restaurantLocation.setLongitude(placeDetails.getGeometry().getLocation().getLng());
            int distance = (int) restaurantLocation.distanceTo(context.currentLocation);
            binding.restaurantDistance.setText(res.getString(R.string.distance, distance));
            binding.nbrOfWorkmates.setText(res.getString(R.string.nbr_of_workmates, placeDetails.getNbrOfWorkmates()));

            // TODO: find out how to calculate the rating
            binding.oneStar.setVisibility(View.VISIBLE);
            binding.twoStars.setVisibility(View.VISIBLE);
            binding.threeStars.setVisibility(View.INVISIBLE);
        }

        private void displayOpeningHours(PlaceDetails placeDetails) {
            int today = OpeningHours.getTodaysDay();
            if (today >= 0 && placeDetails.getOpeningHours().getPeriods()!= null) {
                if (placeDetails.getOpeningHours().getPeriods().size() == Calendar.DAY_OF_WEEK) {
                    String time = placeDetails.getOpeningHours().getPeriods().get(today).getClose().getTime();
                    String finalTime = time.substring(0, 2) + "h" + time.substring(2);
                    binding.openingHours.setText(res.getString(R.string.open_until, finalTime));
                }
            } else
                binding.openingHours.setText(R.string.open);

        }
    }


}
