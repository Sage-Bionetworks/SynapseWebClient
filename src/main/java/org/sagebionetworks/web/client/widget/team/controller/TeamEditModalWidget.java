package org.sagebionetworks.web.client.widget.team.controller;

import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.upload.FileHandleUploadWidget;
import org.sagebionetworks.web.client.widget.upload.FileUpload;
import org.sagebionetworks.web.client.widget.upload.ImageFileValidator;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;


public class TeamEditModalWidget implements IsWidget, TeamEditModalWidgetView.Presenter {

	TeamEditModalWidgetView view;
	SynapseAlert synAlert;
	Callback refreshCallback;
	Team team;
	SynapseClientAsync synapseClient;
	FileHandleUploadWidget uploader;
	String uploadedFileHandleId;
	String baseImageURL;
	
	@Inject
	public TeamEditModalWidget(SynapseAlert synAlert,
			final TeamEditModalWidgetView view, SynapseClientAsync synapseClient,
			final FileHandleUploadWidget uploader, final SynapseJSNIUtils jsniUtils,
			final AuthenticationController authenticationController) {
		this.synAlert = synAlert;
		this.view = view;
		this.synapseClient = synapseClient;
		this.uploader = uploader;
		this.baseImageURL = jsniUtils.getBaseFileHandleUrl();
		uploader.configure("Browse...", new CallbackP<FileUpload>() {
			@Override
			public void invoke(FileUpload fileUpload) {
				uploader.setUploadedFileText(fileUpload.getFileMeta().getFileName());
				view.hideLoading();
				uploadedFileHandleId = fileUpload.getFileHandleId();
				view.setImageURL(baseImageURL + "?rawFileHandleId=" + uploadedFileHandleId);
			}
		});
		uploader.setValidation(new ImageFileValidator());
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
			synapseClient.updateTeam(team, new AsyncCallback<Team>() {
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
	@Override
	public void show() {
		uploader.reset();
		synAlert.clear();
		view.hideLoading();
		view.clear();
		view.configure(team);
		if (team.getIcon() != null)
			view.setImageURL(baseImageURL + "?rawFileHandleId=" + team.getIcon());
		else
			view.setDefaultIconVisible();
		view.show();
	}
	
	@Override
	public void hide() {
		view.hide();
	}
	
	@Override
	public void configure(Team team) {
		uploadedFileHandleId = null;
		this.team = team;
	}

}
