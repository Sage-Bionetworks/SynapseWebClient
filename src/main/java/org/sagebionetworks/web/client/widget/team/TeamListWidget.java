package org.sagebionetworks.web.client.widget.team;

import org.sagebionetworks.repo.model.Team;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TeamListWidget {

	private TeamListWidgetView view;

	@Inject
	public TeamListWidget(TeamListWidgetView view) {
		this.view = view;
	}

	public Widget asWidget() {
		return view.asWidget();
	}

	public void clear() {
		view.clear();
	}

	public void showEmpty() {
		view.showEmpty();
	}

	public void addTeam(Team team) {
		view.addTeam(team);
	}

	public void setNotificationValue(String teamId, Long count) {
		view.setNotificationValue(teamId, count);
	}
}
