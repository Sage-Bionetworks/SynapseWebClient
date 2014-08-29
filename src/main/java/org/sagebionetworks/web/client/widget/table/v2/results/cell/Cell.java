package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * A widget that represents a single cell.
 * 
 * @author jmhill
 *
 */
public interface Cell extends IsWidget {

	/**
	 * Set the value for this cell
	 */
	public void setValue(String value);
	
	/**
	 * Get the value for this cell
	 * @return
	 */
	public String getValue();
	
}
