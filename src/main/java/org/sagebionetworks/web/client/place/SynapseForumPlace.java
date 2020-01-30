package org.sagebionetworks.web.client.place;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class SynapseForumPlace extends ParameterizedPlace {

	public SynapseForumPlace(String token) {
		super(token);
	}

	@Prefix("!SynapseForum")
	public static class Tokenizer implements PlaceTokenizer<SynapseForumPlace> {
		@Override
		public String getToken(SynapseForumPlace place) {
			return place.toToken();
		}

		@Override
		public SynapseForumPlace getPlace(String token) {
			return new SynapseForumPlace(token);
		}
	}
}
