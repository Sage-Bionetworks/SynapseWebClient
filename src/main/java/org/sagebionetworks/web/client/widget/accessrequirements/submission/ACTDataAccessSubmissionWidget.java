package org.sagebionetworks.web.client.widget.accessrequirements.submission;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;
import java.util.List;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.dataaccess.AccessorChange;
import org.sagebionetworks.repo.model.dataaccess.Submission;
import org.sagebionetworks.repo.model.dataaccess.SubmissionState;
import org.sagebionetworks.repo.model.file.FileHandleAssociateType;
import org.sagebionetworks.repo.model.file.FileHandleAssociation;
import org.sagebionetworks.web.client.DataAccessClientAsync;
import org.sagebionetworks.web.client.DateTimeUtils;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.FileHandleWidget;
import org.sagebionetworks.web.client.widget.asynch.UserProfileAsyncHandler;
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
	DateTimeUtils dateTimeUtils;
	UserProfileAsyncHandler userProfileAsyncHandler;

	@Inject
	public ACTDataAccessSubmissionWidget(ACTDataAccessSubmissionWidgetView view, SynapseAlert synAlert, DataAccessClientAsync dataAccessClient, BigPromptModalView promptDialog, FileHandleWidget ducFileRenderer, FileHandleWidget irbFileRenderer, FileHandleList otherDocuments, SynapseJSNIUtils jsniUtils, PortalGinInjector ginInjector, DateTimeUtils dateTimeUtils, UserProfileAsyncHandler userProfileAsyncHandler) {
		this.view = view;
		this.synAlert = synAlert;
		this.dataAccessClient = dataAccessClient;
		fixServiceEntryPoint(dataAccessClient);
		this.promptDialog = promptDialog;
		this.jsniUtils = jsniUtils;
		this.ginInjector = ginInjector;
		this.dateTimeUtils = dateTimeUtils;
		this.userProfileAsyncHandler = userProfileAsyncHandler;

		otherDocuments.configure().setCanDelete(false).setCanUpload(false);
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
		view.setRejectedReasonVisible(SubmissionState.REJECTED.equals(submission.getState()));

		switch (submission.getState()) {
			case SUBMITTED:
				view.showApproveButton();
				view.showRejectButton();
				break;
			case REJECTED:
				String reason = submission.getRejectedReason() == null ? "" : submission.getRejectedReason();
				view.setRejectedReason(reason);
				break;
			case APPROVED:
			case CANCELLED:
			default:
		}

		view.setInstitution(submission.getResearchProjectSnapshot().getInstitution());
		view.setIntendedDataUse(submission.getResearchProjectSnapshot().getIntendedDataUseStatement());
		view.setIsRenewal(submission.getIsRenewalSubmission());
		view.setProjectLead(submission.getResearchProjectSnapshot().getProjectLead());
		view.setPublications(submission.getPublication());
		view.setSummaryOfUse(submission.getSummaryOfUse());
		view.setSubmittedOn(dateTimeUtils.getDateTimeString(submission.getSubmittedOn()));
		view.setRenewalColumnsVisible(submission.getIsRenewalSubmission());
		UserBadge badge = ginInjector.getUserBadgeWidget();
		badge.configure(submission.getSubmittedBy());
		view.setSubmittedBy(badge);
	}

	@Override
	public void onMoreInfo() {
		otherDocuments.clear();
		view.clearAccessors();
		if (submission.getAccessorChanges() != null) {
			addAccessorUserBadges(submission.getAccessorChanges());
		}
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
		view.showMoreInfoDialog();
	}

	public void addAccessorUserBadges(List<AccessorChange> accessorChanges) {
		for (AccessorChange change : accessorChanges) {
			userProfileAsyncHandler.getUserProfile(change.getUserId(), new AsyncCallback<UserProfile>() {
				@Override
				public void onFailure(Throwable caught) {
					synAlert.handleException(caught);
				}

				public void onSuccess(UserProfile profile) {
					UserBadgeItem badge = ginInjector.getUserBadgeItem();
					badge.configure(change, profile);
					badge.setSelectVisible(false);
					badge.setAccessTypeDropdownEnabled(false);
					view.addAccessors(badge, profile.getUserName());
				};
			});

		}
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
		// prompt for reason
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
}
