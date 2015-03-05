package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.shared.ChallengeBundle;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ChallengeBadge implements ChallengeBadgeView.Presenter, SynapseWidgetPresenter {
	
	private ChallengeBadgeView view;
	
	@Inject
	public ChallengeBadge(ChallengeBadgeView view) {
		this.view = view;
		view.setPresenter(this);
	}
	
	public void configure(ChallengeBundle challenge) {
		view.setChallenge(challenge);
		if (challenge.getChallenge() != null && challenge.getChallenge().getProjectId() != null) {
			//set href
			view.setHref(DisplayUtils.getSynapseHistoryToken(challenge.getChallenge().getProjectId()));
		}
	}
	
	public void clearState() {
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
