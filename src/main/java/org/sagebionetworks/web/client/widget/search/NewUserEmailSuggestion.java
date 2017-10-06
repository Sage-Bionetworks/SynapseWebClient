package org.sagebionetworks.web.client.widget.search;

import org.sagebionetworks.web.client.widget.team.InviteWidget;

public class NewUserEmailSuggestion implements InviteeSuggestion {
	private String prefix;
	private long width;

	public NewUserEmailSuggestion(String prefix, long width) {
		this.prefix = prefix;
		this.width = width;
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
		StringBuilder result = new StringBuilder();
		result.append("<div class=\"padding-left-5 userGroupSuggestion\" style=\"height:23px; width:" + width + "px;\">");
		result.append("<span class=\"search-item movedown-1 margin-right-5\">");
		result.append("<span>" + prefix + "</span> ");
		result.append("</span>");
		result.append("</div>");
		return result.toString();
	}

	@Override
	public String getReplacementString() {
		return prefix;
	}
}


