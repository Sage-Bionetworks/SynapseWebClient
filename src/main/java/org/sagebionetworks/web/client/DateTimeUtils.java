package org.sagebionetworks.web.client;

import java.util.Date;

public interface DateTimeUtils {

	String convertDateToSmallString(Date toFormat);
	/**
	 * Return a friendly relative date string.  Like "4 hours ago"
	 * @param toFormat
	 * @return
	 */
	String getRelativeTime(Date toFormat);
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
	String convertDateToSimpleString(Date toFormat);
	void setShowUTCTime(boolean showUTC);
	boolean isShowingUTCTime();
}
