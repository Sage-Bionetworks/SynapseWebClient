package org.sagebionetworks.web.client.place;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class HomeRedirector extends Place{
	
	
	public HomeRedirector() {
	}

	@Prefix("!HomeRedirector")
	public static class Tokenizer implements PlaceTokenizer<HomeRedirector> {
        @Override
        public String getToken(HomeRedirector place) {
            return null;
        }

        @Override
        public HomeRedirector getPlace(String token) {
            return new HomeRedirector();
        }
    }
}
