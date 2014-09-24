package org.sagebionetworks.web.client.widget.table.modal;

import org.sagebionetworks.web.client.widget.table.TableCreatedHandler;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * Abstraction for the CreateTableModalWidget.
 * 
 * @author John
 *
 */
public interface CreateTableModalWidget extends IsWidget{

	/**
	 * Configure the widget before using.
	 * @param parentId The parent of the list of tables.
	 * @param handler Handles table create events.
	 */
	public void configure(String parentId, TableCreatedHandler handler);
	
	/**
	 * Show the create table modal dialog.
	 */
	public void showCreateModal();

}
