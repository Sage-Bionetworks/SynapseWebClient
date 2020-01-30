package org.sagebionetworks.web.client.place;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class Challenges extends Place {

	private String token;

	public Challenges(String token) {
		this.token = token;
	}

	public String toToken() {
		return token;
	}

	@Prefix("!Challenges")
	public static class Tokenizer implements PlaceTokenizer<Challenges> {
		@Override
		public String getToken(Challenges place) {
			return place.toToken();
		}

		@Override
		public Challenges getPlace(String token) {
			return new Challenges(token);
		}
	}
}
