package org.sagebionetworks.web.client.place;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class ErrorPlace extends Place {

	private String token;

	public ErrorPlace(String token) {
		this.token = token;
	}

	public String toToken() {
		return token;
	}

	@Prefix("!Error")
	public static class Tokenizer implements PlaceTokenizer<ErrorPlace> {
		@Override
		public String getToken(ErrorPlace place) {
			return place.toToken();
		}

		@Override
		public ErrorPlace getPlace(String token) {
			return new ErrorPlace(token);
		}
	}

}
