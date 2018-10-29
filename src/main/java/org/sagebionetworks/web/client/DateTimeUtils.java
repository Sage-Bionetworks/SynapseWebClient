package org.sagebionetworks.web.client;

import java.util.Date;

public interface DateTimeUtils {

	String getDateTimeString(Date toFormat);
	/**
	 * Return a friendly relative date string.  Like "4 hours ago"
	 * @param toFormat
	 * @return
	 */
	String getRelativeTime(Date toFormat);
	
	/**
	 * 
	 * @param toFormat
	 * @param forceRelative  if true, even if the date is far into the future or past it will return the relative time
	 * @return
	 */
	String getRelativeTime(Date toFormat, boolean forceRelative);
	/**
	 * Return a friendly calendar date string.  Like "Yesterday at 3:32 PM"
	 * @param toFormat
	 * @return
	 */
	String getCalendarTime(Date toFormat);
	/**
	 * Return a friendly calendar date string.  Like "January 20, 2016 3:47 PM"
	 * @param toFormat
	 * @return
	 */
	String getLongFriendlyDate(Date toFormat);
	String getDateString(Date toFormat);
	String getYear(Date toFormat);
	void setShowUTCTime(boolean showUTC);
	boolean isShowingUTCTime();
	String getFriendlyTimeEstimate(long totalSeconds);
}
