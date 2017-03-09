package org.sagebionetworks.web.client.widget.accessrequirements.requestaccess;

import org.sagebionetworks.repo.model.ACTAccessRequirement;
import org.sagebionetworks.repo.model.dataaccess.ResearchProject;
import org.sagebionetworks.web.client.DataAccessClientAsync;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalPage;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * First page of data access wizard.  
 * @author Jay
 *
 */
public class CreateResearchProjectStep1 implements ModalPage {
	CreateResearchProjectWizardStep1View view;
	DataAccessClientAsync client;
	ACTAccessRequirement ar;
	ModalPresenter modalPresenter;
	CreateDataAccessSubmissionStep2 step2;
	ResearchProject researchProject;
	
	@Inject
	public CreateResearchProjectStep1(
			CreateResearchProjectWizardStep1View view,
			DataAccessClientAsync client, 
			CreateDataAccessSubmissionStep2 step2) {
		super();
		this.view = view;
		this.step2 = step2;
		this.client = client;
	}
	
	/**
	 * Configure this widget before use.
	 * 
	 * @param parentId
	 */
	public void configure(ACTAccessRequirement ar) {
		this.ar = ar;
		view.setIDUPublicNoteVisible(ar.getIsIDUPublic());
		client.getResearchProject(ar.getId(), new AsyncCallback<ResearchProject>() {
			@Override
			public void onFailure(Throwable caught) {
				modalPresenter.setErrorMessage(caught.getMessage());
			}
			
			@Override
			public void onSuccess(ResearchProject researchProject) {
				CreateResearchProjectStep1.this.researchProject = researchProject;
				view.setInstitution(researchProject.getInstitution());
				view.setIntendedDataUseStatement(researchProject.getIntendedDataUseStatement());
				view.setProjectLead(researchProject.getProjectLead());
			}
		});
	}
	
	private void updateResearchProject() {
		modalPresenter.setLoading(true);
		researchProject.setAccessRequirementId(ar.getId().toString());
		researchProject.setInstitution(view.getInstitution());
		researchProject.setIntendedDataUseStatement(view.getIntendedDataUseStatement());
		researchProject.setProjectLead(view.getProjectLead());
		//create/update research project
		client.updateResearchProject(researchProject, new AsyncCallback<ResearchProject>() {
			@Override
			public void onFailure(Throwable caught) {
				modalPresenter.setErrorMessage(caught.getMessage());
			}
			@Override
			public void onSuccess(ResearchProject researchProject) {
				step2.configure(researchProject, ar);
				modalPresenter.setNextActivePage(step2);
			}
		});
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
			updateResearchProject();
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
