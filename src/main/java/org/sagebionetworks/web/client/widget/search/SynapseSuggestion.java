package org.sagebionetworks.web.client.widget.search;

import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;

public interface SynapseSuggestion extends Suggestion {
	public String getId();
	public String isIndividual();
	public String getPrefix();
	public String getName();
}
