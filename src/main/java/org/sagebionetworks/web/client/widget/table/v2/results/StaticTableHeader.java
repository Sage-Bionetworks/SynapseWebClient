package org.sagebionetworks.web.client.widget.table.v2.results;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * A static table header.
 * 
 * @author jhill
 *
 */
public interface StaticTableHeader extends IsWidget {

	/**
	 * Set the table header text.
	 * 
	 * @param headerText
	 */
	public void setHeader(String headerText);

}
