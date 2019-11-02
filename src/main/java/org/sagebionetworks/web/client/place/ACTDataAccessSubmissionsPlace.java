package org.sagebionetworks.web.client.place;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class ACTDataAccessSubmissionsPlace extends ParameterizedPlace {

	public static final String ACCESS_REQUIREMENT_ID_PARAM = "AR_ID";
	public static final String STATE_FILTER_PARAM = "STATE";
	public static final String MIN_DATE_PARAM = "FROM_DATE";
	public static final String MAX_DATE_PARAM = "TO_DATE";

	public ACTDataAccessSubmissionsPlace(String token) {
		super(token);
	}

	@Prefix("!ACTDataAccessSubmissions")
	public static class Tokenizer implements PlaceTokenizer<ACTDataAccessSubmissionsPlace> {
		@Override
		public String getToken(ACTDataAccessSubmissionsPlace place) {
			return place.toToken();
		}

		@Override
		public ACTDataAccessSubmissionsPlace getPlace(String token) {
			return new ACTDataAccessSubmissionsPlace(token);
		}
	}
}
