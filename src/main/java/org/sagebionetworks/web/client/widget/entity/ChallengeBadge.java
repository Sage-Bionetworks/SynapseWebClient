package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.repo.model.ChallengeSummary;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Team;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ChallengeBadge implements ChallengeBadgeView.Presenter, SynapseWidgetPresenter {
	
	private ChallengeBadgeView view;
	private GlobalApplicationState globalAppState;
	private ChallengeSummary header;
	
	@Inject
	public ChallengeBadge(ChallengeBadgeView view, 
			GlobalApplicationState globalAppState
			) {
		this.view = view;
		this.globalAppState = globalAppState;
		view.setPresenter(this);
	}
	
	public void configure(ChallengeSummary header) {
		this.header = header;
		view.setChallenge(header);
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
		globalAppState.getPlaceChanger().goTo(new Synapse(header.getProjectId()));
	}
	
	@Override
	public void onParticipantsClick() {
		globalAppState.getPlaceChanger().goTo(new Team(header.getParticipantTeamId()));
	}
}
