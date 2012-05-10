package org.sagebionetworks.web.client.place;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;

public class Governance extends Place{
	
	private String token;

	public Governance(String token) {
		this.token = token;
	}

	public String toToken() {
		return token;
	}
	
	public static class Tokenizer implements PlaceTokenizer<Governance> {
        @Override
        public String getToken(Governance place) {
            return place.toToken();
        }

        @Override
        public Governance getPlace(String token) {
            return new Governance(token);
        }
    }
}
