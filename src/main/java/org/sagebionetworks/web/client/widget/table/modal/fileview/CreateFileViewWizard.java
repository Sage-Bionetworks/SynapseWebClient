package org.sagebionetworks.web.client.widget.table.modal.fileview;

import org.gwtbootstrap3.client.ui.ModalSize;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalWizardWidget;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalWizardWidget.WizardCallback;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * Wizard used to create a new File View
 * @author Jay
 *
 */
public class CreateFileViewWizard {
	
	ModalWizardWidget modalWizardWidget;
	CreateFileViewWizardStep1 step1;

	@Inject
	public CreateFileViewWizard(ModalWizardWidget modalWizarWidget, CreateFileViewWizardStep1 step1) {
		this.modalWizardWidget = modalWizarWidget;
		this.modalWizardWidget.setTitle("Create File View");
		this.modalWizardWidget.setModalSize(ModalSize.LARGE);
		this.step1 = step1;
		this.modalWizardWidget.configure(this.step1);
	}

	public void configure(String parentId) {
		this.step1.configure(parentId);
	}

	public Widget asWidget() {
		return modalWizardWidget.asWidget();
	}

	public void showModal(WizardCallback wizardCallback) {
		this.modalWizardWidget.showModal(wizardCallback);
	}

}
