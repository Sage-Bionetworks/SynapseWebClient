package org.sagebionetworks.web.client.widget.team;

import java.util.List;

import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.web.client.SynapsePresenter;
import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.IsWidget;

public interface OpenTeamInvitationsWidgetView extends IsWidget, SynapseView {
	
	/**
	 * Set this view's presenter
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
	
	/**
	 * shows nothing if membershipInvitations is empty.
	 */
	public void configure(List<Team> membershipInvitations, List<String> inviteMessages);
	public interface Presenter extends SynapsePresenter {
		//use to go to team page
		void goTo(Place place);
		void joinTeam(String teamId);
	}
}
