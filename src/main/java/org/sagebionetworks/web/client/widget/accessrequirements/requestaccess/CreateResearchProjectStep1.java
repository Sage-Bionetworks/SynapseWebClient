package org.sagebionetworks.web.client.widget.accessrequirements.requestaccess;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;
import org.sagebionetworks.repo.model.ManagedACTAccessRequirement;
import org.sagebionetworks.repo.model.RestrictableObjectDescriptor;
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
 * 
 * @author Jay
 *
 */
public class CreateResearchProjectStep1 implements ModalPage {
	CreateResearchProjectWizardStep1View view;
	DataAccessClientAsync client;
	ManagedACTAccessRequirement ar;
	ModalPresenter modalPresenter;
	CreateDataAccessSubmissionStep2 step2;
	ResearchProject researchProject;
	RestrictableObjectDescriptor targetSubject;

	@Inject
	public CreateResearchProjectStep1(CreateResearchProjectWizardStep1View view, DataAccessClientAsync client, CreateDataAccessSubmissionStep2 step2) {
		super();
		this.view = view;
		this.step2 = step2;
		this.client = client;
		fixServiceEntryPoint(client);
	}

	/**
	 * Configure this widget before use.
	 * 
	 * @param parentId
	 */
	public void configure(ManagedACTAccessRequirement ar, RestrictableObjectDescriptor targetSubject) {
		this.ar = ar;
		this.targetSubject = targetSubject;
		view.setIDUPublicNoteVisible(ar.getIsIDUPublic());
		client.getResearchProject(ar.getId(), new AsyncCallback<ResearchProject>() {
			@Override
			public void onFailure(Throwable caught) {
				modalPresenter.setErrorMessage(caught.getMessage());
			}

			@Override
			public void onSuccess(ResearchProject researchProject) {
				CreateResearchProjectStep1.this.researchProject = researchProject;
				if (researchProject.getInstitution() != null) {
					view.setInstitution(researchProject.getInstitution());
				}
				if (researchProject.getIntendedDataUseStatement() != null) {
					view.setIntendedDataUseStatement(researchProject.getIntendedDataUseStatement());
				}
				if (researchProject.getProjectLead() != null) {
					view.setProjectLead(researchProject.getProjectLead());
				}
			}
		});
	}

	private void updateResearchProject() {
		modalPresenter.setLoading(true);
		researchProject.setAccessRequirementId(ar.getId().toString());
		researchProject.setInstitution(view.getInstitution());
		researchProject.setIntendedDataUseStatement(view.getIntendedDataUseStatement());
		researchProject.setProjectLead(view.getProjectLead());
		// create/update research project
		client.updateResearchProject(researchProject, new AsyncCallback<ResearchProject>() {
			@Override
			public void onFailure(Throwable caught) {
				modalPresenter.setErrorMessage(caught.getMessage());
			}

			@Override
			public void onSuccess(ResearchProject researchProject) {
				step2.configure(researchProject, ar, targetSubject);
				modalPresenter.setNextActivePage(step2);
			}
		});
	}

	@Override
	public void onPrimary() {
		// validate research project values from the view
		if (!DisplayUtils.isDefined(view.getInstitution())) {
			modalPresenter.setErrorMessage("Please fill in the institution.");
		} else if (!DisplayUtils.isDefined(view.getProjectLead())) {
			modalPresenter.setErrorMessage("Please fill in the project lead.");
		} else if (!DisplayUtils.isDefined(view.getIntendedDataUseStatement())) {
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
