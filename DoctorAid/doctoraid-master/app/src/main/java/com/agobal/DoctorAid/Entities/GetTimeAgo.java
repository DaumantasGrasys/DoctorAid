package com.agobal.DoctorAid.Entities;

import android.annotation.SuppressLint;
import android.app.Application;

@SuppressLint("Registered")
public class GetTimeAgo extends Application{
    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;


    public static String getTimeAgo(long time) {
        if (time < 1000000000000L) {
            time *= 1000;
        }

        long now = System.currentTimeMillis();
        if (time > now || time <= 0) {
            return null;
        }

        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            return "prieš mažiau nei minutę";
        } else if (diff < 2 * MINUTE_MILLIS) {
            return "prieš minutę";
        } else if (diff < 50 * MINUTE_MILLIS) {
            return "prieš " + diff / MINUTE_MILLIS + " minutes";
        } else if (diff < 90 * MINUTE_MILLIS) {
            return "prieš valandą";
        } else if(diff < 2 * HOUR_MILLIS){
              return "prieš "+ diff / HOUR_MILLIS + "valandą";
        } else if (diff < 9 * HOUR_MILLIS && diff >2 * HOUR_MILLIS) {
            return "prieš "+diff / HOUR_MILLIS + " valandas";
        } else if (diff < 21 * HOUR_MILLIS && diff >9 * HOUR_MILLIS) {
            return "prieš "+diff / HOUR_MILLIS + " valandų";
        } else if (diff < 48 * HOUR_MILLIS) {
            return "vakar";
        } else {
            return diff / DAY_MILLIS + " days ago";
        }
    }

}
