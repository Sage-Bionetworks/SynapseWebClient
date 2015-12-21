package org.sagebionetworks.web.client.presenter;

import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.TeamMembershipStatus;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.view.TeamView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.team.InviteWidget;
import org.sagebionetworks.web.client.widget.team.JoinTeamWidget;
import org.sagebionetworks.web.client.widget.team.MemberListWidget;
import org.sagebionetworks.web.client.widget.team.OpenMembershipRequestsWidget;
import org.sagebionetworks.web.client.widget.team.OpenUserInvitationsWidget;
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
		
	private TeamView view;
	private SynapseClientAsync synapseClient;
	private AuthenticationController authenticationController;
	private GlobalApplicationState globalApplicationState;
	private Team team;
	private SynapseAlert synAlert;
	private TeamLeaveModalWidget leaveTeamWidget;
	private TeamDeleteModalWidget deleteTeamWidget;
	private TeamEditModalWidget editTeamWidget;
	private InviteWidget inviteWidget;
	private JoinTeamWidget joinTeamWidget;
	private MemberListWidget memberListWidget;
	private OpenMembershipRequestsWidget openMembershipRequestsWidget;
	private OpenUserInvitationsWidget openUserInvitationsWidget;
	
	@Inject
	public TeamPresenter(TeamView view,
			AuthenticationController authenticationController,
			GlobalApplicationState globalApplicationState,
			SynapseClientAsync synapseClient,
			SynapseAlert synAlert, TeamLeaveModalWidget leaveTeamWidget,
			TeamDeleteModalWidget deleteTeamWidget,
			TeamEditModalWidget editTeamWidget, InviteWidget inviteWidget,
			JoinTeamWidget joinTeamWidget,  
			MemberListWidget memberListWidget, 
			OpenMembershipRequestsWidget openMembershipRequestsWidget,
			OpenUserInvitationsWidget openUserInvitationsWidget) {
		this.view = view;
		this.authenticationController = authenticationController;
		this.globalApplicationState = globalApplicationState;
		this.synapseClient = synapseClient;
		this.synAlert = synAlert;
		this.leaveTeamWidget = leaveTeamWidget;
		this.deleteTeamWidget = deleteTeamWidget;
		this.editTeamWidget = editTeamWidget;
		this.inviteWidget = inviteWidget;
		this.joinTeamWidget = joinTeamWidget;
		this.memberListWidget = memberListWidget;
		this.openMembershipRequestsWidget = openMembershipRequestsWidget;
		this.openUserInvitationsWidget = openUserInvitationsWidget;
		view.setPresenter(this);
		view.setSynAlertWidget(synAlert.asWidget());
		view.setLeaveTeamWidget(leaveTeamWidget.asWidget());
		view.setDeleteTeamWidget(deleteTeamWidget.asWidget());
		view.setEditTeamWidget(editTeamWidget.asWidget());
		view.setInviteMemberWidget(inviteWidget.asWidget());
		view.setJoinTeamWidget(joinTeamWidget.asWidget());
		view.setOpenMembershipRequestWidget(memberListWidget.asWidget());
		view.setOpenUserInvitationsWidget(openMembershipRequestsWidget.asWidget());
		view.setMemberListWidget(openUserInvitationsWidget.asWidget());
		Callback refreshCallback = new Callback() {
			@Override
			public void invoke() {
				refresh();
			}
		};
		leaveTeamWidget.setRefreshCallback(refreshCallback);
		editTeamWidget.setRefreshCallback(refreshCallback);
		deleteTeamWidget.setRefreshCallback(refreshCallback);
		inviteWidget.setRefreshCallback(refreshCallback);
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		// Install the view
		panel.setWidget(view);
	}

	@Override
	public void setPlace(org.sagebionetworks.web.client.place.Team place) {
		this.view.setPresenter(this);
		this.view.clear();
		clear();
		showView(place);
	}
	
	@Override
	public void clear() {
		memberListWidget.clear();
		joinTeamWidget.clear();
		openMembershipRequestsWidget.clear();
		openUserInvitationsWidget.clear();
		view.clear();
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
		clear();
		synAlert.clear();
		synapseClient.getTeamBundle(authenticationController.getCurrentUserPrincipalId(), teamId, authenticationController.isLoggedIn(), new AsyncCallback<TeamBundle>() {
			@Override
			public void onSuccess(TeamBundle result) {
				view.clear();
				team = result.getTeam();
				TeamMembershipStatus teamMembershipStatus = result.getTeamMembershipStatus();
				boolean isAdmin = result.isUserAdmin();
				Callback refreshCallback = new Callback() {
					@Override
					public void invoke() {
						refresh(teamId);
					}
				};
				boolean canPublicJoin = team.getCanPublicJoin() == null ? false : team.getCanPublicJoin();
				view.setPublicJoinVisible(canPublicJoin);
				view.setTotalMemberCount(result.getTotalMemberCount().toString());
				view.setMediaObjectPanel(team);
				view.setTeamEmailAddress(getTeamEmail(team.getName()));
				memberListWidget.configure(teamId, isAdmin, refreshCallback);				

				if (teamMembershipStatus != null) {
					if (!teamMembershipStatus.getIsMember())
						//not a member, add Join widget
						joinTeamWidget.configure(teamId, false, teamMembershipStatus,
								refreshCallback, null, null, null, null, false);
					else {
						view.showMemberMenuItems();
						if (isAdmin) {
							openMembershipRequestsWidget.configure(teamId, refreshCallback);
							openUserInvitationsWidget.configure(teamId, refreshCallback);
							view.showAdminMenuItems();
						}
					}
				}
			}
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}
		});
	}
	
	public String getTeamEmail(String teamName) {
		if (authenticationController.isLoggedIn()) {
			//strip out any non-word character.  Not a (letter, number, underscore)
			return teamName.replaceAll("\\W", "") + "@synapse.org";
		} else {
			return "";
		}
	}
	
	private void showView(org.sagebionetworks.web.client.place.Team place) {
		String teamId = place.getTeamId();
		refresh(teamId);
	}
	
	@Override
	public void showInviteModal() {
		synAlert.clear();
		inviteWidget.configure(team);
		inviteWidget.show();
	}

	@Override
	public void showEditModal() {
		synAlert.clear();
		editTeamWidget.configureAndShow(team);
	}

	@Override
	public void showDeleteModal() {
		synAlert.clear();
		deleteTeamWidget.configure(team);
		deleteTeamWidget.showDialog();
	}

	@Override
	public void showLeaveModal() {
		synAlert.clear();
		leaveTeamWidget.configure(team);
		leaveTeamWidget.showDialog();		
	}
	
	//testing only
	public void setTeam(Team team) {
		this.team = team;
	}
}

