package com.antlersoft.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by tWX366549 on 2016/8/18.
 */
public class DateUtils {

    public static String getEndTime(int min) {
        return getDate(System.currentTimeMillis() + min * 60 * 1000);
    }

    public static long getTime(String date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        try {
            return format.parse(date).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new Date(1000 * 60 * 60 * 2).getTime();
    }

    public static String getDate(long time) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        return format.format(new Date(time));
    }

    public static String getMillionDate(long time) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.sss");
        return format.format(new Date(time));
    }
}
