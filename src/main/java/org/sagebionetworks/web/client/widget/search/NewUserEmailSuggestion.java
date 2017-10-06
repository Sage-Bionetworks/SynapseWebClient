package org.sagebionetworks.web.client.widget.search;

import org.sagebionetworks.web.client.widget.team.InviteWidget;

public class NewUserEmailSuggestion implements InviteeSuggestion {
	private String prefix;

	public NewUserEmailSuggestion(String prefix) {
		this.prefix = prefix;
	}

	@Override
	public String getPrefix() {
		return prefix;
	}

	@Override
	public void accept(InviteWidget widget, String invitationMessage) {
		widget.validateAndSendInvite(this, invitationMessage);
	}

	@Override
	public String getDisplayString() {
		return prefix;
	}

	@Override
	public String getReplacementString() {
		return prefix;
	}
}


