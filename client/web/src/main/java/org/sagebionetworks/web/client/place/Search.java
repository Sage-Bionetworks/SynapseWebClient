package org.sagebionetworks.web.client.place;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;

public class Search extends Place{
	
	private String token;

	public Search(String token) {
		this.token = token;
	}

	public String toToken() {
		return token;
	}
	
	public static class Tokenizer implements PlaceTokenizer<Search> {
        @Override
        public String getToken(Search place) {
            return place.toToken();
        }

        @Override
        public Search getPlace(String token) {
            return new Search(token);
        }
    }

}
