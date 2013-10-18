package org.sagebionetworks.web.client.widget.team;

import org.sagebionetworks.repo.model.TeamMembershipStatus;
import org.sagebionetworks.web.client.SynapsePresenter;
import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.user.client.ui.IsWidget;

public interface JoinTeamWidgetView extends IsWidget, SynapseView {
	
	/**
	 * Set this view's presenter
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
	public void configure(boolean isLoggedIn, TeamMembershipStatus teamMembershipStatus);
	public interface Presenter extends SynapsePresenter {
		public void sendJoinRequest(String message, boolean isAcceptingInvite);
		//service may be added later to query for current user requests to allow deletion
//		public void deleteAllJoinRequests();
	}
}
