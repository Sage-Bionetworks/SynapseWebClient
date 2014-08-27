package org.sagebionetworks.web.client.widget.table.v2.results;

import java.util.List;


import com.google.gwt.user.client.ui.IsWidget;

/**
 * 
 * Abstraction for a view of a single page of a table query result.
 * 
 * @author John
 *
 */
public interface TablePageView extends IsWidget {
	
	public interface Presenter {
		
	}
	
	/**
	 * Set the headers for this table.
	 * This will be extended to including column sorting data.
	 * @param headers
	 */
	public void setTableHeaders(List<String> headers);
	
	/**
	 * Add a row to this table.o
	 * @param newRow
	 */
	public void addRow(RowView newRow);
	
	/**
	 * Iterate over the current rows of this table.
	 * @return
	 */
	public Iterable<RowView> getRows();
	
	/**
	 * Delete a row.
	 * @param toDelete
	 */
	public void deleteRow(RowView toDelete);
	

}
