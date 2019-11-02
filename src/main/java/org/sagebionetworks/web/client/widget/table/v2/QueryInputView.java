package org.sagebionetworks.web.client.widget.table.v2;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * Abstraction for a view that includes a query text box and execution button.
 * 
 * @author John
 *
 */
public interface QueryInputView extends IsWidget {

	/**
	 * Business logic for this widget.
	 */
	public interface Presenter {

		/**
		 * Called when the users presses the execute query button.
		 */
		void onExecuteQuery();

		/**
		 * Called to rest the query.
		 */
		void onReset();

		/**
		 * Called when the user clicks edit results.
		 */
		void onEditResults();

		/**
		 * Called when the user clicks download results.
		 */
		void onExportTable();

		/**
		 * Called when the user clicks show query.
		 */
		void onShowQuery();

		/**
		 * Called when the user clicks download files.
		 */
		void onDownloadFilesProgrammatically();

		void onAddToDownloadList();
	}

	/**
	 * Bind this view to its presenter.
	 * 
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);

	/**
	 * Set an accepted and validated query string.
	 * 
	 * @param startQuery
	 */
	void setInputQueryString(String startQuery);

	/**
	 * 
	 * @param loading
	 */
	void setQueryInputLoading(boolean loading);

	/**
	 * The the SQL string from the input box.
	 * 
	 * @return
	 */
	public String getInputQueryString();

	/**
	 * Show or hide the input error message.
	 * 
	 * @param b
	 */
	public void showInputError(boolean visible);

	/**
	 * Set the error message.
	 * 
	 * @param string
	 */
	public void setInputErrorMessage(String string);

	/**
	 * Enable/disable the edit query results button.
	 * 
	 * @param wasSuccessful
	 */
	public void setEditEnabled(boolean enabled);

	/**
	 * Enable/disable the download query results button.
	 * 
	 * @param enabled
	 */
	public void setDownloadEnabled(boolean enabled);

	/**
	 * Show/hide the edit results button.
	 * 
	 * @param isEditable
	 */
	public void setEditVisible(boolean visibile);

	public void setQueryInputVisible(boolean visible);

	public void setShowQueryVisible(boolean visible);

	public void setDownloadFilesVisible(boolean visible);;
}
