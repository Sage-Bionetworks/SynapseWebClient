package org.sagebionetworks.web.client;

import java.util.Date;

import com.google.gwt.user.datepicker.client.CalendarUtil;

public class DateUtils {
	public static Date getDayFromNow() {
		Date date = new Date();
		CalendarUtil.addDaysToDate(date, 1);
		return date;  
	}
	
	public static Date getWeekFromNow() {
		Date date = new Date();
		CalendarUtil.addDaysToDate(date, 7);
		return date;  
	}
	
	public static Date getYearFromNow() {
		Date date = new Date();
		CalendarUtil.addMonthsToDate(date, 12);
		return date;  
	}
}
