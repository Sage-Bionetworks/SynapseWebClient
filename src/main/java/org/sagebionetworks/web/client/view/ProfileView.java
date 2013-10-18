package org.sagebionetworks.web.client.view;

import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.attachment.AttachmentData;
import org.sagebionetworks.web.client.SynapsePresenter;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.shared.PaginatedResults;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface ProfileView extends IsWidget, SynapseView {
	
	/**
	 * Set this view's presenter
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
	
	/**
	 * Renders the view for a given presenter
	 */
	public void render();
	
	public void updateView(UserProfile profile, boolean editable, boolean isOwner, Widget profileFormView);
	public void refreshHeader();
	
	public interface Presenter extends SynapsePresenter {

		void updateProfileWithLinkedIn(String requestToken, String verifier);
		
		void redirectToLinkedIn();
		
		void redirectToEditProfile();
		
		void redirectToViewProfile();
		
		void goTo(Place place);
		
		String getEmailAddress();
	}
}
