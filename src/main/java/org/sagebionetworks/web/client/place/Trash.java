package org.sagebionetworks.web.client.place;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class Trash extends Place {

	public static final String START_DELIMITER = "/start/";

	private String token;
	private Integer start;

	public Trash(String token) {
		this.token = token;
		start = null;
		if (token.contains(START_DELIMITER)) {
			String[] parts = token.split(START_DELIMITER);
			if (parts.length == 2) {
				start = Integer.parseInt(parts[1]);
				return;
			}
		}
	}

	public Trash(String token, Integer start) {
		this.token = token;
		if (start != null)
			this.token += START_DELIMITER + start;
		this.start = start;
	}

	public String toToken() {
		return token;
	}

	public Integer getStart() {
		return start;
	}

	@Prefix("!Trash")
	public static class Tokenizer implements PlaceTokenizer<Trash> {
		@Override
		public String getToken(Trash place) {
			return place.toToken();
		}

		@Override
		public Trash getPlace(String token) {
			return new Trash(token);
		}
	}
}
