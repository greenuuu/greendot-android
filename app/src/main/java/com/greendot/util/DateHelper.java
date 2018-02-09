package com.greendot.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateHelper {

    public static Date StrToDate(String str) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        try {
            date = format.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static String DateToStr(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String str = format.format(date);
        return str;
    }

    public static String getFormatDateStr(String dateStr, String formatStr){
        if (null == formatStr || "".equals(formatStr)){
            formatStr = "yyyy-MM-dd";
        }

        Date date = StrToDate(dateStr);
        SimpleDateFormat formatter = new SimpleDateFormat(formatStr);
        String dateString = formatter.format(date);
        return dateString;
    }
}
