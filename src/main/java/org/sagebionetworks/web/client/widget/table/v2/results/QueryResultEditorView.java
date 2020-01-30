package org.sagebionetworks.web.client.widget.table.v2.results;

import org.sagebionetworks.web.client.utils.Callback;
import com.google.gwt.user.client.ui.IsWidget;

/**
 * An abstraction for a view that allows a single query result page to be edited.
 * 
 * @author John
 *
 */
public interface QueryResultEditorView extends IsWidget {

	/**
	 * All business logic for this widget goes here.
	 *
	 */
	public interface Presenter {

		/**
		 * Add a row to the table.
		 */
		void onAddRow();

		/**
		 * Toggle the selected rows.
		 */
		void onToggleSelect();

		/**
		 * Select all rows.
		 */
		void onSelectAll();

		/**
		 * Select no rows.
		 */
		void onSelectNone();

		/**
		 * Delete the selected rows.
		 */
		void onDeleteSelected();

		/**
		 * Called when the save button is pressed.
		 */
		void onSave();

		/**
		 * Called when the user clicks the close button.
		 */
		void onCancel();

	}

	/**
	 * Bind the presenter to the view.
	 * 
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);

	/**
	 * This widget contains the table of data for the page.
	 * 
	 * @param pageWidget
	 */
	public void setTablePageWidget(TablePageWidget pageWidget);

	/**
	 * Enable or disable the delete button.
	 * 
	 * @param enabled
	 */
	public void setDeleteButtonEnabled(boolean enabled);

	/**
	 * Show an error message.
	 * 
	 * @param message
	 */
	public void showErrorMessage(String message);

	/**
	 * Show or hide the error message alert box.
	 * 
	 * @param b
	 */
	public void setErrorMessageVisible(boolean visible);

	/**
	 * Widget that displays append progress.
	 * 
	 * @param progress
	 */
	public void setProgressWidget(IsWidget progress);

	/**
	 * Show the progress dialog.
	 * 
	 */
	public void showProgress();

	/**
	 * Hide the progress dialog.
	 * 
	 */
	public void hideProgress();

	/**
	 * Show a confirm dialog.
	 * 
	 * @param title
	 * @param message
	 * @param okayCallback
	 */
	void showConfirmDialog(String message, Callback callback);

	/**
	 * Show the editor dialog.
	 * 
	 */
	void showEditor();

	/**
	 * Change the state of the save button while saving.
	 * 
	 * @param b
	 */
	void setSaveButtonLoading(boolean b);

	/**
	 * Hide the editor.
	 */
	void hideEditor();

	void showErrorDialog(String message);

	void showMessage(String title, String message);

	void setAddRowButtonVisible(boolean visible);

	void setButtonToolbarVisible(boolean visible);

}
