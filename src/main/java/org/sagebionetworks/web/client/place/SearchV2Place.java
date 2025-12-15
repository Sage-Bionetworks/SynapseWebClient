package org.sagebionetworks.web.client.place;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;
import com.google.gwt.user.client.Window;

public class SearchV2Place extends Place {

  public static final String QUERY = "query";

  public SearchV2Place(String token) {
    if (token != null && !token.startsWith("default")) {
      // Redirect /SearchV2:{token} to /SearchV2:default
      Window.Location.assign("/SearchV2:default?" + QUERY + "=" + token);
    }
  }

  @Prefix("SearchV2")
  public static class Tokenizer implements PlaceTokenizer<SearchV2Place> {

    @Override
    public String getToken(SearchV2Place place) {
      return "default";
    }

    @Override
    public SearchV2Place getPlace(String token) {
      return new SearchV2Place(token);
    }
  }
}
