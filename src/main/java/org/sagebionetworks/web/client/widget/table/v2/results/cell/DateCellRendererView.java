package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import java.util.Date;

import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.IsWidget;

public interface DateCellRendererView extends TakesValue<Date>, IsWidget {
	
	/**
	 * Set the date format.
	 * @see com.google.gwt.i18n.shared.DateTimeFormat
	 * @param format
	 */
	public void setFormat(String format);
	/**
	 * Clear the value since we cannot set null dates.
	 */
	public void clear();

}
