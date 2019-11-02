package org.sagebionetworks.web.client.place;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class SubscriptionPlace extends ParameterizedPlace {

	public static final String SUBSCRIPTION_ID_FILTER_PARAM = "subscriptionID";
	public static final String OBJECT_TYPE_PARAM = "objectType";
	public static final String OBJECT_ID_PARAM = "objectID";

	public SubscriptionPlace(String token) {
		super(token);
	}

	@Prefix("!Subscription")
	public static class Tokenizer implements PlaceTokenizer<SubscriptionPlace> {
		@Override
		public String getToken(SubscriptionPlace place) {
			return place.toToken();
		}

		@Override
		public SubscriptionPlace getPlace(String token) {
			return new SubscriptionPlace(token);
		}
	}
}
