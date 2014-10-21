package org.sagebionetworks.web.client.widget.table.modal.upload;

import org.gwtbootstrap3.client.ui.ModalSize;
import org.sagebionetworks.web.client.widget.table.modal.wizard.AbstractModalWizard;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalWizardView;

import com.google.inject.Inject;

/**
 * All business logic is here.
 * 
 * @author John
 *
 */
public class UploadTableModalWidgetImpl extends AbstractModalWizard implements UploadTableModalWidget {
	
	UploadCSVFilePage uploadCSVFileWidget;

	@Inject
	public UploadTableModalWidgetImpl(ModalWizardView view, UploadCSVFilePage uploadCSVFileWidget) {
		super(view, uploadCSVFileWidget);
		this.setTitle("Upload Table");
		this.setModalSize(ModalSize.LARGE);
		this.uploadCSVFileWidget = uploadCSVFileWidget;
	}

	@Override
	public void configure(String parentId) {
		this.uploadCSVFileWidget.configure(parentId);
	}
}
