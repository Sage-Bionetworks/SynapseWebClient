package org.sagebionetworks.web.client.widget.search;

import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.Portal;
import org.sagebionetworks.web.client.widget.team.TeamBadge;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.SuggestOracle;

public class UserGroupSuggestion implements IsSerializable, SuggestOracle.Suggestion {
	private UserGroupHeader header;
	private String prefix;
	private int width;
	public static final String DATA_USER_GROUP_ID = "data-user-group-id";
	public static final String DATA_IS_INDIVIDUAL = "data-is-individual";

	public UserGroupSuggestion() {}

	public UserGroupSuggestion(UserGroupHeader header, String prefix, int width) {
		this.header = header;
		this.prefix = prefix;
		this.width = width;
	}

	@Override
	public String getDisplayString() {
		StringBuilder result = new StringBuilder();
		String uniqueId = HTMLPanel.createUniqueId();
		result.append("<div id=\"" + uniqueId + "\" " + DATA_IS_INDIVIDUAL + "=\"" + header.getIsIndividual() + "\" " + DATA_USER_GROUP_ID + "=\"" + header.getOwnerId() + "\" class=\"padding-left-5 userGroupSuggestion\" style=\"height:28px; width:" + width + "px;\"></div>");
		loadBadge(uniqueId);
		return result.toString();
	}

	private void loadBadge(String elementId) {
		Scheduler.get().scheduleDeferred(new Command() {
			@Override
			public void execute() {
				Element el = Document.get().getElementById(elementId);
				if (el != null) {
					String id = el.getAttribute(DATA_USER_GROUP_ID);
					boolean isIndividual = Boolean.valueOf(el.getAttribute(DATA_IS_INDIVIDUAL));
					if (isIndividual) {
						UserBadge userBadge = Portal.getInjector().getUserBadgeWidget();
						userBadge.addStyleNames("ignore-click-events");
						el.appendChild(userBadge.asWidget().getElement());
						userBadge.configure(id);
					} else {
						TeamBadge teamBadge = Portal.getInjector().getTeamBadgeWidget();
						teamBadge.configure(id);
						teamBadge.addStyleName("ignore-click-events");
						el.appendChild(teamBadge.asWidget().getElement());
					}
				}
			}
		});
	}

	@Override
	public String getReplacementString() {
		// Example output:
		// Pac Man | 114085
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
