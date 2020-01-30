package org.sagebionetworks.web.client.place;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class Home extends Place {

	private String token;

	public Home(String token) {
		this.token = token;
	}

	public String toToken() {
		return token;
	}

	@Prefix("!Home")
	public static class Tokenizer implements PlaceTokenizer<Home> {
		@Override
		public String getToken(Home place) {
			return place.toToken();
		}

		@Override
		public Home getPlace(String token) {
			return new Home(token);
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
		Home other = (Home) obj;
		if (token == null) {
			if (other.token != null)
				return false;
		} else if (!token.equals(other.token))
			return false;
		return true;
	}

}
