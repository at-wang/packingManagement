package cn.itwang.packingmanagement.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 日期类
 */
public class DateUtils {
    public static Date date(Date date){
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String format = simpleDateFormat.format(date);
        try {
            Date parse = simpleDateFormat.parse(format);
            return parse;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}
