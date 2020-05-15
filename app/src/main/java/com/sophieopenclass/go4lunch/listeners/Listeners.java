package com.sophieopenclass.go4lunch.listeners;

public class Listeners {

    public interface OnRestaurantClickListener {
        void onRestaurantClick(String placeId);
    }

    public interface OnWorkmateClickListener {
        void onWorkmateClick(String uid);
    }
}
