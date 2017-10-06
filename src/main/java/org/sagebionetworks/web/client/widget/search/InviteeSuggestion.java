package org.sagebionetworks.web.client.widget.search;

import com.google.gwt.user.client.ui.SuggestOracle;
import org.sagebionetworks.web.client.widget.team.InviteWidget;

public interface InviteeSuggestion extends SuggestOracle.Suggestion {
	String getPrefix();
	void accept(InviteWidget widget, String invitationMessage);
}


