package org.sagebionetworks.web.client.widget.asynch;

/**
 * Basic abstraction for the GWT number format.
 * 
 * @author John
 *
 */
public interface NumberFormatProvider {

	/**
	 * Set the format to use.
	 * 
	 * @param format
	 */
	public void setFormat(String format);

	/**
	 * Format the double using the previously set format.
	 * 
	 * @param toformat
	 * @return
	 */
	public String format(double toformat);

}
