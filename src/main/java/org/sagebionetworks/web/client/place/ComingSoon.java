package org.sagebionetworks.web.client.place;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class ComingSoon extends Place {

	private String token;

	public ComingSoon(String token) {
		this.token = token;
	}

	public String toToken() {
		return token;
	}

	@Prefix("!ComingSoon")
	public static class Tokenizer implements PlaceTokenizer<ComingSoon> {
		@Override
		public String getToken(ComingSoon place) {
			return place.toToken();
		}

		@Override
		public ComingSoon getPlace(String token) {
			return new ComingSoon(token);
		}
	}
}
