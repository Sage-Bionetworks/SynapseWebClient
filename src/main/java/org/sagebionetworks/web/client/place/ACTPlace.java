package org.sagebionetworks.web.client.place;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class ACTPlace extends Place{
	
	private String token;

	public ACTPlace(String token) {
		this.token = token;
	}

	public String toToken() {
		return token;
	}
	
	@Prefix("!ACT")
	public static class Tokenizer implements PlaceTokenizer<ACTPlace> {
        @Override
        public String getToken(ACTPlace place) {
            return place.toToken();
        }

        @Override
        public ACTPlace getPlace(String token) {
            return new ACTPlace(token);
        }
    }
}
