package org.sagebionetworks.web.client.widget.team.controller;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;

import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.place.Synapse.ProfileArea;
import org.sagebionetworks.web.client.security.AuthenticationController;
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
	AuthenticationController authController;
	Callback refreshCallback;
	TeamDeleteModalWidgetView view;
	Team team;
	
	@Inject
	public TeamDeleteModalWidget(SynapseAlert synAlert, 
			SynapseClientAsync synapseClient,
			GlobalApplicationState globalApplicationState, 
			TeamDeleteModalWidgetView view,
			AuthenticationController authController) {
		this.globalApplicationState = globalApplicationState;
		this.synAlert = synAlert;
		this.synapseClient = synapseClient;
		fixServiceEntryPoint(synapseClient);
		this.view = view;
		this.authController = authController;
		this.view.setPresenter(this);
		this.view.setSynAlertWidget(synAlert.asWidget());
	}
	
	@Override
	public void setRefreshCallback(Callback refreshCallback) {
		this.refreshCallback = refreshCallback;
	}
	
	@Override
	public void onConfirm() {
		synAlert.clear();
		synapseClient.deleteTeam(team.getId(), new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				view.showInfo(DisplayConstants.DELETE_TEAM_SUCCESS);
				// global app state gotoLastPlace() behavior can be unpredictable (because the last place cookie value may be set in a different window).
				// go to the user dashboard, into the Teams area. 
				view.hide();
				Profile gotoPlace = new Profile(authController.getCurrentUserPrincipalId(), ProfileArea.TEAMS);
				globalApplicationState.getPlaceChanger().goTo(gotoPlace);
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

	@Override
	public void configure(Team team) {
		this.team = team;
	}

}
