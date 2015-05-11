package org.sagebionetworks.web.client.widget.team;

import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.web.client.GlobalApplicationState;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TeamListWidget implements TeamListWidgetView.Presenter{

	private TeamListWidgetView view;
	private GlobalApplicationState globalApplicationState;

	@Inject
	public TeamListWidget(TeamListWidgetView view, 
			GlobalApplicationState globalApplicationState) {
		this.view = view;
		view.setPresenter(this);
		this.globalApplicationState = globalApplicationState;
	}
	
	@Override
	public void goTo(Place place) {
		globalApplicationState.getPlaceChanger().goTo(place);
	}
	
	public void configure(boolean isBig) {
		view.configure(isBig);
	}	
	
	public Widget asWidget() {
		view.setPresenter(this);
		return view.asWidget();
	}
	
	public void clear() {
		view.clear();
	}
	
	public void showLoading() {
		view.showLoading();
	}
	
	public void showEmpty() {
		view.showEmpty();
	}
	
	public void addTeam(Team team, Long notificationCount) {
		view.addTeam(team, notificationCount);
	}

}
