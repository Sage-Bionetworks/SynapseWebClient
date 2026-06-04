package org.sagebionetworks.web.client.place;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class LegacySearchPlace extends Place {

  private String token;

  public LegacySearchPlace(String token) {
    this.token = token;
  }

  public String toToken() {
    return token;
  }

  @Prefix("Search")
  public static class Tokenizer implements PlaceTokenizer<LegacySearchPlace> {

    @Override
    public String getToken(LegacySearchPlace place) {
      return place.token;
    }

    @Override
    public LegacySearchPlace getPlace(String token) {
      return new LegacySearchPlace(token);
    }
  }
}
