package org.sagebionetworks.web.client.place;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class EmailInvitation extends Place {
	public String invitationId;

	public EmailInvitation(String token) {
		this.invitationId = token;
	}

	public String toToken() {
		return invitationId;
	}

	@Prefix("!EmailInvitation")
	public static class Tokenizer implements PlaceTokenizer<EmailInvitation> {
		@Override
		public String getToken(EmailInvitation place) {
			return place.toToken();
		}

		@Override
		public EmailInvitation getPlace(String token) {
			return new EmailInvitation(token);
		}
	}
}
