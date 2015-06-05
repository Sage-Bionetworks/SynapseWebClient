package org.sagebionetworks.web.client.presenter;

import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.TeamMembershipStatus;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.view.TeamView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.team.controller.TeamDeleteModalWidget;
import org.sagebionetworks.web.client.widget.team.controller.TeamEditModalWidget;
import org.sagebionetworks.web.client.widget.team.controller.TeamLeaveModalWidget;
import org.sagebionetworks.web.shared.TeamBundle;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class TeamPresenter extends AbstractActivity implements TeamView.Presenter, Presenter<org.sagebionetworks.web.client.place.Team> {
		
	private org.sagebionetworks.web.client.place.Team place;
	private TeamView view;
	private SynapseClientAsync synapseClient;
	private AuthenticationController authenticationController;
	private GlobalApplicationState globalApplicationState;
	private JSONObjectAdapter jsonObjectAdapter;
	private Team team;
	private TeamMembershipStatus teamMembershipStatus;
	private SynapseAlert synAlert;
	private TeamLeaveModalWidget leaveTeamWidget;
	private TeamDeleteModalWidget deleteTeamWidget;
	private TeamEditModalWidget editTeamWidget;
	
	@Inject
	public TeamPresenter(TeamView view,
			AuthenticationController authenticationController,
			GlobalApplicationState globalApplicationState,
			SynapseClientAsync synapseClient,
			JSONObjectAdapter jsonObjectAdapter,
			SynapseAlert synAlert, TeamLeaveModalWidget leaveTeamWidget,
			TeamDeleteModalWidget deleteTeamWidget,
			TeamEditModalWidget editTeamWidget) {
		this.view = view;
		view.setPresenter(this);
		this.authenticationController = authenticationController;
		this.globalApplicationState = globalApplicationState;
		this.synapseClient = synapseClient;
		this.jsonObjectAdapter = jsonObjectAdapter;
		this.synAlert = synAlert;
		this.leaveTeamWidget = leaveTeamWidget;
		this.deleteTeamWidget = deleteTeamWidget;
		this.editTeamWidget = editTeamWidget;
		view.setPresenter(this);
		view.setSynAlertWidget(synAlert.asWidget());
		view.setLeaveTeamWidget(leaveTeamWidget.asWidget());
		view.setDeleteTeamWidget(deleteTeamWidget.asWidget());
		view.setEditTeamWidget(editTeamWidget.asWidget());
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		// Install the view
		panel.setWidget(view);
	}

	@Override
	public void setPlace(org.sagebionetworks.web.client.place.Team place) {
		this.place = place;
		this.view.setPresenter(this);
		this.view.clear();
		showView(place);
	}
	
	@Override
    public String mayStop() {
        view.clear();
        return null;
    }
	

	@Override
	public void goTo(Place place) {
		globalApplicationState.getPlaceChanger().goTo(place);
	}
	
	private void refresh() {
		refresh(team.getId());
	}
	
	@Override
	public void refresh(final String teamId) {
		synAlert.clear();
		synapseClient.getTeamBundle(authenticationController.getCurrentUserPrincipalId(), teamId, authenticationController.isLoggedIn(), new AsyncCallback<TeamBundle>() {
			@Override
			public void onSuccess(TeamBundle result) {
				team = result.getTeam();
				if (result.getTeamMembershipStatus() != null)
					teamMembershipStatus = result.getTeamMembershipStatus();
				else
					teamMembershipStatus = null;
				boolean isAdmin = result.isUserAdmin();
				view.configure(team, isAdmin, teamMembershipStatus, result.getTotalMemberCount());
			}
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}
		});
	}
	private void showView(org.sagebionetworks.web.client.place.Team place) {
		String teamId = place.getTeamId();
		refresh(teamId);
	}

	@Override
	public void deleteTeam() {
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
	public void leaveTeam() {
		synAlert.clear();
//		String userId = authenticationController.getCurrentUserPrincipalId();
//		synapseClient.deleteTeamMember(userId, userId, team.getId(), new AsyncCallback<Void>() {
//			@Override
//			public void onSuccess(Void result) {
//				view.showInfo(DisplayConstants.LEAVE_TEAM_SUCCESS, "");
//				refresh();
//			}
//			@Override
//			public void onFailure(Throwable caught) {
//				synAlert.handleException(caught);
//			}
//		});
		leaveTeamWidget.setRefreshCallback(new Callback() {
			@Override
			public void invoke() {
				refresh();
			}
		});
		leaveTeamWidget.setTeam(team);
		leaveTeamWidget.showDialog();
	}

	@Override
	public void updateTeamInfo(String name, String description, boolean canPublicJoin, String fileHandleId) {
		synAlert.clear();
		if (name == null || name.trim().length() == 0) {
			view.showErrorMessage(DisplayConstants.ERROR_NAME_MUST_BE_DEFINED);
		}
		else {
			team.setName(name);
			team.setDescription(description);
			team.setCanPublicJoin(canPublicJoin);
			team.setIcon(fileHandleId);
			synapseClient.updateTeam(team, new AsyncCallback<Team>() {
				@Override
				public void onSuccess(Team result) {
					view.showInfo(DisplayConstants.UPDATE_TEAM_SUCCESS, "");
					refresh();
				}
				@Override
				public void onFailure(Throwable caught) {
					synAlert.handleException(caught);
				}
			});
		}
	}

	@Override
	public void showEditModal() {
		synAlert.clear();
		editTeamWidget.setRefreshCallback(new Callback() {
			@Override
			public void invoke() {
				refresh();
			}
		});
		editTeamWidget.setTeam(team);
		editTeamWidget.setVisible(true);
	}

	@Override
	public void showDeleteModal() {
		synAlert.clear();
		deleteTeamWidget.setRefreshCallback(new Callback() {
			@Override
			public void invoke() {
				refresh();
			}
		});
		deleteTeamWidget.setTeam(team);
		deleteTeamWidget.showDialog();
	}

	@Override
	public void showLeaveModal() {
		synAlert.clear();
		leaveTeamWidget.setRefreshCallback(new Callback() {
			@Override
			public void invoke() {
				refresh();
			}
		});
		leaveTeamWidget.setTeam(team);
		leaveTeamWidget.showDialog();		
	}
}

