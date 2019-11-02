package org.sagebionetworks.web.client.place;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class PeopleSearch extends Place {
	public static final String START_DELIMITER = "/start/";
	private String token, searchTerm;
	private Integer start;

	public PeopleSearch(String token) {
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

	public String getSearchTerm() {
		return searchTerm;
	}

	public Integer getStart() {
		return start;
	}

	public String toToken() {
		return token;
	}

	@Prefix("!PeopleSearch")
	public static class Tokenizer implements PlaceTokenizer<PeopleSearch> {
		@Override
		public String getToken(PeopleSearch place) {
			return place.toToken();
		}

		@Override
		public PeopleSearch getPlace(String token) {
			return new PeopleSearch(token);
		}
	}
}
