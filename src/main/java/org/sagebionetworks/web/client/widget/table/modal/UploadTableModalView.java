package org.sagebionetworks.web.client.widget.table.modal;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * An abstraction for a view used to upload a CSV from the local machine and create a table with the data.
 * 
 * @author John
 *
 */
public interface UploadTableModalView extends IsWidget{

	/**
	 * Business logic for this view can be found in the presenter.
	 * 
	 * @author John
	 *
	 */
	public interface Presenter{
		/**
		 * Called when the primary button is pressed.
		 */
		void onPrimary();
		
	}
	
	/**
	 * Bind this view to its presenter.
	 * 
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);

	/**
	 * Clear all data in the view.
	 */
	public void clear();

	/**
	 * Show the modal dialog.
	 */
	public void showModal();
}
