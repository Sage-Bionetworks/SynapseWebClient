package org.sagebionetworks.web.client.place;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class Quiz extends Place {

	private String token;

	public Quiz(String token) {
		this.token = token;
	}

	public String toToken() {
		return token;
	}

	@Prefix("!Quiz")
	public static class Tokenizer implements PlaceTokenizer<Quiz> {
		@Override
		public String getToken(Quiz place) {
			return place.toToken();
		}

		@Override
		public Quiz getPlace(String token) {
			return new Quiz(token);
		}
	}

}

