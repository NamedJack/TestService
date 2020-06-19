package com.mk.testservice.utils;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {

    private static String TAG = "dateUtilsTag";
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

    private static SimpleDateFormat birdDateFormat = new SimpleDateFormat("yyyyMMdd");

    public static String messageTime() {
        String messageDate = dateFormat.format(new Date());
        return messageDate;
    }

    public static String birdTime() {
        String birdTime = birdDateFormat.format(new Date());
        return birdTime;
    }

    public static boolean fromDate(String time) {
        try {
            return time.equals(dateFormat.parse(time));
        } catch (ParseException e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
            return false;
        }
    }
}
