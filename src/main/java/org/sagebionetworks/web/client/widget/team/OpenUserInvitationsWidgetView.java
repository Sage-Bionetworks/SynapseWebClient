package org.sagebionetworks.web.client.widget.team;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.IsWidget;

public interface OpenUserInvitationsWidgetView extends IsWidget {

	/**
	 * Set this view's presenter
	 * 
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);

	void hideMoreButton();

	void showMoreButton();

	void setSynAlert(IsWidget w);

	void addInvitation(IsWidget userBadge, String inviteeEmail, String misId, String message, String createdOn);

	void clear();

	void setVisible(boolean visible);

	interface Presenter {
		// use to go to user profile page
		void goTo(Place place);

		void removeInvitation(String ownerId);

		void getNextBatch();

		void resendInvitation(String membershipInvitationId);
	}
}
