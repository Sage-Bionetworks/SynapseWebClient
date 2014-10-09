package org.sagebionetworks.web.client.widget.table.modal;

import org.sagebionetworks.web.client.widget.table.TableCreatedHandler;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * Abstraction for a widget used to upload CSV files to a table.
 * 
 * This widget is a modal dialog and must be added to the page.
 * 
 * @author John
 *
 */
public interface UploadTableModalWidget extends IsWidget {

	/**
	 * Configure this widget before using it.
	 * 
	 * @param parentId
	 * @param handler Table creation notification.
	 */
	public void configure(String parentId, TableCreatedHandler handler);
	
	/**
	 * Call this method to show the modal dialog.
	 */
	public void showModal();
}
