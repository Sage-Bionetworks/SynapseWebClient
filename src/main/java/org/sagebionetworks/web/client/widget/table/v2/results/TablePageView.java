package org.sagebionetworks.web.client.widget.table.v2.results;

import java.util.List;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * Abstraction for a view of a single page of a table query result.
 * 
 * @author John
 *
 */
public interface TablePageView extends IsWidget {
	/**
	 * Set the headers for this table.
	 * This will be extended to including column sorting data.
	 * @param headers
	 */
	public void setTableHeaders(List<IsWidget> headers);
	
	/**
	 * Add a row to this table.o
	 * @param rowWidget
	 */
	public void addRow(RowWidget rowWidget);
	
	/**
	 * Remove this row from the view.
	 * @param row
	 */
	public void removeRow(RowWidget row);
	
	/**
	 * Set the pagination widget
	 * 
	 * @param paginationWidget
	 */
	public void setPaginationWidget(IsWidget paginationWidget);
	
	/**
	 * Show or hide the pagination widgets
	 * 
	 * @param visible
	 */
	public void setPaginationWidgetVisible(boolean visible);

	/**
	 * Show or hide the editor buffer.
	 * 
	 * @param isEditable
	 */
	public void setEditorBufferVisible(boolean isEditable);
	
	void setTableVisible(boolean visible);
	void showLoading();
	void hideLoading();
}
