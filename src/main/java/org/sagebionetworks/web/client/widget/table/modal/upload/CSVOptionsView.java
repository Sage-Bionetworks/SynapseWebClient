package org.sagebionetworks.web.client.widget.table.modal.upload;

import com.google.gwt.user.client.ui.IsWidget;
/**
 * A view of the CSV options used for CSV file upload.
 * 
 * @author jhill
 *
 */
public interface CSVOptionsView extends IsWidget {

	/**
	 * Comma, tab, or other.
	 * @param delimiter
	 */
	void setSeparator(Delimiter delimiter);

	/**
	 * For the case of other, the text of the separator.
	 * @param separator
	 */
	void setOtherSeparatorValue(String separator);

	/**
	 * Comma, tab, or other.
	 * @return
	 */
	Delimiter getSeparator();

	/**
	 * For the case of other, the text of the separator.
	 * @return
	 */
	String getOtherSeparatorValue();
}
