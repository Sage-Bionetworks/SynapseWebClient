package org.sagebionetworks.web.client.place;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class SignedToken extends Place {
	public static final String DELIMITER = "/";

	private String token;
	private String signedEncodedToken;

	public SignedToken(String token) {
		this.token = token;
		if (token.contains(DELIMITER)) {
			String[] parts = token.split(DELIMITER);
			if (parts.length == 2) {
				// tokenType = parts[0];
				signedEncodedToken = parts[1];
			}
		} else {
			signedEncodedToken = token;
		}
	}

	public String toToken() {
		return token;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getSignedEncodedToken() {
		return signedEncodedToken;
	}

	public void setSignedEncodedToken(String signedEncodedToken) {
		this.signedEncodedToken = signedEncodedToken;
	}

	@Prefix("!SignedToken")
	public static class Tokenizer implements PlaceTokenizer<SignedToken> {
		@Override
		public String getToken(SignedToken place) {
			return place.toToken();
		}

		@Override
		public SignedToken getPlace(String token) {
			return new SignedToken(token);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((token == null) ? 0 : token.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SignedToken other = (SignedToken) obj;
		if (token == null) {
			if (other.token != null)
				return false;
		} else if (!token.equals(other.token))
			return false;
		return true;
	}


}
