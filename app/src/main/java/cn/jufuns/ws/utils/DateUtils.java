package cn.jufuns.ws.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author zch
 * @description
 * @created at 2017/1/18
 */

public class DateUtils {

    /**
     * 默认格式
     */
    public static final SimpleDateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * 两个日期相差天数
     *
     * @param smalldate ：较小日期
     * @param bigdate   ：较大日期
     */
    public static int getDaysBetween(String smalldate, String bigdate) {
        Calendar cal = Calendar.getInstance();
        long between_days = 0;
        try {
            cal.setTime(DEFAULT_DATE_FORMAT.parse(smalldate));
            long time1 = cal.getTimeInMillis();
            cal.setTime(DEFAULT_DATE_FORMAT.parse(bigdate));
            long time2 = cal.getTimeInMillis();
            between_days = (time2 - time1) / (1000 * 3600 * 24);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return Integer.parseInt(String.valueOf(between_days));
    }

    /**
     * 日期类型转字符串
     *
     * @param date
     */
    public static String date2Str(Date date) {
        return DEFAULT_DATE_FORMAT.format(date);
    }

}
