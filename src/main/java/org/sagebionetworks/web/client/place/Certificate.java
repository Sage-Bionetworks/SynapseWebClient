package org.sagebionetworks.web.client.place;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class Certificate extends Place {
	
	private String token;	
	
	public Certificate(String token) {
		this.token = token;
	}

	public String toToken() {
		return token;
	}
	
	@Prefix("!Certificate")
	public static class Tokenizer implements PlaceTokenizer<Certificate> {
        @Override
        public String getToken(Certificate place) {
            return place.toToken();
        }

        @Override
        public Certificate getPlace(String token) {
            return new Certificate(token);
        }
    }

}

