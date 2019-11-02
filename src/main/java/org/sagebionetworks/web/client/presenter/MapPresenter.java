package org.sagebionetworks.web.client.presenter;

import org.sagebionetworks.web.client.view.MapView;
import org.sagebionetworks.web.client.widget.googlemap.GoogleMap;
import org.sagebionetworks.web.client.widget.team.TeamBadge;
import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class MapPresenter extends AbstractActivity implements Presenter<org.sagebionetworks.web.client.place.MapPlace>, MapView.Presenter {
	public static final String ALL_USERS = "all";
	GoogleMap map;
	MapView view;
	TeamBadge teamBadge;

	@Inject
	public MapPresenter(MapView view, GoogleMap map, TeamBadge teamBadge) {
		this.view = view;
		this.map = map;
		this.teamBadge = teamBadge;
		view.setPresenter(this);
		view.setMap(map.asWidget());
		view.setTeamBadge(teamBadge.asWidget());
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		panel.setWidget(view.asWidget());
	}

	@Override
	public void setPlace(org.sagebionetworks.web.client.place.MapPlace place) {
		String teamId = place.getTeamId();
		map.setHeight((view.getClientHeight() - 300) + "px");
		if (ALL_USERS.equalsIgnoreCase(teamId)) {
			map.configure();
			view.setAllUsersTitleVisible(true);
			view.setTeamBadgeVisible(false);
		} else {
			teamBadge.configure(teamId);
			map.configure(teamId);
			view.setAllUsersTitleVisible(false);
			view.setTeamBadgeVisible(true);
		}
	}

	@Override
	public String mayStop() {
		return null;
	}
}

