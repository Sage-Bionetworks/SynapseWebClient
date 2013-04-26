package org.sagebionetworks.web.client.place;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class Evaluation extends Place{
	private String token;
	
	public Evaluation(String token) {
		this.token = token;
	}

	public String toToken() {
		return token;
	}

	@Prefix("!Evaluation")
	public static class Tokenizer implements PlaceTokenizer<Evaluation> {
        @Override
        public String getToken(Evaluation place) {
            return place.toToken();
        }

        @Override
        public Evaluation getPlace(String token) {
            return new Evaluation(token);
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
		Evaluation other = (Evaluation) obj;
		if (token == null) {
			if (other.token != null)
				return false;
		} else if (!token.equals(other.token))
			return false;
		return true;
	}

	
}
