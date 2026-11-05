package org.sagebionetworks.web.client.place;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class SearchManagementPlace extends Place {

  private String token;

  public SearchManagementPlace(String token) {
    this.token = token;
  }

  public String toToken() {
    return token;
  }

  @Prefix("SearchManagement")
  public static class Tokenizer
    implements PlaceTokenizer<SearchManagementPlace> {

    @Override
    public String getToken(SearchManagementPlace place) {
      return place.toToken();
    }

    @Override
    public SearchManagementPlace getPlace(String token) {
      return new SearchManagementPlace(token);
    }
  }
}
