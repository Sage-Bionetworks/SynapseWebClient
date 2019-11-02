package org.sagebionetworks.web.client.widget.accessrequirements;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;
import org.sagebionetworks.repo.model.ManagedACTAccessRequirement;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.RestrictableObjectDescriptor;
import org.sagebionetworks.repo.model.dataaccess.AccessRequirementStatus;
import org.sagebionetworks.repo.model.dataaccess.ManagedACTAccessRequirementStatus;
import org.sagebionetworks.repo.model.dataaccess.SubmissionStatus;
import org.sagebionetworks.web.client.DataAccessClientAsync;
import org.sagebionetworks.web.client.DateTimeUtils;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.accessrequirements.requestaccess.CreateDataAccessRequestWizard;
import org.sagebionetworks.web.client.widget.entity.WikiPageWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.lazyload.LazyLoadHelper;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalWizardWidget.WizardCallback;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ManagedACTAccessRequirementWidget implements ManagedACTAccessRequirementWidgetView.Presenter, IsWidget {

	private ManagedACTAccessRequirementWidgetView view;
	SynapseJavascriptClient jsClient;
	DataAccessClientAsync dataAccessClient;
	SynapseAlert synAlert;
	WikiPageWidget wikiPageWidget;
	ManagedACTAccessRequirement ar;
	PortalGinInjector ginInjector;
	CreateAccessRequirementButton createAccessRequirementButton;
	DeleteAccessRequirementButton deleteAccessRequirementButton;
	IntendedDataUseReportButton iduReportButton;
	SubjectsWidget subjectsWidget;
	ReviewAccessRequestsButton reviewAccessRequestsButton;
	String submissionId;
	LazyLoadHelper lazyLoadHelper;
	AuthenticationController authController;
	UserBadge submitterUserBadge;
	DateTimeUtils dateTimeUtils;
	ReviewAccessorsButton manageAccessButton;
	RestrictableObjectDescriptor targetSubject;

	@Inject
	public ManagedACTAccessRequirementWidget(ManagedACTAccessRequirementWidgetView view, SynapseJavascriptClient jsClient, WikiPageWidget wikiPageWidget, SynapseAlert synAlert, PortalGinInjector ginInjector, SubjectsWidget subjectsWidget, CreateAccessRequirementButton createAccessRequirementButton, DeleteAccessRequirementButton deleteAccessRequirementButton, ReviewAccessRequestsButton reviewAccessRequestsButton, IntendedDataUseReportButton iduReportButton, DataAccessClientAsync dataAccessClient, LazyLoadHelper lazyLoadHelper, AuthenticationController authController, UserBadge submitterUserBadge, DateTimeUtils dateTimeUtils, ReviewAccessorsButton manageAccessButton) {
		this.view = view;
		this.jsClient = jsClient;
		this.synAlert = synAlert;
		this.wikiPageWidget = wikiPageWidget;
		this.ginInjector = ginInjector;
		this.subjectsWidget = subjectsWidget;
		this.createAccessRequirementButton = createAccessRequirementButton;
		this.deleteAccessRequirementButton = deleteAccessRequirementButton;
		this.reviewAccessRequestsButton = reviewAccessRequestsButton;
		this.iduReportButton = iduReportButton;
		this.manageAccessButton = manageAccessButton;
		this.dataAccessClient = dataAccessClient;
		fixServiceEntryPoint(dataAccessClient);
		this.lazyLoadHelper = lazyLoadHelper;
		this.authController = authController;
		this.submitterUserBadge = submitterUserBadge;
		this.dateTimeUtils = dateTimeUtils;
		wikiPageWidget.setModifiedCreatedByHistoryVisible(false);
		view.setSubmitterUserBadge(submitterUserBadge);
		view.setPresenter(this);
		view.setWikiTermsWidget(wikiPageWidget.asWidget());
		view.setEditAccessRequirementWidget(createAccessRequirementButton);
		view.setDeleteAccessRequirementWidget(deleteAccessRequirementButton);
		view.setReviewAccessRequestsWidget(reviewAccessRequestsButton);
		view.setManageAccessWidget(manageAccessButton);
		view.setSubjectsWidget(subjectsWidget);
		view.setIDUReportButton(iduReportButton);
		view.setSynAlert(synAlert);
		Callback loadDataCallback = new Callback() {
			@Override
			public void invoke() {
				refreshApprovalState();
			}
		};

		lazyLoadHelper.configure(loadDataCallback, view);
	}

	public void setRequirement(final ManagedACTAccessRequirement ar, Callback refreshCallback) {
		this.ar = ar;
		synAlert.clear();
		view.setWikiTermsWidgetVisible(false);
		jsClient.getRootWikiPageKey(ObjectType.ACCESS_REQUIREMENT.toString(), ar.getId().toString(), new AsyncCallback<String>() {
			@Override
			public void onFailure(Throwable caught) {
				if (!(caught instanceof NotFoundException)) {
					synAlert.handleException(caught);
				}
			}

			@Override
			public void onSuccess(String rootWikiId) {
				// get wiki terms
				view.setWikiTermsWidgetVisible(true);
				WikiPageKey wikiKey = new WikiPageKey(ar.getId().toString(), ObjectType.ACCESS_REQUIREMENT.toString(), rootWikiId);
				wikiPageWidget.configure(wikiKey, false, null);
			}
		});
		createAccessRequirementButton.configure(ar, refreshCallback);
		deleteAccessRequirementButton.configure(ar, refreshCallback);
		iduReportButton.configure(ar);
		reviewAccessRequestsButton.configure(ar);
		manageAccessButton.configure(ar);
		subjectsWidget.configure(ar.getSubjectIds());
		lazyLoadHelper.setIsConfigured();
	}

	public void setTargetSubject(RestrictableObjectDescriptor targetSubject) {
		this.targetSubject = targetSubject;
	}

	public void setDataAccessSubmissionStatus(ManagedACTAccessRequirementStatus status) {
		SubmissionStatus currentSubmissionStatus = status.getCurrentSubmissionStatus();
		submissionId = currentSubmissionStatus.getSubmissionId();
		switch (currentSubmissionStatus.getState()) {
			case SUBMITTED:
				// request has been submitted on your behalf, or by you?
				String submitterUserId = currentSubmissionStatus.getSubmittedBy();
				view.showUnapprovedHeading();
				if (authController.getCurrentUserPrincipalId().equals(submitterUserId)) {
					view.showRequestSubmittedMessage();
					view.showCancelRequestButton();
				} else {
					submitterUserBadge.configure(submitterUserId);
					view.showRequestSubmittedByOtherUser();
				}
				break;
			case REJECTED:
				view.showUnapprovedHeading();
				view.showRequestRejectedMessage(currentSubmissionStatus.getRejectedReason());
				view.showUpdateRequestButton();
				break;
			case CANCELLED:
			case APPROVED:
			default:
				if (status.getIsApproved()) {
					showApproved();
					if (status.getExpiredOn() != null && status.getExpiredOn().getTime() > 0) {
						view.showExpirationDate(dateTimeUtils.getLongFriendlyDate(status.getExpiredOn()));
					}
				} else {
					showUnapproved();
				}
				break;
		}
	}

	public void showAnonymous() {
		view.showUnapprovedHeading();
		view.showLoginButton();
	}

	public void showUnapproved() {
		view.showUnapprovedHeading();
		view.showRequestAccessButton();
	}

	public void showApproved() {
		view.showApprovedHeading();
		view.showRequestApprovedMessage();
		view.showUpdateRequestButton();
	}

	public void refreshApprovalState() {
		view.resetState();
		if (!authController.isLoggedIn()) {
			showAnonymous();
			return;
		}
		dataAccessClient.getAccessRequirementStatus(ar.getId().toString(), new AsyncCallback<AccessRequirementStatus>() {
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}

			@Override
			public void onSuccess(AccessRequirementStatus status) {
				ManagedACTAccessRequirementStatus managedACTARStatus = (ManagedACTAccessRequirementStatus) status;
				if (managedACTARStatus.getCurrentSubmissionStatus() == null) {
					if (status.getIsApproved()) {
						showApproved();
					} else {
						showUnapproved();
					}
				} else {
					setDataAccessSubmissionStatus(managedACTARStatus);
				}
			}
		});
	}

	@Override
	public void onCancelRequest() {
		// cancel DataAccessSubmission
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
		// pop up DataAccessRequest dialog
		CreateDataAccessRequestWizard wizard = ginInjector.getCreateDataAccessRequestWizard();
		view.setDataAccessRequestWizard(wizard);
		wizard.configure(ar, targetSubject);
		wizard.showModal(new WizardCallback() {
			// In any case, the state may have changed, so refresh this AR
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

	public void hideButtons() {
		view.hideButtonContainers();
	}

	public void setReviewAccessRequestsVisible(boolean visible) {
		view.setReviewAccessRequestsWidgetContainerVisible(visible);
	}
}
