package com.sophieopenclass.go4lunch.controllers.adapters;

import android.content.res.Resources;
import android.os.Build;
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
import com.sophieopenclass.go4lunch.models.json_to_java.Close;
import com.sophieopenclass.go4lunch.models.json_to_java.OpeningHours;
import com.sophieopenclass.go4lunch.models.json_to_java.Period;
import com.sophieopenclass.go4lunch.models.json_to_java.PlaceDetails;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.sophieopenclass.go4lunch.listeners.Listeners.OnRestaurantClickListener;
import static com.sophieopenclass.go4lunch.utils.Constants.ONE_HOUR;
import static com.sophieopenclass.go4lunch.utils.Constants.OPEN_24H;

public class RestaurantListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<PlaceDetails> placeDetailsList;
    private OnRestaurantClickListener onRestaurantClickListener;
    private RequestManager glide;
    private final int VIEW_TYPE_ITEM = 0;

    public RestaurantListAdapter(List<PlaceDetails> placeDetailsList,
                                 OnRestaurantClickListener onRestaurantClickListener, RequestManager glide) {
        this.placeDetailsList = placeDetailsList;
        this.onRestaurantClickListener = onRestaurantClickListener;
        this.glide = glide;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_list_view,
                    parent, false);
            return new PlaceViewHolder(view, onRestaurantClickListener);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_place_loading, parent, false);
            return new LoadingViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof PlaceViewHolder)
            ((PlaceViewHolder) holder).bind(placeDetailsList.get(position));
    }

    @Override
    public int getItemCount() {
        return placeDetailsList == null ? 0 : placeDetailsList.size();
    }

    @Override
    public int getItemViewType(int position) {
        int VIEW_TYPE_LOADING = 1;
        return placeDetailsList.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    public void clearList(){
        placeDetailsList.clear();
        notifyDataSetChanged();
    }

    public void updateList(ArrayList<PlaceDetails> placeDetailsList) {
        this.placeDetailsList = placeDetailsList;
        notifyDataSetChanged();
    }

    // Progress bar
    static class LoadingViewHolder extends RecyclerView.ViewHolder {
        LoadingViewHolder(View itemView) {
            super(itemView);
        }
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
                    displayClosed(placeDetails);
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
                int numberOfStars = PlaceDetails.getNumberOfStarsToDisplay(placeDetails.getRating());
                if (numberOfStars == 1)
                    binding.oneStar.setVisibility(View.VISIBLE);
                if (numberOfStars == 2)
                    binding.twoStars.setVisibility(View.VISIBLE);
                if (numberOfStars == 3)
                    binding.threeStars.setVisibility(View.VISIBLE);
            }
        }

        private void displayOpeningHours(PlaceDetails placeDetails) {
            Period todayOpeningHours = getTodayOpeningHours(placeDetails);
            if (todayOpeningHours != null) {
                if (todayOpeningHours.getClose() != null) {
                    if (!restaurantClosingSoon(todayOpeningHours.getClose())) {
                        String time = todayOpeningHours.getClose().getTime();
                        String finalTime = time.substring(0, 2) + "h" + time.substring(2);
                        binding.openingHours.setText(res.getString(R.string.open_until, finalTime));
                    }
                } else if (todayOpeningHours.getOpen().getDay() == 0 && todayOpeningHours.getOpen().getTime().equals(OPEN_24H))
                    binding.openingHours.setText(R.string.open_24h);
            } else {
                binding.openingHours.setText(R.string.open);
            }
            binding.openingHours.setTextColor(res.getColor(R.color.quantum_googgreen));
        }

        // To check if restaurant is currently closed but will open later or not
        private void displayClosed(PlaceDetails placeDetails) {
            Period todayOpeningHours = getTodayOpeningHours(placeDetails);
            if (todayOpeningHours != null) {
                String openingHour = todayOpeningHours.getOpen().getTime();
                if (Integer.parseInt(openingHour) > Integer.parseInt(convertDateToStringHour(new Date()))) {
                    String finalTime = openingHour.substring(0, 2) + "h" + openingHour.substring(2);
                    binding.openingHours.setText(res.getString(R.string.close_will_open_at, finalTime));
                } else
                    binding.openingHours.setText(R.string.close);
            } else
                binding.openingHours.setText(R.string.close);
            binding.openingHours.setTextColor(res.getColor(R.color.quantum_googred));
        }

        private Period getTodayOpeningHours(PlaceDetails placeDetails) {
            Period openingHours = null;
            int today = OpeningHours.getTodaysDay();
            if (today >= 0 && placeDetails.getOpeningHours().getPeriods() != null) {
                if (placeDetails.getOpeningHours().getPeriods().size() == Calendar.DAY_OF_WEEK) {
                    openingHours = placeDetails.getOpeningHours().getPeriods().get(today);
                }
            }
            return openingHours;
        }

        private boolean restaurantClosingSoon(Close close) {
            int timeLeftBeforeClosing = Integer.parseInt(close.getTime()) - Integer.parseInt(convertDateToStringHour(new Date()));
            if (timeLeftBeforeClosing > 0 && timeLeftBeforeClosing < ONE_HOUR) {
                binding.openingHours.setText(R.string.closing_soon);
                binding.openingHours.setTextColor(res.getColor(R.color.quantum_googred));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    binding.openingHours.setTextAppearance(R.style.TextStyleRedBold);
                }
                return true;
            } else
                return false;
        }

        String convertDateToStringHour(Date date) {
            DateFormat format = new SimpleDateFormat("HHmm", Locale.getDefault());
            return format.format(date);
        }
    }
}
