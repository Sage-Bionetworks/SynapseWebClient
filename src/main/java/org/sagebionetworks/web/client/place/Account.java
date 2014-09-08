package org.sagebionetworks.web.client.place;

import org.sagebionetworks.web.client.presenter.AccountPresenter;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class Account extends Place{
	
	private String token;

	public Account(String token) {
		this.token = token;
	}

	public String toToken() {
		return token;
	}
	
	public String getFixedToken(){
		if (_isFirefox())
			return AccountPresenter.encodeTokenKeysAndValues(token);
		else
			return token;
	}
	
	public final static native boolean _isFirefox() /*-{ 
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
