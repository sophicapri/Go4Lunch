package com.sophieopenclass.go4lunch.controllers.adapters;

import android.content.res.Resources;
import android.util.Log;
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
import com.sophieopenclass.go4lunch.utils.CalculateRatings;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.sophieopenclass.go4lunch.listeners.Listeners.OnRestaurantClickListener;

public class RestaurantListAdapter extends RecyclerView.Adapter<RestaurantListAdapter.PlaceViewHolder> {
    private static final String TAG = "RESO LIST ADAPTER";
    private List<PlaceDetails> placeDetailsList;
    private OnRestaurantClickListener onRestaurantClickListener;
    private RequestManager glide;

    public RestaurantListAdapter(List<PlaceDetails> placeDetailsList,
                                 OnRestaurantClickListener onRestaurantClickListener, RequestManager glide) {
        this.placeDetailsList = placeDetailsList;
        this.onRestaurantClickListener = onRestaurantClickListener;
        this.glide = glide;
    }

    @NonNull
    @Override
    public PlaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_list_view,
                parent, false);
        return new PlaceViewHolder(view, onRestaurantClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceViewHolder holder, int position) {
        holder.bind(placeDetailsList.get(position));
    }

    @Override
    public int getItemCount() {
        return placeDetailsList.size();
    }

    class PlaceViewHolder extends RecyclerView.ViewHolder {
        FragmentListViewBinding binding;
        OnRestaurantClickListener onRestaurantClickListener;
        BaseActivity context;
        Resources res;

        PlaceViewHolder(@NonNull View itemView, OnRestaurantClickListener onRestaurantClickListener) {
            super(itemView);
            this.onRestaurantClickListener = onRestaurantClickListener;
            binding = FragmentListViewBinding.bind(itemView);
            itemView.setOnClickListener(v -> onRestaurantClickListener
                    .onRestaurantClick(placeDetailsList.get(getBindingAdapterPosition()).getPlaceId()));

            context = (BaseActivity) itemView.getContext();
            res = context.getResources();
        }

        void bind(PlaceDetails placeDetails) {
            binding.oneStar.setVisibility(View.GONE);
            binding.twoStars.setVisibility(View.GONE);
            binding.threeStars.setVisibility(View.GONE);

            binding.restaurantName.setText(placeDetails.getName());
            binding.restaurantAddress.setText(placeDetails.getVicinity());

            if (placeDetails.getOpeningHours() != null) {
                if (placeDetails.getOpeningHours().getOpenNow())
                    displayOpeningHours(placeDetails);
                else {
                    binding.openingHours.setText(R.string.close);
                    binding.openingHours.setTextColor(res.getColor(R.color.quantum_googred));
                }
            } else {
                binding.openingHours.setTextColor(res.getColor(R.color.quantum_grey));
                binding.openingHours.setText(R.string.opening_hours_unavailable);
            }


            String urlPhoto = PlaceDetails.urlPhotoFormatter(placeDetails, 0);
            glide.load(urlPhoto).apply(RequestOptions.centerCropTransform())
                    .into(binding.restaurantPhoto);

            binding.restaurantDistance.setText(res.getString(R.string.distance, placeDetails.getDistance()));
            binding.nbrOfWorkmates.setText(res.getString(R.string.nbr_of_workmates, placeDetails.getNbrOfWorkmates()));

            if (placeDetails.getRating() != null) {
                int numberOfStars = CalculateRatings.getNumberOfStarsToDisplay(placeDetails.getRating());
                if (numberOfStars == 1)
                    binding.oneStar.setVisibility(View.VISIBLE);
                if (numberOfStars == 2)
                    binding.twoStars.setVisibility(View.VISIBLE);
                if (numberOfStars == 3)
                    binding.threeStars.setVisibility(View.VISIBLE);
            }
        }

        private void displayOpeningHours(PlaceDetails placeDetails) {
            int today = OpeningHours.getTodaysDay();

            if (today >= 0 && placeDetails.getOpeningHours().getPeriods() != null) {
                if (placeDetails.getOpeningHours().getPeriods().size() == Calendar.DAY_OF_WEEK) {
                    String time = placeDetails.getOpeningHours().getPeriods().get(today).getClose().getTime();
                    String finalTime = time.substring(0, 2) + "h" + time.substring(2);
                    binding.openingHours.setText(res.getString(R.string.open_until, finalTime));
                    binding.openingHours.setTextColor(res.getColor(R.color.quantum_googgreen));
                }
            } else {
                binding.openingHours.setText(R.string.open);
                binding.openingHours.setTextColor(res.getColor(R.color.quantum_googgreen));
            }
        }
    }
}
