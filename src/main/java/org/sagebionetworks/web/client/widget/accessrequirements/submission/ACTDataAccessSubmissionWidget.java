package org.sagebionetworks.web.client.widget.accessrequirements.submission;

import java.util.List;

import org.sagebionetworks.repo.model.dataaccess.AccessApprovalResult;
import org.sagebionetworks.repo.model.dataaccess.BatchAccessApprovalRequest;
import org.sagebionetworks.repo.model.dataaccess.BatchAccessApprovalResult;
import org.sagebionetworks.repo.model.dataaccess.Submission;
import org.sagebionetworks.repo.model.dataaccess.SubmissionState;
import org.sagebionetworks.repo.model.file.FileHandleAssociateType;
import org.sagebionetworks.repo.model.file.FileHandleAssociation;
import org.sagebionetworks.web.client.DataAccessClientAsync;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.FileHandleWidget;
import org.sagebionetworks.web.client.widget.entity.BigPromptModalView;
import org.sagebionetworks.web.client.widget.entity.act.UserBadgeItem;
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
	Submission submission;
	BigPromptModalView promptDialog;
	FileHandleList otherDocuments;
	FileHandleWidget ducFileRenderer;
	FileHandleWidget irbFileRenderer;
	SynapseJSNIUtils jsniUtils;
	PortalGinInjector ginInjector;
	
	@Inject
	public ACTDataAccessSubmissionWidget(ACTDataAccessSubmissionWidgetView view, 
			SynapseAlert synAlert,
			DataAccessClientAsync dataAccessClient,
			final BigPromptModalView promptDialog,
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
			.setCanDelete(false)
			.setCanUpload(false);
		this.ducFileRenderer = ducFileRenderer;
		this.irbFileRenderer = irbFileRenderer;
		this.otherDocuments = otherDocuments;
		view.setPresenter(this);
		
		view.setDucWidget(ducFileRenderer);
		view.setIrbWidget(irbFileRenderer);
		view.setPromptModal(promptDialog);
		view.setOtherAttachmentWidget(otherDocuments);
		view.setSynAlert(synAlert);
		promptDialog.configure("Reason", "Rejection reason:", "", new Callback() {
			@Override
			public void invoke() {
				updateDataAccessSubmissionState(SubmissionState.REJECTED, promptDialog.getValue());
				promptDialog.hide();
			}
		});
	}
	
	public void configure(Submission submission) {
		this.submission = submission;
		view.hideActions();
		// setup the view wrt submission state
		view.setState(submission.getState().name());
		switch (submission.getState()) {
			case SUBMITTED:
				view.showApproveButton();
				view.showRejectButton();
				break;
			case APPROVED:
			case CANCELLED:
			case NOT_SUBMITTED:
			case REJECTED:
			default:
		}
		view.clearAccessors();
		if (submission.getAccessors() != null) {
			getApprovalState(submission.getAccessors());
		}
		otherDocuments.clear();
		if (submission.getAttachments() != null) {
			for (String fileHandleId : submission.getAttachments()) {
				otherDocuments.addFileLink(getFileHandleAssociation(fileHandleId));
			}
		}
		
		if (submission.getDucFileHandleId() != null) {
			ducFileRenderer.configure(getFileHandleAssociation(submission.getDucFileHandleId()));
			ducFileRenderer.setVisible(true);			
		} else {
			ducFileRenderer.setVisible(false);
		}
		if (submission.getIrbFileHandleId() != null) {
			irbFileRenderer.configure(getFileHandleAssociation(submission.getIrbFileHandleId()));
			irbFileRenderer.setVisible(true);
		} else {
			irbFileRenderer.setVisible(false);
		}
		view.setInstitution(submission.getResearchProjectSnapshot().getInstitution());
		view.setIntendedDataUse(submission.getResearchProjectSnapshot().getIntendedDataUseStatement());
		view.setIsRenewal(submission.getIsRenewalSubmission());
		view.setProjectLead(submission.getResearchProjectSnapshot().getProjectLead());
		view.setPublications(submission.getPublication());
		view.setSummaryOfUse(submission.getSummaryOfUse());
		view.setSubmittedOn(jsniUtils.convertDateToSmallString(submission.getSubmittedOn()));
		UserBadge badge = ginInjector.getUserBadgeWidget();
		badge.configure(submission.getSubmittedBy());
		view.setSubmittedBy(badge);
	}
	
	public void getApprovalState(List<String> accessorIds) {
		BatchAccessApprovalRequest request = new BatchAccessApprovalRequest();
		request.setAccessRequirementId(submission.getAccessRequirementId());
		request.setUserIds(accessorIds);
		dataAccessClient.getAccessApprovalInfo(request, new AsyncCallback<BatchAccessApprovalResult>() {
			@Override
			public void onSuccess(BatchAccessApprovalResult result) {
				for (AccessApprovalResult approvalResult : result.getResults()) {
					UserBadgeItem badge = ginInjector.getUserBadgeItem();
					badge.configure(approvalResult.getUserId());
					badge.setSelectVisible(false);
					//set access requirement approval icon visibility 
					badge.setMetRequirementIconVisible(approvalResult.getHasApproval());
					view.addAccessors(badge);
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}
		});
	}
	
	private FileHandleAssociation getFileHandleAssociation(String fileHandleId) {
		FileHandleAssociation fha = new FileHandleAssociation();
		fha.setAssociateObjectId(submission.getId());
		fha.setAssociateObjectType(FileHandleAssociateType.DataAccessSubmissionAttachment);
		fha.setFileHandleId(fileHandleId);
		return fha;
	}
	
	public void updateDataAccessSubmissionState(SubmissionState state, String reason) {
		dataAccessClient.updateDataAccessSubmissionState(submission.getId(), state, reason, new AsyncCallback<Submission>() {
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}
			@Override
			public void onSuccess(Submission result) {
				configure(result);
			}
		});
	}
	
	@Override
	public void onApprove() {
		updateDataAccessSubmissionState(SubmissionState.APPROVED, null);
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
