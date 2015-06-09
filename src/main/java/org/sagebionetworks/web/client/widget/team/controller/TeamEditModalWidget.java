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
		uploader.configure("Browse", new CallbackP<FileUpload>() {
			@Override
			public void invoke(FileUpload fileUpload) {
				uploader.setUploadedFileText(fileUpload.getFileMeta().getFileName());
				view.setLoading(false);
				uploadedFileHandleId = fileUpload.getFileHandleId();
				view.setImageURL(baseImageURL + "?rawFileHandleId=" + uploadedFileHandleId);
			}
		});
		uploader.setValidation(new ImageFileValidator());
		uploader.setUploadingCallback(new Callback() {
			@Override
			public void invoke() {
				uploader.setUploadedFileText("");
				view.setLoading(true);
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
	public void onConfirm(String newName, String newDescription, boolean canPublicJoin) {
		if (newName != null && newName.equals(team.getName()) && newDescription != null && newDescription.equals(team.getDescription())
				&& team.getCanPublicJoin() == canPublicJoin && (uploadedFileHandleId == null || team.getIcon().equals(uploadedFileHandleId))) {
			synAlert.showError("No changes were provided");
		} else {
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
						view.setVisible(false);
					}
					@Override
					public void onFailure(Throwable caught) {
						uploader.reset();
						view.setLoading(false);
						synAlert.handleException(caught);
					}
				});
			}
		}
	}
	
	@Override
	public void setTeam(Team team) {
		this.team = team;
		view.setTeam(team);
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
	
	@Override
	public void clear() {
		synAlert.clear();
		uploader.reset();
		uploadedFileHandleId = null;
	}

	@Override
	public void setVisible(boolean isVisible) {
		clear();
		if (isVisible) {
			if (team.getIcon() != null)
				view.setImageURL(baseImageURL + "?rawFileHandleId=" + team.getIcon());
			else
				view.setDefaultIconVisible();
		}
		view.setVisible(isVisible);
	}

}
