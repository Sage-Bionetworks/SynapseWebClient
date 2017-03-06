package org.sagebionetworks.web.client.widget.table.modal.fileview;

import org.gwtbootstrap3.client.ui.ModalSize;
import org.sagebionetworks.web.client.widget.entity.tabs.TablesTab;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalWizardWidget;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalWizardWidget.WizardCallback;
import org.sagebionetworks.web.shared.WebConstants;

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
	public static final String VIEW_HELP = "Synapse File Views are views of all files within one or more Projects or Folders.";
	public static final String VIEW_URL = WebConstants.DOCS_URL + "fileviews.html";
	
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
			this.modalWizardWidget.setTitle("Create File View");
			this.modalWizardWidget.setHelp(VIEW_HELP, VIEW_URL);
			
		} else {
			this.modalWizardWidget.setTitle("Create Table");
			this.modalWizardWidget.setHelp(TablesTab.TABLES_HELP, TablesTab.TABLES_HELP_URL);
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
