package org.sagebionetworks.web.client.widget.evaluation;

import org.sagebionetworks.repo.model.Challenge;
import org.sagebionetworks.web.client.ChallengeClientAsync;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.team.BigTeamBadge;
import org.sagebionetworks.web.client.widget.team.SelectTeamModal;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ChallengeWidget implements ChallengeWidgetView.Presenter, IsWidget {
	
	private ChallengeClientAsync challengeClient;
	private ChallengeWidgetView view;
	private SynapseAlert synAlert;
	private String entityId;
	private BigTeamBadge teamBadge;
	AsyncCallback<Challenge> callback;
	private Challenge currentChallenge;
	private SelectTeamModal selectTeamModal;
	boolean isCreatingChallenge;
	@Inject
	public ChallengeWidget(
			ChallengeWidgetView view, 
			ChallengeClientAsync challengeClient,
			SynapseAlert synAlert,
			BigTeamBadge teamBadge,
			SelectTeamModal selectTeamModal
			) {
		this.challengeClient = challengeClient;
		this.view = view;
		this.synAlert = synAlert;
		this.teamBadge = teamBadge;
		this.selectTeamModal = selectTeamModal;
		view.setPresenter(this);
		view.add(synAlert.asWidget());
		view.setChallengeTeamWidget(teamBadge.asWidget());
		callback = getConfigureCallback();
		view.setSelectTeamModal(selectTeamModal.asWidget());
		selectTeamModal.setTitle("Select Participant Team");
		selectTeamModal.configure(new CallbackP<String>() {
			@Override
			public void invoke(String selectedTeamId) {
				onSelectChallengeTeam(selectedTeamId);
			}
		});
	}
	
	private AsyncCallback<Challenge> getConfigureCallback() {
		return new AsyncCallback<Challenge>() {
			@Override
			public void onSuccess(Challenge challenge) {
				currentChallenge = challenge;
				teamBadge.configure(challenge.getParticipantTeamId());
				view.setChallengeVisible(true);
				view.setChallengeId(currentChallenge.getId());
			}
			
			@Override
			public void onFailure(Throwable caught) {
				if (caught instanceof NotFoundException) {
					view.setCreateChallengeVisible(true);
				} else {
					synAlert.handleException(caught);	
				}
			}
		};
	}

	public void configure(String entityId) {
		this.entityId = entityId;
		synAlert.clear();
		view.setChallengeVisible(false);
		view.setCreateChallengeVisible(false);
		challengeClient.getChallengeForProject(entityId, callback);
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void onDeleteChallengeClicked() {
		challengeClient.deleteChallenge(currentChallenge.getId(), new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				currentChallenge = null;
				view.setChallengeVisible(false);
				view.setCreateChallengeVisible(true);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}
		});
	}

	@Override
	public void onCreateChallengeClicked() {
		isCreatingChallenge = true;
		selectTeamModal.show();
	}

	@Override
	public void onEditTeamClicked() {
		isCreatingChallenge = false;
		selectTeamModal.show();
	}
	
	public void onSelectChallengeTeam(String id) {
		view.setChallengeVisible(false);
		view.setCreateChallengeVisible(false);
		if (isCreatingChallenge) {
			Challenge c = new Challenge();
			c.setProjectId(entityId);
			c.setParticipantTeamId(id);
			challengeClient.createChallenge(c, callback);
		} else {
			currentChallenge.setParticipantTeamId(id);
			challengeClient.updateChallenge(currentChallenge, callback);
		}
	}
	
	/**
	 * exposed for testing purposes
	 * @param currentChallenge
	 */
	public void setCurrentChallenge(Challenge currentChallenge) {
		this.currentChallenge = currentChallenge;
	}
}
