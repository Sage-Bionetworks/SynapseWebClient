package org.sagebionetworks.web.client.widget.team;

import java.util.List;

import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.IsWidget;

public interface OpenMembershipRequestsWidgetView extends IsWidget, SynapseView {
	
	/**
	 * Set this view's presenter
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
	
	/**
	 * shows nothing if membershipRequests is empty.
	 */
	public void configure(List<UserProfile> profiles, List<String> requestMessages);
	public interface Presenter {
		//use to go to user profile page
		void goTo(Place place);
		void acceptRequest(String userId);
		void clear();
	}
}
