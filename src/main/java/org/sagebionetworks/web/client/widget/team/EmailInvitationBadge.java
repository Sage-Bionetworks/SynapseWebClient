package org.sagebionetworks.web.client.widget.team;

import javax.inject.Inject;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public class EmailInvitationBadge implements EmailInvitationBadgeView.Presenter, IsWidget {
	private EmailInvitationBadgeView view;

	@Inject
	public EmailInvitationBadge(EmailInvitationBadgeView view) {
		this.view = view;
	}

	@Override
	public void configure(String inviteeEmail) {
		view.setEmail(inviteeEmail);
		view.setIconLetter(Character.toString(inviteeEmail.charAt(0)).toUpperCase());
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
