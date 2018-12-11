package org.sagebionetworks.web.client.widget.entity;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;

import java.util.List;

import org.gwtbootstrap3.extras.bootbox.client.callback.ConfirmCallback;
import org.sagebionetworks.repo.model.ChallengeTeam;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.web.client.ChallengeClientAsync;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.place.Synapse;
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
		fixServiceEntryPoint(challengeClient);
		this.globalApplicationState = globalApplicationState;
		this.authenticationController = authenticationController;
		
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
		if (!authenticationController.isLoggedIn()) {
			view.showConfirmDialog(DisplayConstants.ANONYMOUS_JOIN_EVALUATION, getConfirmCallback());
			return;
		}
		clearState();
		this.callback = callback;
		this.challengeId = challengeId;
		view.setRecruitmentMessage("");
		view.setNewTeamLink("#!Profile:"+authenticationController.getCurrentUserPrincipalId()+Profile.DELIMITER+Synapse.ProfileArea.TEAMS);
		refreshRegistratableTeams();
	}
	
	public Callback getConfirmCallback() {
		return () ->{
			globalApplicationState.getPlaceChanger().goTo(new LoginPlace(LoginPlace.LOGIN_TOKEN));
		};
	}

	public void refreshRegistratableTeams() {
		challengeClient.getRegistratableTeams(authenticationController.getCurrentUserPrincipalId(), challengeId, new AsyncCallback<List<Team>>() {
			@Override
			public void onSuccess(List<Team> result) {
				teams = result;
				//if there is a team, then select the first team by default.  otherwise, show no teams visible ui
				view.setNoTeamsFoundVisible(teams.isEmpty());
				if (!teams.isEmpty()) {
					view.setTeams(teams);	
					selectedTeamId = teams.get(0).getId();
				}
				view.showModal();
			}
			@Override
			public void onFailure(Throwable caught) {
				//note: call will result in a NotFoundException if the current user is not registered for the challenge
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
					view.showInfo("Successfully registered your team for the challenge.");
					if (callback != null) {
						callback.invoke();
					}
					view.hideModal();
				}
				@Override
				public void onFailure(Throwable caught) {
					view.showErrorMessage(caught.getMessage());
				}
			});
		}
	}
	
	@Override
	public void teamSelected(int selectedIndex) {
		selectedTeamId = null;
		if (teams != null && selectedIndex >= 0 && selectedIndex<teams.size()) {
			selectedTeamId = teams.get(selectedIndex).getId();
		}
	}
	
	/*********
	 * Exposed for testing purposes
	 */
	public String getSelectedTeamId() {
		return selectedTeamId;
	}
}
