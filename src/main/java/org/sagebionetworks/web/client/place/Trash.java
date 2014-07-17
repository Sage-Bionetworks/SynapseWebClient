package org.sagebionetworks.web.client.place;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class Trash extends Place{
	
	private String token;

	public Trash(String token) {
		this.token = token;
	}

	public String toToken() {
		return token;
	}

	@Prefix("!Trash")
	public static class Tokenizer implements PlaceTokenizer<Trash> {
		@Override
		public String getToken(Trash place) {
			return place.toToken();
		}

		@Override
		public Trash getPlace(String token) {
			return new Trash(token);
		}
	}
}