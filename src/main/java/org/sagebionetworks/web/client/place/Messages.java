package org.sagebionetworks.web.client.place;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class Messages extends Place{
	
	private String token;

	public Messages(String token) {
		this.token = token;
	}

	public String toToken() {
		return token;
	}

	@Prefix("!Messages")
	public static class Tokenizer implements PlaceTokenizer<Messages> {
		@Override
		public String getToken(Messages place) {
			return place.toToken();
		}

		@Override
		public Messages getPlace(String token) {
			return new Messages(token);
		}
	}
}