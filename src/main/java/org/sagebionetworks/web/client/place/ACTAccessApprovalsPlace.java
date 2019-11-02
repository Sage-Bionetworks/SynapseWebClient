package org.sagebionetworks.web.client.place;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class ACTAccessApprovalsPlace extends ParameterizedPlace {

	public static final String ACCESS_REQUIREMENT_ID_PARAM = "AR_ID";
	public static final String SUBMITTER_ID_PARAM = "SUBMITTER_USER_ID";
	public static final String EXPIRES_BEFORE_PARAM = "EXPIRES_BEFORE";

	public ACTAccessApprovalsPlace(String token) {
		super(token);
	}

	@Prefix("!ACTAccessApprovals")
	public static class Tokenizer implements PlaceTokenizer<ACTAccessApprovalsPlace> {
		@Override
		public String getToken(ACTAccessApprovalsPlace place) {
			return place.toToken();
		}

		@Override
		public ACTAccessApprovalsPlace getPlace(String token) {
			return new ACTAccessApprovalsPlace(token);
		}
	}
}
