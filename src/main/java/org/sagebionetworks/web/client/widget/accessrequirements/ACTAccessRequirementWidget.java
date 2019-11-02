package org.sagebionetworks.web.client.widget.accessrequirements;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;
import static org.sagebionetworks.web.shared.WebConstants.GRANT_ACCESS_REQUEST_COMPONENT_ID;
import static org.sagebionetworks.web.shared.WebConstants.ISSUE_PRIORITY_MINOR;
import static org.sagebionetworks.web.shared.WebConstants.REQUEST_ACCESS_ISSUE_COLLECTOR_URL;
import static org.sagebionetworks.web.shared.WebConstants.REQUEST_ACCESS_ISSUE_DESCRIPTION;
import static org.sagebionetworks.web.shared.WebConstants.REQUEST_ACCESS_ISSUE_SUMMARY;
import org.sagebionetworks.repo.model.ACTAccessRequirement;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.dataaccess.AccessRequirementStatus;
import org.sagebionetworks.repo.model.dataaccess.BasicAccessRequirementStatus;
import org.sagebionetworks.web.client.DataAccessClientAsync;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.WikiPageWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.lazyload.LazyLoadHelper;
import org.sagebionetworks.web.shared.WikiPageKey;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ACTAccessRequirementWidget implements ACTAccessRequirementWidgetView.Presenter, IsWidget {

	private ACTAccessRequirementWidgetView view;
	SynapseJavascriptClient jsClient;
	DataAccessClientAsync dataAccessClient;
	SynapseAlert synAlert;
	WikiPageWidget wikiPageWidget;
	ACTAccessRequirement ar;
	PortalGinInjector ginInjector;
	CreateAccessRequirementButton createAccessRequirementButton;
	DeleteAccessRequirementButton deleteAccessRequirementButton;
	SubjectsWidget subjectsWidget;
	String submissionId;
	LazyLoadHelper lazyLoadHelper;
	AuthenticationController authController;
	ReviewAccessorsButton manageAccessButton;
	ConvertACTAccessRequirementButton convertACTAccessRequirementButton;
	SynapseJSNIUtils jsniUtils;

	@Inject
	public ACTAccessRequirementWidget(ACTAccessRequirementWidgetView view, SynapseJavascriptClient jsClient, WikiPageWidget wikiPageWidget, SynapseAlert synAlert, PortalGinInjector ginInjector, SubjectsWidget subjectsWidget, CreateAccessRequirementButton createAccessRequirementButton, DeleteAccessRequirementButton deleteAccessRequirementButton, DataAccessClientAsync dataAccessClient, LazyLoadHelper lazyLoadHelper, AuthenticationController authController, ReviewAccessorsButton manageAccessButton, ConvertACTAccessRequirementButton convertACTAccessRequirementButton, SynapseJSNIUtils jsniUtils) {
		this.view = view;
		this.jsClient = jsClient;
		this.synAlert = synAlert;
		this.wikiPageWidget = wikiPageWidget;
		this.ginInjector = ginInjector;
		this.subjectsWidget = subjectsWidget;
		this.createAccessRequirementButton = createAccessRequirementButton;
		this.deleteAccessRequirementButton = deleteAccessRequirementButton;
		this.dataAccessClient = dataAccessClient;
		fixServiceEntryPoint(dataAccessClient);
		this.lazyLoadHelper = lazyLoadHelper;
		this.authController = authController;
		this.manageAccessButton = manageAccessButton;
		this.convertACTAccessRequirementButton = convertACTAccessRequirementButton;
		this.jsniUtils = jsniUtils;
		wikiPageWidget.setModifiedCreatedByHistoryVisible(false);
		view.setPresenter(this);
		view.setWikiTermsWidget(wikiPageWidget.asWidget());
		view.setEditAccessRequirementWidget(createAccessRequirementButton);
		view.setDeleteAccessRequirementWidget(deleteAccessRequirementButton);
		view.setSubjectsWidget(subjectsWidget);
		view.setSynAlert(synAlert);
		view.setManageAccessWidget(manageAccessButton);
		view.setConvertAccessRequirementWidget(convertACTAccessRequirementButton);
		Callback loadDataCallback = new Callback() {
			@Override
			public void invoke() {
				refreshApprovalState();
			}
		};

		lazyLoadHelper.configure(loadDataCallback, view);
	}

	public void setRequirement(final ACTAccessRequirement ar, Callback refreshCallback) {
		this.ar = ar;
		jsClient.getRootWikiPageKey(ObjectType.ACCESS_REQUIREMENT.toString(), ar.getId().toString(), new AsyncCallback<String>() {
			@Override
			public void onFailure(Throwable caught) {
				view.setTerms(ar.getActContactInfo());
				view.showTermsUI();
			}

			@Override
			public void onSuccess(String rootWikiId) {
				// get wiki terms
				WikiPageKey wikiKey = new WikiPageKey(ar.getId().toString(), ObjectType.ACCESS_REQUIREMENT.toString(), rootWikiId);
				wikiPageWidget.configure(wikiKey, false, null);
				view.showWikiTermsUI();
			}
		});
		createAccessRequirementButton.configure(ar, refreshCallback);
		deleteAccessRequirementButton.configure(ar, refreshCallback);
		subjectsWidget.configure(ar.getSubjectIds());
		manageAccessButton.configure(ar);
		convertACTAccessRequirementButton.configure(ar, refreshCallback);
		lazyLoadHelper.setIsConfigured();
	}

	public void showAnonymous() {
		view.showUnapprovedHeading();
		view.showLoginButton();
	}

	public void showUnapproved() {
		view.showUnapprovedHeading();
		if (ar.getOpenJiraIssue() != null && ar.getOpenJiraIssue()) {
			view.showRequestAccessButton();
		}
	}

	@Override
	public void onRequestAccess() {
		// request access via Jira
		UserProfile userProfile = authController.getCurrentUserProfile();
		if (userProfile == null)
			throw new IllegalStateException("UserProfile is null");
		String primaryEmail = DisplayUtils.getPrimaryEmail(userProfile);
		jsniUtils.showJiraIssueCollector(REQUEST_ACCESS_ISSUE_SUMMARY, REQUEST_ACCESS_ISSUE_DESCRIPTION, REQUEST_ACCESS_ISSUE_COLLECTOR_URL, userProfile.getOwnerId(), DisplayUtils.getDisplayName(userProfile), primaryEmail, ar.getSubjectIds().get(0).getId(), GRANT_ACCESS_REQUEST_COMPONENT_ID, ar.getId().toString(), ISSUE_PRIORITY_MINOR);
	}

	public void showApproved() {
		view.showApprovedHeading();
		view.showRequestApprovedMessage();
	}

	public void refreshApprovalState() {
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
				if (((BasicAccessRequirementStatus) status).getIsApproved()) {
					showApproved();
				} else {
					showUnapproved();
				}
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
}
