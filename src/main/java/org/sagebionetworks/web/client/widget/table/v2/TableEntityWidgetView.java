package org.sagebionetworks.web.client.widget.table.v2;

import java.util.List;

import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.TableBundle;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * Abstraction for a widget of a TableEntity.
 * @author John
 *
 */
public interface TableEntityWidgetView extends IsWidget {
	
	public interface Presenter{
		
	}
	
	/**
	 * Bind this view to the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);

	/**
	 * Configure the view with the table data.
	 * @param tableId
	 * @param tableBundel
	 * @param isEditable
	 */
	void configure(String tableId, List<ColumnModel> schema, boolean isEditable);

	/**
	 * Notify the view that there are no columns.
	 */
	public void showNoColumns(String message);

	/**
	 * Show or hide the query input
	 * @param b
	 */
	public void setQueryInputVisible(boolean visible);

	/**
	 * Show or hide the query results
	 * @param visible
	 */
	public void setQueryResultsVisible(boolean visible);

}
