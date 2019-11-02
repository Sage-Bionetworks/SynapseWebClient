package org.sagebionetworks.web.client.place;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class Down extends Place {

	private String token;

	public Down(String token) {
		this.token = token;
	}

	public String toToken() {
		return token;
	}

	@Prefix("!Down")
	public static class Tokenizer implements PlaceTokenizer<Down> {
		@Override
		public String getToken(Down place) {
			return place.toToken();
		}

		@Override
		public Down getPlace(String token) {
			return new Down(token);
		}
	}

}
