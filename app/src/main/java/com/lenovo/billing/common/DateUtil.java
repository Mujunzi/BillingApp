package com.lenovo.billing.common;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtil {

    private static SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss:SSS", Locale.CHINA);

    public static String getCurrentDate(){

        Date curDate = new Date(System.currentTimeMillis());
        return formatter.format(curDate);
    }
}
