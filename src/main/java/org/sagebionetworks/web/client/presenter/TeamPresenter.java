package org.sagebionetworks.web.client.presenter;

import static org.sagebionetworks.web.client.ClientProperties.DEFAULT_PLACE_TOKEN;

import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.TeamMembershipStatus;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.Home;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.view.TeamView;
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
	private NodeModelCreator nodeModelCreator;
	private AuthenticationController authenticationController;
	private GlobalApplicationState globalApplicationState;
	private JSONObjectAdapter jsonObjectAdapter;
	private Team team;
	private TeamMembershipStatus teamMembershipStatus;
	
	@Inject
	public TeamPresenter(TeamView view,
			AuthenticationController authenticationController,
			GlobalApplicationState globalApplicationState,
			SynapseClientAsync synapseClient,
			NodeModelCreator nodeModelCreator,
			JSONObjectAdapter jsonObjectAdapter) {
		this.view = view;
		view.setPresenter(this);
		this.authenticationController = authenticationController;
		this.globalApplicationState = globalApplicationState;
		this.synapseClient = synapseClient;
		this.nodeModelCreator = nodeModelCreator;
		this.jsonObjectAdapter = jsonObjectAdapter;
		
		view.setPresenter(this);
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
		synapseClient.getTeamBundle(authenticationController.getCurrentUserPrincipalId(), teamId, authenticationController.isLoggedIn(), new AsyncCallback<TeamBundle>() {
			@Override
			public void onSuccess(TeamBundle result) {
				try {
					team = nodeModelCreator.createJSONEntity(result.getTeamJson(), Team.class);
					if (result.getTeamMembershipStatusJson() != null)
						teamMembershipStatus = nodeModelCreator.createJSONEntity(result.getTeamMembershipStatusJson(), TeamMembershipStatus.class);
					else
						teamMembershipStatus = null; 
					boolean isAdmin = result.isUserAdmin();
					view.configure(team, isAdmin, teamMembershipStatus, result.getTotalMemberCount());
				} catch (JSONObjectAdapterException e) {
					onFailure(e);
				}
			}
			@Override
			public void onFailure(Throwable caught) {
				if(!DisplayUtils.handleServiceException(caught, globalApplicationState.getPlaceChanger(), authenticationController.isLoggedIn(), view)) {					
					view.showErrorMessage(caught.getMessage());
				} 
			}
		});
	}
	private void showView(org.sagebionetworks.web.client.place.Team place) {
		String teamId = place.getTeamId();
		refresh(teamId);
	}

	@Override
	public void deleteTeam() {
		synapseClient.deleteTeam(team.getId(), new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				//go home
				view.showInfo(DisplayConstants.DELETE_TEAM_SUCCESS, "");
				goTo(new Home(DEFAULT_PLACE_TOKEN));
			}
			@Override
			public void onFailure(Throwable caught) {
				if(!DisplayUtils.handleServiceException(caught, globalApplicationState.getPlaceChanger(), authenticationController.isLoggedIn(), view)) {					
					view.showErrorMessage(caught.getMessage());
				}
			}
		});

		
	}

	@Override
	public void leaveTeam() {
		String userId = authenticationController.getCurrentUserPrincipalId();
		synapseClient.deleteTeamMember(userId, userId, team.getId(), new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				view.showInfo(DisplayConstants.LEAVE_TEAM_SUCCESS, "");
				refresh();
			}
			@Override
			public void onFailure(Throwable caught) {
				if(!DisplayUtils.handleServiceException(caught, globalApplicationState.getPlaceChanger(), authenticationController.isLoggedIn(), view)) {					
					view.showErrorMessage(caught.getMessage());
				}
			}
		});
	}

	@Override
	public void updateTeamInfo(String name, String description, boolean canPublicJoin, String fileHandleId) {
		if (name == null || name.trim().length() == 0) {
			view.showErrorMessage(DisplayConstants.ERROR_NAME_MUST_BE_DEFINED);
		}
		else {
			team.setName(name);
			team.setDescription(description);
			team.setCanPublicJoin(canPublicJoin);
			team.setIcon(fileHandleId);
			try {
				JSONObjectAdapter adapter = team.writeToJSONObject(jsonObjectAdapter.createNew());
				String teamJson = adapter.toJSONString();
				synapseClient.updateTeam(teamJson, new AsyncCallback<String>() {
					@Override
					public void onSuccess(String result) {
						view.showInfo(DisplayConstants.UPDATE_TEAM_SUCCESS, "");
						refresh();
					}
					@Override
					public void onFailure(Throwable caught) {
						if(!DisplayUtils.handleServiceException(caught, globalApplicationState.getPlaceChanger(), authenticationController.isLoggedIn(), view)) {					
							view.showErrorMessage(caught.getMessage());
						}
					}
				});
			} catch (JSONObjectAdapterException e) {
				view.showErrorMessage(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION);
			}
		}
	}
}

