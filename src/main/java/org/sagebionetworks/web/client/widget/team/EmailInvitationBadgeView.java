package org.sagebionetworks.web.client.widget.team;

import com.google.gwt.user.client.ui.IsWidget;

public interface EmailInvitationBadgeView extends IsWidget {
	void setEmail(String inviteeEmail);

	void setIconColor(String color);

	void setIconLetter(String letter);

	interface Presenter {
		void configure(String inviteeEmail);
	}
}

