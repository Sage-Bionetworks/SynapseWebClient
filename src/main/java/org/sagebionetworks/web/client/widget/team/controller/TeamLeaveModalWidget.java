package org.sagebionetworks.web.client.widget.team.controller;

import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TeamLeaveModalWidget implements IsWidget, TeamLeaveModalWidgetView.Presenter {

	SynapseAlert synAlert;
	SynapseJavascriptClient jsClient;
	AuthenticationController authenticationController;
	Callback refreshCallback;
	TeamLeaveModalWidgetView view;
	Team team;
	
	@Inject
	public TeamLeaveModalWidget(SynapseAlert synAlert, SynapseJavascriptClient jsClient,
			AuthenticationController authenticationController, TeamLeaveModalWidgetView view) {
		this.authenticationController = authenticationController;
		this.synAlert = synAlert;
		this.jsClient = jsClient;
		this.view = view;
		view.setPresenter(this);
		view.setSynAlertWidget(synAlert.asWidget());
	}
	
	@Override
	public void setRefreshCallback(Callback refreshCallback) {
		this.refreshCallback = refreshCallback;
	}
	
	@Override
	public void onConfirm() {
		synAlert.clear();
		String userId = authenticationController.getCurrentUserPrincipalId();
		jsClient.deleteTeamMember(team.getId(), userId, new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				view.showInfo(DisplayConstants.LEAVE_TEAM_SUCCESS);
				if (refreshCallback != null)
					refreshCallback.invoke();
				view.hide();
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
		synAlert.clear();
		view.show();
	}

	@Override
	public void configure(Team team) {
		this.team = team;
	}

}
