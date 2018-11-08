package org.sagebionetworks.web.client.widget.entity.restriction.v2;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Folder;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.RestrictableObjectType;
import org.sagebionetworks.repo.model.RestrictionInformationResponse;
import org.sagebionetworks.repo.model.RestrictionLevel;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.DataAccessClientAsync;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.place.AccessRequirementsPlace;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.asynch.IsACTMemberAsyncHandler;
import org.sagebionetworks.web.client.widget.entity.JiraURLHelper;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class RestrictionWidget implements RestrictionWidgetView.Presenter, SynapseWidgetPresenter, IsWidget {
	private AuthenticationController authenticationController;
	private JiraURLHelper jiraURLHelper;
	private GlobalApplicationState globalApplicationState;
	private RestrictionWidgetView view;
	private boolean showChangeLink, showIfProject, showFlagLink;
	private String jiraFlagLink;
	private Entity entity;
	private boolean canChangePermissions;
	private DataAccessClientAsync dataAccessClient;
	private SynapseAlert synAlert;
	private IsACTMemberAsyncHandler isACTMemberAsyncHandler;
	private SynapseJavascriptClient jsClient;
	@Inject
	public RestrictionWidget(
			RestrictionWidgetView view,
			AuthenticationController authenticationController,
			GlobalApplicationState globalApplicationState,
			JiraURLHelper jiraURLHelper,
			DataAccessClientAsync dataAccessClient,
			SynapseAlert synAlert,
			IsACTMemberAsyncHandler isACTMemberAsyncHandler,
			SynapseJavascriptClient jsClient) {
		this.view = view;
		this.authenticationController = authenticationController;
		this.globalApplicationState = globalApplicationState;
		this.jiraURLHelper = jiraURLHelper;
		this.dataAccessClient = dataAccessClient;
		fixServiceEntryPoint(dataAccessClient);
		this.synAlert = synAlert;
		this.isACTMemberAsyncHandler = isACTMemberAsyncHandler;
		this.jsClient = jsClient;
		view.setSynAlert(synAlert.asWidget());
		view.setPresenter(this);
	}
	
	public void configure(Entity entity, boolean canChangePermissions) {
		this.entity = entity;
		this.canChangePermissions = canChangePermissions;
		loadRestrictionInformation();
	}
	
	public void setShowChangeLink(boolean showChangeLink) {
		this.showChangeLink = showChangeLink;
	}
	
	public void setShowIfProject(boolean showIfProject) {
		this.showIfProject = showIfProject;
	}
	
	public void setShowFlagLink(boolean showFlagLink) {
		this.showFlagLink = showFlagLink;
	}
	
	public void showFolderRestrictionUI() {
		view.showFolderRestrictionUI();
	}
	
	public String getJiraFlagUrl() {
		UserProfile userProfile = getUserProfile();
		if (userProfile==null) throw new IllegalStateException("UserProfile is null");
		String primaryEmail = DisplayUtils.getPrimaryEmail(userProfile);
		return jiraURLHelper.createFlagIssue(
				primaryEmail, 
				DisplayUtils.getDisplayName(userProfile), 
				entity.getId());
	}
	
	public boolean includeRestrictionWidget() {
		return (entity instanceof FileEntity) || (entity instanceof TableEntity) || (entity instanceof Folder) || (showIfProject && entity instanceof Project);
	}
	
	private UserProfile getUserProfile() {
		return authenticationController.getCurrentUserProfile();
	}
	
	public boolean isAnonymous() {
		return !authenticationController.isLoggedIn();
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
	
	public void loadRestrictionInformation() {
		view.clear();
		synAlert.clear();
		jsClient.getRestrictionInformation(entity.getId(), RestrictableObjectType.ENTITY, new AsyncCallback<RestrictionInformationResponse>() {
			@Override
			public void onSuccess(RestrictionInformationResponse restrictionInformation) {
				configureUI(restrictionInformation);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}
		});

	}
	
	public void configureUI(RestrictionInformationResponse restrictionInformation) {
		boolean isAnonymous = isAnonymous();
		boolean hasAdministrativeAccess = false;
		
		jiraFlagLink = null;
		if (!isAnonymous) {
			hasAdministrativeAccess = canChangePermissions;
			jiraFlagLink = getJiraFlagUrl();
		}
		RestrictionLevel restrictionLevel = restrictionInformation.getRestrictionLevel();
		
		switch (restrictionLevel) {
			case OPEN:
				view.showNoRestrictionsUI();
				break;
			case RESTRICTED_BY_TERMS_OF_USE:
			case CONTROLLED_BY_ACT:
				view.showControlledUseUI();
				if (restrictionInformation.getHasUnmetAccessRequirement())
					view.showUnmetRequirementsIcon();
				else
					view.showMetRequirementsIcon();
				break;
			default:
				throw new IllegalArgumentException(restrictionLevel.toString());
		}
		
		//show the info link if there are any restrictions, or if we are supposed to show the flag link (to allow people to flag or  admin to "change" the data access level).
		boolean isChangeLink = restrictionLevel==RestrictionLevel.OPEN && hasAdministrativeAccess;
		boolean isRestricted = restrictionLevel!=RestrictionLevel.OPEN;
		if ((isChangeLink && showChangeLink) || isRestricted) {
			if (isChangeLink)
				view.showChangeLink();
			else {
				if (restrictionInformation.getHasUnmetAccessRequirement()) {
					view.showShowUnmetLink();
				} else {
					view.showShowLink();
				}
			}
		}
		
		if (showFlagLink) {
			if (isAnonymous)
				view.showAnonymousFlagUI();
			else
				view.showFlagUI();
		}
	}
	
	
	@Override
	public void flagData() {
		view.open(jiraFlagLink);
	}
	
	@Override
	public void imposeRestrictionOkClicked() {
		Boolean isYesSelected = view.isYesHumanDataRadioSelected();
		Boolean isNoSelected = view.isNoHumanDataRadioSelected();
		
		if (isNoSelected != null && isNoSelected) {
			// no-op, just hide the dialog
			imposeRestrictionCancelClicked();
		} else if ((isYesSelected == null || !isYesSelected) && (isNoSelected == null || !isNoSelected)) {
			//no selection
			view.showErrorMessage("You must make a selection before continuing.");
		} else {
			view.showLoading();
			view.setImposeRestrictionModalVisible(false);
			
			dataAccessClient.createLockAccessRequirement(entity.getId(), new AsyncCallback<Void>(){
				@Override
				public void onSuccess(Void result) {
					view.showInfo("Successfully imposed restriction");
					loadRestrictionInformation();
				}
				@Override
				public void onFailure(Throwable caught) {
					synAlert.handleException(caught);
				}
			});
		}
	}

	@Override
	public void imposeRestrictionCancelClicked() {
		view.setImposeRestrictionModalVisible(false);
	}

	@Override
	public void anonymousFlagModalOkClicked() {
		globalApplicationState.getPlaceChanger().goTo(new LoginPlace(ClientProperties.DEFAULT_PLACE_TOKEN));
	}

	@Override
	public void reportIssueClicked() {
		view.showFlagModal();
	}
	
	@Override
	public void anonymousReportIssueClicked() {
		view.showAnonymousFlagModal();
	}
	
	
	@Override
	public void notHumanDataClicked() {
		//and show the warning message
		view.setNotSensitiveHumanDataMessageVisible(true);		
	}
	
	@Override
	public void yesHumanDataClicked() {
		//and hide the warning message
		view.setNotSensitiveHumanDataMessageVisible(false);
	}
	
	@Override
	public void linkClicked() {
		view.open("#!AccessRequirements:" + AccessRequirementsPlace.ID_PARAM + "=" + entity.getId() + "&" + AccessRequirementsPlace.TYPE_PARAM + "=" + RestrictableObjectType.ENTITY.toString());
	}
	
	@Override
	public void changeClicked() {
		isACTMemberAsyncHandler.isACTActionAvailable(new CallbackP<Boolean>() {
			@Override
			public void invoke(Boolean isACT) {
				if (isACT) {
					// go to access requirements place where they can modify access requirements
					linkClicked();
				} else {
					view.showVerifyDataSensitiveDialog();
				}
			}
		});
	}
}