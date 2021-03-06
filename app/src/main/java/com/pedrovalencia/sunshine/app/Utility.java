package com.pedrovalencia.sunshine.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.pedrovalencia.sunshine.app.data.WeatherContract;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by pedrovalencia on 23/09/2014.
 */
public class Utility {

    // Format used for storing dates in the database.  ALso used for converting those strings
    // back into date objects for comparison/processing.
    public static final String DATE_FORMAT = "yyyyMMdd";


    public static String getPreferredLocation(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        return prefs.getString("location", "94043");
    }

    public static String getPreferredUnit(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        return prefs.getString("unit", "metric");
    }

    public static String formatTemperature(Context context, double tempIn, boolean isMetric) {
        double tempOut;
        if(!isMetric) {
            tempOut = 9*tempIn/5+32;
        } else {
            tempOut = tempIn;
        }
        return context.getString(R.string.format_temperature, tempOut);

    }

    public static boolean isMetric(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString("unit", "metric").equals("metric");
    }

    static String formatDate(String dateString) {
        Date date = WeatherContract.getDateFromDb(dateString);
        return DateFormat.getDateInstance().format(date);
    }
    /**
     * Helper method to convert the database representation of the date into something to display
     * to users.  As classy and polished a user experience as "20140102" is, we can do better.
     *
     * @param context Context to use for resource localization
     * @param dateStr The db formatted date string, expected to be of the form specified
     *                in Utility.DATE_FORMAT
     * @return a user-friendly representation of the date.
     */
    public static String getFriendlyDayString(Context context, String dateStr) {
        // The day string for forecast uses the following logic:
        // For today: "Today, June 8"
        // For tomorrow:  "Tomorrow"
        // For the next 5 days: "Wednesday" (just the day name)
        // For all days after that: "Mon Jun 8"

        Date todayDate = new Date();
        String todayStr = WeatherContract.getDbDateString(todayDate);
        Date inputDate = WeatherContract.getDateFromDb(dateStr);

        // If the date we're building the String for is today's date, the format
        // is "Today, June 24"
        if (todayStr.equals(dateStr)) {
            String today = context.getString(R.string.today);
            return context.getString(
                    R.string.format_full_friendly_date,
                    today,
                    getFormattedMonthDay(context, dateStr));
        } else {
            Calendar cal = Calendar.getInstance();
            cal.setTime(todayDate);
            cal.add(Calendar.DATE, 7);
            String weekFutureString = WeatherContract.getDbDateString(cal.getTime());

            if (dateStr.compareTo(weekFutureString) < 0) {
                // If the input date is less than a week in the future, just return the day name.
                return getDayName(context, dateStr);
            } else {
                // Otherwise, use the form "Mon Jun 3"
                SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
                return shortenedDateFormat.format(inputDate);
            }
        }
    }

    /**
     * Given a day, returns just the name to use for that day.
     * E.g "today", "tomorrow", "wednesday".
     *
     * @param context Context to use for resource localization
     * @param dateStr The db formatted date string, expected to be of the form specified
     *                in Utility.DATE_FORMAT
     * @return
     */
    public static String getDayName(Context context, String dateStr) {
        SimpleDateFormat dbDateFormat = new SimpleDateFormat(Utility.DATE_FORMAT);
        try {
            Date inputDate = dbDateFormat.parse(dateStr);
            Date todayDate = new Date();
            // If the date is today, return the localized version of "Today" instead of the actual
            // day name.
            if (WeatherContract.getDbDateString(todayDate).equals(dateStr)) {
                return context.getString(R.string.today);
            } else {
                // If the date is set for tomorrow, the format is "Tomorrow".
                Calendar cal = Calendar.getInstance();
                cal.setTime(todayDate);
                cal.add(Calendar.DATE, 1);
                Date tomorrowDate = cal.getTime();
                if (WeatherContract.getDbDateString(tomorrowDate).equals(
                        dateStr)) {
                    return context.getString(R.string.tomorrow);
                } else {
                    // Otherwise, the format is just the day of the week (e.g "Wednesday".
                    SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE");
                    return dayFormat.format(inputDate);
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
            // It couldn't process the date correctly.
            return "";
        }
    }

    /**
     * Converts db date format to the format "Month day", e.g "June 24".
     * @param context Context to use for resource localization
     * @param dateStr The db formatted date string, expected to be of the form specified
     *                in Utility.DATE_FORMAT
     * @return The day in the form of a string formatted "December 6"
     */
    public static String getFormattedMonthDay(Context context, String dateStr) {
        SimpleDateFormat dbDateFormat = new SimpleDateFormat(Utility.DATE_FORMAT);
        try {
            Date inputDate = dbDateFormat.parse(dateStr);
            SimpleDateFormat monthDayFormat = new SimpleDateFormat("MMMM dd");
            String monthDayString = monthDayFormat.format(inputDate);
            return monthDayString;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String formatHumidity(Context context, double humIn) {
        return context.getString(R.string.format_humidity, humIn);
    }

    public static String getFormattedWind(Context context, float windSpeed, float degrees) {
        int windFormat;
        if (Utility.isMetric(context)) {
            windFormat = R.string.format_wind_kmh;
        } else {
            windFormat = R.string.format_wind_mph;
            windSpeed = .621371192237334f * windSpeed;
        }

        // From wind direction in degrees, determine compass direction as a string (e.g NW)
        // You know what's fun, writing really long if/else statements with tons of possible
        // conditions.  Seriously, try it!
        String direction = "Unknown";
        if (degrees >= 337.5 || degrees < 22.5) {
            direction = "N";
        } else if (degrees >= 22.5 && degrees < 67.5) {
            direction = "NE";
        } else if (degrees >= 67.5 && degrees < 112.5) {
            direction = "E";
        } else if (degrees >= 112.5 && degrees < 157.5) {
            direction = "SE";
        } else if (degrees >= 157.5 && degrees < 202.5) {
            direction = "S";
        } else if (degrees >= 202.5 && degrees < 247.5) {
            direction = "SW";
        } else if (degrees >= 247.5 && degrees < 292.5) {
            direction = "W";
        } else if (degrees >= 292.5 || degrees < 22.5) {
            direction = "NW";
        }
        return String.format(context.getString(windFormat), windSpeed, direction);
    }

    public static String formatPressure(Context context, double pressureIn) {
        return context.getString(R.string.format_pressure, pressureIn);
    }

    public static int getImageDetail(String descriptionWeather) {
        //TODO Use jdk 7 or greater for switch with strings
        int icon;
        if("Clear".toLowerCase().equals(descriptionWeather.toLowerCase())) {
            icon = R.drawable.art_clear;
        } else if("Clouds".toLowerCase().equals(descriptionWeather.toLowerCase())) {
            icon = R.drawable.art_clouds;
        } else if("Fog".toLowerCase().equals(descriptionWeather.toLowerCase())) {
            icon = R.drawable.art_fog;
        } else if("light clouds".toLowerCase().equals(descriptionWeather.toLowerCase())) {
            icon = R.drawable.art_light_clouds;
        } else if("light rain".toLowerCase().equals(descriptionWeather.toLowerCase())) {
            icon = R.drawable.art_light_rain;
        } else if("Rain".toLowerCase().equals(descriptionWeather.toLowerCase())) {
            icon = R.drawable.art_rain;
        } else if("Snow".toLowerCase().equals(descriptionWeather.toLowerCase())) {
            icon = R.drawable.art_snow;
        } else if("Storm".toLowerCase().equals(descriptionWeather.toLowerCase())) {
            icon = R.drawable.art_storm;
        } else {
            icon = R.mipmap.ic_launcher;
        }
        return icon;
    }

    public static int getImageList(String descriptionWeather) {
        //TODO Use jdk 7 or greater for switch with strings
        int icon;
        if("Clear".toLowerCase().equals(descriptionWeather.toLowerCase())) {
            icon = R.drawable.ic_clear;
        } else if("Clouds".toLowerCase().equals(descriptionWeather.toLowerCase())) {
            icon = R.drawable.ic_cloudy;
        } else if("Fog".toLowerCase().equals(descriptionWeather.toLowerCase())) {
            icon = R.drawable.ic_fog;
        } else if("light clouds".toLowerCase().equals(descriptionWeather.toLowerCase())) {
            icon = R.drawable.ic_light_clouds;
        } else if("light rain".toLowerCase().equals(descriptionWeather.toLowerCase())) {
            icon = R.drawable.ic_light_rain;
        } else if("Rain".toLowerCase().equals(descriptionWeather.toLowerCase())) {
            icon = R.drawable.ic_rain;
        } else if("Snow".toLowerCase().equals(descriptionWeather.toLowerCase())) {
            icon = R.drawable.ic_snow;
        } else if("Storm".toLowerCase().equals(descriptionWeather.toLowerCase())) {
            icon = R.drawable.ic_storm;
        } else {
            icon = R.mipmap.ic_launcher;
        }
        return icon;
    }

}
