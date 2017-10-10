package org.sagebionetworks.web.client.widget.search;

import org.sagebionetworks.repo.model.UserGroupHeader;

public class UserGroupEmailSuggestion extends UserGroupSuggestion {
	private String email;
	private int width;

	public UserGroupEmailSuggestion() {}

	public UserGroupEmailSuggestion(String email, int width) {
		this.email = email;
		this.width = width;
	}

	public UserGroupEmailSuggestion(UserGroupHeader header, String prefix, int width) {
		super(header, prefix, width);
	}

	@Override
	public String getDisplayString() {
		if (email != null) {
			StringBuilder result = new StringBuilder();
			result.append("<div class=\"padding-left-5 userGroupSuggestion\" style=\"height:23px; width:" + width + "px;\">");
			result.append("<span class=\"search-item movedown-1 margin-right-5\">");
			result.append("<span>" + email + "</span> ");
			result.append("</div>");
			return result.toString();
		} else {
			return super.getDisplayString();
		}
	}

	@Override
	public String getReplacementString() {
		if (email != null) {
			return email;
		} else {
			return super.getReplacementString();
		}
	}

	public String getEmail() {
		return email;
	}
}
