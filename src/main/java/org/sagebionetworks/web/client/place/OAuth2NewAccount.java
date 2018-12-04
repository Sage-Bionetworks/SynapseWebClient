package org.sagebionetworks.web.client.place;

import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class OAuth2NewAccount extends ParameterizedPlace {
	public OAuth2NewAccount(String token) {
		super(token);
	}
	
	public OAuth2NewAccount(WikiPageKey key) {
		super(ParameterizedToken.DEFAULT_TOKEN);
	}

	@Prefix("!OAuth2NewAccount")
	public static class Tokenizer implements PlaceTokenizer<OAuth2NewAccount> {
        @Override
        public String getToken(OAuth2NewAccount place) {
            return place.toToken();
        }

        @Override
        public OAuth2NewAccount getPlace(String token) {
            return new OAuth2NewAccount(token);
        }
    }
}
