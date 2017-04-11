package org.sagebionetworks.web.client.widget.accessrequirements.submission;

import org.sagebionetworks.repo.model.dataaccess.DataAccessSubmission;
import org.sagebionetworks.repo.model.dataaccess.DataAccessSubmissionState;
import org.sagebionetworks.repo.model.file.FileHandleAssociateType;
import org.sagebionetworks.repo.model.file.FileHandleAssociation;
import org.sagebionetworks.web.client.DataAccessClientAsync;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.widget.FileHandleWidget;
import org.sagebionetworks.web.client.widget.entity.PromptModalView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.upload.FileHandleList;
import org.sagebionetworks.web.client.widget.user.UserBadge;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ACTDataAccessSubmissionWidget implements ACTDataAccessSubmissionWidgetView.Presenter, IsWidget {
	
	private ACTDataAccessSubmissionWidgetView view;
	DataAccessClientAsync dataAccessClient;
	SynapseAlert synAlert;
	DataAccessSubmission submission;
	PromptModalView promptDialog;
	FileHandleList otherDocuments;
	FileHandleWidget ducFileRenderer;
	FileHandleWidget irbFileRenderer;
	SynapseJSNIUtils jsniUtils;
	PortalGinInjector ginInjector;
	
	@Inject
	public ACTDataAccessSubmissionWidget(ACTDataAccessSubmissionWidgetView view, 
			SynapseAlert synAlert,
			DataAccessClientAsync dataAccessClient,
			final PromptModalView promptDialog,
			FileHandleWidget ducFileRenderer,
			FileHandleWidget irbFileRenderer,
			FileHandleList otherDocuments,
			SynapseJSNIUtils jsniUtils,
			PortalGinInjector ginInjector) {
		this.view = view;
		this.synAlert = synAlert;
		this.dataAccessClient = dataAccessClient;
		this.promptDialog = promptDialog;
		this.jsniUtils = jsniUtils;
		this.ginInjector = ginInjector;
		
		otherDocuments.configure()
			.setUploadButtonText("Browse...")
			.setCanDelete(false)
			.setCanUpload(false);
		this.ducFileRenderer = ducFileRenderer;
		this.irbFileRenderer = irbFileRenderer;
		this.otherDocuments = otherDocuments;
		view.setPresenter(this);
		
		view.setDucWidget(ducFileRenderer);
		view.setIrbWidget(irbFileRenderer);
		view.setPromptModal(promptDialog);
		view.setSynAlert(synAlert);
		promptDialog.configure("Reason", "Rejection reason:", "Send", "");
		promptDialog.setPresenter(new PromptModalView.Presenter() {
			@Override
			public void onPrimary() {
				updateDataAccessSubmissionState(DataAccessSubmissionState.REJECTED, promptDialog.getValue());
			}
		});
	}
	
	public void configure(DataAccessSubmission submission) {
		this.submission = submission;
		view.hideActions();
		// setup the view wrt submission state
		view.setState(submission.getState().name());
		switch (submission.getState()) {
			case APPROVED:
				view.showRejectButton();
				break;
			case SUBMITTED:
				view.showApproveButton();
				view.showRejectButton();
				break;
			case CANCELLED:
			case NOT_SUBMITTED:
			case REJECTED:
			default:
		}
		view.clearAccessors();
		if (submission.getAccessors() != null) {
			for (String userId : submission.getAccessors()) {
				UserBadge badge = ginInjector.getUserBadgeWidget();
				badge.configure(userId);
				view.addAccessors(badge);
			}
		}
		otherDocuments.clear();
		if (submission.getAttachments() != null) {
			for (String fileHandleId : submission.getAttachments()) {
				otherDocuments.addFileLink(getFileHandleAssociation(fileHandleId));
			}	
		}
		
		
		if (submission.getDucFileHandleId() != null) {
			ducFileRenderer.configure(getFileHandleAssociation(submission.getDucFileHandleId()));
		}
		if (submission.getIrbFileHandleId() != null) {
			irbFileRenderer.configure(getFileHandleAssociation(submission.getIrbFileHandleId()));
		}
		view.setInstitution(submission.getResearchProjectSnapshot().getInstitution());
		view.setIntendedDataUse(submission.getResearchProjectSnapshot().getIntendedDataUseStatement());
		view.setIsRenewal(submission.getIsRenewalSubmission());
		view.setProjectLead(submission.getResearchProjectSnapshot().getProjectLead());
		view.setPublications(submission.getPublication());
		view.setSummaryOfUse(submission.getSummaryOfUse());
		view.setSubmittedOn(jsniUtils.convertDateToSmallString(submission.getSubmittedOn()));
	}
	
	private FileHandleAssociation getFileHandleAssociation(String fileHandleId) {
		FileHandleAssociation fha = new FileHandleAssociation();
		fha.setAssociateObjectId(submission.getId());
		fha.setAssociateObjectType(FileHandleAssociateType.DataAccessSubmissionAttachment);
		fha.setFileHandleId(fileHandleId);
		return fha;
	}
	
	private void updateDataAccessSubmissionState(DataAccessSubmissionState state, String reason) {
		dataAccessClient.updateDataAccessSubmissionState(submission.getId(), state, reason, new AsyncCallback<DataAccessSubmission>() {
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}
			@Override
			public void onSuccess(DataAccessSubmission result) {
				configure(result);
			}
		});
	}
	
	@Override
	public void onApprove() {
		updateDataAccessSubmissionState(DataAccessSubmissionState.APPROVED, null);
	}
	@Override
	public void onReject() {
		//prompt for reason
		promptDialog.show();
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
	
	public void setDucColumnVisible(boolean visible) {
		view.setDucColumnVisible(visible);
	}
	public void setIrbColumnVisible(boolean visible) {
		view.setIrbColumnVisible(visible);
	}
	public void setOtherAttachmentsColumnVisible(boolean visible) {
		view.setOtherAttachmentsColumnVisible(visible);
	}
	public void setRenewalColumnsVisible(boolean visible) {
		view.setRenewalColumnsVisible(visible);
	}
}
