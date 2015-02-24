package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Team;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.shared.ChallengeBundle;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ChallengeBadge implements ChallengeBadgeView.Presenter, SynapseWidgetPresenter {
	
	private ChallengeBadgeView view;
	private GlobalApplicationState globalAppState;
	private ChallengeBundle challenge;
	
	@Inject
	public ChallengeBadge(ChallengeBadgeView view, 
			GlobalApplicationState globalAppState
			) {
		this.view = view;
		this.globalAppState = globalAppState;
		view.setPresenter(this);
	}
	
	public void configure(ChallengeBundle challenge) {
		this.challenge = challenge;
		view.setChallenge(challenge);
	}
	
	@SuppressWarnings("unchecked")
	public void clearState() {
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
	
	@Override
	public void onClick() {
		if (challenge.getChallenge() != null && challenge.getChallenge().getProjectId() != null)
			globalAppState.getPlaceChanger().goTo(new Synapse(challenge.getChallenge().getProjectId()));
		else {
			view.showErrorMessage("Challenge project is not set.");
		}
	}
	
	@Override
	public void onParticipantsClick() {
		if (challenge.getChallenge() != null && challenge.getChallenge().getParticipantTeamId() != null)
			globalAppState.getPlaceChanger().goTo(new Team(challenge.getChallenge().getParticipantTeamId()));
		else {
			view.showErrorMessage("Challenge participant team is not set.");
		}
	}
}
