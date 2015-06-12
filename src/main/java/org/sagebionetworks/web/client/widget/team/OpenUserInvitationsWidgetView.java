package org.sagebionetworks.web.client.widget.team;

import java.util.List;

import org.sagebionetworks.repo.model.MembershipInvtnSubmission;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.SynapsePresenter;
import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.IsWidget;

public interface OpenUserInvitationsWidgetView extends IsWidget, SynapseView {
	
	/**
	 * Set this view's presenter
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
	
	/**
	 * shows nothing if membershipRequests is empty.
	 */
	public void configure(List<UserProfile> profiles, List<MembershipInvtnSubmission> invitations);
	
	public void setMoreResultsVisible(boolean isVisible);
	
	public interface Presenter extends SynapsePresenter {
		//use to go to user profile page
		void goTo(Place place);
		void removeInvitation(String ownerId);
		void getNextBatch();
		void clear();
	}
}
