package org.sagebionetworks.web.client.place;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class PasswordResetSignedTokenPlace extends Place {
	private String token;

	public PasswordResetSignedTokenPlace(String token) {
		this.token = token;
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

	@Prefix("!PasswordResetSignedToken")
	public static class Tokenizer implements PlaceTokenizer<PasswordResetSignedTokenPlace> {
		@Override
		public String getToken(PasswordResetSignedTokenPlace place) {
			return place.toToken();
		}

		@Override
		public PasswordResetSignedTokenPlace getPlace(String token) {
			return new PasswordResetSignedTokenPlace(token);
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
		PasswordResetSignedTokenPlace other = (PasswordResetSignedTokenPlace) obj;
		if (token == null) {
			if (other.token != null)
				return false;
		} else if (!token.equals(other.token))
			return false;
		return true;
	}
}
