package org.sagebionetworks.web.client.place;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class NewAccount extends Place {

	private String token;

	public NewAccount(String token) {
		this.token = token;
	}

	public String toToken() {
		return token;
	}

	@Prefix("!NewAccount")
	public static class Tokenizer implements PlaceTokenizer<NewAccount> {
		@Override
		public String getToken(NewAccount place) {
			return place.toToken();
		}

		@Override
		public NewAccount getPlace(String token) {
			return new NewAccount(token);
		}
	}
}
