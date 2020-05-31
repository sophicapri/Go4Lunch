package com.sophieopenclass.go4lunch.utils;

public class CalculateRatings {

    public static int getNumberOfStarsToDisplay(double rating) {
        int numberOfStars;
        if (rating < 3)
            numberOfStars = 1;
        else if (rating >= 3 && rating < 4 )
            numberOfStars = 2;
        else
            numberOfStars = 3;
        return numberOfStars;
    }
}
