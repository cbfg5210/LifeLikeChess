package com.ue.library.util;

import java.text.SimpleDateFormat;

/**
 * Created by hawk on 2016/11/21.
 */

public class DateUtils {
    public static final String FORMAT_SHORT_DATETIME = "MM-dd HH:mm";
    public static final String FORMAT_TIME = "HH:mm:ss";
    private static SimpleDateFormat simpleDateFormat;

    private static SimpleDateFormat getSimpleDateFormat(String format) {
        if (simpleDateFormat == null) {
            simpleDateFormat = new SimpleDateFormat();
        }
        simpleDateFormat.applyPattern(format);
        return simpleDateFormat;
    }

    public static String getFormatTime(long timeMills, String format) {
        return getSimpleDateFormat(format).format(timeMills);
    }
}
