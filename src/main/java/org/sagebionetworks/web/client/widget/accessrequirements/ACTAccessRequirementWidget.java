package org.sagebionetworks.web.client.widget.accessrequirements;

import org.sagebionetworks.repo.model.ACTAccessRequirement;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.dataaccess.AccessRequirementStatus;
import org.sagebionetworks.repo.model.dataaccess.ManagedACTAccessRequirementStatus;
import org.sagebionetworks.repo.model.dataaccess.SubmissionStatus;
import org.sagebionetworks.web.client.DataAccessClientAsync;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.accessrequirements.requestaccess.CreateDataAccessRequestWizard;
import org.sagebionetworks.web.client.widget.entity.JiraURLHelper;
import org.sagebionetworks.web.client.widget.entity.WikiPageWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.lazyload.LazyLoadHelper;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalWizardWidget.WizardCallback;
import org.sagebionetworks.web.client.widget.user.UserBadge;
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
	LazyLoadHelper lazyLoadHelper;
	AuthenticationController authController;
	UserBadge submitterUserBadge;
	ACTRevokeUserAccessButton revokeUserAccessButton;
	JiraURLHelper jiraURLHelper;
	PopupUtilsView popupUtils;
	
	@Inject
	public ACTAccessRequirementWidget(ACTAccessRequirementWidgetView view, 
			SynapseClientAsync synapseClient,
			WikiPageWidget wikiPageWidget,
			SynapseAlert synAlert,
			PortalGinInjector ginInjector,
			SubjectsWidget subjectsWidget,
			CreateAccessRequirementButton createAccessRequirementButton,
			DeleteAccessRequirementButton deleteAccessRequirementButton,
			ACTRevokeUserAccessButton revokeUserAccessButton,
			ManageAccessButton manageAccessButton,
			DataAccessClientAsync dataAccessClient,
			LazyLoadHelper lazyLoadHelper,
			AuthenticationController authController,
			UserBadge submitterUserBadge,
			JiraURLHelper jiraURLHelper,
			PopupUtilsView popupUtils) {
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
		this.lazyLoadHelper = lazyLoadHelper;
		this.authController = authController;
		this.submitterUserBadge = submitterUserBadge;
		this.revokeUserAccessButton = revokeUserAccessButton;
		this.jiraURLHelper = jiraURLHelper;
		this.popupUtils = popupUtils;
		wikiPageWidget.setModifiedCreatedByHistoryVisible(false);
		view.setSubmitterUserBadge(submitterUserBadge);
		view.setPresenter(this);
		view.setWikiTermsWidget(wikiPageWidget.asWidget());
		view.setEditAccessRequirementWidget(createAccessRequirementButton);
		view.setDeleteAccessRequirementWidget(deleteAccessRequirementButton);
		view.setRevokeUserAccessWidget(revokeUserAccessButton);
		view.setManageAccessWidget(manageAccessButton);
		view.setSubjectsWidget(subjectsWidget);
		view.setSynAlert(synAlert);
		Callback loadDataCallback = new Callback() {
			@Override
			public void invoke() {
				refreshApprovalState();
			}
		};
		
		lazyLoadHelper.configure(loadDataCallback, view);
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
		revokeUserAccessButton.configure(ar);
		manageAccessButton.configure(ar);
		subjectsWidget.configure(ar.getSubjectIds(), true);
		lazyLoadHelper.setIsConfigured();
	}
	
	public static boolean isAcceptDataAccessRequest(Boolean isAcceptDataAccessRequest) {
		return isAcceptDataAccessRequest != null && isAcceptDataAccessRequest;
	}
	
	public void setDataAccessSubmissionStatus(SubmissionStatus status) {
		submissionId = status.getSubmissionId();
		view.resetState();
		switch (status.getState()) {
			case SUBMITTED:
				// request has been submitted on your behalf, or by you?
				String submitterUserId = status.getSubmittedBy();
				view.showUnapprovedHeading();
				if (authController.getCurrentUserPrincipalId().equals(submitterUserId)) {
					view.showRequestSubmittedMessage();
					view.showCancelRequestButton();
				} else {
					submitterUserBadge.configure(submitterUserId);
					view.showRequestSubmittedByOtherUser();
				}
				
				break;
			case APPROVED:
				showApproved();
				break;
			case REJECTED:
				view.showUnapprovedHeading();
				view.showRequestRejectedMessage(status.getRejectedReason());
				view.showUpdateRequestButton();
				break;
			case CANCELLED:
			default:
				showUnapproved();
				break;
		}
	}
	
	public void showUnapproved() {
		view.showUnapprovedHeading();
		if (isAcceptDataAccessRequest(ar.getAcceptRequest() || ar.getOpenJiraIssue())) {
			view.showRequestAccessButton();
		}
	}
	
	public void showApproved() {
		view.showApprovedHeading();
		view.showRequestApprovedMessage();
		if (isAcceptDataAccessRequest(ar.getAcceptRequest())) {
			view.showUpdateRequestButton();	
		}
	}
	
	public void refreshApprovalState() {
		dataAccessClient.getAccessRequirementStatus(ar.getId().toString(), new AsyncCallback<AccessRequirementStatus>() {
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}
			@Override
			public void onSuccess(AccessRequirementStatus status) {
				if (((ManagedACTAccessRequirementStatus)status).getCurrentSubmissionStatus() == null) {
					if (status.getIsApproved()) {
						showApproved();
					} else {
						showUnapproved();
					}
				} else {
					setDataAccessSubmissionStatus(((ManagedACTAccessRequirementStatus)status).getCurrentSubmissionStatus());	
				}
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
		if (ACTAccessRequirementWidget.isAcceptDataAccessRequest(ar.getAcceptRequest())) {
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
		} else {
			// request access via Jira
			UserProfile userProfile = authController.getCurrentUserSessionData().getProfile();
			if (userProfile==null) throw new IllegalStateException("UserProfile is null");
			String primaryEmail = DisplayUtils.getPrimaryEmail(userProfile);
			String createJiraIssueURL = jiraURLHelper.createRequestAccessIssue(
					userProfile.getOwnerId(), 
					DisplayUtils.getDisplayName(userProfile), 
					primaryEmail, 
					ar.getSubjectIds().get(0).getId(), 
					ar.getId().toString());
			popupUtils.openInNewWindow(createJiraIssueURL);
		}
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
	
	public void hideButtons() {
		view.hideButtonContainers();
	}
	
	public void setManageAccessVisible(boolean visible) {
		view.setManageAccessWidgetContainerVisible(visible);
	}
}
