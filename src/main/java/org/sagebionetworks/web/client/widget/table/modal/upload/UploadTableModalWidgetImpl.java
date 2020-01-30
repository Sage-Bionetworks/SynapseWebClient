package org.sagebionetworks.web.client.widget.table.modal.upload;

import org.gwtbootstrap3.client.ui.ModalSize;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalWizardWidget;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalWizardWidget.WizardCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * All business logic is here.
 * 
 * @author John
 *
 */
public class UploadTableModalWidgetImpl implements UploadTableModalWidget {

	ModalWizardWidget modalWizarWidget;
	UploadCSVFilePage uploadCSVFileWidget;

	@Inject
	public UploadTableModalWidgetImpl(ModalWizardWidget modalWizarWidget, UploadCSVFilePage uploadCSVFileWidget) {
		this.modalWizarWidget = modalWizarWidget;
		this.modalWizarWidget.setTitle("Upload Table");
		this.modalWizarWidget.setModalSize(ModalSize.LARGE);
		this.uploadCSVFileWidget = uploadCSVFileWidget;
		this.modalWizarWidget.configure(this.uploadCSVFileWidget);
	}

	@Override
	public void configure(String parentId, String tableId) {
		this.uploadCSVFileWidget.configure(parentId, tableId);
	}

	@Override
	public Widget asWidget() {
		return modalWizarWidget.asWidget();
	}

	@Override
	public void showModal(WizardCallback wizardCallback) {
		this.modalWizarWidget.showModal(wizardCallback);
	}

}
