package org.sagebionetworks.web.client.presenter;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;

import org.sagebionetworks.repo.model.RestrictableObjectType;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.TeamMembershipStatus;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
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
	private GoogleMap map;
	private String currentTeamId;
	private IsACTMemberAsyncHandler isACTMemberAsyncHandler; 
	
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
			OpenUserInvitationsWidget openUserInvitationsWidget,
			GoogleMap map,
			CookieProvider cookies,
			IsACTMemberAsyncHandler isACTMemberAsyncHandler) {
		this.view = view;
		this.authenticationController = authenticationController;
		this.globalApplicationState = globalApplicationState;
		this.synapseClient = synapseClient;
		fixServiceEntryPoint(synapseClient);
		this.synAlert = synAlert;
		this.leaveTeamWidget = leaveTeamWidget;
		this.deleteTeamWidget = deleteTeamWidget;
		this.editTeamWidget = editTeamWidget;
		this.inviteWidget = inviteWidget;
		this.joinTeamWidget = joinTeamWidget;
		this.memberListWidget = memberListWidget;
		this.openMembershipRequestsWidget = openMembershipRequestsWidget;
		this.openUserInvitationsWidget = openUserInvitationsWidget;
		this.map = map;
		this.isACTMemberAsyncHandler = isACTMemberAsyncHandler;
		view.setPresenter(this);
		view.setSynAlertWidget(synAlert.asWidget());
		view.setLeaveTeamWidget(leaveTeamWidget.asWidget());
		view.setDeleteTeamWidget(deleteTeamWidget.asWidget());
		view.setEditTeamWidget(editTeamWidget.asWidget());
		view.setInviteMemberWidget(inviteWidget.asWidget());
		view.setJoinTeamWidget(joinTeamWidget.asWidget());
		view.setOpenMembershipRequestWidget(openUserInvitationsWidget.asWidget());
		view.setOpenUserInvitationsWidget(openMembershipRequestsWidget.asWidget());
		view.setMemberListWidget(memberListWidget.asWidget());
		view.setMap(map.asWidget());
		view.setShowMapVisible(DisplayUtils.isInTestWebsite(cookies));
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
				CallbackP<Long> memberCountUpdated = count -> {
					view.setMemberCountShown(count.toString());
				};
				boolean canPublicJoin = team.getCanPublicJoin() == null ? false : team.getCanPublicJoin();
				view.setPublicJoinVisible(canPublicJoin);
				view.setMemberCountShown(result.getTotalMemberCount().toString());
				view.setMediaObjectPanel(team);
				boolean canSendEmail = teamMembershipStatus != null && teamMembershipStatus.getCanSendEmail();
				view.setTeamEmailAddress(getTeamEmail(team.getName(), canSendEmail));
				memberListWidget.configure(teamId, isAdmin, refreshCallback, memberCountUpdated);				
				openMembershipRequestsWidget.setVisible(isAdmin);
				
				if (teamMembershipStatus == null || !teamMembershipStatus.getIsMember()) {
					//not a member, add Join widget
					joinTeamWidget.configure(teamId, false, teamMembershipStatus,
							refreshCallback, null, null, null, null, false);
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
	
	public String getTeamEmail(String teamName, boolean canSendEmail) {
		if (authenticationController.isLoggedIn() && canSendEmail) {
			//strip out any non-word character.  Not a (letter, number, underscore)
			return teamName.replaceAll("\\W", "") + "@synapse.org";
		} else {
			return "";
		}
	}
	
	private void showView(org.sagebionetworks.web.client.place.Team place) {
		currentTeamId = place.getTeamId();
		//full refresh
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
	@Override
	public void onManageAccess() {
		AccessRequirementsPlace place = new AccessRequirementsPlace(AccessRequirementsPlace.ID_PARAM + "=" + team.getId() + "&" + AccessRequirementsPlace.TYPE_PARAM + "=" + RestrictableObjectType.TEAM.toString());
		goTo(place);
	}
	@Override
	public void onMemberSearch(String searchTerm) {
		memberListWidget.search(searchTerm);
	}
}

