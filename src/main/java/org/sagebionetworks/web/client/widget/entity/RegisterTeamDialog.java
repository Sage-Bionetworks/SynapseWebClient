package org.sagebionetworks.web.client.widget.entity;

import java.util.List;

import org.sagebionetworks.repo.model.ChallengeTeam;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.web.client.ChallengeClientAsync;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class RegisterTeamDialog implements RegisterTeamDialogView.Presenter {
	private RegisterTeamDialogView view;
	private String challengeId;
	private String selectedTeamId;
	private List<Team> teams;
	private ChallengeClientAsync challengeClient;
	private GlobalApplicationState globalApplicationState;
	private AuthenticationController authenticationController;
	private Callback callback;
	
	@Inject
	public RegisterTeamDialog(RegisterTeamDialogView view, 
			ChallengeClientAsync challengeClient,
			GlobalApplicationState globalApplicationState,
			AuthenticationController authenticationController) {
		this.view = view;
		this.challengeClient = challengeClient;
		this.globalApplicationState = globalApplicationState;
		this.authenticationController = authenticationController;
		
		view.setRecruitmentMessage("");
		view.setPresenter(this);
	}		
	
	public Widget asWidget() {
		return view.asWidget();
	}
	
	private void clearState() {
		selectedTeamId = null;
		teams = null;
		challengeId = null;
	}
	
	public void configure(String challengeId, Callback callback) {
		clearState();
		this.callback = callback;
		this.challengeId = challengeId;
		view.setRecruitmentMessage("");
		view.showModal();
		getRegistratableTeams();
	}
	
	public void getRegistratableTeams() {
		challengeClient.getRegistratableTeams(challengeId, new AsyncCallback<List<Team>>() {
			@Override
			public void onSuccess(List<Team> result) {
				teams = result;
				//if there is a team, then select the first team by default.  otherwise, show no teams visible ui
				if (teams.isEmpty()) {
					view.setNoTeamsFoundVisible(true);
				} else {
					view.setNoTeamsFoundVisible(false);
					view.setTeams(teams);	
					selectedTeamId = teams.get(0).getId();
				}
			}
			@Override
			public void onFailure(Throwable caught) {
				if(!DisplayUtils.handleServiceException(caught, globalApplicationState, authenticationController.isLoggedIn(), view))
					view.showErrorMessage(caught.getMessage());
			}
		});
	}
	
	public boolean isValid() {
		if (selectedTeamId == null) {
			view.showErrorMessage("Please select a team.");
			return false;
		}
		return true;
	}
	
	@Override
	public void onOk() {
		if (isValid()) {
			ChallengeTeam challengeTeam = new ChallengeTeam();
			challengeTeam.setTeamId(selectedTeamId);
			challengeTeam.setChallengeId(challengeId);
			challengeTeam.setMessage(view.getRecruitmentMessage());
			challengeClient.registerChallengeTeam(challengeTeam, new AsyncCallback<ChallengeTeam>() {
				@Override
				public void onSuccess(ChallengeTeam result) {
					if (callback != null) {
						callback.invoke();
					}
					view.hideModal();
				}
				@Override
				public void onFailure(Throwable caught) {
					if(!DisplayUtils.handleServiceException(caught, globalApplicationState, authenticationController.isLoggedIn(), view))
						view.showErrorMessage(caught.getMessage());
				}
			});
		}
	}
	
	@Override
	public void onNewTeamClicked() {
		//go to user profile page, team tab
		if (authenticationController.isLoggedIn())
			globalApplicationState.getPlaceChanger().goTo(new Profile(authenticationController.getCurrentUserPrincipalId()+Profile.TEAMS_DELIMITER));
		else {
			globalApplicationState.getPlaceChanger().goTo(new LoginPlace(LoginPlace.LOGIN_TOKEN));
		}
	}
	
	@Override
	public void teamSelected(String teamName) {
		for (Team team : teams) {
			if (teamName.equals(team.getName())) {
				selectedTeamId = team.getId();
				break;
			}
		}
	}
}
