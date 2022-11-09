package org.sagebionetworks.web.client.place;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class EmailInvitation extends Place {

  public String signedEncodedToken;

  public EmailInvitation(String token) {
    this.signedEncodedToken = token;
  }

  public String toToken() {
    return signedEncodedToken;
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
