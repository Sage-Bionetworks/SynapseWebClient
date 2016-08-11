package org.sagebionetworks.web.client.widget.team.controller;

import java.util.Set;

import org.sagebionetworks.repo.model.AccessControlList;
import org.sagebionetworks.repo.model.ResourceAccess;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.util.ModelConstants;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.profile.UserProfileEditorWidgetImpl;
import org.sagebionetworks.web.client.widget.upload.FileHandleUploadWidget;
import org.sagebionetworks.web.client.widget.upload.FileUpload;
import org.sagebionetworks.web.client.widget.upload.ImageFileValidator;
import org.sagebionetworks.web.shared.WebConstants;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;


public class TeamEditModalWidget implements IsWidget, TeamEditModalWidgetView.Presenter {

	TeamEditModalWidgetView view;
	SynapseAlert synAlert;
	Callback refreshCallback;
	Team team;
	AccessControlList teamACL;
	SynapseClientAsync synapseClient;
	FileHandleUploadWidget uploader;
	String uploadedFileHandleId;
	String baseImageURL;
	Long authenticatedUserGroupId;
	AuthenticationController authController;
	
	@Inject
	public TeamEditModalWidget(
			SynapseAlert synAlert,
			final TeamEditModalWidgetView view, 
			SynapseClientAsync synapseClient,
			final FileHandleUploadWidget uploader, 
			SynapseJSNIUtils jsniUtils,
			AuthenticationController authenticationController,
			GlobalApplicationState globalApplicationState) {
		this.synAlert = synAlert;
		this.view = view;
		this.synapseClient = synapseClient;
		this.uploader = uploader;
		this.baseImageURL = jsniUtils.getBaseFileHandleUrl();
		this.authController = authenticationController;
		authenticatedUserGroupId = Long.parseLong(globalApplicationState.getSynapseProperty(WebConstants.AUTHENTICATED_ACL_PRINCIPAL_ID));
		uploader.configure("Browse...", new CallbackP<FileUpload>() {
			@Override
			public void invoke(FileUpload fileUpload) {
				uploader.setUploadedFileText(fileUpload.getFileMeta().getFileName());
				view.hideLoading();
				uploadedFileHandleId = fileUpload.getFileHandleId();
				view.setImageURL(DisplayUtils.createRawFileHandleUrl(baseImageURL, uploadedFileHandleId, authController.getCurrentXsrfToken()));
			}
		});
		ImageFileValidator imageValidator = new ImageFileValidator();
		imageValidator.setMaxSize(UserProfileEditorWidgetImpl.MAX_IMAGE_SIZE);
		uploader.setValidation(imageValidator);
		uploader.setUploadingCallback(new Callback() {
			@Override
			public void invoke() {
				uploader.setUploadedFileText("");
				view.showLoading();
			}
		});
		view.setUploadWidget(uploader.asWidget());
		view.setAlertWidget(synAlert.asWidget());
		view.setPresenter(this);
	}
	
	@Override
	public void setRefreshCallback(Callback refreshCallback) {
		this.refreshCallback = refreshCallback;
	}
	
	@Override
	public void onConfirm() {
		String newName = view.getName();
		String newDescription = view.getDescription();
		boolean canPublicJoin = view.getPublicJoin();
		if (newName == null || newName.trim().length() == 0) {
			synAlert.showError("You must provide a name.");
		}
		else {
			team.setName(newName);
			team.setDescription(newDescription);
			team.setCanPublicJoin(canPublicJoin);
			if (uploadedFileHandleId != null)
				team.setIcon(uploadedFileHandleId);
			updateACLFromView();
			synapseClient.updateTeam(team, teamACL, new AsyncCallback<Team>() {
				@Override
				public void onSuccess(Team result) {
					view.showInfo(DisplayConstants.UPDATE_TEAM_SUCCESS, "");
					if (refreshCallback != null)
						refreshCallback.invoke();
					view.hide();
				}
				@Override
				public void onFailure(Throwable caught) {
					uploader.reset();
					view.hideLoading();
					synAlert.handleException(caught);
				}
			});
		}
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
	
	// resets view including widgets with ui, then shows the modal
	private void show() {
		uploader.reset();
		synAlert.clear();
		view.hideLoading();
		view.clear();
		view.configure(team);
		if (team.getIcon() != null)
			view.setImageURL(DisplayUtils.createRawFileHandleUrl(baseImageURL, team.getIcon(), authController.getCurrentXsrfToken()));
		else
			view.setDefaultIconVisible();
		
		updateViewFromACL();
		view.show();
	}
	
	@Override
	public void hide() {
		view.hide();
	}
	
	public void configureAndShow(Team team) {
		uploadedFileHandleId = null;
		this.team = team;
		//get the messaging parameter, and show
		synapseClient.getTeamAcl(team.getId(), new AsyncCallback<AccessControlList>() {
			public void onSuccess(AccessControlList result) {
				teamACL = result;
				show();
			};
			
			@Override
			public void onFailure(Throwable caught) {
				synAlert.showError(caught.getMessage());
			}
		});
	}
	public void updateViewFromACL() {
		//look for authenticated users principal ID from resource access set.
		ResourceAccess ra = getResourceAccess(authenticatedUserGroupId);
		if (ra != null) {
			view.setAuthenticatedUsersCanSendMessageToTeam(ModelConstants.TEAM_MESSENGER_PERMISSIONS.equals(ra.getAccessType()));
		} else {
			view.setAuthenticatedUsersCanSendMessageToTeam(false);
		}
	}
	
	public void updateACLFromView() {
		if (view.canAuthenticatedUsersSendMessageToTeam()) {
			//add authenticated users principal id to the ACL (team messenger permission)
			//add/update
			ResourceAccess ra = getResourceAccess(authenticatedUserGroupId);
			if (ra == null) {
				ra = new ResourceAccess();
				ra.setPrincipalId(authenticatedUserGroupId);
			}
			ra.setAccessType(ModelConstants.TEAM_MESSENGER_PERMISSIONS);
			teamACL.getResourceAccess().add(ra);
		} else {
			//remove authenticated users (if exists and is set to team messenger permission)
			ResourceAccess ra = getResourceAccess(authenticatedUserGroupId);
			if (ra != null && ModelConstants.TEAM_MESSENGER_PERMISSIONS.equals(ra.getAccessType())) {
				teamACL.getResourceAccess().remove(ra);
			}
		}
	}
	
	private ResourceAccess getResourceAccess(Long principalId) {
		Set<ResourceAccess> resourceAccessSet = teamACL.getResourceAccess();
		for (ResourceAccess ra : resourceAccessSet) {
			if(principalId.equals(ra.getPrincipalId())) {
				//found
				return ra;
			}
		}
		return null;
	}
}
