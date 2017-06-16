package org.sagebionetworks.web.client.widget.table.modal.fileview;

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
	private String parentId;
	private TableType type;
	public static final String VIEW_HELP = "Synapse File Views are views of all files within one or more Projects or Folders.";
	public static final String VIEW_URL = WebConstants.DOCS_URL + "fileviews.html";
	public static final String PROJECT_VIEW_HELP = "A Synapse Project View represents a logical collection of Projects.";
	public static final String PROJECT_VIEW_URL = WebConstants.DOCS_URL + "projectviews.html";
	
	@Inject
	public CreateTableViewWizard(ModalWizardWidget modalWizardWidget, CreateTableViewWizardStep1 step1) {
		this.modalWizardWidget = modalWizardWidget;
		this.modalWizardWidget.addStyleNames("modal-90-percent");
		this.step1 = step1;
	}

	public void configure(String parentId, TableType type) {
		this.parentId = parentId;
		this.type = type;
		if (TableType.fileview.equals(type)) {
			this.modalWizardWidget.setTitle("Create File View");
			this.modalWizardWidget.setHelp(VIEW_HELP, VIEW_URL);
		} else if (TableType.projectview.equals(type)) {
			this.modalWizardWidget.setTitle("Create Project View");
			this.modalWizardWidget.setHelp(PROJECT_VIEW_HELP, PROJECT_VIEW_URL);
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
