package com.sophieopenclass.go4lunch.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateFormatting {

    // Always save the date as Locale.US in Firebase
    public static String getTodayDateInString() {
        Date date = new Date();
        DateFormat formatter = DateFormat.getDateInstance(DateFormat.LONG, Locale.US);
        return formatter.format(date);
    }

    // Formatting the date saved in Firestore in Locale.US to display it in French or in English
    // depending on the user's default language
    public static String formatLocaleDate(String dateString) {
        SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
        Date date = null;
        try {
            date = formatter.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault());
        if (date != null)
            return dateFormat.format(date);
        return "";
    }

    public static String formatDateToString(Date date) {
        DateFormat formatter = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault());
        return formatter.format(date);
    }

    public static String convertDateToHour(Date date) {
        DateFormat format = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return format.format(date);
    }

    // To be able to compare two dates
    public static Date getDateWithoutTime(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }
}
