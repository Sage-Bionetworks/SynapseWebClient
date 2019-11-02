package org.sagebionetworks.web.client.widget.accessrequirements.createaccessrequirement;

import org.gwtbootstrap3.client.ui.ModalSize;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.RestrictableObjectDescriptor;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalWizardWidget;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalWizardWidget.WizardCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * Wizard used to create a new access requirement
 * 
 * @author Jay
 *
 */
public class CreateAccessRequirementWizard implements IsWidget {

	public static final String CREATE_ACCESS_REQUIREMENT_TITLE = "Create Access Requirement";
	public static final String EDIT_ACCESS_REQUIREMENT_TITLE = "Edit Access Requirement";

	ModalWizardWidget modalWizardWidget;
	CreateAccessRequirementStep1 step1;

	@Inject
	public CreateAccessRequirementWizard(ModalWizardWidget modalWizardWidget, CreateAccessRequirementStep1 step1) {
		this.modalWizardWidget = modalWizardWidget;
		this.modalWizardWidget.setModalSize(ModalSize.LARGE);
		this.step1 = step1;
	}

	public void configure(RestrictableObjectDescriptor subject) {
		this.modalWizardWidget.setTitle(CREATE_ACCESS_REQUIREMENT_TITLE);
		this.step1.configure(subject);
	}

	public void configure(AccessRequirement accessRequirement) {
		this.modalWizardWidget.setTitle(EDIT_ACCESS_REQUIREMENT_TITLE);
		this.step1.configure(accessRequirement);
	}

	public Widget asWidget() {
		return modalWizardWidget.asWidget();
	}

	public void showModal(WizardCallback wizardCallback) {
		this.modalWizardWidget.configure(this.step1);
		this.modalWizardWidget.showModal(wizardCallback);
	}

}
