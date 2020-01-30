package org.sagebionetworks.web.client.place;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class ChangeUsername extends Place {

	private String token;

	public ChangeUsername(String token) {
		this.token = token;
	}

	public String toToken() {
		return token;
	}

	@Prefix("!ChangeUsername")
	public static class Tokenizer implements PlaceTokenizer<ChangeUsername> {
		@Override
		public String getToken(ChangeUsername place) {
			return place.toToken();
		}

		@Override
		public ChangeUsername getPlace(String token) {
			return new ChangeUsername(token);
		}
	}
}
