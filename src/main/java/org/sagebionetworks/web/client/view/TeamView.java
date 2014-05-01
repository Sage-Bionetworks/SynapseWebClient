package org.sagebionetworks.web.client.view;

import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.TeamMembershipStatus;
import org.sagebionetworks.web.client.SynapsePresenter;
import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.IsWidget;

public interface TeamView extends IsWidget, SynapseView {
	
	/**
	 * Set this view's presenter
	 * @param presenter
	 */
	void setPresenter(Presenter presenter);
	void configure(Team team, boolean isAdmin, TeamMembershipStatus teamMembershipStatus, Long totalMemberCount);
	
	public interface Presenter extends SynapsePresenter {
		void goTo(Place place);
		void deleteTeam();
		void leaveTeam();
		void updateTeamInfo(String name, String description, boolean canPublicJoin, String iconFileHandleId);
		void refresh(String teamId);
	}
}
