package org.sagebionetworks.web.client;

import java.util.Date;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.datepicker.client.CalendarUtil;

public class DateTimeUtilsImpl implements DateTimeUtils {
	private DateTimeFormat dateOnly = DateTimeFormat.getFormat("MM/dd/yyyy");
	private DateTimeFormat smallDateFormat = DateTimeFormat.getFormat("MM/dd/yyyy hh:mm:ssaa");
	private DateTimeFormat longDateFormat = DateTimeFormat.getFormat("EEEE, MMMM d, yyyy h:mm a");
	private DateTimeFormat iso8601Format =  DateTimeFormat.getFormat(PredefinedFormat.ISO_8601);
	
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
	

	private static native String _getRelativeTime(String s) /*-{
		return $wnd.moment(s).fromNow();
	}-*/;
	private static native String _getCalendarTime(String s) /*-{
		return $wnd.moment(s).calendar();
	}-*/;
	
	
	@Override
	public String convertDateToSmallString(Date toFormat) {
		return smallDateFormat.format(toFormat, GlobalApplicationStateImpl.currentTimezone);
	}
	
	@Override
	public String getRelativeTime(Date toFormat) {
		return _getRelativeTime(iso8601Format.format(toFormat));
	}
	@Override
	public String getCalendarTime(Date toFormat) {
		return _getCalendarTime(iso8601Format.format(toFormat));
	}
	
	@Override
	public String getLongFriendlyDate(Date toFormat) {
		return longDateFormat.format(toFormat, GlobalApplicationStateImpl.currentTimezone);
	}
	
	//  convertDataToPrettyString, use getLongFriendlyDate!
	
	/**
	 * Converts a date to just a date.
	 * @return  yyyy-MM-dd
	 * @return
	 */
	public String convertDateToSimpleString(Date toFormat) {
		if(toFormat == null) throw new IllegalArgumentException("Date cannot be null");
		return dateOnly.format(toFormat);
	}
}
