package org.sagebionetworks.web.client.place;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class TrustCenterPlace extends Place {

  public static final String SUBPROCESSORS_KEY = "Subprocessors";
  public static final String COOKIES_KEY = "Cookies";
  public static final String PRIVACY_POLICY_KEY = "PrivacyPolicy";
  public static final String TERMS_OF_SERVICE_KEY = "TermsOfService";

  private String documentKey;

  public TrustCenterPlace(String token) {
    this.documentKey = token;
  }

  public String toToken() {
    return documentKey;
  }

  public String getDocumentKey() {
    return documentKey;
  }

  @Prefix("TrustCenter")
  public static class Tokenizer implements PlaceTokenizer<TrustCenterPlace> {

    @Override
    public String getToken(TrustCenterPlace place) {
      return place.toToken();
    }

    @Override
    public TrustCenterPlace getPlace(String token) {
      return new TrustCenterPlace(token);
    }
  }
}
