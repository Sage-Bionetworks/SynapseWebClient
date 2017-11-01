package org.sagebionetworks.web.client.widget.search;

import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.web.client.DisplayUtils;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.ui.SuggestOracle;

public class UserGroupSuggestion implements IsSerializable, SuggestOracle.Suggestion {
	private UserGroupHeader header;
	private String prefix;
	private int width;

	public UserGroupSuggestion() {}

	public UserGroupSuggestion(UserGroupHeader header, String prefix, int width) {
		this.header = header;
		this.prefix = prefix;
		this.width = width;
	}

	@Override
	public String getDisplayString() {
		StringBuilder result = new StringBuilder();
		result.append("<div class=\"padding-left-5 userGroupSuggestion\" style=\"height:23px; width:" + width + "px;\">");
		result.append("<span class=\"search-item movedown-1 margin-right-5\">");
		if (header.getIsIndividual()) {
			result.append("<span class=\"font-italic\">" + header.getFirstName() + " " + header.getLastName() + "</span> ");
		}
		result.append("<span>" + header.getUserName() + "</span> ");
		result.append("</span>");
		if (!header.getIsIndividual()) {
			result.append("(Team)");
		}
		result.append("</div>");
		return result.toString();
	}

	@Override
	public String getReplacementString() {
		// Example output:
		// Pac Man  |  114085
		StringBuilder sb = new StringBuilder();
		if (!header.getIsIndividual())
			sb.append("(Team) ");

		String firstName = header.getFirstName();
		String lastName = header.getLastName();
		String username = header.getUserName();
		sb.append(DisplayUtils.getDisplayName(firstName, lastName, username));
		sb.append("  |  " + header.getOwnerId());
		return sb.toString();
	}

	public String getId() {
		return header.getOwnerId();
	}

	public String isIndividual() {
		return header.getIsIndividual().toString();
	}

	public String getName() {
		return header.getUserName();
	}

	public UserGroupHeader getHeader() {
		return header;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
}
