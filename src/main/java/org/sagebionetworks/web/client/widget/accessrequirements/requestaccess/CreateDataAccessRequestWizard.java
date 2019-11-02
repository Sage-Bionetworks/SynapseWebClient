package org.sagebionetworks.web.client.widget.accessrequirements.requestaccess;

import org.gwtbootstrap3.client.ui.ModalSize;
import org.sagebionetworks.repo.model.ManagedACTAccessRequirement;
import org.sagebionetworks.repo.model.RestrictableObjectDescriptor;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalWizardWidget;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalWizardWidget.WizardCallback;
import org.sagebionetworks.web.shared.WebConstants;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * Wizard used to create a new access request
 * 
 * @author Jay
 *
 */
public class CreateDataAccessRequestWizard implements IsWidget {

	ModalWizardWidget modalWizardWidget;
	CreateResearchProjectStep1 step1;
	private ManagedACTAccessRequirement ar;
	RestrictableObjectDescriptor targetSubject;
	public static final String VIEW_HELP = "To access Controlled Data (data with Conditions for Use), you must fulfill the Conditions for Use set by the data contributor.";
	public static final String VIEW_URL = WebConstants.DOCS_URL + "contribute_and_access_controlled_use_data.html";

	@Inject
	public CreateDataAccessRequestWizard(ModalWizardWidget modalWizardWidget, CreateResearchProjectStep1 step1) {
		this.modalWizardWidget = modalWizardWidget;
		this.modalWizardWidget.setModalSize(ModalSize.LARGE);
		this.step1 = step1;
	}

	public void configure(ManagedACTAccessRequirement ar, RestrictableObjectDescriptor targetSubject) {
		this.ar = ar;
		this.targetSubject = targetSubject;
		this.modalWizardWidget.setTitle("Request Access");
		this.modalWizardWidget.setHelp(VIEW_HELP, VIEW_URL);
	}

	public Widget asWidget() {
		return modalWizardWidget.asWidget();
	}

	public void showModal(WizardCallback wizardCallback) {
		this.step1.configure(ar, targetSubject);
		this.modalWizardWidget.configure(this.step1);
		this.modalWizardWidget.showModal(wizardCallback);
	}

}
