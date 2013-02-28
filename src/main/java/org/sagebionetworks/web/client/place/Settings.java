package org.sagebionetworks.web.client.place;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class Settings extends Place{
	
	private String token;

	public Settings(String token) {
		this.token = token;
	}

	public String toToken() {
		return token;
	}
	
	@Prefix("!Settings")
	public static class Tokenizer implements PlaceTokenizer<Settings> {
        @Override
        public String getToken(Settings place) {
            return place.toToken();
        }

        @Override
        public Settings getPlace(String token) {
            return new Settings(token);
        }
    }

}
