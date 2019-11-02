package org.sagebionetworks.web.client.place;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class AccessRequirementsPlace extends ParameterizedPlace {

	public static final String ID_PARAM = "ID";
	public static final String TYPE_PARAM = "TYPE";

	public AccessRequirementsPlace(String token) {
		super(token);
	}

	@Prefix("!AccessRequirements")
	public static class Tokenizer implements PlaceTokenizer<AccessRequirementsPlace> {
		@Override
		public String getToken(AccessRequirementsPlace place) {
			return place.toToken();
		}

		@Override
		public AccessRequirementsPlace getPlace(String token) {
			return new AccessRequirementsPlace(token);
		}
	}
}
