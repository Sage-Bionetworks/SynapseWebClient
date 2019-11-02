package org.sagebionetworks.web.client.widget.search;

import java.util.List;

public class SynapseSuggestionBundle {
	List<UserGroupSuggestion> suggestions;
	long totalResults;

	public SynapseSuggestionBundle(List<UserGroupSuggestion> suggestions, long totalResults) {
		this.totalResults = totalResults;
		this.suggestions = suggestions;
	}

	public long getTotalNumberOfResults() {
		return totalResults;
	}

	public List<UserGroupSuggestion> getSuggestionBundle() {
		return suggestions;
	}
}
