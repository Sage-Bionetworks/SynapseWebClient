package org.sagebionetworks.web.client.place;

import org.sagebionetworks.web.client.ClientProperties;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class LoginPlace extends Place {
	
	private String token;	
	
	public static final String LOGOUT_TOKEN = "logout";
	public static final String LOGIN_TOKEN = ClientProperties.DEFAULT_PLACE_TOKEN;
	public static final String FASTPASS_TOKEN = "fastpassing";
	
	public LoginPlace(String token) {
		this.token = token;
	}

	public String toToken() {
		return token;
	}
	
	@Prefix("!LoginPlace")
	public static class Tokenizer implements PlaceTokenizer<LoginPlace> {
        @Override
        public String getToken(LoginPlace place) {
            return place.toToken();
        }

        @Override
        public LoginPlace getPlace(String token) {
            return new LoginPlace(token);
        }
    }

}

