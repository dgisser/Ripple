package com.facebook.peepingtom.UI;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Given a date String of the format given by the Twitter API, returns a display-formatted
 * String representing the relative time difference, e.g. "2m", "6d", "23 May", "1 Jan 14"
 * depending on how great the time difference between now and the given date is.
 * This, as of 2016-06-29, matches the behavior of the official Twitter app.
 */
public class TimeFormatter {
    public static String getTimeDifference(Long timesecs) {
        long diff = (System.currentTimeMillis() - timesecs) / 1000;
        String time = "";
        if (diff < 5)
            time = "Just now";
        else if (diff < 60)
            time = String.format(Locale.ENGLISH, "%ds",diff);
        else if (diff < 60 * 60)
            time = String.format(Locale.ENGLISH, "%dm", diff / 60);
        else if (diff < 60 * 60 * 24)
            time = String.format(Locale.ENGLISH, "%dh", diff / (60 * 60));
        else if (diff < 60 * 60 * 24 * 30)
            time = String.format(Locale.ENGLISH, "%dd", diff / (60 * 60 * 24));
        else {
            Calendar now = Calendar.getInstance();
            Calendar then = Calendar.getInstance();
            then.setTime(new Date(timesecs));
            if (now.get(Calendar.YEAR) == then.get(Calendar.YEAR)) {
                time = String.valueOf(then.get(Calendar.DAY_OF_MONTH)) + " "
                        + then.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.US);
            } else {
                time = String.valueOf(then.get(Calendar.DAY_OF_MONTH)) + " "
                        + then.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.US)
                        + " " + String.valueOf(then.get(Calendar.YEAR) - 2000);
            }
        }
        return time;
    }

    /**
     * Given a date String of the format given by the Twitter API, returns a display-formatted
     * String of the absolute date of the form "30 Jun 16".
     * This, as of 2016-06-30, matches the behavior of the official Twitter app.
     */
    public static String getTimeStamp(String rawJsonDate) {
        String time = "";
        String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat format = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
        format.setLenient(true);
        try {
            Calendar then = Calendar.getInstance();
            then.setTime(format.parse(rawJsonDate));
            Date date = then.getTime();

            SimpleDateFormat format1 = new SimpleDateFormat("h:mm a \u00b7 dd MMM yy");

            time = format1.format(date);

        }  catch (ParseException e) {
            e.printStackTrace();
        }
        return time;
    }

    public static float getAge(final Date current, final Date birthdate) {

        if (birthdate == null) return 0;
        if (current == null) return 0;
        else {
            final Calendar calend = new GregorianCalendar();
            calend.set(Calendar.HOUR_OF_DAY, 0);
            calend.set(Calendar.MINUTE, 0);
            calend.set(Calendar.SECOND, 0);
            calend.set(Calendar.MILLISECOND, 0);

            calend.setTimeInMillis(current.getTime() - birthdate.getTime());

            float result = 0;
            result = calend.get(Calendar.YEAR) - 1970;
            result += (float) calend.get(Calendar.MONTH) / (float) 12;
            return result;
        }

    }

}