package org.sagebionetworks.web.client.place;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class NrgrSynapseGlue extends Place {
	private String token;
	public NrgrSynapseGlue(String token) {
		this.token = token;
	}

	public String toToken() {
		return token;
	}
	
	@Prefix("!NrgrSynapseGlue")
	public static class Tokenizer implements PlaceTokenizer<NrgrSynapseGlue> {
		@Override
		public String getToken(NrgrSynapseGlue place) {
			return place.toToken();
		}

		@Override
		public NrgrSynapseGlue getPlace(String token) {
			return new NrgrSynapseGlue(token);
		}
	}
}
