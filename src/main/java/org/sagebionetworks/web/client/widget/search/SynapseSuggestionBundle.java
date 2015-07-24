package org.sagebionetworks.web.client.widget.search;

import java.util.List;

public class SynapseSuggestionBundle {
	List<SynapseSuggestion> suggestions;
	long totalResults;
	
	public SynapseSuggestionBundle(List<SynapseSuggestion> suggestions, long totalResults) {
		this.totalResults = totalResults;
		this.suggestions = suggestions;
	}

	public long getTotalNumberOfResults() {
		return totalResults;
	}
	
	public List<SynapseSuggestion> getSuggestionBundle() {
		return suggestions;
	}
}
