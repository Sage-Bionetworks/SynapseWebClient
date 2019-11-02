package org.sagebionetworks.web.client.widget.table.modal.upload;

import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalWizardWidget.WizardCallback;
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
	 * @param tableId When provided, the uploaded file will be used to update this table. Can be null.
	 */
	public void configure(String parentId, String tableId);

	/**
	 * After configuring the widget call this method to show the wizard.
	 * 
	 * @param wizardCallback
	 */
	public void showModal(WizardCallback wizardCallback);

}
