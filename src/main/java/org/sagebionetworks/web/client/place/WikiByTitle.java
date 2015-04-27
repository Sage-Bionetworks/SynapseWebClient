package org.sagebionetworks.web.client.place;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

/**
 * Standalone wiki place.  A single wiki page is shown here. It will show the associated wiki page based on page title.
 * The place prefix is simply "W"
 */
public class WikiByTitle extends Place{
	
	private String token;

	public WikiByTitle(String token) {
		this.token = token;
	}

	public String toToken() {
		return token;
	}
	
	@Prefix("!W")
	public static class Tokenizer implements PlaceTokenizer<WikiByTitle> {
        @Override
        public String getToken(WikiByTitle place) {
            return place.toToken();
        }

        @Override
        public WikiByTitle getPlace(String token) {
            return new WikiByTitle(token);
        }
    }
}
