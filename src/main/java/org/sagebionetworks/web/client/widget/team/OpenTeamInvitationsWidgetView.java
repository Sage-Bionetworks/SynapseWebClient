package org.sagebionetworks.web.client.widget.team;

import org.sagebionetworks.repo.model.Team;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface OpenTeamInvitationsWidgetView extends IsWidget {

	/**
	 * Set this view's presenter
	 * 
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);

	void setSynAlert(IsWidget w);

	void addTeamInvite(Team team, String inviteMessage, String createdOn, String inviteId, Widget joinButtonWidget);

	void clear();

	public interface Presenter {
		// use to go to team page
		void goTo(Place place);

		void deleteInvitation(String inviteId);
	}
}
