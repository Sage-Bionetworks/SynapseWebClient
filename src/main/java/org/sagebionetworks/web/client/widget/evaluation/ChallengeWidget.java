package org.sagebionetworks.web.client.widget.evaluation;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;
import java.util.HashMap;
import org.sagebionetworks.repo.model.Challenge;
import org.sagebionetworks.web.client.ChallengeClientAsync;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.entity.renderer.SubmitToEvaluationWidget;
import org.sagebionetworks.web.client.widget.team.BigTeamBadge;
import org.sagebionetworks.web.client.widget.team.SelectTeamModal;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ChallengeWidget implements ChallengeWidgetView.Presenter, IsWidget {

	private ChallengeClientAsync challengeClient;
	private ChallengeWidgetView view;
	private SynapseAlert synAlert;
	private BigTeamBadge teamBadge;
	AsyncCallback<Challenge> callback;
	private Challenge currentChallenge;
	private SelectTeamModal selectTeamModal;
	private SubmitToEvaluationWidget submitToChallengeWidget;

	@Inject
	public ChallengeWidget(ChallengeWidgetView view, ChallengeClientAsync challengeClient, SynapseAlert synAlert, BigTeamBadge teamBadge, SelectTeamModal selectTeamModal, SubmitToEvaluationWidget submitToChallengeWidget) {
		this.challengeClient = challengeClient;
		fixServiceEntryPoint(challengeClient);
		this.view = view;
		this.synAlert = synAlert;
		this.teamBadge = teamBadge;
		this.selectTeamModal = selectTeamModal;
		this.submitToChallengeWidget = submitToChallengeWidget;
		view.setPresenter(this);
		view.add(synAlert.asWidget());
		view.setChallengeTeamWidget(teamBadge.asWidget());
		callback = getConfigureCallback();
		view.setSelectTeamModal(selectTeamModal.asWidget());
		view.setSubmitToChallengeWidget(submitToChallengeWidget);
		selectTeamModal.setTitle("Select Participant Team");
		selectTeamModal.configure(selectedTeamId -> {
			onSelectChallengeTeam(selectedTeamId);
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

				HashMap<String, String> submitToChallengeParams = new HashMap<>();
				submitToChallengeParams.put(WidgetConstants.CHALLENGE_ID_KEY, challenge.getId());
				submitToChallengeParams.put(WidgetConstants.BUTTON_TEXT_KEY, "Submit");
				submitToChallengeWidget.configure(null, submitToChallengeParams, null, null);
			}

			@Override
			public void onFailure(Throwable caught) {
				if (caught instanceof NotFoundException) {
					view.setChallengeVisible(false);
				} else {
					synAlert.handleException(caught);
				}
			}
		};
	}

	public void configure(String entityId) {
		synAlert.clear();
		view.setChallengeVisible(false);
		challengeClient.getChallengeForProject(entityId, callback);
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void onEditTeamClicked() {
		selectTeamModal.show();
	}

	public void onSelectChallengeTeam(String id) {
		view.setChallengeVisible(false);
		currentChallenge.setParticipantTeamId(id);
		challengeClient.updateChallenge(currentChallenge, callback);
	}

	/**
	 * exposed for testing purposes
	 * 
	 * @param currentChallenge
	 */
	public void setCurrentChallenge(Challenge currentChallenge) {
		this.currentChallenge = currentChallenge;
	}
}
