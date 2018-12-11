package org.sagebionetworks.web.client.widget.table.v2;

import org.gwtbootstrap3.client.ui.constants.AlertType;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.web.client.utils.Callback;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * Abstraction for a widget of a TableEntity.
 * @author John
 *
 */
public interface TableEntityWidgetView extends IsWidget {
	
	public interface Presenter {
		void onShowSimpleSearch();
		void onShowAdvancedSearch();
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
	 * Show or hide the query results
	 * @param visible
	 */
	public void setQueryResultsVisible(boolean visible);

	/**
	 * Set the query results widget.
	 * @param queryResultsWidget
	 */
	public void setQueryResultsWidget(IsWidget queryResultsWidget);
	
	/**
	 * Set the query input widget.
	 * @param queryInputWidget
	 */
	public void setQueryInputWidget(IsWidget queryInputWidget);

	/**
	 * Show or hide the query input.
	 * @param b
	 */
	public void setQueryInputVisible(boolean visible);
	public void setTableToolbarVisible(boolean visible);

	/**
	 * Add a modal to the page.
	 * @param w
	 */
	
	public void addModalWidget(IsWidget w);
	void setScopeVisible(boolean visible);
	void setSchemaVisible(boolean visible);
	
	void showErrorMessage(String message);
	void showConfirmDialog(String title, String confirmationMessage, Callback yesCallback);
	void setSimpleSearchLinkVisible(boolean visible);
	void setAdvancedSearchLinkVisible(boolean visible);
	void setAddToDownloadList(IsWidget w);
}
