package org.sagebionetworks.web.client.widget.accessrequirements.requestaccess;

import org.gwtbootstrap3.client.ui.ModalSize;
import org.sagebionetworks.repo.model.ACTAccessRequirement;
import org.sagebionetworks.web.client.widget.entity.tabs.TablesTab;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalWizardWidget;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalWizardWidget.WizardCallback;
import org.sagebionetworks.web.shared.WebConstants;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * Wizard used to create a new access request
 * @author Jay
 *
 */
public class CreateDataAccessRequestWizard {
	
	ModalWizardWidget modalWizardWidget;
	CreateResearchProjectStep1 step1;
	private ACTAccessRequirement ar;
	public static final String VIEW_HELP = "";
	public static final String VIEW_URL = WebConstants.DOCS_URL + "contribute_and_access_controlled_use_data.html";
	
	@Inject
	public CreateDataAccessRequestWizard(ModalWizardWidget modalWizardWidget, CreateResearchProjectStep1 step1) {
		this.modalWizardWidget = modalWizardWidget;
		this.modalWizardWidget.setModalSize(ModalSize.LARGE);
		this.step1 = step1;
	}

	public void configure(ACTAccessRequirement ar) {
		this.ar = ar;
		this.modalWizardWidget.setTitle("Request Access");
		this.modalWizardWidget.setHelp(VIEW_HELP, VIEW_URL);
	}

	public Widget asWidget() {
		return modalWizardWidget.asWidget();
	}

	public void showModal(WizardCallback wizardCallback) {
		this.step1.configure(ar);
		this.modalWizardWidget.configure(this.step1);
		this.modalWizardWidget.showModal(wizardCallback);
	}

}
