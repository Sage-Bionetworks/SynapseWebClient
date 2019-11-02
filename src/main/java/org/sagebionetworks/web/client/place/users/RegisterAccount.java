package org.sagebionetworks.web.client.place.users;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class RegisterAccount extends Place {

	private String token;

	public RegisterAccount(String token) {
		this.token = token;
	}

	public String toToken() {
		return token;
	}

	@Prefix("!RegisterAccount")
	public static class Tokenizer implements PlaceTokenizer<RegisterAccount> {
		@Override
		public String getToken(RegisterAccount place) {
			return place.toToken();
		}

		@Override
		public RegisterAccount getPlace(String token) {
			return new RegisterAccount(token);
		}
	}

}
