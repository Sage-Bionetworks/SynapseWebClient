package org.sagebionetworks.web.client.place;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;

public class BCCOverview extends Place{
	
	private String token;

	public BCCOverview(String token) {
		this.token = token;
	}

	public String toToken() {
		return token;
	}
	
	public static class Tokenizer implements PlaceTokenizer<BCCOverview> {
        @Override
        public String getToken(BCCOverview place) {
            return place.toToken();
        }

        @Override
        public BCCOverview getPlace(String token) {
            return new BCCOverview(token);
        }
    }
}
