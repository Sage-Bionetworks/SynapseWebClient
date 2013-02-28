package org.sagebionetworks.web.client.place;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class Home extends Place{
	
	private String token;	

	public Home(String token) {
		this.token = token;
	}

	public String toToken() {
		return token;
	}
	@Prefix("!Home")
	public static class Tokenizer implements PlaceTokenizer<Home> {
        @Override
        public String getToken(Home place) {
            return place.toToken();
        }

        @Override
        public Home getPlace(String token) {
            return new Home(token);
        }
    }

}
