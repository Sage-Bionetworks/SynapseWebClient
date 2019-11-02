package org.sagebionetworks.web.client.place;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class Search extends Place {

	public static final String START_DELIMITER = "/start/";
	private String token, searchTerm;
	private Long start;

	public Search(String token) {
		this.token = token;
		if (token.contains(START_DELIMITER)) {
			String[] parts = token.split(START_DELIMITER);
			if (parts.length == 2) {
				searchTerm = parts[0];
				start = Long.parseLong(parts[1]);
				return;
			}
		}
		// default
		searchTerm = token;
	}

	public String getSearchTerm() {
		return searchTerm;
	}

	public Long getStart() {
		return start;
	}

	public String toToken() {
		return token;
	}

	@Prefix("!Search")
	public static class Tokenizer implements PlaceTokenizer<Search> {
		@Override
		public String getToken(Search place) {
			return place.toToken();
		}

		@Override
		public Search getPlace(String token) {
			return new Search(token);
		}
	}

}
