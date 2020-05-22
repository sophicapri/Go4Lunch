package com.sophieopenclass.go4lunch.utils;

public class CalculateRatings {

    public static int getNumberOfStarsToDisplay(int totalUsers, int numberOfLikes) {
        int numberOfStars;
        if (numberOfLikes == 0)
            numberOfStars = 0;
        else if (numberOfLikes == 1)
            numberOfStars = 1;
        else if (totalUsers / 2 < numberOfLikes && numberOfLikes > 1)
            numberOfStars = 2;
        else
            numberOfStars = 3;
        return numberOfStars;
    }
}
