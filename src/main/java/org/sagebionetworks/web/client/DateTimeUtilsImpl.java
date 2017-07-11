package org.sagebionetworks.web.client;

import java.util.Date;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.TimeZone;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.datepicker.client.CalendarUtil;

public class DateTimeUtilsImpl implements DateTimeUtils {
	private DateTimeFormat dateOnly = DateTimeFormat.getFormat("MM/dd/yyyy");
	private static final String SMALL_DATE_FORMAT = "MM/dd/yyyy hh:mm:ssaa";
	private DateTimeFormat smallDateFormat = DateTimeFormat.getFormat(SMALL_DATE_FORMAT);
	private DateTimeFormat smallDateFormatUTC = DateTimeFormat.getFormat(SMALL_DATE_FORMAT + " 'UTC'");
	private static final String LONG_DATE_FORMAT = "EEEE, MMMM d, yyyy h:mm a";
	private DateTimeFormat longDateFormat = DateTimeFormat.getFormat(LONG_DATE_FORMAT);
	private DateTimeFormat longDateFormatUTC = DateTimeFormat.getFormat(LONG_DATE_FORMAT + " 'UTC'");
	private DateTimeFormat iso8601Format =  DateTimeFormat.getFormat(PredefinedFormat.ISO_8601);
	public TimeZone currentTimezone;
	public static final TimeZone UTC_TIMEZONE = TimeZone.createTimeZone(0);
	
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
		DateTimeFormat formatter = isShowingUTCTime() ? smallDateFormatUTC : smallDateFormat;
		return formatter.format(toFormat, currentTimezone);
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
		DateTimeFormat formatter = isShowingUTCTime() ? longDateFormatUTC : longDateFormat;
		return formatter.format(toFormat, currentTimezone);
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
	
	@Override
	public void setShowUTCTime(boolean showUTC) {
		currentTimezone = showUTC ? UTC_TIMEZONE : null;
	}
	
	@Override
	public boolean isShowingUTCTime() {
		return UTC_TIMEZONE.equals(currentTimezone);
	}
	
	public TimeZone getCurrentTimezone() {
		return currentTimezone;
	}
}
