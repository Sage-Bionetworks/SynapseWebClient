package org.sagebionetworks.web.client.widget.accessrequirements.requestaccess;

import org.sagebionetworks.repo.model.ACTAccessRequirement;
import org.sagebionetworks.repo.model.dataaccess.ResearchProject;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalPage;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalWizardWidget;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * First page of data access wizard.  
 * @author Jay
 *
 */
public class CreateDataAccessSubmissionStep2 implements ModalPage {
	CreateDataAccessSubmissionWizardStep2View view;
	SynapseClientAsync synapseClient;
	ACTAccessRequirement ar;
	ModalPresenter modalPresenter;
	ResearchProject researchProject;
	
	@Inject
	public CreateDataAccessSubmissionStep2(
			CreateDataAccessSubmissionWizardStep2View view,
			SynapseClientAsync synapseClient) {
		super();
		this.view = view;
		this.synapseClient = synapseClient;
	}
	
	/**
	 * Configure this widget before use.
	 */
	public void configure(ResearchProject researchProject, ACTAccessRequirement ar) {
		this.ar = ar;
		this.researchProject = researchProject;
		// TODO: retrieve a suitable request object to start with, /accessRequirement/{id}/dataAccessRequestForUpdate
	}
	
	private void createDataAccessSubmission() {
		modalPresenter.setLoading(true);
		//TODO: create data access submission
	}

	@Override
	public void onPrimary() {
		// TODO: validate values from the view
		createDataAccessSubmission();
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void setModalPresenter(ModalPresenter modalPresenter) {
		this.modalPresenter = modalPresenter;
		modalPresenter.setPrimaryButtonText(DisplayConstants.SUBMIT);
		((ModalWizardWidget)modalPresenter).configure(this);
		((ModalWizardWidget)modalPresenter).showModal(new ModalWizardWidget.WizardCallback() {
			
			@Override
			public void onFinished() {
			}
			
			@Override
			public void onCanceled() {
				// need to check to see if the user would like to discard changes.
				// if Discard recent changes, then do nothing.
				// if Save, then update the DataAccessRequest 
			}
		});
	}


}
