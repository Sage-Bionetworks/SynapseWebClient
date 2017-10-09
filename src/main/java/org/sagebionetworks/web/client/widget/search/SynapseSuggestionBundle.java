package org.sagebionetworks.web.client.widget.search;

import java.util.List;

import org.sagebionetworks.web.client.widget.search.UserGroupSuggestionProvider.UserGroupSuggestion;

import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;

public class SynapseSuggestionBundle {
	List<Suggestion> suggestions;
	long totalResults;
	
	public SynapseSuggestionBundle(List<Suggestion> suggestions, long totalResults) {
		this.totalResults = totalResults;
		this.suggestions = suggestions;
	}

	public long getTotalNumberOfResults() {
		return totalResults;
	}
	
	public List<Suggestion> getSuggestionBundle() {
		return suggestions;
	}
}
