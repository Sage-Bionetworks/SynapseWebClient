package org.sagebionetworks.web.client.presenter;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;
import org.sagebionetworks.repo.model.RestrictableObjectType;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.TeamMemberTypeFilterOptions;
import org.sagebionetworks.repo.model.TeamMembershipStatus;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.AccessRequirementsPlace;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.view.TeamView;
import org.sagebionetworks.web.client.widget.asynch.IsACTMemberAsyncHandler;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.googlemap.GoogleMap;
import org.sagebionetworks.web.client.widget.team.InviteWidget;
import org.sagebionetworks.web.client.widget.team.JoinTeamWidget;
import org.sagebionetworks.web.client.widget.team.MemberListWidget;
import org.sagebionetworks.web.client.widget.team.OpenMembershipRequestsWidget;
import org.sagebionetworks.web.client.widget.team.OpenUserInvitationsWidget;
import org.sagebionetworks.web.client.widget.team.controller.TeamDeleteModalWidget;
import org.sagebionetworks.web.client.widget.team.controller.TeamEditModalWidget;
import org.sagebionetworks.web.client.widget.team.controller.TeamLeaveModalWidget;
import org.sagebionetworks.web.client.widget.team.controller.TeamProjectsModalWidget;
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
	private MemberListWidget memberListWidget, managerListWidget;
	private OpenMembershipRequestsWidget openMembershipRequestsWidget;
	private OpenUserInvitationsWidget openUserInvitationsWidget;
	private GoogleMap map;
	private String currentTeamId;
	private IsACTMemberAsyncHandler isACTMemberAsyncHandler;
	private TeamProjectsModalWidget teamProjectsModalWidget;
	private PortalGinInjector ginInjector;
	Callback refreshCallback = () -> {
		refresh();
	};

	@Inject
	public TeamPresenter(TeamView view, AuthenticationController authenticationController, GlobalApplicationState globalApplicationState, SynapseClientAsync synapseClient, SynapseAlert synAlert, InviteWidget inviteWidget, JoinTeamWidget joinTeamWidget, MemberListWidget managerListWidget, MemberListWidget memberListWidget, OpenMembershipRequestsWidget openMembershipRequestsWidget, OpenUserInvitationsWidget openUserInvitationsWidget, GoogleMap map, CookieProvider cookies, IsACTMemberAsyncHandler isACTMemberAsyncHandler, PortalGinInjector ginInjector) {
		this.view = view;
		this.ginInjector = ginInjector;
		this.authenticationController = authenticationController;
		this.globalApplicationState = globalApplicationState;
		this.synapseClient = synapseClient;
		fixServiceEntryPoint(synapseClient);
		this.synAlert = synAlert;
		this.inviteWidget = inviteWidget;
		this.joinTeamWidget = joinTeamWidget;
		this.managerListWidget = managerListWidget;
		this.memberListWidget = memberListWidget;
		this.openMembershipRequestsWidget = openMembershipRequestsWidget;
		this.openUserInvitationsWidget = openUserInvitationsWidget;
		this.map = map;
		this.isACTMemberAsyncHandler = isACTMemberAsyncHandler;
		view.setPresenter(this);
		view.setSynAlertWidget(synAlert.asWidget());
		view.setInviteMemberWidget(inviteWidget.asWidget());
		view.setJoinTeamWidget(joinTeamWidget.asWidget());
		view.setOpenMembershipRequestWidget(openUserInvitationsWidget.asWidget());
		view.setOpenUserInvitationsWidget(openMembershipRequestsWidget.asWidget());
		view.setManagerListWidget(managerListWidget.asWidget());
		view.setMemberListWidget(memberListWidget.asWidget());
		view.setMap(map.asWidget());
		view.setShowMapVisible(DisplayUtils.isInTestWebsite(cookies));
		inviteWidget.setRefreshCallback(refreshCallback);
	}

	private TeamDeleteModalWidget getTeamDeleteModalWidget() {
		if (deleteTeamWidget == null) {
			deleteTeamWidget = ginInjector.getTeamDeleteModalWidget();
			deleteTeamWidget.setRefreshCallback(refreshCallback);
			view.addWidgets(deleteTeamWidget.asWidget());
		}
		return deleteTeamWidget;
	}

	private TeamLeaveModalWidget getTeamLeaveModalWidget() {
		if (leaveTeamWidget == null) {
			leaveTeamWidget = ginInjector.getTeamLeaveModalWidget();
			leaveTeamWidget.setRefreshCallback(refreshCallback);
			view.addWidgets(leaveTeamWidget.asWidget());
		}
		return leaveTeamWidget;
	}

	private TeamEditModalWidget getTeamEditModalWidget() {
		if (editTeamWidget == null) {
			editTeamWidget = ginInjector.getTeamEditModalWidget();
			editTeamWidget.setRefreshCallback(refreshCallback);
			view.addWidgets(editTeamWidget.asWidget());
		}
		return editTeamWidget;
	}

	private TeamProjectsModalWidget getTeamProjectsModalWidget() {
		if (teamProjectsModalWidget == null) {
			teamProjectsModalWidget = ginInjector.getTeamProjectsModalWidget();
			view.addWidgets(teamProjectsModalWidget.asWidget());
		}
		return teamProjectsModalWidget;
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
		openMembershipRequestsWidget.setVisible(false);
		openUserInvitationsWidget.setVisible(false);
		refresh(currentTeamId);
		refreshOpenMembershipRequests();
		refreshOpenUserInvitations();
	}

	@Override
	public void refresh(final String teamId) {
		this.currentTeamId = teamId;
		clear();
		synAlert.clear();
		isACTMemberAsyncHandler.isACTActionAvailable(new CallbackP<Boolean>() {
			@Override
			public void invoke(Boolean isACT) {
				view.setManageAccessVisible(isACT);
				if (isACT) {
					view.setCommandsVisible(true);
				}
			}
		});
		synapseClient.getTeamBundle(authenticationController.getCurrentUserPrincipalId(), teamId, authenticationController.isLoggedIn(), new AsyncCallback<TeamBundle>() {
			@Override
			public void onSuccess(TeamBundle result) {
				team = result.getTeam();
				TeamMembershipStatus teamMembershipStatus = result.getTeamMembershipStatus();
				boolean isAdmin = result.isUserAdmin();
				Callback refreshCallback = () -> {
					refresh(teamId);
				};
				boolean canPublicJoin = team.getCanPublicJoin() == null ? false : team.getCanPublicJoin();
				view.setPublicJoinVisible(canPublicJoin);
				view.setTeam(team, teamMembershipStatus);
				managerListWidget.configure(teamId, isAdmin, TeamMemberTypeFilterOptions.ADMIN, refreshCallback);
				memberListWidget.configure(teamId, isAdmin, TeamMemberTypeFilterOptions.MEMBER, refreshCallback);
				openMembershipRequestsWidget.setVisible(isAdmin);

				if (teamMembershipStatus == null || !teamMembershipStatus.getIsMember()) {
					// not a member, add Join widget
					joinTeamWidget.configure(teamId, false, teamMembershipStatus, refreshCallback, null, null, null, null, false);
				} else {
					view.setCommandsVisible(true);
					view.showMemberMenuItems();
					if (isAdmin) {
						view.showAdminMenuItems();
					}
				}
			}

			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}
		});
	}

	public void refreshOpenMembershipRequests() {
		openMembershipRequestsWidget.clear();

		Callback refreshOpenMembershipRequestsCallback = new Callback() {
			@Override
			public void invoke() {
				refresh(currentTeamId);
				openMembershipRequestsWidget.configure(currentTeamId, this);
			}
		};
		openMembershipRequestsWidget.configure(currentTeamId, refreshOpenMembershipRequestsCallback);
	}

	public void refreshOpenUserInvitations() {
		openUserInvitationsWidget.clear();

		Callback refreshOpenUserInvitationsCallback = new Callback() {
			@Override
			public void invoke() {
				refresh(currentTeamId);
				openUserInvitationsWidget.configure(currentTeamId, this);
			}
		};
		openUserInvitationsWidget.configure(currentTeamId, refreshOpenUserInvitationsCallback);
	}


	@Override
	public void onShowMap() {
		map.setHeight((view.getClientHeight() - 200) + "px");
		map.configure(currentTeamId);
		view.showMapModal();
	}

	private void showView(org.sagebionetworks.web.client.place.Team place) {
		currentTeamId = place.getTeamId();
		// full refresh
		refresh();
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
		getTeamEditModalWidget().configureAndShow(team);
	}

	@Override
	public void showDeleteModal() {
		synAlert.clear();
		getTeamDeleteModalWidget().configure(team);
		getTeamDeleteModalWidget().showDialog();
	}

	@Override
	public void showLeaveModal() {
		synAlert.clear();
		getTeamLeaveModalWidget().configure(team);
		getTeamLeaveModalWidget().showDialog();
	}

	// testing only
	public void setTeam(Team team) {
		this.team = team;
	}

	@Override
	public void onManageAccess() {
		AccessRequirementsPlace place = new AccessRequirementsPlace(AccessRequirementsPlace.ID_PARAM + "=" + team.getId() + "&" + AccessRequirementsPlace.TYPE_PARAM + "=" + RestrictableObjectType.TEAM.toString());
		goTo(place);
	}

	@Override
	public void onMemberSearch(String searchTerm) {
		memberListWidget.search(searchTerm);
	}

	@Override
	public void showTeamProjectsModal() {
		getTeamProjectsModalWidget().configureAndShow(team);
	}
}

