package org.sagebionetworks.web.client.widget.table.v2;

import org.gwtbootstrap3.client.ui.constants.AlertType;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.model.EntityBundle;

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

}
