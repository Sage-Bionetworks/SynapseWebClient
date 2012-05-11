package org.sagebionetworks.web.client.place;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;

public class Synapse extends Place{
	
	private String token;

	public Synapse(String token) {
		this.token = token;
	}

	public String toToken() {
		return token;
	}
	
	public static class Tokenizer implements PlaceTokenizer<Synapse> {
        @Override
        public String getToken(Synapse place) {
            return place.toToken();
        }

        @Override
        public Synapse getPlace(String token) {
            return new Synapse(token);
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
		Synapse other = (Synapse) obj;
		if (token == null) {
			if (other.token != null)
				return false;
		} else if (!token.equals(other.token))
			return false;
		return true;
	}

	
}
