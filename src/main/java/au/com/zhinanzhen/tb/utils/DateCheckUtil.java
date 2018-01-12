package au.com.zhinanzhen.tb.utils;


import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ikasoa.core.utils.StringUtil;

/**
 * DateCheckUtil
 * 
 * @author <a href="mailto:leisu@zhinanzhen.org">sulei</a>
 * @version 0.1
 */
public class DateCheckUtil {

    private static final Logger LOG = LoggerFactory.getLogger(DateCheckUtil.class);

    private static DateFormat format = buildDateFormat("yyyy-MM-dd", null);
    private static DateFormat timeFormat = buildDateFormat("yyyy-MM-dd HH:mm:ss", null);

    public static boolean isSameDay(String dateStr, String timeZone) {
	try {
	    return DateUtils.isSameDay(buildDateFormat(format, timeZone).parse(dateStr), new Date());
	} catch (ParseException e) {
	    return false;
	}
    }

    public static boolean isSameWeek(int n) {
	return getWeekOfDate(new Date()) == n;
    }

    public static boolean isTimeRange(String businessTimes, boolean defRet, String timeZone) {
	if (StringUtil.isEmpty(businessTimes)) {
	    return defRet;
	}
	String[] businessTimeStrs = businessTimes.split(",");
	if (businessTimeStrs.length == 0) {
	    return defRet;
	}
	for (String businessTime : businessTimeStrs) {
	    if (StringUtil.isEmpty(businessTime)) {
		continue;
	    }
	    String[] s = businessTime.split("-");
	    if (s.length == 2) {
		if (isNowRange(format.format(new Date()) + " " + s[0].trim() + ":00",
			format.format(new Date()) + " " + s[1].trim() + ":00", defRet, timeZone)) {
		    return true;
		}
	    } else {
		return defRet;
	    }
	}
	return false;
    }

    private static int getWeekOfDate(Date date) {
	Integer[] weekDays = { 0, 1, 2, 3, 4, 5, 6 };
	Calendar cal = Calendar.getInstance();
	cal.setTime(date);
	int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
	if (w < 0) {
	    w = 0;
	}
	return weekDays[w];
    }

    private static boolean isNowRange(String star, String end, boolean defRet, String timeZone) {
	try {
	    Date sdate = buildDateFormat(timeFormat, timeZone).parse(star);
	    Date edate = buildDateFormat(timeFormat, timeZone).parse(end);
	    Date date = buildDateFormat(timeFormat, timeZone).parse(timeFormat.format(new Date()));
	    return date.after(sdate) && date.before(edate);
	} catch (Exception e) {
	    LOG.error(e.getMessage());
	    return defRet;
	}
    }

    private static DateFormat buildDateFormat(String format, String timeZone) {
	return buildDateFormat(new SimpleDateFormat(format), timeZone);
    }

    private static DateFormat buildDateFormat(DateFormat df, String timeZone) {
	if (StringUtil.isNotEmpty(timeZone)) {
	    df.setTimeZone(TimeZone.getTimeZone(timeZone));
	}
	return df;
    }

    public static void main(String[] args) {
	/*
	 * timeFormat.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
	 * System.out.println(timeFormat.format(new Date()));
	 */
	System.out.println(isTimeRange("11:30-14:30,17:40-21:00",true,"GMT+10:00"));
    }

}
