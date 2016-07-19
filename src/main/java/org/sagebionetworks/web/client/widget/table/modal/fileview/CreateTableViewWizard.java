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
	private String parentId;
	private TableType type;
	
	@Inject
	public CreateTableViewWizard(ModalWizardWidget modalWizardWidget, CreateTableViewWizardStep1 step1) {
		this.modalWizardWidget = modalWizardWidget;
		this.modalWizardWidget.setModalSize(ModalSize.LARGE);
		this.step1 = step1;
	}

	public void configure(String parentId, TableType type) {
		this.parentId = parentId;
		this.type = type;
		if (TableType.view.equals(type)) {
			this.modalWizardWidget.setTitle("Create View");
		} else {
			this.modalWizardWidget.setTitle("Create Table");
		}
	}

	public Widget asWidget() {
		return modalWizardWidget.asWidget();
	}

	public void showModal(WizardCallback wizardCallback) {
		this.step1.configure(parentId, type);
		this.modalWizardWidget.configure(this.step1);
		this.modalWizardWidget.showModal(wizardCallback);
	}

}
