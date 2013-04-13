package org.sagebionetworks.web.client.utils;

import java.util.Date;

import org.gwttime.time.DateTime;
import org.gwttime.time.format.ISODateTimeFormat;

/**
 * Extracted from DisplayUtils.
 * @author jmhill
 *
 */
public class GwtDateUtils {

	public static String convertDateToString(Date toFormat) {
		if(toFormat == null) throw new IllegalArgumentException("Date cannot be null");
		DateTime dt = new DateTime(toFormat.getTime());
		return ISODateTimeFormat.dateTime().print(dt);
	}
	
	/**
	 * YYYY-MM-DD HH:mm:ss
	 * @param toFormat
	 * @return
	 */
	public static String converDataToPrettyString(Date toFormat) {
		if(toFormat == null) throw new IllegalArgumentException("Date cannot be null");
		DateTime dt = new DateTime(toFormat.getTime());
		return ISODateTimeFormat.dateHourMinuteSecond().print(dt).replaceAll("T", " ");		
	}
	
	
	/**
	 * Converts a date to just a date.
     * @return  yyyy-MM-dd
	 * @return
	 */
	public static String converDateaToSimpleString(Date toFormat) {
		if(toFormat == null) throw new IllegalArgumentException("Date cannot be null");
		DateTime dt = new DateTime(toFormat.getTime());
		return ISODateTimeFormat.date().print(dt);		
	}
 
	public static Date convertStringToDate(String toFormat) {
		if(toFormat == null) throw new IllegalArgumentException("Date cannot be null");
		DateTime dt = ISODateTimeFormat.dateTime().parseDateTime(toFormat);
		return dt.toDate();
	}
}
