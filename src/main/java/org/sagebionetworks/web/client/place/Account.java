package org.sagebionetworks.web.client.place;

import com.google.gwt.http.client.URL;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class Account extends Place{
	
	private String token;

	public Account(String token) {
		if (_isFirefox())
			this.token = URL.encodePathSegment(token);
		else
			this.token = token;
	}

	public String toToken() {
		return token;
	}
	
	private final static native boolean _isFirefox() /*-{ 
		return navigator.userAgent.toLowerCase().indexOf('firefox') > -1;
	}-*/;

	@Prefix("!Account")
	public static class Tokenizer implements PlaceTokenizer<Account> {
        @Override
        public String getToken(Account place) {
            return place.toToken();
        }

        @Override
        public Account getPlace(String token) {
            return new Account(token);
        }
    }
}
