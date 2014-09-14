package org.sagebionetworks.web.client.widget.table.v2.results;

import org.sagebionetworks.web.client.widget.asynch.AsynchronousProgressWidget;
import org.sagebionetworks.web.client.widget.asynch.JobTrackingWidget;
import org.sagebionetworks.web.client.widget.pagination.BasicPaginationWidget;

import com.google.gwt.user.client.ui.IsWidget;


public interface TableQueryResultView extends IsWidget {

	public interface Presenter {

		/**
		 * Called when the user selected the edit row button.
		 */
		void onEditRows();

		/**
		 * Called when the save button is pressed.
		 */
		void onSave();
		
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

	/**
	 * Change the state of the save button while saving.
	 * @param b
	 */
	void setSaveButtonLoading(boolean b);

	/**
	 * Hide the editor.
	 */
	void hideEditor();

	/**
	 * Show or hide the tool bar.
	 * @param b
	 */
	void setToolbarVisible(boolean visible);

	/**
	 * Enable the edit button.
	 * @param isEditable
	 */
	void setEditEnabled(boolean isEditable);

	/**
	 * The progress widget shows query progress.s
	 * @param progressWidget
	 */
	void setProgressWidget(JobTrackingWidget progressWidget);

	/**
	 * Show or hide the progress widget.
	 * @param visible
	 */
	void setProgressWidgetVisible(boolean visible);
	
}
