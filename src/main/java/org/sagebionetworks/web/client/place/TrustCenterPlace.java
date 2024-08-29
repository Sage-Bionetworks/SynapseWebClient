package org.sagebionetworks.web.client.place;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class TrustCenterPlace extends Place {

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
