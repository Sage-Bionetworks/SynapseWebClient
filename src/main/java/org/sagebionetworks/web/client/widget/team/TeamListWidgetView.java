package org.sagebionetworks.web.client.widget.team;

import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.IsWidget;

public interface TeamListWidgetView extends IsWidget, SynapseView {
	
	/**
	 * Set this view's presenter
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
	public void configure(boolean isBig);
	public void showEmpty();
	public void addTeam(Team team);
	void showLoading();
	void setNotificationValue(String teamId, Long notificationCount);
	public interface Presenter {
		void goTo(Place place);
	}
}
