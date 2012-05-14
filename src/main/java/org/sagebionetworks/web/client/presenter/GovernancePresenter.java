package org.sagebionetworks.web.client.presenter;

import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.place.Governance;
import org.sagebionetworks.web.client.view.GovernanceView;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class GovernancePresenter extends AbstractActivity implements GovernanceView.Presenter {
		
	private Governance place;
	private GovernanceView view;
	private GlobalApplicationState globalApplicationState;

	
	@Inject
	public GovernancePresenter(GovernanceView view, GlobalApplicationState globalApplicationState){
		this.globalApplicationState = globalApplicationState;
		this.view = view;

		view.setPresenter(this);
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		// Install the view
		panel.setWidget(view);
	}

	public void setPlace(Governance place) {
		this.place = place;
		this.view.setPresenter(this);
	}

	@Override
    public String mayStop() {
        view.clear();
        return null;
    }
	
}
