package org.sagebionetworks.web.client.place;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class Help extends Place {

	private String token;

	public Help(String token) {
		this.token = token;
	}

	public String toToken() {
		return token;
	}

	@Prefix("!Help")
	public static class Tokenizer implements PlaceTokenizer<Help> {
		@Override
		public String getToken(Help place) {
			return place.toToken();
		}

		@Override
		public Help getPlace(String token) {
			return new Help(token);
		}
	}
}
