package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.IsWidget;

public interface FileCellEditorView extends IsWidget, TakesValue<String>, Focusable {
	
	/**
	 * Contract between the view and presenter business logic.
	 *
	 */
	interface Presenter {
		
		/**
		 * Called when the upload button is clicked.
		 */
		public void onUpload();
	}
	
	/**
	 * Bind the view to its presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);

	/**
	 * Show the modal dialog.
	 */
	public void showModal();

}
