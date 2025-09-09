package com.example.studymate.Dialog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtils {

    // Convert String dateTime to long timestamp
    public static long getTimestampFromDateTime(String dateTime) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        try {
            Date date = format.parse(dateTime);
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return 0; // Return 0 or handle error
        }
    }

    // Format long timestamp to a human-readable date string
    public static String formatDateTime(String dateTimeStr) {
        long timestamp = getTimestampFromDateTime(dateTimeStr);
        SimpleDateFormat format = new SimpleDateFormat("MMM dd, yyyy - HH:mm", Locale.getDefault());
        return format.format(new Date(timestamp));
    }
}
