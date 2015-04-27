package org.sagebionetworks.web.client.place;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

/**
 * Standalone wiki place.  A single wiki page is shown here. It will show the associated wiki page based on page title.
 */
public class SynWiki extends Place{
	
	private String token;

	public SynWiki(String token) {
		this.token = token;
	}

	public String toToken() {
		return token;
	}
	
	@Prefix("!W")
	public static class Tokenizer implements PlaceTokenizer<SynWiki> {
        @Override
        public String getToken(SynWiki place) {
            return place.toToken();
        }

        @Override
        public SynWiki getPlace(String token) {
            return new SynWiki(token);
        }
    }
}
