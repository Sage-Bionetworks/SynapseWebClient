package org.sagebionetworks.web.client;

import java.util.Date;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.TimeZone;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.datepicker.client.CalendarUtil;
import com.google.inject.Inject;

public class DateTimeUtilsImpl implements DateTimeUtils {
	public static final String UTC = " 'UTC'";
	public static final String YEAR_ONLY_FORMAT_STRING = "yyyy";
	public static DateTimeFormat YEAR_ONLY_FORMAT;
	public static final String DATE_ONLY_FORMAT_STRING = "MM/dd/yyyy";
	private static DateTimeFormat DATE_ONLY_FORMAT;
	private static DateTimeFormat DATE_ONLY_UTC_FORMAT;
	public static final String SMALL_DATE_FORMAT_STRING = "MM/dd/yyyy h:mm aa";
	private static DateTimeFormat SMALL_DATE_FORMAT;
	private static DateTimeFormat SMALL_DATE_FORMAT_UTC;
	public static final String LONG_DATE_FORMAT_STRING = "EEEE, MMMM d, yyyy h:mm a";
	private static DateTimeFormat LONG_DATE_FORMAT;
	private static DateTimeFormat LONG_DATE_FORMAT_UTC;
	private DateTimeFormat iso8601Format;
	public TimeZone currentTimezone;
	public static final TimeZone UTC_TIMEZONE = TimeZone.createTimeZone(0);
	private Moment moment;
	
	@Inject
	public DateTimeUtilsImpl(Moment moment, GWTWrapper gwt) {
		this.moment = moment;
		YEAR_ONLY_FORMAT = gwt.getFormat(YEAR_ONLY_FORMAT_STRING);
		DATE_ONLY_FORMAT = gwt.getFormat(DATE_ONLY_FORMAT_STRING);
		DATE_ONLY_UTC_FORMAT = gwt.getFormat(DATE_ONLY_FORMAT_STRING + UTC);
		SMALL_DATE_FORMAT = gwt.getFormat(SMALL_DATE_FORMAT_STRING);
		SMALL_DATE_FORMAT_UTC = gwt.getFormat(SMALL_DATE_FORMAT_STRING + UTC);
		LONG_DATE_FORMAT = gwt.getFormat(LONG_DATE_FORMAT_STRING);
		LONG_DATE_FORMAT_UTC = gwt.getFormat(LONG_DATE_FORMAT_STRING + UTC);
		iso8601Format =  gwt.getFormat(PredefinedFormat.ISO_8601);
	}
	
	public static Date getDayFromNow() {
		Date date = new Date();
		CalendarUtil.addDaysToDate(date, 1);
		return date;  
	}
	
	public static Date getDaysFromNow(int days) {
		Date date = new Date();
		CalendarUtil.addDaysToDate(date, days);
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
	
	@Override
	public String getDateTimeString(Date toFormat) {
		DateTimeFormat formatter = isShowingUTCTime() ? SMALL_DATE_FORMAT_UTC : SMALL_DATE_FORMAT;
		return formatter.format(toFormat, currentTimezone);
	}
	
	@Override
	public String getRelativeTime(Date toFormat) {
		return getRelativeTime(toFormat, false);
	}
	
	@Override
	public String getRelativeTime(Date toFormat, boolean forceRelative) {
		if (!forceRelative && (toFormat.before(getDaysFromNow(-1)) || toFormat.after(getDaysFromNow(1)))) {
			//older than a day (or more than a day into the future), show a long date (show in UTC if user wants)
			return getDateString(toFormat);
		} else {
			//posted in the last 24 hours
			return moment.getRelativeTime(iso8601Format.format(toFormat));
		}
	}
	
	@Override
	public String getCalendarTime(Date toFormat) {
		return moment.getCalendarTime(iso8601Format.format(toFormat));
	}
	
	@Override
	public String getLongFriendlyDate(Date toFormat) {
		DateTimeFormat formatter = isShowingUTCTime() ? LONG_DATE_FORMAT_UTC : LONG_DATE_FORMAT;
		return formatter.format(toFormat, currentTimezone);
	}
	
	/**
	 * Converts a date to just a date.
	 * @return  yyyy-MM-dd
	 * @return
	 */
	public String getDateString(Date toFormat) {
		DateTimeFormat formatter = isShowingUTCTime() ? DATE_ONLY_UTC_FORMAT : DATE_ONLY_FORMAT;
		return formatter.format(toFormat);
	}

	public String getYear(Date toFormat) {
		DateTimeFormat formatter = YEAR_ONLY_FORMAT;
		return formatter.format(toFormat);
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
	
	@Override
	public String getFriendlyTimeEstimate(long totalSeconds) {
		long seconds = totalSeconds % 60;
	    long totalMinutes = totalSeconds / 60;
	    long minutes = totalMinutes % 60;
	    long hours = totalMinutes / 60;
	    StringBuilder sb = new StringBuilder();
	    boolean isHours = hours > 0;
	    if (isHours) {
	    	sb.append(hours + " h ");
	    }
	    if (minutes > 0) {
	    	sb.append(minutes + " min ");
	    }
	    if (!isHours && (seconds > 0 || sb.toString().isEmpty())) {
	    	sb.append(seconds + " s");
	    }
	    return sb.toString().trim();
	}
}
