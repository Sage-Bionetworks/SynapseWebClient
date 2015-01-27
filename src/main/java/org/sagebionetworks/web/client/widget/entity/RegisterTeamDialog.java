package org.sagebionetworks.web.client.widget.entity;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
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
	private SynapseClientAsync synapseClient;
	private GlobalApplicationState globalAppState;
	private AuthenticationController authController;
	private Callback callback;
	
	@Inject
	public RegisterTeamDialog(RegisterTeamDialogView view, 
			SynapseClientAsync synapseClient,
			GlobalApplicationState globalAppState,
			AuthenticationController authController) {
		this.view = view;
		this.synapseClient = synapseClient;
		this.globalAppState = globalAppState;
		this.authController = authController;
		
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
		view.clearTeams();
		getRegistratableTeams();
		view.showModal();
	}
	
	public void getRegistratableTeams() {
		synapseClient.getRegistratableTeams(new AsyncCallback<ArrayList<Team>>() {
			@Override
			public void onSuccess(ArrayList<Team> result) {
				teams = result;
				view.setTeams(teams);
			}
			@Override
			public void onFailure(Throwable caught) {
				if(!DisplayUtils.handleServiceException(caught, globalAppState, authController.isLoggedIn(), view))
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
			synapseClient.registerChallengeTeam(challengeTeam, new AsyncCallback<ChallengeTeam>() {
				@Override
				public void onSuccess(ChallengeTeam result) {
					if (callback != null) {
						callback.invoke();
					}
					view.hideModal();
				}
				@Override
				public void onFailure(Throwable caught) {
					if(!DisplayUtils.handleServiceException(caught, globalAppState, authController.isLoggedIn(), view))
						view.showErrorMessage(caught.getMessage());
				}
			});
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
