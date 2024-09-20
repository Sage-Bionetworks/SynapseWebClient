package org.sagebionetworks.web.client.place.users;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;
import org.sagebionetworks.web.client.place.ParameterizedPlace;

public class RegisterAccount extends ParameterizedPlace {

  public static final String EMAIL_QUERY_PARAM = "email";
  public static final String MEMBERSHIP_INVTN_QUERY_PARAM =
    "membershipInvtnSignedToken";

  public RegisterAccount(String token) {
    super(token);
  }

  @Prefix("RegisterAccount")
  public static class Tokenizer implements PlaceTokenizer<RegisterAccount> {

    @Override
    public String getToken(RegisterAccount place) {
      return place.toToken();
    }

    @Override
    public RegisterAccount getPlace(String token) {
      return new RegisterAccount(token);
    }
  }
}
