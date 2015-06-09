package org.sagebionetworks.web.client.widget.team.controller;

import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TeamDeleteModalWidget implements IsWidget, TeamDeleteModalWidgetView.Presenter {

	SynapseAlert synAlert;
	SynapseClientAsync synapseClient;
	GlobalApplicationState globalApplicationState;
	Callback refreshCallback;
	TeamDeleteModalWidgetView view;
	Team team;
	
	@Inject
	public TeamDeleteModalWidget(SynapseAlert synAlert, SynapseClientAsync synapseClient,
			GlobalApplicationState globalApplicationState, TeamDeleteModalWidgetView view) {
		this.globalApplicationState = globalApplicationState;
		this.synAlert = synAlert;
		this.synapseClient = synapseClient;
		this.view = view;
		this.view.setPresenter(this);
		this.view.setSynAlertWidget(synAlert.asWidget());
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
		synapseClient.deleteTeam(team.getId(), new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				//go home
				view.showInfo(DisplayConstants.DELETE_TEAM_SUCCESS, "");
				globalApplicationState.gotoLastPlace();
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

	@Override
	public void showDialog() {
		synAlert.clear();
		view.show();
	}

}
