package org.sagebionetworks.web.client.widget.accessrequirements;

import org.sagebionetworks.repo.model.ACTAccessRequirement;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.dataaccess.AccessRequirementStatus;
import org.sagebionetworks.repo.model.dataaccess.BasicAccessRequirementStatus;
import org.sagebionetworks.web.client.DataAccessClientAsync;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.JiraURLHelper;
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
	SynapseClientAsync synapseClient;
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
	JiraURLHelper jiraURLHelper;
	PopupUtilsView popupUtils;
	ManageAccessButton manageAccessButton;
	@Inject
	public ACTAccessRequirementWidget(ACTAccessRequirementWidgetView view, 
			SynapseClientAsync synapseClient,
			WikiPageWidget wikiPageWidget,
			SynapseAlert synAlert,
			PortalGinInjector ginInjector,
			SubjectsWidget subjectsWidget,
			CreateAccessRequirementButton createAccessRequirementButton,
			DeleteAccessRequirementButton deleteAccessRequirementButton,
			DataAccessClientAsync dataAccessClient,
			LazyLoadHelper lazyLoadHelper,
			AuthenticationController authController,
			JiraURLHelper jiraURLHelper,
			PopupUtilsView popupUtils,
			ManageAccessButton manageAccessButton) {
		this.view = view;
		this.synapseClient = synapseClient;
		this.synAlert = synAlert;
		this.wikiPageWidget = wikiPageWidget;
		this.ginInjector = ginInjector;
		this.subjectsWidget = subjectsWidget;
		this.createAccessRequirementButton = createAccessRequirementButton;
		this.deleteAccessRequirementButton = deleteAccessRequirementButton;
		this.dataAccessClient = dataAccessClient;
		this.lazyLoadHelper = lazyLoadHelper;
		this.authController = authController;
		this.jiraURLHelper = jiraURLHelper;
		this.popupUtils = popupUtils;
		this.manageAccessButton = manageAccessButton;
		wikiPageWidget.setModifiedCreatedByHistoryVisible(false);
		view.setPresenter(this);
		view.setWikiTermsWidget(wikiPageWidget.asWidget());
		view.setEditAccessRequirementWidget(createAccessRequirementButton);
		view.setDeleteAccessRequirementWidget(deleteAccessRequirementButton);
		view.setSubjectsWidget(subjectsWidget);
		view.setSynAlert(synAlert);
		view.setManageAccessWidget(manageAccessButton);
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
		subjectsWidget.configure(ar.getSubjectIds(), true);
		manageAccessButton.configure(ar);
		lazyLoadHelper.setIsConfigured();
	}
	
	public void showUnapproved() {
		view.showUnapprovedHeading();
		if (ar.getOpenJiraIssue() != null && ar.getOpenJiraIssue()) {
			view.showRequestAccessButton();	
		}
	}
	
	public void showApproved() {
		view.showApprovedHeading();
		view.showRequestApprovedMessage();
	}
	
	public void refreshApprovalState() {
		dataAccessClient.getAccessRequirementStatus(ar.getId().toString(), new AsyncCallback<AccessRequirementStatus>() {
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}
			@Override
			public void onSuccess(AccessRequirementStatus status) {
				if (((BasicAccessRequirementStatus)status).getIsApproved()) {
					showApproved();
				} else {
					showUnapproved();
				}
			}
		});
	}
	
	@Override
	public void onRequestAccess() {
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
