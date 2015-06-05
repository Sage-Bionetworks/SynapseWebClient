package org.sagebionetworks.web.client.widget.team.controller;

import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TeamLeaveModalWidget implements IsWidget, TeamLeaveModalWidgetView.Presenter {

	SynapseAlert synAlert;
	SynapseClientAsync synapseClient;
	AuthenticationController authenticationController;
	Callback refreshCallback;
	TeamLeaveModalWidgetView view;
	Team team;
	
	@Inject
	public TeamLeaveModalWidget(SynapseAlert synAlert, SynapseClientAsync synapseClient,
			AuthenticationController authenticationController, TeamLeaveModalWidgetView view) {
		this.authenticationController = authenticationController;
		this.synAlert = synAlert;
		this.synapseClient = synapseClient;
		this.view = view;
		view.setPresenter(this);
		view.setSynAlertWidget(synAlert.asWidget());
	}
	
	@Override
	public void setRefreshCallback(Callback refreshCallback) {
		this.refreshCallback = refreshCallback;
	}
	
	@Override
	public void setTeam(Team team) {
		this.team = team;
	}
	
	@Override
	public void onConfirm() {
		synAlert.clear();
		String userId = authenticationController.getCurrentUserPrincipalId();
		synapseClient.deleteTeamMember(userId, userId, team.getId(), new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				DisplayUtils.showInfo(DisplayConstants.LEAVE_TEAM_SUCCESS, "");
				if (refreshCallback != null)
					refreshCallback.invoke();
			}
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}
		});
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	public void showDialog() {
		view.show();
	}

}
