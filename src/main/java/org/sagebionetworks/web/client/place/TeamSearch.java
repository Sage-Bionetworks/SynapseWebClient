package org.sagebionetworks.web.client.place;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class TeamSearch extends Place {

	public static final String START_DELIMITER = "/start/";
	private String token, searchTerm;
	private Integer start;

	public TeamSearch(String token) {
		this.token = token;
		start = null;
		if (token.contains(START_DELIMITER)) {
			String[] parts = token.split(START_DELIMITER);
			if (parts.length == 2) {
				searchTerm = parts[0];
				start = Integer.parseInt(parts[1]);
				return;
			}
		}
		// default
		searchTerm = token;
	}

	public TeamSearch(String searchTerm, Integer start) {
		this.token = searchTerm;
		if (start != null)
			this.token += START_DELIMITER + start;
		this.searchTerm = searchTerm;
		this.start = start;
	}

	public String getSearchTerm() {
		return searchTerm;
	}

	public Integer getStart() {
		return start;
	}

	public String toToken() {
		return token;
	}

	@Prefix("!TeamSearch")
	public static class Tokenizer implements PlaceTokenizer<TeamSearch> {
		@Override
		public String getToken(TeamSearch place) {
			return place.toToken();
		}

		@Override
		public TeamSearch getPlace(String token) {
			return new TeamSearch(token);
		}
	}

}
