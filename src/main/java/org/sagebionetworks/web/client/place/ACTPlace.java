package org.sagebionetworks.web.client.place;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class ACTPlace extends ParameterizedPlace {

	public static final String SUBMITTER_ID_FILTER_PARAM = "submitterID";
	public static final String STATE_FILTER_PARAM = "state";

	public ACTPlace(String token) {
		super(token);
	}

	@Prefix("!ACT")
	public static class Tokenizer implements PlaceTokenizer<ACTPlace> {
		@Override
		public String getToken(ACTPlace place) {
			return place.toToken();
		}

		@Override
		public ACTPlace getPlace(String token) {
			return new ACTPlace(token);
		}
	}
}
