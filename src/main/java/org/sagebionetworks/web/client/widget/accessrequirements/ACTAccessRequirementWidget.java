package org.sagebionetworks.web.client.widget.accessrequirements;

import org.sagebionetworks.repo.model.ACTAccessRequirement;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.dataaccess.DataAccessSubmissionStatus;
import org.sagebionetworks.web.client.DataAccessClientAsync;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.widget.accessrequirements.requestaccess.CreateDataAccessRequestWizard;
import org.sagebionetworks.web.client.widget.entity.WikiPageWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalWizardWidget.WizardCallback;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ACTAccessRequirementWidget implements ACTAccessRequirementWidgetView.Presenter, IsWidget {
	
	private ACTAccessRequirementWidgetView view;
	SynapseClientAsync synapseClient;
	DataAccessClientAsync dataAccessClient;
	SynapseAlert synAlert;
	WikiPageWidget wikiPageWidget;
	ACTAccessRequirement ar;
	PortalGinInjector ginInjector;
	CreateAccessRequirementButton createAccessRequirementButton;
	DeleteAccessRequirementButton deleteAccessRequirementButton;
	SubjectsWidget subjectsWidget;
	ManageAccessButton manageAccessButton;
	String submissionId;
	
	@Inject
	public ACTAccessRequirementWidget(ACTAccessRequirementWidgetView view, 
			SynapseClientAsync synapseClient,
			WikiPageWidget wikiPageWidget,
			SynapseAlert synAlert,
			PortalGinInjector ginInjector,
			SubjectsWidget subjectsWidget,
			CreateAccessRequirementButton createAccessRequirementButton,
			DeleteAccessRequirementButton deleteAccessRequirementButton,
			ManageAccessButton manageAccessButton,
			DataAccessClientAsync dataAccessClient) {
		this.view = view;
		this.synapseClient = synapseClient;
		this.synAlert = synAlert;
		this.wikiPageWidget = wikiPageWidget;
		this.ginInjector = ginInjector;
		this.subjectsWidget = subjectsWidget;
		this.createAccessRequirementButton = createAccessRequirementButton;
		this.deleteAccessRequirementButton = deleteAccessRequirementButton;
		this.manageAccessButton = manageAccessButton;
		this.dataAccessClient = dataAccessClient;
		wikiPageWidget.setModifiedCreatedByHistoryVisible(false);
		view.setPresenter(this);
		view.setWikiTermsWidget(wikiPageWidget.asWidget());
		view.setEditAccessRequirementWidget(createAccessRequirementButton);
		view.setDeleteAccessRequirementWidget(deleteAccessRequirementButton);
		view.setManageAccessWidget(manageAccessButton);
		view.setSubjectsWidget(subjectsWidget);
		view.setSynAlert(synAlert);
	}
	
	public void setRequirement(final ACTAccessRequirement ar) {
		this.ar = ar;
		synapseClient.getRootWikiId(ar.getId().toString(), ObjectType.ACCESS_REQUIREMENT.toString(), new AsyncCallback<String>() {
			@Override
			public void onFailure(Throwable caught) {
				view.setTerms(ar.getActContactInfo());
	 			view.showTermsUI();
			}
			@Override
			public void onSuccess(String rootWikiId) {
				//get wiki terms
	 			WikiPageKey wikiKey = new WikiPageKey(ar.getId().toString(), ObjectType.ACCESS_REQUIREMENT.toString(), rootWikiId);
	 			wikiPageWidget.configure(wikiKey, false, null, false);
	 			view.showWikiTermsUI();
			}
		});
		createAccessRequirementButton.configure(ar);
		deleteAccessRequirementButton.configure(ar);
		manageAccessButton.configure(ar);
		subjectsWidget.configure(ar.getSubjectIds(), true);
		// TODO: either get these in bulk, or lazy load
		refreshApprovalState();
	}
	
	public void setDataAccessSubmissionStatus(DataAccessSubmissionStatus status) {
		submissionId = status.getSubmissionId();
		view.resetState();
		switch (status.getState()) {
			case SUBMITTED:
				// TODO: request has been submitted on your behalf, or by you?
				view.showUnapprovedHeading();
				view.showRequestSubmittedMessage();
				view.showCancelRequestButton();
				break;
			case APPROVED:
				view.showApprovedHeading();
				view.showRequestApprovedMessage();
				view.showUpdateRequestButton();
				break;
			case REJECTED:
				view.showUnapprovedHeading();
				view.showRequestRejectedMessage(status.getRejectedReason());
				view.showUpdateRequestButton();
				break;
			case CANCELLED:
			default:
				view.showUnapprovedHeading();
				view.showRequestAccessButton();
				break;
		}
	}
	
	public void refreshApprovalState() {
		dataAccessClient.getDataAccessSubmissionStatus(ar.getId().toString(), new AsyncCallback<DataAccessSubmissionStatus>() {
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}
			@Override
			public void onSuccess(DataAccessSubmissionStatus status) {
				setDataAccessSubmissionStatus(status);
			}
		});
	}
	
	@Override
	public void onCancelRequest() {
		//cancel DataAccessSubmission
		dataAccessClient.cancelDataAccessSubmission(submissionId, new AsyncCallback<Void>() {
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}
			@Override
			public void onSuccess(Void result) {
				refreshApprovalState();
			}
		});
	}
	
	@Override
	public void onRequestAccess() {
		//pop up DataAccessRequest dialog
		CreateDataAccessRequestWizard wizard = ginInjector.getCreateDataAccessRequestWizard();
		view.setDataAccessRequestWizard(wizard);
		wizard.configure(ar);
		wizard.showModal(new WizardCallback() {
			//In any case, the state may have changed, so refresh this AR
			
			@Override
			public void onFinished() {
				refreshApprovalState();
			}
			
			@Override
			public void onCanceled() {
				refreshApprovalState();
			}
		});
	}
	
	public void addStyleNames(String styleNames) {
		view.addStyleNames(styleNames);
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
	
	public void setVisible(boolean visible) {
		view.setVisible(visible);
	}
}
