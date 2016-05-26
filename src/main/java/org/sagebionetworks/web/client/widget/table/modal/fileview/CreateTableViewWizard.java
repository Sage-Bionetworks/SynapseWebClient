package org.sagebionetworks.web.client.widget.table.modal.fileview;

import org.gwtbootstrap3.client.ui.ModalSize;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalWizardWidget;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalWizardWidget.WizardCallback;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * Wizard used to create a new Table or View
 * @author Jay
 *
 */
public class CreateTableViewWizard {
	
	ModalWizardWidget modalWizardWidget;
	CreateTableViewWizardStep1 step1;
	public enum TableType {
	    table,
	    view
	}

	@Inject
	public CreateTableViewWizard(ModalWizardWidget modalWizarWidget, CreateTableViewWizardStep1 step1) {
		this.modalWizardWidget = modalWizarWidget;
		this.modalWizardWidget.setModalSize(ModalSize.LARGE);
		this.step1 = step1;
		this.modalWizardWidget.configure(this.step1);
	}

	public void configure(String parentId, TableType type) {
		if (TableType.view.equals(type)) {
			this.modalWizardWidget.setTitle("Create View");
		} else {
			this.modalWizardWidget.setTitle("Create Table");
		}
		this.step1.configure(parentId, type);
	}

	public Widget asWidget() {
		return modalWizardWidget.asWidget();
	}

	public void showModal(WizardCallback wizardCallback) {
		this.modalWizardWidget.showModal(wizardCallback);
	}

}
