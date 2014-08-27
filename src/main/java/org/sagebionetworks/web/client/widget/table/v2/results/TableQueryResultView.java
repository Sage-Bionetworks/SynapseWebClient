package org.sagebionetworks.web.client.widget.table.v2.results;

import com.google.gwt.user.client.ui.IsWidget;


public interface TableQueryResultView extends IsWidget {

	public interface Presenter {

		/**
		 * Called when the user selected the edit row button.
		 */
		void onEditRows();
		
	}

	/**
	 * Bind the view to the presenter.
	 * @param presenter
	 */
	void setPresenter(Presenter presenter);

	/**
	 * Show or hide the table.
	 * @param b
	 */
	void setTableVisible(boolean visible);

	/**
	 * Bind the page widget to this view.
	 * @param pageWidget
	 */
	void setPageWidget(TablePageWidget pageWidget);

	/**
	 * Show an error message.
	 * @param message
	 */
	void showError(String message);

	/**
	 * Show or hide the error alert.
	 * @param b
	 */
	void setErrorVisible(boolean visible);

	/**
	 * Set the editor widget
	 * @param queryResultEditor
	 */
	void setEditorWidget(QueryResultEditorWidget queryResultEditor);

	/**
	 * Show the editor dialog.
	 * 
	 */
	void showEditor();
}
