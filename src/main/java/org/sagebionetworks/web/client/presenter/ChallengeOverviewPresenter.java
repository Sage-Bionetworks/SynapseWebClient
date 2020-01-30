package org.sagebionetworks.web.client.presenter;

import org.sagebionetworks.web.client.place.Challenges;
import org.sagebionetworks.web.client.view.ChallengeOverviewView;
import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class ChallengeOverviewPresenter extends AbstractActivity implements ChallengeOverviewView.Presenter, Presenter<Challenges> {

	private Challenges place;
	private ChallengeOverviewView view;

	@Inject
	public ChallengeOverviewPresenter(ChallengeOverviewView view) {
		this.view = view;
		view.setPresenter(this);
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		// Install the view
		panel.setWidget(view);
	}

	@Override
	public void setPlace(Challenges place) {
		this.place = place;
		this.view.setPresenter(this);
		this.view.showOverView();

		view.showChallengeInfo();
	}

	@Override
	public String mayStop() {
		view.clear();
		return null;
	}
}
