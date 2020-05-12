
package com.sophieopenclass.go4lunch.models.json_to_java;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.sophieopenclass.go4lunch.utils.Constants;

import java.util.Calendar;
import java.util.List;

import static com.sophieopenclass.go4lunch.utils.Constants.MONDAY;

public class OpeningHours {

    @SerializedName("open_now")
    @Expose
    private Boolean openNow;
    @SerializedName("periods")
    @Expose
    private List<Period> periods = null;
    @SerializedName("weekday_text")
    @Expose
    private List<String> weekdayText = null;

    public Boolean getOpenNow() {
        return openNow;
    }

    public void setOpenNow(Boolean openNow) {
        this.openNow = openNow;
    }

    public List<Period> getPeriods() {
        return periods;
    }

    public void setPeriods(List<Period> periods) {
        this.periods = periods;
    }

    public List<String> getWeekdayText() {
        return weekdayText;
    }

    public void setWeekdayText(List<String> weekdayText) {
        this.weekdayText = weekdayText;
    }

    public static int getTodaysDay() {
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        int today = -1;

        if (day == Calendar.MONDAY)
            today = Constants.MONDAY;
        else if (day == Calendar.TUESDAY)
            today = Constants.TUESDAY;
        else if (day == Calendar.WEDNESDAY)
            today = Constants.WEDNESDAY;
        else if (day == Calendar.THURSDAY)
            today = Constants.THURSDAY;
        else if (day == Calendar.FRIDAY)
            today = Constants.FRIDAY;
        else if (day == Calendar.SATURDAY)
            today = Constants.SATURDAY;
        else if (day == Calendar.SUNDAY)
            today = Constants.SUNDAY;

        return today;
    }

}
