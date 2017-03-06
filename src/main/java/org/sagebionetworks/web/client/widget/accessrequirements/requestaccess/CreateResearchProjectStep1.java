package org.sagebionetworks.web.client.widget.accessrequirements.requestaccess;

import org.sagebionetworks.repo.model.ACTAccessRequirement;
import org.sagebionetworks.repo.model.dataaccess.ResearchProject;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalPage;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * First page of data access wizard.  
 * @author Jay
 *
 */
public class CreateResearchProjectStep1 implements ModalPage {
	CreateResearchProjectWizardStep1View view;
	SynapseClientAsync synapseClient;
	ACTAccessRequirement ar;
	ModalPresenter modalPresenter;
	CreateDataAccessSubmissionStep2 step2;
	
	@Inject
	public CreateResearchProjectStep1(
			CreateResearchProjectWizardStep1View view,
			SynapseClientAsync synapseClient, 
			CreateDataAccessSubmissionStep2 step2) {
		super();
		this.view = view;
		this.step2 = step2;
		this.synapseClient = synapseClient;
	}
	
	/**
	 * Configure this widget before use.
	 * 
	 * @param parentId
	 */
	public void configure(ACTAccessRequirement ar) {
		this.ar = ar;
		view.setIDUPublicNoteVisible(ar.getIsIDUPublic());
	}
	
	private void createResearchProject() {
		modalPresenter.setLoading(true);
		ResearchProject researchProject = new ResearchProject();
		researchProject.setAccessRequirementId(ar.getId().toString());
		researchProject.setInstitution(view.getInstitution());
		researchProject.setIntendedDataUseStatement(view.getIntendedDataUseStatement());
		researchProject.setProjectLead(view.getProjectLead());
		//TODO: create research project
//		synapseClient.createResearchProject(researchProject, new AsyncCallback<Entity>() {
//			@Override
//			public void onSuccess(ResearchProject researchProject) {
//				step2.configure(researchProject, ar);
//				modalPresenter.setNextActivePage(step2);
//			}
//			@Override
//			public void onFailure(Throwable caught) {
//				modalPresenter.setErrorMessage(caught.getMessage());
//			}
//		});
	}

	@Override
	public void onPrimary() {
		// validate research project values from the view
		if (!DisplayUtils.isDefined(view.getInstitution())){
			modalPresenter.setErrorMessage("Please fill in the institution.");
		} else if (!DisplayUtils.isDefined(view.getProjectLead())){
			modalPresenter.setErrorMessage("Please fill in the project lead.");
		} else if (!DisplayUtils.isDefined(view.getIntendedDataUseStatement())){
			modalPresenter.setErrorMessage("Please fill in the intended data use statement.");
		} else {
			createResearchProject();
		}
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void setModalPresenter(ModalPresenter modalPresenter) {
		this.modalPresenter = modalPresenter;
		modalPresenter.setPrimaryButtonText(DisplayConstants.NEXT);
	}


}
