package org.sagebionetworks.web.client.widget.accessrequirements;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;
import static org.sagebionetworks.web.client.presenter.ProfilePresenter.IS_CERTIFIED;
import static org.sagebionetworks.web.client.presenter.ProfilePresenter.IS_VERIFIED;

import org.sagebionetworks.repo.model.AccessApproval;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.SelfSignAccessRequirement;
import org.sagebionetworks.repo.model.UserBundle;
import org.sagebionetworks.repo.model.dataaccess.AccessRequirementStatus;
import org.sagebionetworks.repo.model.dataaccess.BasicAccessRequirementStatus;
import org.sagebionetworks.web.client.DataAccessClientAsync;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.WikiPageWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.lazyload.LazyLoadHelper;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SelfSignAccessRequirementWidget implements SelfSignAccessRequirementWidgetView.Presenter, IsWidget {
	public static final String GET_VALIDATED_PROFILE_PAGE = "accounts_certified_users_and_profile_validation.html#validated-profile";
	public static final String GET_CERTIFIED_PAGE = "accounts_certified_users_and_profile_validation.html#certified-users";
	private SelfSignAccessRequirementWidgetView view;
	SynapseClientAsync synapseClient;
	DataAccessClientAsync dataAccessClient;
	SynapseAlert synAlert;
	WikiPageWidget wikiPageWidget;
	SelfSignAccessRequirement ar;
	AuthenticationController authController;
	CreateAccessRequirementButton createAccessRequirementButton;
	DeleteAccessRequirementButton deleteAccessRequirementButton;
	SubjectsWidget subjectsWidget;
	LazyLoadHelper lazyLoadHelper;
	PopupUtilsView popupUtils;
	ReviewAccessorsButton manageAccessButton;
	Callback refreshCallback;
	SynapseJavascriptClient jsClient;
	
	@Inject
	public SelfSignAccessRequirementWidget(SelfSignAccessRequirementWidgetView view,
			AuthenticationController authController,
			DataAccessClientAsync dataAccessClient,
			SynapseClientAsync synapseClient,
			WikiPageWidget wikiPageWidget,
			SynapseAlert synAlert,
			SubjectsWidget subjectsWidget,
			CreateAccessRequirementButton createAccessRequirementButton,
			DeleteAccessRequirementButton deleteAccessRequirementButton,
			LazyLoadHelper lazyLoadHelper,
			ReviewAccessorsButton manageAccessButton,
			PopupUtilsView popupUtils,
			SynapseJavascriptClient jsClient) {
		this.view = view;
		this.synapseClient = synapseClient;
		fixServiceEntryPoint(synapseClient);
		this.dataAccessClient = dataAccessClient;
		fixServiceEntryPoint(dataAccessClient);
		this.synAlert = synAlert;
		this.wikiPageWidget = wikiPageWidget;
		this.authController = authController;
		this.subjectsWidget = subjectsWidget;
		this.createAccessRequirementButton = createAccessRequirementButton;
		this.deleteAccessRequirementButton = deleteAccessRequirementButton;
		this.lazyLoadHelper = lazyLoadHelper;
		this.manageAccessButton = manageAccessButton;
		this.popupUtils = popupUtils;
		this.jsClient = jsClient;
		wikiPageWidget.setModifiedCreatedByHistoryVisible(false);
		view.setPresenter(this);
		view.setWikiTermsWidget(wikiPageWidget.asWidget());
		view.setEditAccessRequirementWidget(createAccessRequirementButton);
		view.setDeleteAccessRequirementWidget(deleteAccessRequirementButton);
		view.setSubjectsWidget(subjectsWidget);
		view.setManageAccessWidget(manageAccessButton);
		Callback loadDataCallback = new Callback() {
			@Override
			public void invoke() {
				refreshApprovalState();
			}
		};
		
		lazyLoadHelper.configure(loadDataCallback, view);
	}
	
	
	public void setRequirement(final SelfSignAccessRequirement ar, Callback refreshCallback) {
		this.ar = ar;
		this.refreshCallback = refreshCallback;
		jsClient.getRootWikiPageKey(ObjectType.ACCESS_REQUIREMENT.toString(), ar.getId().toString(), new AsyncCallback<String>() {
			@Override
			public void onFailure(Throwable caught) {
				if (!(caught instanceof NotFoundException)) {
					synAlert.handleException(caught);	
				}
			}
			@Override
			public void onSuccess(String rootWikiId) {
				//get wiki terms
	 			WikiPageKey wikiKey = new WikiPageKey(ar.getId().toString(), ObjectType.ACCESS_REQUIREMENT.toString(), rootWikiId);
	 			wikiPageWidget.configure(wikiKey, false, null);
			}
		});
		createAccessRequirementButton.configure(ar, refreshCallback);
		deleteAccessRequirementButton.configure(ar, refreshCallback);
		subjectsWidget.configure(ar.getSubjectIds());
		manageAccessButton.configure(ar);
		lazyLoadHelper.setIsConfigured();
	}
	
	public void setDataAccessSubmissionStatus(BasicAccessRequirementStatus status) {
		// set up view based on DataAccessSubmission state
		if (status.getIsApproved()) {
			view.showApprovedHeading();
		} else {
			view.showUnapprovedHeading();
			if (ar.getIsCertifiedUserRequired() || ar.getIsValidatedProfileRequired()) {
				// get user certification and validation state
				int mask = IS_CERTIFIED | IS_VERIFIED;
				synAlert.clear();
				jsClient.getUserBundle(Long.parseLong(authController.getCurrentUserPrincipalId()), mask, new AsyncCallback<UserBundle>() {
					@Override
					public void onFailure(Throwable caught) {
						synAlert.handleException(caught);
					}
					@Override
					public void onSuccess(UserBundle userBundle) {
						if (ar.getIsCertifiedUserRequired() && !userBundle.getIsCertified()) {
							view.showGetCertifiedUI();
						} else if (ar.getIsValidatedProfileRequired() && !userBundle.getIsVerified()) {
							view.showGetProfileValidatedUI();
						} else {
							// user met criteria
							view.showSignTermsButton();
						}
					}
				});
			} else {
				// user can sign
				view.showSignTermsButton();
			}
		}
	}
	
	public void showAnonymous() {
		view.showUnapprovedHeading();
		view.showLoginButton();
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
				setDataAccessSubmissionStatus((BasicAccessRequirementStatus)status);
			}
		});
	}
	
	@Override
	public void onSignTerms() {
		// create the self-signed access approval, then update this object
		synAlert.clear();
		AsyncCallback<AccessApproval> callback = new AsyncCallback<AccessApproval>() {
			@Override
			public void onFailure(Throwable t) {
				synAlert.handleException(t);
			}
			@Override
			public void onSuccess(AccessApproval result) {
				refreshCallback.invoke();
			}
		};
		AccessApproval approval = new AccessApproval();
		approval.setAccessorId(authController.getCurrentUserPrincipalId());
		approval.setRequirementId(ar.getId());
		approval.setRequirementVersion(ar.getVersionNumber());
		synapseClient.createAccessApproval(approval, callback);
	}
	
	public void addStyleNames(String styleNames) {
		view.addStyleNames(styleNames);
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
	
	@Override
	public void onCertify() {
		popupUtils.openInNewWindow(WebConstants.DOCS_URL + GET_CERTIFIED_PAGE);
	}
	
	@Override
	public void onValidateProfile() {
		popupUtils.openInNewWindow(WebConstants.DOCS_URL + GET_VALIDATED_PROFILE_PAGE);
	}
}
