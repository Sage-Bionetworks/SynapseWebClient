package org.sagebionetworks.web.client.widget.table.v2;

import org.gwtbootstrap3.client.ui.constants.AlertType;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousProgressWidget;
import org.sagebionetworks.web.client.widget.table.v2.results.TableQueryResultWidget;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * Abstraction for a widget of a TableEntity.
 * @author John
 *
 */
public interface TableEntityWidgetView extends IsWidget {
	
	public interface Presenter extends EntityUpdatedHandler {
		
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
	void configure(EntityBundle bundle, boolean isEditable);
	
	/**
	 * 
	 * @param type
	 * @param message
	 */
	public void showTableMessage(AlertType type, String message);

	/**
	 * Show or hide the table message.
	 * 
	 * @param visible
	 */
	public void setTableMessageVisible(boolean visible);
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

	/**
	 * Set the text of the query input editor.
	 * @param startQuery
	 */
	public void setInputQueryString(String startQuery);

	/**
	 * Set the query input loading state.
	 * @param loading
	 */
	public void setQueryInputLoading(boolean isLoading);

	/**
	 * Query error messages
	 * 
	 * @param danger
	 * @param message
	 */
	public void setQueryMessage(AlertType danger, String message);

	/**
	 * Show or hide the query result message.
	 * @param b
	 */
	public void setQueryResultsMessageVisible(boolean b);

	/**
	 * Set the AsynchronousProgressWidget to be shown when queries are run.
	 * @param asynchProgressWidget
	 */
	public void setProgressWidget(AsynchronousProgressWidget asynchProgressWidget);

	/**
	 * Show or hide the Query Progress widget.
	 * @param b
	 */
	public void setQueryProgressVisible(boolean b);

	/**
	 * Set the query results widget.
	 * @param queryResultsWidget
	 */
	public void setQueryResultsWidget(TableQueryResultWidget queryResultsWidget);


}
