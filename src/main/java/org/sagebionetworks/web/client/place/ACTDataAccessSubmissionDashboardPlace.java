package org.sagebionetworks.web.client.place;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class ACTDataAccessSubmissionDashboardPlace extends Place {

	private String token;

	public ACTDataAccessSubmissionDashboardPlace(String token) {
		this.token = token;
	}

	public String toToken() {
		return token;
	}

	@Prefix("!ACTDataAccessSubmissionDashboard")
	public static class Tokenizer implements PlaceTokenizer<ACTDataAccessSubmissionDashboardPlace> {
		@Override
		public String getToken(ACTDataAccessSubmissionDashboardPlace place) {
			return place.toToken();
		}

		@Override
		public ACTDataAccessSubmissionDashboardPlace getPlace(String token) {
			return new ACTDataAccessSubmissionDashboardPlace(token);
		}
	}

}
